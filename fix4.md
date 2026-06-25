# SI-BLO Railway Production Readiness — Fix Prompt (`fix4.md`)

> **Audience:** Developers or AI agents preparing SI-BLO for production deploy on Railway.  
> **Project:** Spring Boot 3.4 (`com.siblo.rent`) + Thymeleaf + JWT + PostgreSQL + Midtrans + Gmail OTP  
> **Companion docs:** `fix.md`, `fix1.md`, `fix3.md`, `Prompt.md`  
> **Goal:** Make the project **safe and stable** for Railway production — security, payment integrity, config, and platform wiring — with a clear go/no-go checklist.

---

## 0. Re-audit verdict

### Re-audit #2 (2026-06-25, setelah implementasi fix4 Phase 1–5)

### ⚠️ HAMPIR SIAP — deploy Railway **sandbox/staging** boleh dicoba, **production GO** masih ditahan 4 blocker

Banyak item fix4 sudah diimplementasi. `./mvnw test` → **lulus**. Aplikasi lokal jalan setelah constraint `HELD` diperbaiki manual.

**Deploy sandbox** (Midtrans sandbox + `MIDTRANS_IS_PRODUCTION=false`) bisa dicoba **setelah** menjalankan migration SQL di Railway Postgres **dan** memperbaiki 4 blocker di bawah.

---

### Progress sejak re-audit #1

| # | Item | Status sekarang |
|---|------|-----------------|
| R1 | `server.port=${PORT:8080}` | ✅ `application-prod.properties` |
| R2 | `ddl-auto=update` (bukan create-drop) | ✅ |
| R4 | OTP bypass `/api/auth/register` | ✅ Return `410 Gone` |
| R5 | Simulate pay di frontend | ✅ Tidak ada `/pay` setelah Snap |
| R6 | IDOR `/payment/charge/{id}` | ✅ Ownership check |
| R7 | `GET /api/bookings/{id}` | ✅ Auth + owner/admin |
| R8 | Webhook update slot `BOOKED` | ✅ `PaymentService.handleNotification` |
| R9 | Snap URL dinamis | ✅ `/payment/snap-config` |
| R11 | `DEPLOY-RAILWAY.md` | ✅ Ada |
| R15 | Thymeleaf cache prod | ✅ `true` |
| — | OTP password di-hash saat pending | ✅ |
| — | OTP rate limit kirim (60s) | ✅ |
| — | SecurityConfig diperketat | ✅ (tapi ada regresi `/register`) |
| — | Tests | ✅ Pass lokal |

---

### Sisa blocker sebelum GO production

| # | Masalah | Dampak | File |
|---|---------|--------|------|
| **B1** | Halaman `/register` tidak `permitAll` | User dapat **401** saat buka halaman register | `SecurityConfig.java` |
| **B2** | Env var email tidak konsisten | OTP **gagal** di Railway: prod pakai `MAIL_*`, doc pakai `EMAIL_*` | `application-prod.properties`, `DEPLOY-RAILWAY.md` |
| **B3** | `EmailService.setFrom` hardcoded | Gmail bisa tolak email OTP di prod | `EmailService.java` |
| **B4** | `POST /api/bookings/{id}/pay` masih confirm tanpa Midtrans | API bypass pembayaran (curl langsung) | `BookingService.payBooking()` |

---

### ⚠️ Migration `HELD` — WAJIB di Railway (jika DB punya constraint lama)

File `src/main/resources/db/migration-v1-held-status.sql` **sudah diperbarui** dan harus dijalankan **sekali** di PostgreSQL Railway (Query tab / psql) **sebelum atau sesudah first deploy** jika:

- DataSeeder crash: `violates check constraint "time_slots_status_check" ... HELD`
- DB dibuat dari schema lama tanpa `HELD`

```sql
ALTER TABLE time_slots DROP CONSTRAINT IF EXISTS time_slots_status_check;

ALTER TABLE time_slots ADD CONSTRAINT time_slots_status_check
  CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED'));
```

**Kapan TIDAK perlu:** DB baru murni dari Hibernate `ddl-auto=update` tanpa CHECK constraint (status = VARCHAR biasa) — `HELD` langsung jalan.

**Kapan WAJIB:** DB di-import dari `db_siblo.sql` lama, atau Laragon DB yang pernah error constraint — sama seperti yang kamu perbaiki lokal.

---

### Deployment readiness matrix (re-audit #2)

