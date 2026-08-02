# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

"when it animated in shuffle the peek cards look weird like its cutt off or
something, so fix that without chnaging the design."

## Root cause

During the Spin shuffle reel (v6.6 wipe), the peek cards' `AnimatedContent`
slides content ±height/3, and `togetherWith` defaults to
`SizeTransform(clip = true)` — the sliding card is hard-clipped at the
card's own top/bottom edge, so the peek looks sliced in half mid-wipe. The
same artifact existed on the hero content reel (title/tags/teaser, ±height/3
and /4 slides). It only became visible after the v6.6 wipe went from 90ms
linear to 200ms eased — long enough to actually see the clip.

## Fix (design untouched — motion identical, clipping only)

**`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
- Added `using SizeTransform(clip = false)` to all four `AnimatedContent`
  transitionSpec branches:
  - Peek card shuffling (200/180ms)
  - Peek card non-shuffling (240/200ms)
  - Hero content shuffling (200/180ms)
  - Hero content non-shuffling (260/240ms)
- `import androidx.compose.animation.SizeTransform` added (alphabetical).
- v6.8 comments added to both transitionSpecs explaining the unclip.

Unclipped overflow is benign: zIndex ordering (back peek 2 < front peek 5 <
hero 10) keeps overlap clean, and the hero's outer rounded clip still
bounds the card edge.

## Review
- code-reviewer-deepseek-flash (x2): clean. All four branches verified,
  `using` precedence correct (`+` binds tighter than infix, left-assoc →
  SizeTransform attaches to the whole ContentTransform), single alphabetical
  import, durations/easings/directions untouched.
- Per AGENTS.md no local Gradle build — CI validates compilation on push.
