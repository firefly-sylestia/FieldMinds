/**
 * FieldMind Bug Reporter — Cloudflare Worker
 * ─────────────────────────────────────────────────────────────────────────
 * Forwards Android bug-report POSTs to the GitHub Issues API so users can
 * file issues directly from inside the app, without bundling a GitHub
 * Personal Access Token in the .apk.
 *
 * Quick tour
 * ──────────
 *   METHOD: POST  /  (only POST is accepted)
 *   BODY:   JSON  { title: String, body: String, labels: String[] }
 *   RESP:   200   { ok: true,  issueNumber, issueUrl }
 *           200   { error: "github_rejected", status, message }   (soft-fail)
 *           400   { error: "bad_json" | "title_required" | "body_size" }
 *           405   Method Not Allowed (non-POST)
 *           500   { error: "worker_missing_env" }
 *           502   { error: "github_unreachable" }
 *
 * Required environment variables (bound as Secrets in Cloudflare):
 *   GITHUB_TOKEN   — fine-grained PAT, scope = Issues: Write on this repo
 *   GITHUB_OWNER   — firefly-sylestia
 *   GITHUB_REPO    — FieldMinds
 *
 * Setup tutorial: see README.md in this directory.
 */

// ── Configuration ────────────────────────────────────────────────────────
const GITHUB_API = "https://api.github.com";
const MAX_TITLE_LEN = 256;
const MAX_BODY_LEN = 60_000;          // GitHub hard limit is 65_536; keep margin
const MAX_LABEL_COUNT = 10;
const HEAD_TIMEOUT_MS = 15_000;

// ── PII sanitization ─────────────────────────────────────────────────────
// Applied to `body` before it is forwarded to GitHub. Strips the most
// common accidental leaks from crash logs, exception messages, and stack
// traces without removing the useful debugging information.
function sanitizeBody(input) {
  return String(input || "")
    // ── Android internal paths ───────────────────────────────────────
    .replace(/\/data\/user\/\d+/g, "/data/user/[REDACTED]")
    .replace(/\/data\/data\/[a-zA-Z0-9_.]+/g, "/data/data/[REDACTED]")
    .replace(/\/storage\/emulated\/\d+\/Android\/data\/[^ \n\r]+/g, "/storage/[REDACTED]")
    // ── Auth tokens / API keys ───────────────────────────────────────
    .replace(/Bearer\s+[A-Za-z0-9_\-./=]+/g, "Bearer [REDACTED]")
    .replace(/Basic\s+[A-Za-z0-9+/=]+/g, "Basic [REDACTED]")
    .replace(/github_pat_[A-Za-z0-9_]{20,}/g, "github_pat_[REDACTED]")
    .replace(/ghp_[A-Za-z0-9]{20,}/g, "ghp_[REDACTED]")
    .replace(/gho_[A-Za-z0-9]{20,}/g, "gho_[REDACTED]")
    .replace(/ghu_[A-Za-z0-9]{20,}/g, "ghu_[REDACTED]")
    .replace(/ghs_[A-Za-z0-9]{20,}/g, "ghs_[REDACTED]")
    .replace(/ghr_[A-Za-z0-9]{20,}/g, "ghr_[REDACTED]")
    .replace(/xox[abprs]-[A-Za-z0-9-]{10,}/g, "[SLACK_TOKEN_REDACTED]")
    // ── Email addresses (RFC 5322-ish — covers >=99% of real usage) ──
    .replace(/[a-zA-Z0-9._%+-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+/g, "email@[REDACTED]")
    // ── IPv6 (with optional zone-id `%zone`) then IPv4 (last-resort for logs that leak them) ─
    .replace(/\b(?:[0-9a-fA-F]{1,4}:){2,7}[0-9a-fA-F]{1,4}(?:%[A-Za-z0-9_.\-]+)?\b/g, "[IP_REDACTED]")
    .replace(/\b(?:\d{1,3}\.){3}\d{1,3}\b/g, "[IP_REDACTED]");
}

// ── JSON helpers ─────────────────────────────────────────────────────────
function jsonResponse(status, payload) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "content-type": "application/json; charset=utf-8" },
  });
}