| # | Area | Status | Blocker? |
|---|------|--------|----------|
| R1 | PORT binding | ✅ | — |
| R2 | ddl-auto prod | ✅ | — |
| R3 | Secrets di git | ⚠️ Default fallback masih ada | Medium |
| R4 | OTP bypass | ✅ | — |
| R5 | Frontend simulate pay | ✅ | — |
| R6 | IDOR charge | ✅ | — |
| R7 | Booking leak GET | ✅ | — |
| R8 | Webhook slots | ✅ | — |
| R9 | Snap URL dinamis | ✅ | — |
| R10 | Midtrans webhook URL | ⚠️ Manual di dashboard | **Ops** — wajib diset |
| R11 | Deploy doc | ✅ | — |
| R12 | Email setFrom | ❌ Hardcoded | **YES** (B3) |
| R13 | OTP in-memory | ⚠️ OK `numReplicas: 1` | Documented |
| R14 | Test profile H2 | ⚠️ Masih PostgreSQL | Low |
| R15 | Thymeleaf cache | ✅ | — |
| **B1** | `/register` page 401 | ❌ | **YES** |
| **B2** | MAIL vs EMAIL env | ❌ Mismatch | **YES** |
| **B4** | API pay bypass | ❌ | **YES** untuk prod |
| **DB** | HELD constraint migration | ⚠️ Run once on Railway | **YES** jika constraint lama |

**GO production:** B1–B4 ✅ + migration SQL di Railway + webhook Midtrans diset + env vars benar.

---

## 1. Root cause analysis

### Blocker R1 — Railway PORT not bound (critical deploy)

**File:** `application-prod.properties`

Railway injects `PORT` (e.g. `3000`). Spring Boot default listen `8080`. Tanpa binding, healthcheck gagal → deploy loop / crash.

**Fix:**

```properties
server.port=${PORT:8080}
```

---

### Blocker R2 — Prod `create-drop` destroys database (critical deploy)

**File:** `application-prod.properties` line 10

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

Setiap start/redeploy: **DROP semua tabel** → users, bookings, payments hilang. DataSeeder jalan ulang hanya jika `users` kosong.

**Fix:**

```properties
spring.jpa.hibernate.ddl-auto=update
```

Untuk production serius: `validate` + Flyway (out of scope UAS, tapi catat di README).

---

### Blocker R3 — Secrets still in tracked `application.properties` (critical security)

**File:** `src/main/resources/application.properties`

Masih berisi Midtrans keys, Gmail password, DB password `root`.

**Risk:** Push ke GitHub = kredensial bocor. Railway build tidak butuh file ini untuk prod (pakai profile `prod`), tapi repo tetap tidak aman.

**Fix:** Placeholder + `application.properties.example`. Rotate semua key yang pernah ter-commit.

---

### Blocker R4 — OTP bypass (critical security)

**File:** `AuthController.java` — `POST /api/auth/register`

Masih membuat akun tanpa OTP. Production = spam account + bypass verifikasi email.

**Fix:** Hapus atau return `410 Gone`.

---

### Blocker R5 — Simulated payment still active (critical security)

**Files:** `booking.html` (lines 425, 451), `my-bookings.html` (lines 286, 300)

Frontend masih memanggil `POST /api/bookings/{id}/pay` setelah Snap / payment link.

`PaymentService.completePayment()` menulis `method: SIMULATED`.

**Fix:** Hapus semua client-side `/pay` untuk member flow. Konfirmasi hanya via webhook Midtrans (+ admin confirm opsional).

---

### Blocker R6 — IDOR on payment charge (critical security)

**File:** `PaymentController.java`

```java
@PostMapping("/charge/{bookingId}")
public ResponseEntity<?> charge(@PathVariable Long bookingId) {
```

Tidak cek `booking.user == currentUser`.

**Fix:** Tambah `Authentication auth`, verify ownership.

---

### Blocker R7 — Public booking leak (critical security)

**File:** `BookingController.java`

```java
@GetMapping("/{id}")
public ResponseEntity<?> getBooking(@PathVariable Long id) {
```

Tanpa auth — data booking user lain bisa dibaca.

**Fix:** Require auth + owner/admin check. Tambah `userId` di `BookingDTO` jika belum ada.

---

### Blocker R8 — Webhook tidak update slots (critical functional)

**File:** `PaymentService.handleNotification()`

Set `booking CONFIRMED` tapi slot tetap `HELD`.

