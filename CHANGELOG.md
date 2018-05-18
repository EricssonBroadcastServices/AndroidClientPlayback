# Release Notes

## 2.0.89

### New features and Enhancements
- **EMP-11067** updated timeline ui controls for live programs

### Bug Fixes
- **EMP-11328** fix start time issue in SimplePlaybackActivity


## 2.0.81

### New features and Enhancements
- ExoPlayer Update - v2.7.0 (https://github.com/google/ExoPlayer/tree/r2.7.0)
- Adding null-check for tech on preparePlayback@EMPPlayer function

### Bug fixes
- **EMP-10988** Fixed text relocations in libaricentomxplugin.so
- **EMP-11216** Sending onStop from tech so that analytics Aborted event is always sent
- **EMP-11217** Preventing double analytics when user seeks to previous program
- **EMP-11220** Added a configurable safety offset for start over so that there is no risk the exo player inadvertently plays a few milliseconds before the program starts


## 2.0.80

### Bug fixes
- Warnings are no longer treated as errors
- When device loses connection to the internet while playback is ongoing NETWORK_ERROR is triggered instead of EXO_PLAYER_INTERNAL_ERROR


## 2.0.79

### New features and Enhancements
- Added native controls extra PlaybackProperties (withNativeControlsHideOnTouch, withNativeControlsShowTimeoutMs).

### Bug fixes
- Fixed bug in release() method in Player in which the Analytics listener was being wrongly detached.
- **EMP-10982** player no longer sends WaitingEnd event after playback is aborted.
- **EMP-11090** removed setVisibility(INVISIBLE) in ExoPlayer Tech as it is no longer needed and causes crash in some devices.

## 2.0.78

### New features and Enhancements
- Added program-based timeline in EMP built-in controls.
- Added fuzzy delay on entitlement check when program changes.

### Bug fixes
- **EMP-11019** geCurrentProgram() is now returning NULL when playback enters a Gap in EPG.
- **EMP-11009** Buffering analytics events are now less verbose and only happen when the player is in PLAYING state and cannot proceed with playback.
- **EMP-11036** Playback is now stopping and throwing error if dash manifest does not have AdaptationSets. 

## 2.0.77 --> First official release

### Features
- Live playback starts from the "live edge" as default behaviour. There is an option to start the live playback from the beginning of the current live program or the bookmarked position. 
- Catchup playback starts from the beginning of the program as default behaviour. There is an option to start the catchup playback from the bookmarked position.
- If timeshift is enabled the playback can be paused/resumed at any time.
- If timeshift and rewind are enabled it is possible to jump back to the beginning of the current program.
- If timeshift and rewind are enabled it is possible to jump back 30s, potentially into the previous program.
- If timeshift and rewind are enabled it is possible to scrub or seek using the progress bar to any point between the current playhead position and the beginning of the program. 
- It is always possible to jump to the live point.
- If timeshift and fast-forward are enabled it is possible to jump forward 30s, potentially into the next program.
- If timeshift and fast-forward are enabled it is possible to scrub or seek using the progress bar to any point between the playhead position and the live edge or the end of the current catchup program.
- When playing a catchup program, the playback continues seamlessly between programs (no reload / license request needed).
- Playback stops if the user is not entitled to the upcoming program.
- If a user seeks to a program that is not available due to a gap in the EPG, a warning message is thrown and the playback continues from the sought position. 
- It is possible to switch subtitles on/off if there is an available subtitle track on the channel.
- It is possible to select a subtitle language if there are several available subtitle tracks on the channel.
- It is possible to select a default/preferred subtitle language, used if available. If the default subtitle language is not available, the playback starts without subtitles.
- It is possible to choose an audio track if there are several available audio tracks on the channel.
- It is possible to select a default/preferred audio track.
- It is possible to retrieve the restrictions that apply to the current program (disabled controls, contract restrictions, ...).
- It is possible to limit the streaming quality (maxBitrate as a playback property)