// ── Env read + validation ────────────────────────────────────────────────
function readConfig(env) {
  return {
    token: (env.GITHUB_TOKEN || "").trim(),
    owner: (env.GITHUB_OWNER || "").trim(),
    repo:  (env.GITHUB_REPO  || "").trim(),
  };
}

// ── Main request handler ─────────────────────────────────────────────────
export default {
  async fetch(request, env /*, ctx */) {
    // Only POST is accepted.
    if (request.method !== "POST") {
      return new Response("Method Not Allowed", {
        status: 405,
        headers: { Allow: "POST" },
      });
    }

    // ── Pre-flight body size guard (defense-in-depth before we read the body) ──
    // Cloudflare Workers currently accept up to ~100 MB request bodies. We enforce a
    // hard cap via Content-Length when present (cheapest), then enforce the same limit
    // again after parsing in case Content-Length was missing or lied.
    const declaredLengthRaw = request.headers.get("content-length");
    const declaredLength = declaredLengthRaw === null ? 0 : Number(declaredLengthRaw);
    if (!Number.isFinite(declaredLength) || declaredLength > MAX_BODY_LEN) {
      return jsonResponse(413, { error: "body_too_large" });
    }

    const cfg = readConfig(env);
    if (!cfg.token || !cfg.owner || !cfg.repo) {
      return jsonResponse(500, { error: "worker_missing_env" });
    }

    // ── Parse JSON body ────────────────────────────────────────────────
    let payload;
    try {
      payload = await request.json();
    } catch (_) {
      return jsonResponse(400, { error: "bad_json" });
    }

    // ── Validate fields ────────────────────────────────────────────────
    const rawTitle = (payload.title || "").toString().trim();
    if (rawTitle.length < 3 || rawTitle.length > MAX_TITLE_LEN) {
      return jsonResponse(400, { error: "title_required" });
    }
    const rawBody = (payload.body || "").toString();
    if (rawBody.length < 10 || rawBody.length > MAX_BODY_LEN) {
      return jsonResponse(400, { error: "body_size" });
    }
    const labels = Array.isArray(payload.labels) && payload.labels.length > 0
      ? payload.labels.map((l) => String(l).trim()).filter(Boolean).slice(0, MAX_LABEL_COUNT)
      : ["bug"];

    const body = sanitizeBody(rawBody);

    // ── Forward to GitHub Issues API ──────────────────────────────────
    const url = `${GITHUB_API}/repos/${encodeURIComponent(cfg.owner)}/${encodeURIComponent(cfg.repo)}/issues`;
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), HEAD_TIMEOUT_MS);

    let ghResponse;
    try {
      ghResponse = await fetch(url, {
        method: "POST",
        signal: controller.signal,
        headers: {
          "authorization": `Bearer ${cfg.token}`,
          "accept": "application/vnd.github+json",
          "content-type": "application/json; charset=utf-8",
          "user-agent": "fieldmind-bug-reporter-worker",
          "x-github-api-version": "2022-11-28",
        },
        body: JSON.stringify({ title: rawTitle, body, labels }),
      });
    } catch (e) {
      clearTimeout(timer);
      return jsonResponse(502, { error: "github_unreachable", detail: String(e?.message || e) });
    }
    clearTimeout(timer);

    // ── Translate GitHub's response ───────────────────────────────────
    const text = await ghResponse.text();
    let ghBody = {};
    try { ghBody = JSON.parse(text); } catch (_) { ghBody = { raw: text.slice(0, 500) }; }

    if (!ghResponse.ok) {
      return jsonResponse(200, {
        error: "github_rejected",
        status: ghResponse.status,
        message: ghBody.message || ghBody.raw || "unknown",
      });
    }

    return jsonResponse(200, {
      ok: true,
      issueNumber: ghBody.number,
      issueUrl: ghBody.html_url,
    });
  },
};