**Symptom di prod:** User bayar via Midtrans, webhook jalan, booking confirmed, slot masih held → double-book risk.

**Fix:** Set slot `BOOKED` saat payment `COMPLETED` (sama seperti `BookingService.payBooking`).

---

### Blocker R9 — Midtrans Snap JS hardcoded sandbox (critical prod)

**Files:** `booking.html`, `my-bookings.html`

```javascript
script.src = 'https://app.sandbox.midtrans.com/snap/snap.js';
```

Prod Midtrans butuh `https://app.midtrans.com/snap/snap.js` ketika `midtrans.is-production=true`.

**Fix:** Endpoint config atau inject dari server:

```java
// PaymentController atau MidtransConfig
@GetMapping("/payment/snap-config")
public Map<String, String> snapConfig() {
    String base = midtransConfig.isProduction()
        ? "https://app.midtrans.com"
        : "https://app.sandbox.midtrans.com";
    return Map.of("clientKey", ..., "snapScriptUrl", base + "/snap/snap.js");
}
```

Frontend load script URL dari API, bukan hardcode.

---

### Blocker R10 — Midtrans webhook not registered (critical ops)

Webhook `POST /payment/notification` harus didaftarkan di Midtrans Dashboard:

```
https://<railway-app-domain>/payment/notification
```

Tanpa ini, setelah fix5 (hapus simulate pay), **tidak ada cara** booking jadi `CONFIRMED` otomatis.

**Railway note:** Pastikan service punya **public domain** (Generate Domain di Railway). HTTPS wajib.

---

### Issue R11 — Railway environment variables (high ops)

**Required env vars** di Railway service (selain auto-inject dari PostgreSQL plugin):

| Variable | Required | Notes |
|----------|----------|-------|
| `JWT_SECRET` | ✅ | Min 32 chars random; **no default** di prod |
| `MIDTRANS_SERVER_KEY` | ✅ | Production key jika `is-production=true` |
| `MIDTRANS_CLIENT_KEY` | ✅ | Production client key |
| `MIDTRANS_IS_PRODUCTION` | ✅ | `true` untuk live, `false` untuk sandbox |
| `MAIL_USERNAME` | ✅ | Gmail atau SMTP provider |
| `MAIL_PASSWORD` | ✅ | App password |
| `MAIL_HOST` | Optional | Default `smtp.gmail.com` |
| `MAIL_PORT` | Optional | Default `587` |
| `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` | ✅ Auto | Dari Railway PostgreSQL plugin |
| `PORT` | ✅ Auto | Railway injects — jangan override manual |

**Optional fallback** — jika hanya `DATABASE_URL` tersedia, tambah di prod:

```properties
# Optional: parse DATABASE_URL if PG* not set (requires custom config bean or spring-boot 3.4 env)
# Prefer linking PostgreSQL plugin so PG* vars are injected
```

---

### Issue R12 — Email OTP `setFrom` missing (high functional)

**File:** `EmailService.java`

Gmail menolak email tanpa From. Registrasi OTP gagal di production.

**Fix:** `message.setFrom(mailUsername);`

---

### Issue R13 — In-memory OTP (medium — acceptable for UAS single replica)

**Files:** `OtpService.java`, `AuthController.pendingRegistrations`

- Restart Railway → OTP pending hilang
- `railway.json` sudah `numReplicas: 1` ✅ — jangan scale > 1 tanpa DB-backed OTP

**Fix minimal:** Document limitation. **Fix better:** table `otp_codes` (optional Phase 7).

---

### Issue R14 — No test profile (medium)

Tidak ada `application-test.properties`. `./mvnw test` gagal tanpa PostgreSQL lokal.

**Fix:** H2 test profile + `@ActiveProfiles("test")`.

---

### Issue R15 — Thymeleaf cache off in prod (low)

```properties
spring.thymeleaf.cache=true
```

Hanya di `application-prod.properties`.

---

### Issue R16 — DataSeeder on production (low/medium)

`DataSeeder` jalan jika `users` kosong — OK untuk first deploy demo. Setelah ada user real, tidak re-seed.

**Optional:** `app.seed.enabled=${SEED_ENABLED:false}` — default `false` di prod, `true` di dev.

---

### Issue R17 — SecurityConfig still permissive (medium)

```java
.requestMatchers("/api/auth/**").permitAll()
.anyRequest().permitAll()
```

`/api/auth/me`, `/profile`, `/password` technically public at filter level.

