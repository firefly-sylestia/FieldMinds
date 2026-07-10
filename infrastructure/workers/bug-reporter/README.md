# FieldMind Bug Reporter — Cloudflare Worker

This directory contains the Cloudflare Worker that the FieldMind Android app uses
to file bug reports into the GitHub Issues API.

The Worker sits between your users and GitHub so that:

1. **Your GitHub token never ships inside the .apk.** Decompiled APKs are trivially
   decompilable — bundling a token would let anyone either spam your repo or abuse
   your account. With this Worker, the token stays server-side.
2. **PII gets stripped before GitHub sees it.** Crash logs are sanitized for
   Android internal paths, auth tokens, email addresses, and IP addresses — see
   `sanitizeBody()` in `worker.js`.
3. **Abuse is bounded.** The Worker accepts only POST requests, rejects oversized
   payloads, and (optionally) adds a per-IP rate limit using Cloudflare's free
   `Rate Limiting` rules.

If you don't want to run any infra at all, use the simpler
[Zero-infra web-URL approach](#zero-infra-alternative-recommended-for-truly-static-projects)
described at the bottom of this document instead.

---

## 1. One-time Cloudflare account setup (≈ 5 minutes)

If you don't already have one:

1. Go to **https://dash.cloudflare.com/sign-up** and create a free account.
2. Verify your email address.
3. You will land on the Cloudflare dashboard.

The free Workers plan gives you **100,000 requests / day**. FieldMind is going
to use a tiny fraction of that.

---

## 2. Create the Worker (≈ 2 minutes)

1. In the Cloudflare dashboard, click **Workers & Pages** in the left sidebar.
2. Click **Create application**.
3. Click **Create Worker** (not Pages).
4. Give it a name. Suggested name: **`fieldmind-bug-reporter`**.
5. Click **Deploy** — this creates an initial hello-world Worker at
   `https://fieldmind-bug-reporter.<your-cloudflare-subdomain>.workers.dev`.
6. Click **Edit code** (or **Open editor** depending on UI version).
7. **Delete all of the sample code** currently in the editor.
8. **Paste the entire contents of [`worker.js`](./worker.js)** into the editor.
9. Click **Save and Deploy**.

> **Test it manually (optional):**
>
> In a terminal:
>
> ```bash
> curl -X POST https://fieldmind-bug-reporter.<your-account>.workers.dev/ \
>   -H "content-type: application/json" \
>   -d '{"title":"[TEST] Worker smoke test","body":"Hello from a curl test. Please ignore.","labels":["bug"]}'
> ```
>
> If the secrets are not yet set, you'll get `{"error":"worker_missing_env"}`.
> That is correct — set up the secrets next, then re-run.

---

## 3. Bind three secrets (≈ 3 minutes)

The Worker needs three environment variables, all of which should be **Secrets**
(encrypted at rest, never echoed back).

1. Go to your Worker → **Settings** tab → **Variables and Secrets** section.
2. Click **Add** three times, one for each of:

| Type | Name           | Value                                                |
|------|----------------|------------------------------------------------------|
| Secret | `GITHUB_TOKEN` | A fine-grained GitHub PAT (see step 4 to create one) |
| Secret | `GITHUB_OWNER` | `firefly-sylestia`                                  |
| Secret | `GITHUB_REPO`  | `FieldMinds`                                        |

3. Click **Deploy** (or **Save**) so the Worker re-binds with the new secrets.

---

## 4. Create the GitHub fine-grained Personal Access Token (≈ 3 minutes)

A **classic** PAT has repo-wide scope and can be a footgun. Use a fine-grained
one so the token can only file issues on this one repo.

1. Go to **https://github.com/settings/personal-access-tokens/new** (or
   Settings → Developer settings → Personal access tokens → Fine-grained tokens
   → Generate new token).
2. **Token name:** `FieldMind Bug Reporter (Cloudflare Worker)`.
3. **Resource owner:** `firefly-sylestia`.
4. **Expiration:** pick 90 days or 1 year — set a calendar reminder to renew.
   You'll need to update the Cloudflare secret on the same day.
5. **Repository access:** "Only select repositories" → check **`FieldMinds`**.
6. **Permissions:**
   - Account permissions: none.
   - Repository permissions: **Issues: Read and write**. Nothing else.
7. Click **Generate token**.
8. **Copy the token immediately** — GitHub only shows it once.
9. Paste it into the Cloudflare Worker's `GITHUB_TOKEN` secret (step 3).

