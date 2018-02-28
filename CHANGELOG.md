# Release Notes

## 2.0.78

### New features and Enhancements
- Added fuzzy delay on entitlement check when program changes.

### Bug fixes
- **EMP-11019** geCurrentProgram() is now returning NULL when playback enters a Gap in EPG.
- **EMP-11009** Buffering analytics events are now less verbose and only happen when the player is in PLAYING state and cannot proceed with playback.
- **EMP-11036** Playback is now stopping and throwing error if dash manifest does not have AdaptationSets. 