**Fix:** Split auth routes (lihat Phase 4 di bawah).

---

### Schema note — `HELD` constraint (resolved locally)

Error sebelumnya:

```
violates check constraint "time_slots_status_check" ... HELD
```

**Penyebab:** DB lama tanpa `HELD` di CHECK constraint. `ddl-auto=update` tidak mengubah constraint.

**Fix SQL** (jalankan sekali per environment, termasuk Railway Postgres jika DB dibuat dari schema lama):

```sql
ALTER TABLE time_slots DROP CONSTRAINT IF EXISTS time_slots_status_check;
ALTER TABLE time_slots ADD CONSTRAINT time_slots_status_check
  CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED'));
```

Sertakan sebagai `db/migration-v1-held-status.sql` di repo.

---

## 2. Constraints

1. **Target platform:** Railway (Nixpacks, Java 21, PostgreSQL plugin, `numReplicas: 1`).
2. **Keep Midtrans Snap** — jangan ganti payment gateway.
3. **Keep OTP register UX** — dua langkah di `register.html`.
4. **Admin manual confirm** boleh tetap untuk demo (`POST /api/admin/bookings/{id}/confirm`).
5. **Secrets never in git** — env vars only untuk prod; local pakai `.env` atau Laragon tanpa commit.
6. **Single replica** sampai OTP pindah ke database.
7. **Run `./mvnw test`** setelah perubahan backend (lokal, dengan test profile).

---

## 3. Target behavior — definition of done (Railway GO)

| Area | Production behavior |
|------|---------------------|
| **Startup** | App binds `PORT`, connects PostgreSQL, tidak drop tables |
| **Health** | `GET /home` returns 200 (Railway healthcheck) |
| **Register** | OTP only; email terkirim; no `/register` bypass |
| **Payment** | Snap prod/sandbox sesuai env; webhook confirms; slots `BOOKED` |
| **Security** | No secrets in repo; IDOR closed; booking data protected |
| **Redeploy** | Data persists across restarts |
| **Env** | All required Railway vars documented and set |

---

## 4. Implementation phases

Execute **in order**. Setiap phase selesai → commit terpisah (opsional tapi disarankan).

---

### Phase 1 — Railway platform wiring (P0 deploy blockers)

#### 4.1.1 Fix `application-prod.properties`

```properties
# Railway port binding — WAJIB
server.port=${PORT:8080}

# JPA — JANGAN create-drop di production
spring.jpa.hibernate.ddl-auto=update

# Performance
spring.thymeleaf.cache=true

# Optional: disable seed on prod unless explicitly enabled
# app.seed.enabled=${SEED_ENABLED:false}
```

#### 4.1.2 Verify `railway.json`

```json
{
  "deploy": {
    "numReplicas": 1,
    "startCommand": "java -jar target/rent-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod",
    "healthcheckPath": "/home",
    "restartPolicyType": "ON_FAILURE"
  }
}
```

Pastikan PostgreSQL service **linked** ke app service di Railway dashboard.

#### 4.1.3 Add schema migration file

**New file:** `db/migration-v1-held-status.sql`

```sql
ALTER TABLE time_slots DROP CONSTRAINT IF EXISTS time_slots_status_check;
ALTER TABLE time_slots ADD CONSTRAINT time_slots_status_check
  CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED'));
```

Jalankan manual di Railway Postgres setelah first deploy, atau via Railway Query tab.

#### 4.1.4 Document Railway env vars

**New file:** `DEPLOY-RAILWAY.md` (atau section di README):

- List semua env vars dari tabel R11
- Cara link PostgreSQL
- Cara set Midtrans Notification URL ke `https://<domain>/payment/notification`
- Cara generate `JWT_SECRET`: `openssl rand -base64 32`

**Acceptance Phase 1:**
- [ ] `server.port=${PORT:8080}` in prod properties
- [ ] `ddl-auto=update` in prod
- [ ] Migration SQL file in repo
- [ ] DEPLOY-RAILWAY.md written

---

### Phase 2 — Secrets hygiene (P0 security)

#### 4.2.1 Sanitize `application.properties` (local dev only)

```properties
spring.datasource.password=${DB_PASSWORD:root}
midtrans.server-key=${MIDTRANS_SERVER_KEY:}
midtrans.client-key=${MIDTRANS_CLIENT_KEY:}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
app.jwt.secret=${JWT_SECRET:dev-only-change-me-must-be-at-least-256-bits-long}
```

