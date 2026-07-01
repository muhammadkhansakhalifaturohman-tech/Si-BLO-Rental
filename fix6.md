# SI-BLO Audit & Fix Report (fix6)

## Goal: PWA support + SendGrid SMTP OTP + PostgreSQL migration (Supabase)

---

## 1. PWA (Progressive Web App) — ✅ Added

### Files Created
| File | Path | Purpose |
|------|------|---------|
| `manifest.json` | `src/main/resources/static/manifest.json` | Web app manifest (name, icons, theme) |
| `sw.js` | `src/main/resources/static/sw.js` | Service worker (offline cache + fetch) |

### Assets
Ikon PWA dari `favicon_io/` telah disalin ke `src/main/resources/static/`:
- `favicon.ico`, `favicon-16x16.png`, `favicon-32x32.png`
- `apple-touch-icon.png`
- `android-chrome-192x192.png`, `android-chrome-512x512.png`

### Template Changes
**`src/main/resources/templates/fragments/head.html`** — ditambahkan:
- `<link rel="manifest" href="/manifest.json">`
- `<meta name="apple-mobile-web-app-capable" content="yes">`
- `<meta name="theme-color" content="#6c5ce7">`
- `<link rel="icon">` untuk favicon berbagai ukuran
- `<link rel="apple-touch-icon">` untuk iOS
- Service worker registration script (deferred `onload`)

---

## 2. Email OTP via SendGrid SMTP — ✅ Changed

**Gmail SMTP dihapus, diganti SendGrid.**

### File Changed
`EmailService.java`:
- `message.setFrom()` sekarang baca dari `app.mail.from` env var (tidak hardcoded)
- Nilai default: `muhammadkhansakhalifaturohman@gmail.com`

### Configuration
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${MAIL_PASSWORD}
app.mail.from=${MAIL_FROM}
```

### WhatsApp dihapus total
- `WhatsAppService.java` — dihapus
- `AuthController.java` — semua referensi WhatsAppService dihapus
- `register.html` — field No. WhatsApp dihapus dari form & JavaScript

---

## 3. PostgreSQL Migration for Supabase — ✅ Done

### Default Database Change
`application.properties`:

```
Before: jdbc:h2:mem:db_siblo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
After:  jdbc:postgresql://${PGHOST:localhost}:${PGPORT:5432}/${PGDATABASE:db_siblo}?sslmode=require
```

- `?sslmode=require` untuk koneksi aman ke Supabase
- Environment variables Railway (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`)
- `spring.profiles.include=local` dihapus

### application-h2.properties (new)
Profile `h2` untuk development lokal tanpa PostgreSQL:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2,local
```

### application-prod.properties
Disederhanakan — hanya override spesifik Railway.
`seed.enabled=${SEED_ENABLED:false}` — seed mati secara default di production.

### application-test.properties
Menggunakan H2 in-memory (test self-contained, tidak perlu database eksternal).

---

## 4. Cara Menjalankan

### Local Development (PostgreSQL)
```bash
./mvnw spring-boot:run
```

### Local Development (H2 — tanpa PostgreSQL)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2,local
```

### Railway / Production
```bash
# Sudah dikonfigurasi di railway.json:
--spring.profiles.active=prod
```

### Running Tests
```bash
./mvnw test
```

---

## 5. Verdict

| Requirement | Status | Notes |
|---|---|---|
| PWA (manifest, service worker, icons) | ✅ | Installable, offline-ready |
| OTP via SendGrid SMTP | ✅ | Configurable sender email |
| PostgreSQL (Supabase, sslmode=require) | ✅ | Env vars via Railway |
| H2 fallback for local dev | ✅ | Profile `h2` |
| Railway deploy | ✅ | railway.json & Procfile |
| Tests self-contained | ✅ | H2 in-memory |
| WhatsApp dihapus | ✅ | Tidak dipakai |

---

## 6. Environment Variables Reference (Final)

| Variable | Required | Default | Description |
|---|---|---|---|
| `PGHOST` | **Yes** | — | Supabase DB host |
| `PGPORT` | No | `5432` | PostgreSQL port |
| `PGDATABASE` | No | `postgres` | Database name |
| `PGUSER` | No | `postgres` | Database user |
| `PGPASSWORD` | **Yes** | — | Supabase DB password |
| `JWT_SECRET` | **Yes** | — | 256-bit key for JWT |
| `MIDTRANS_SERVER_KEY` | **Yes** | — | Midtrans server key |
| `MIDTRANS_CLIENT_KEY` | **Yes** | — | Midtrans client key |
| `MIDTRANS_IS_PRODUCTION` | No | `false` | Midtrans mode |
| `MAIL_HOST` | No | `smtp.sendgrid.net` | SMTP server |
| `MAIL_PORT` | No | `587` | SMTP port |
| `MAIL_USERNAME` | No | `apikey` | Literal "apikey" untuk SendGrid |
| `MAIL_PASSWORD` | **Yes** | — | SendGrid API key |
| `MAIL_FROM` | No | `muhammadkhansakhalifaturohman@gmail.com` | Sender email |
| `SEED_ENABLED` | No | `false` | Aktifkan seed data awal |
