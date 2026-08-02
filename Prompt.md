# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"add in instructions that if u dont understand my request like 80% you gonna
ask me for confimation"

## Change (docs-only agent-instruction change — MUST push per DOX rail)

Added a durable rule: agents ask for confirmation when they understand a
request less than ~80%.

**`AGENTS.md` (root)**
- New **"❓ ASK WHEN UNSURE"** section after Purpose: understanding < ~80%
  → ask for confirmation (ask_user) before doing anything; never guess or
  run with the most plausible reading. Covers ambiguous phrasing, missing
  context, conflicting instructions, multiple readings. Sub-agent note:
  spawned sub-agents don't have ask_user — they report uncertainty back to
  the parent, who asks the user.

**`master.md`**
- New **"### ❓ Ask When Unsure"** bullet under User Preferences (the
  canonical home for durable behavior changes), mirroring the rule and
  deferring to root AGENTS.md per DOX hierarchy.

## Review
- code-reviewer-deepseek-flash (x2): clean. Placement logical, wording
  matches ~80% intent, master.md defers to root AGENTS.md correctly. One
  actionable note (sub-agents lack ask_user) addressed with the added
  clarification line, confirmed in the final pass.