#### 4.2.2 Add `application.properties.example`

Template tanpa nilai asli.

#### 4.2.3 Update `.gitignore`

```
.env
.env.*
application-local.properties
```

#### 4.2.4 Rotate exposed credentials

Jika pernah push ke GitHub: rotate Midtrans keys, Gmail app password, JWT secret.

**Acceptance Phase 2:**
- [ ] `git grep "Mid-server-"` → no real keys in tracked files
- [ ] `application.properties.example` exists

---

### Phase 3 — Payment integrity (P0 security + functional)

#### 4.3.1 Remove simulate `/pay` from frontend

**Files:** `booking.html`, `my-bookings.html`

Delete all:
```javascript
await fetch('/api/bookings/' + id + '/pay', { method: 'POST', ... });
await apiFetch('/api/bookings/' + id + '/pay', { method: 'POST' });
```

Replace Snap callbacks with status polling:

```javascript
async function waitForBookingConfirmed(bookingId, maxAttempts = 30) {
    for (let i = 0; i < maxAttempts; i++) {
        const b = await apiFetch('/api/bookings/' + bookingId);
        if (b && b.status === 'CONFIRMED') return true;
        await new Promise(r => setTimeout(r, 2000));
    }
    return false;
}
```

Payment link flow — **do not** auto-confirm:

```javascript
if (courtPaymentLinkUrl) {
    window.open(courtPaymentLinkUrl, '_blank');
    alert('Selesaikan pembayaran di tab baru. Status akan terupdate setelah dikonfirmasi.');
    window.location.href = '/my-bookings';
    return;
}
```

#### 4.3.2 Fix webhook slot booking

**File:** `PaymentService.java` — inject `TimeSlotRepository`:

```java
if (payment.getStatus() == PaymentStatus.COMPLETED) {
    payment.setPaidAt(LocalDateTime.now());
    Booking booking = payment.getBooking();
    if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
        List<TimeSlot> slots = timeSlotRepository.findAllById(booking.getSlotIds());
        for (TimeSlot slot : slots) {
            if (slot.getStatus() == SlotStatus.HELD) {
                slot.setStatus(SlotStatus.BOOKED);
            }
        }
        timeSlotRepository.saveAll(slots);
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }
}
```

#### 4.3.3 Harden member `payBooking` endpoint

**File:** `BookingService.payBooking()`

Reject simulate — only return if already confirmed via webhook:

```java
if (booking.getStatus() == BookingStatus.CONFIRMED)
    return BookingDTO.fromEntity(booking);

Payment payment = paymentRepository.findByBookingId(bookingId).orElse(null);
if (payment == null || payment.getStatus() != PaymentStatus.COMPLETED)
    throw new BookingException("Payment not yet confirmed. Complete payment via Midtrans.");

// Idempotent slot sync if webhook booked booking but slots lagged
...
```

#### 4.3.4 Dynamic Midtrans Snap script URL

**New endpoint** `GET /payment/snap-config` (public):

```java
@GetMapping("/snap-config")
public ResponseEntity<Map<String, String>> snapConfig() {
    String base = midtransConfig.isProduction()
            ? "https://app.midtrans.com"
            : "https://app.sandbox.midtrans.com";
    return ResponseEntity.ok(Map.of(
        "clientKey", midtransConfig.getClientKey(),
        "snapScriptUrl", base + "/snap/snap.js"
    ));
}
```

**Frontend** — replace hardcoded URL:

```javascript
var cfg = await fetch('/payment/snap-config').then(r => r.json());
script.src = cfg.snapScriptUrl;
script.setAttribute('data-client-key', cfg.clientKey);
```

Merge with existing `/payment/client-key` if preferred (single endpoint).

**Acceptance Phase 3:**
- [ ] No `/pay` calls from member frontend after Snap
- [ ] Webhook sets slots `BOOKED`
- [ ] Snap script URL follows `MIDTRANS_IS_PRODUCTION`

---

### Phase 4 — Auth & authorization (P0 security)

#### 4.4.1 Remove OTP bypass

**File:** `AuthController.java` — delete or 410 `POST /register`.

#### 4.4.2 Hash pending password

```java
pendingRegistrations.put(email, Map.of(
    "name", name,
    "email", email,
    "passwordHash", passwordEncoder.encode(password)
));
// verify-otp: user.setPassword(pending.get("passwordHash"))
```

