# Prompt.md — Request Log

## Latest Request (IN PROGRESS)

**Correct Home colors and fully revert the torn-paper/quote-tilt commit**

### Requested

- Today's Quest should use a readable, better-balanced color.
- In default light mode, the Home menu and profile icons must not turn white.
- Fully revert commit `c8bb0dc784a712ad520440b4ba582d58765308c`, which introduced the deeper torn-paper layer and quote-tilt stabilization changes.
- Do not change the Material palette or gradients.

### Plan

1. Resolve the in-progress revert of `c8bb0dc7` while preserving the current request log.
2. Use a Home-only dark ink resolver for default light mode; keep pastel and dark behavior readable.
3. Apply that resolver to Today's Quest and the sticky menu/profile controls.
4. Run safe static checks, review, update the changelog, and commit/push.