> **Why a fine-grained token?**
>
> If a fine-grained token ever leaks, an attacker can only file issues on the
> one repo you scoped it to. A classic token with `repo` scope can wipe your
> repository.

---

## 5. Test end-to-end (≈ 2 minutes)

Re-run the curl test from step 2:

```bash
curl -X POST https://fieldmind-bug-reporter.<your-account>.workers.dev/ \
  -H "content-type: application/json" \
  -d '{
    "title": "[TEST] Bug Reporter smoke test",
    "body": "## Steps to Reproduce\n1. Run this curl.\n2. Expect an issue filed.\n\n## Expected vs Actual\nExpected: issue 1 (or N) filed on the repo.\nActual: TBD.\n\n## App\nVersion: 0.45.0-gh\nDevice: curl on macOS",
    "labels": ["bug", "test"]
  }'
```

Expected response (HTTP 200 always):

```json
{ "ok": true, "issueNumber": 42, "issueUrl": "https://github.com/firefly-sylestia/FieldMinds/issues/42" }
```

Then open that URL in your browser — you should see a newly created GitHub issue
with title `[TEST] Bug Reporter smoke test`, body containing the markdown you
sent, and labels `bug` + `test`.

If you see `{ "error": "github_rejected" }`, check:

- The token hasn't expired.
- The repo name is exactly `FieldMinds` (case-sensitive).
- The token has `Issues: Read and write` and is scoped to `firefly-sylestia/FieldMinds`.

---

## 6. Wire the URL into the Android app (≈ 1 minute)

Edit `app/build.gradle.kts`. Near the existing `buildConfigField` declarations,
add (or uncomment / update) the `BUG_REPORTER_URL`:

```kotlin
buildConfigField(
    "String",
    "BUG_REPORTER_URL",
    "\"https://fieldmind-bug-reporter.<your-account>.workers.dev\""
)
```

(or pass it via a `gradle.properties` entry so different flavors can target
different workers — see `app/build.gradle.kts` for an example).

The app reads this value as `BuildConfig.BUG_REPORTER_URL`. If you'd rather
leave it unset while testing, the in-app reporter will show a friendly
"Reporter not configured — let the user copy the body to clipboard" message
instead of failing hard.

---

## 7. (Optional but recommended) Add a rate limit

Cloudflare's free tier already protects you with per-Worker concurrency limits,
but explicit rate limiting per source IP is a one-click win.

1. Worker → **Settings** → **Variables** (NOT Secrets).
2. Click **Add** with Type = `Workers KV Namespace`, name whatever you want
   (or skip if you don't want to bind state).
3. Or, simpler: **Worker → Settings → Triggers → Add route**, then go to
   **Account Home → Security → WAF → Rate limit rules** and create:

| Field            | Value                                  |
|------------------|----------------------------------------|
| Rule name        | `fieldmind-bug-reporter-rate-limit`    |
| Match expression | `http.request.uri eq "/"`             |
| Rate             | `5 requests per 10 seconds per IP`     |
| Action           | `Block for 60 seconds`                 |

This caps the Worker at half a million issues per day from a single user —
more than enough for a hobby project.

---

## 8. (Optional) Local development

If you have `wrangler` installed (`npm install -g wrangler`):

```bash
cd infrastructure/workers/bug-reporter
wrangler dev
```

It will prompt for a `*.workers.dev` URL and start a local server. For full
end-to-end testing you still need real GitHub credentials bound (use a
`.dev.vars` file locally — gitignored at the repo root).

---

## Zero-infra alternative (recommended for truly static projects)

If you don't want to run the Worker at all, the Android client can be
configured to fall back to opening
`https://github.com/firefly-sylestia/FieldMinds/issues/new?template=bug-report.yml&title=...&body=...`
in the user's browser via `Intent.ACTION_VIEW`. The user signs into their own
GitHub account in the browser, no auth needed on the app side, zero
infrastructure. The trade-off is a two-step UX (review in app → submit in
browser), and you lose the PII sanitization the Worker provides.

To use this path, leave `BUG_REPORTER_URL` empty in `build.gradle.kts` and
the app will automatically use the web URL fallback.

---

## File map

```
infrastructure/workers/bug-reporter/
├── README.md       ← you are here
├── worker.js       ← Cloudflare Worker source
└── wrangler.toml   ← sample Wrangler config (optional, for CLI users)
```