#### 4.4.3 Ownership on charge

**File:** `PaymentController.java` — add `Authentication auth`, verify `booking.getUser().getId()`.

#### 4.4.4 Protect GET booking by id

**File:** `BookingController.java` — require auth, owner or admin.

#### 4.4.5 Tighten SecurityConfig

```java
.requestMatchers("/", "/home", "/login", "/register", "/booking", "/my-bookings",
    "/manage-admin", "/privacy", "/terms", "/map",
    "/css/**", "/js/**", "/images/**").permitAll()
.requestMatchers("/api/auth/login").permitAll()
.requestMatchers("/api/auth/register/send-otp", "/api/auth/register/verify-otp").permitAll()
.requestMatchers("/payment/notification", "/payment/client-key", "/payment/snap-config").permitAll()
.requestMatchers("/api/sports/**", "/api/courts/**").permitAll()
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/auth/**").authenticated()
.requestMatchers("/api/bookings/**").authenticated()
.requestMatchers("/payment/charge/**").authenticated()
.anyRequest().authenticated()
```

Remove `/h2-console/**` (not used with PostgreSQL).

**Acceptance Phase 4:**
- [ ] OTP bypass closed
- [ ] IDOR on charge closed
- [ ] GET booking by id protected

---

### Phase 5 — Email, OTP, tests (P1)

#### 4.5.1 EmailService `setFrom`

```java
@Value("${spring.mail.username}")
private String mailFrom;

message.setFrom(mailFrom);
```

#### 4.5.2 OTP rate limiting

**File:** `OtpService.java` — max 5 failed attempts per email.

#### 4.5.3 Input validation on `sendOtp`

Email format, name not blank, password min 6 chars.

#### 4.5.4 Test profile

**New:** `src/test/resources/application-test.properties` (H2 in-memory).

**Update:** `@ActiveProfiles("test")` on `@SpringBootTest` classes.

**Acceptance Phase 5:**
- [ ] `./mvnw test` passes without local PostgreSQL
- [ ] OTP email sends with valid SMTP on Railway

---

### Phase 6 — Optional production polish (P2)

| Item | Action |
|------|--------|
| `app.seed.enabled` | Default `false` in prod; guard `DataSeeder` |
| `GlobalExceptionHandler` | Remove catch-all `RuntimeException` |
| `BookingRequest` | Add `@NotNull` validation |
| Junk files | Delete `MidtransExample.java`, `payment-links.txt` if still present |
| README | Align with PostgreSQL, OTP, Railway deploy doc |

---

### Phase 7 — Railway deploy checklist (execute after Phase 1–5)

#### Pre-deploy (local)

```bash
./mvnw clean package -DskipTests   # or with tests if Phase 5 done
./mvnw test                        # recommended
```

#### Railway setup

1. Create project → Add **PostgreSQL** plugin
2. Link PostgreSQL to **app service**
3. Set environment variables (see R11 table)
4. Generate **public domain** for app
5. Deploy from GitHub branch
6. Run `db/migration-v1-held-status.sql` on Railway Postgres (if tables pre-exist without HELD)
7. Midtrans Dashboard → Settings → Notification URL:
   ```
   https://<your-railway-domain>/payment/notification
   ```
8. Test Snap payment end-to-end on deployed URL

#### Post-deploy smoke test

| Step | Expected |
|------|----------|
| `GET https://<domain>/home` | 200, page loads |
| Register new user via OTP | Email received, account created |
| `POST /api/auth/register` (curl) | 410 or error, no account |
| Login → book → Pay Now (Snap) | No `/pay` in network tab |
| Complete sandbox payment | Webhook → booking CONFIRMED, slots BOOKED |
| Redeploy app | Data still exists (users, bookings) |
| `POST /payment/charge/{otherUserBooking}` | 403 |

---

## 5. Files to modify

