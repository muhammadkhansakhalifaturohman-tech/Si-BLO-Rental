# Deploy SI-BLO to Railway

## Prerequisites
1. Railway account + CLI (`railway login`)
2. PostgreSQL plugin added to your Railway project
3. Midtrans production/live account (or sandbox for staging)
4. Gmail account with App Password

## Steps

### 1. Environment Variables
Set these in Railway Dashboard → Variables:

| Variable | Example Value | Notes |
|---|---|---|
| `PORT` | `8080` | Railway injects this automatically |
| `JWT_SECRET` | `a-strong-random-secret-256-bits` | Required |
| `MIDTRANS_SERVER_KEY` | `Mid-server-...` | From Midtrans dashboard |
| `MIDTRANS_CLIENT_KEY` | `Mid-client-...` | From Midtrans dashboard |
| `MIDTRANS_IS_PRODUCTION` | `true` | `false` for sandbox |
| `EMAIL_USERNAME` | `your@gmail.com` | Gmail address |
| `EMAIL_PASSWORD` | `your-app-password` | Gmail App Password |
| `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` | | Injected by Railway PostgreSQL plugin |

### 2. Midtrans Payment Notification URL
Set in Midtrans Dashboard → Settings → Configuration:
```
https://your-app.railway.app/payment/notification
```

### 3. Deploy
```bash
railway login
railway link
railway up
```

### 4. Verify
- Health: `https://your-app.railway.app/`
- API: `https://your-app.railway.app/api/courts`
- Midtrans webhook: `https://your-app.railway.app/payment/notification`

## Notes
- `ddl-auto=update` preserves data across restarts (no more `create-drop`)
- Thymeleaf cache is ON in production for performance
- Static assets are served via Thymeleaf, not a CDN — consider Cloudflare in front for production
- OTP is stored in-memory (not suitable for multi-instance scale-out — use Redis if needed)