| File | Phase | Changes |
|------|-------|---------|
| `application-prod.properties` | 1 | `PORT`, `ddl-auto=update`, thymeleaf cache |
| `application.properties` | 2 | Env placeholders, remove secrets |
| `application.properties.example` | 2 | **New** |
| `.gitignore` | 2 | `.env`, local props |
| `db/migration-v1-held-status.sql` | 1 | **New** |
| `DEPLOY-RAILWAY.md` | 1 | **New** |
| `booking.html` | 3 | Remove `/pay`, dynamic Snap URL, poll status |
| `my-bookings.html` | 3 | Remove `/pay`, dynamic Snap URL |
| `PaymentService.java` | 3 | Webhook books slots |
| `BookingService.java` | 3 | No simulate pay for members |
| `PaymentController.java` | 3, 4 | `snap-config`, ownership check |
| `AuthController.java` | 4 | Remove bypass, hash pending password |
| `BookingController.java` | 4 | Auth on GET by id |
| `BookingDTO.java` | 4 | Add `userId` if missing |
| `SecurityConfig.java` | 4 | Tighter rules, `/register` page |
| `EmailService.java` | 5 | `setFrom` |
| `OtpService.java` | 5 | Rate limiting |
| `application-test.properties` | 5 | **New** |
| `RentApplicationTests.java` | 5 | `@ActiveProfiles("test")` |
| `BookingControllerTest.java` | 5 | `@ActiveProfiles("test")` |
| `DataSeeder.java` | 6 | Optional `app.seed.enabled` guard |
| `README.md` | 6 | Railway + OTP + PostgreSQL |

---

## 6. Acceptance criteria — Railway GO / NO-GO

### GO — deploy allowed when ALL checked

**Platform**
- [ ] `server.port=${PORT:8080}` in prod
- [ ] `ddl-auto=update` (not `create-drop`) in prod
- [ ] PostgreSQL linked; app starts without JDBC errors
- [ ] Healthcheck `/home` returns 200 on Railway domain
- [ ] Redeploy preserves existing data

**Security**
- [ ] No real secrets in git
- [ ] `JWT_SECRET` set on Railway (strong random)
- [ ] OTP bypass closed
- [ ] IDOR on charge closed
- [ ] GET booking by id protected
- [ ] Pending password hashed in memory

**Payment**
- [ ] No member `/pay` simulate from frontend
- [ ] Webhook URL registered in Midtrans
- [ ] Webhook confirms booking + slots `BOOKED`
- [ ] Snap script URL matches `MIDTRANS_IS_PRODUCTION`

**Functional**
- [ ] OTP register works with Railway SMTP env vars
- [ ] `HELD` constraint exists in production DB
- [ ] `./mvnw test` passes locally

### NO-GO — do not deploy if any true

- [ ] `create-drop` still in prod profile
- [ ] Midtrans/Gmail passwords in committed files
- [ ] Frontend still calls `/api/bookings/{id}/pay` after Snap
- [ ] `/api/auth/register` still creates users without OTP
- [ ] No `server.port=${PORT}` — healthcheck will fail

---

## 7. Manual QA script (production)

1. Open `https://<railway-domain>/home` — landing page OK.
2. Register via `/register` — OTP email arrives.
3. `curl -X POST https://<domain>/api/auth/register -H "Content-Type: application/json" -d '{"name":"x","email":"t@t.com","password":"123456"}'` → rejected.
4. Login → book court → Pay Now → DevTools shows **no** `POST .../pay` after Snap.
5. Complete Midtrans sandbox payment → booking becomes `CONFIRMED` within ~1 min (webhook).
6. Redeploy from Railway dashboard → login still works, bookings still visible.
7. Second user cannot charge first user's booking (403).

---

## 8. Out of scope

- Multi-instance / Redis OTP
- Flyway/Liquibase full migration framework
- httpOnly JWT cookies
- Custom domain + SSL (Railway provides HTTPS on `*.up.railway.app`)
- PCI compliance audit
- `fix3.md` UI fixes (separate; should be done but not blocking deploy if cancel/reschedule already fixed)

---

## 9. Success statement

After **all Phase 1–5** of `fix4.md` are implemented:

- SI-BLO **starts reliably on Railway**, binds the correct port, and **retains data** across redeploys.
- **Payment** is webhook-driven with no simulate bypass; slots stay consistent.
- **OTP registration** is the only register path; secrets are not in git.
- **Authorization** closes IDOR and data leaks.
- **Midtrans** works in both sandbox and production via env-driven Snap URL.
- Team has **DEPLOY-RAILWAY.md** with env vars and webhook setup for repeatable deploys.

**Current status (re-audit #2): HAMPIR SIAP — fix B1–B4 + jalankan migration SQL di Railway sebelum GO.**

---

## 12. Phase 8 — Sisa perbaikan sebelum GO (re-audit #2)

Jalankan setelah Phase 1–5. Urutan disarankan: **B1 → B2/B3 → DB migration → B4**.

---

### Fix B1 — `/register` page diblokir SecurityConfig

**Gejala:** `GET /register` → 401 Unauthorized (karena `.anyRequest().authenticated()`).

**File:** `SecurityConfig.java`

Tambahkan ke `permitAll`:

```java
.requestMatchers("/", "/home", "/login", "/register", "/booking", "/my-bookings",
    "/manage-admin", "/privacy", "/terms", "/map",
```

---

### Fix B2 — Samakan env var email (Railway OTP)

**Gejala:** OTP tidak terkirim di Railway — `spring.mail.username` kosong karena nama env salah.

**Opsi A (disarankan):** Ubah `application-prod.properties`:

```properties
spring.mail.username=${EMAIL_USERNAME:${MAIL_USERNAME:}}
spring.mail.password=${EMAIL_PASSWORD:${MAIL_PASSWORD:}}
```

**Opsi B:** Ubah `DEPLOY-RAILWAY.md` agar pakai `MAIL_USERNAME` / `MAIL_PASSWORD` (sesuai prod saat ini).

Pastikan **satu nama** dipakai di doc + prod + Railway dashboard.

---

### Fix B3 — `EmailService.setFrom` dari config

**File:** `EmailService.java`

```java
@Value("${spring.mail.username}")
private String mailFrom;

public void sendOtpEmail(String toEmail, String otpCode) {
    ...
    message.setFrom(mailFrom);
```

Jangan hardcode `si.bookinglapangonline@gmail.com`.

---

### Fix B4 — Tutup API bypass `POST /api/bookings/{id}/pay`

**File:** `BookingService.payBooking()`

Endpoint masih mengonfirmasi booking + slot `BOOKED` tanpa cek payment Midtrans.

**Fix:** Hanya izinkan jika payment sudah `COMPLETED` (webhook sudah jalan), atau return error:

```java
if (booking.getStatus() == BookingStatus.CONFIRMED)
    return BookingDTO.fromEntity(booking);

Payment payment = paymentRepository.findByBookingId(bookingId).orElse(null);
if (payment == null || payment.getStatus() != PaymentStatus.COMPLETED)
    throw new BookingException("Payment not yet confirmed. Complete payment via Midtrans.");

// sync slots if needed, return DTO
```

Atau hapus endpoint member `/pay` sepenuhnya (admin confirm tetap lewat `/api/admin/bookings/{id}/confirm`).

---

### Fix DB — Migration HELD (jalankan di Railway Postgres)

**File:** `src/main/resources/db/migration-v1-held-status.sql` (sudah lengkap)

Jalankan di Railway → PostgreSQL → Query / Connect:

```sql
ALTER TABLE time_slots DROP CONSTRAINT IF EXISTS time_slots_status_check;
ALTER TABLE time_slots ADD CONSTRAINT time_slots_status_check
  CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED'));
```

Tanpa ini, **first deploy bisa crash** di `DataSeeder` (sama seperti error lokal kamu).

---

### Checklist GO final (re-audit #2)

- [ ] B1: `/register` accessible tanpa login
- [ ] B2: Email env vars konsisten + diset di Railway
- [ ] B3: `setFrom` dari `spring.mail.username`
- [ ] B4: Member `/pay` tidak bypass Midtrans
- [ ] Migration HELD di Railway Postgres (jika perlu)
- [ ] `JWT_SECRET`, Midtrans keys diset di Railway
- [ ] Midtrans Notification URL → `https://<domain>/payment/notification`
- [ ] Smoke test: register OTP → book → Snap pay → webhook → CONFIRMED

---

## 13. Changelog (re-audit #2)

| Issue | Status | Action |
|-------|--------|--------|
| PORT, ddl-auto, snap-config, webhook slots, IDOR, OTP bypass | ✅ Fixed | — |
| Frontend simulate pay | ✅ Fixed | — |
| `/register` page 401 | ❌ Open | Phase 8 B1 |
| MAIL vs EMAIL env | ❌ Open | Phase 8 B2 |
| Email setFrom hardcoded | ❌ Open | Phase 8 B3 |
| API `/pay` bypass | ❌ Open | Phase 8 B4 |
| HELD CHECK migration file incomplete | ✅ Fixed | SQL file updated |
| HELD constraint on Railway DB | ⚠️ Manual | Run migration SQL once |
| Secrets default in application.properties | ⚠️ Open | Remove fallbacks (medium) |
| Test profile still PostgreSQL | ⚠️ Open | Optional H2 (low) |
