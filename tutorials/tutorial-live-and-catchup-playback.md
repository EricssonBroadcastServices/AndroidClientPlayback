# Live & Catchup Playback

This tutorial shows how to play live and catchup with the help of an EMPPlayer instance.
If you are not familiar with the basic EMPPlayer API, please check the first 2 tutorials on Github before reading this one.


**Playing a Live Channel**

In order to play a Live Channel simply build an EmpChannel object specifying the channel ID and pass it as an argument to the **play()** method. When you play a live channel using an EmpChannel you do not need to know which program is live at the current moment. However, in that case, it might not be possible to store or play from a bookmark.
  
```java
EmpChannel channel = new EmpChannel();
channel.channelId = "MY_CHANNEL_ID";
player.play(channel, PlaybackProperties.DEFAULT);
```

**Playing a Live Program**

In order to play a Live Program simply build an EmpProgram object specifying the channel ID and live program ID and pass it as an argument to the **play()** method. In this case it is possible to store and play from a bookmark.

```java
EmpProgram program = new EmpProgram();
program.channelId = "MY_CHANNEL_ID";
program.programId = "MY_LIVE_PROGRAM_ID";
player.play(program, PlaybackProperties.DEFAULT);
```

**Playing a Catchup Program**

Any program that belongs to a Channel and is not live anymore is considered to be a Catchup.
In order to play a Catchup Program simply build an EmpProgram object specifying the channel ID and catchup program ID and pass it as an argument to the **play()** method. In this case it is possible to store and play from a bookmark. The code syntax is the same regardless if the program is live or not.

```java
EmpProgram program = new EmpProgram();
program.channelId = "MY_CHANNEL_ID";
program.programId = "MY_CATCHUP_ID";
player.play(program, PlaybackProperties.DEFAULT);
```


**Custom Playback Properties**

If you want to start a custom playback, such as a playback with a specified start time or with autoplay mode, you can set those values in the **PlaybackProperties** object and passing it as the second argument to the play() call.
Here is an example of a possible playback scenario:

```java
EmpProgram program = new EmpProgram();
program.channelId = "MY_CHANNEL_ID";
program.programId = "MY_PROGRAM_ID";

PlaybackProperties props = new PlaybackProperties();

// Autoplay set to false means playback does NOT start automatically
props.withAutoplay(false);

// Subs language will be set to French if available
props.withPreferredTextLanguage("fr");

// Audio language will be set to English if available
props.withPreferredAudioLanguage("en");

// Developer chose to disable native controls so that he could build his own UI controls
props.withNativeControls(false);

// Pick one of the following:
// If you want to play program from previous bookmark
props.withPlayFrom(PlaybackProperties.PlayFrom.BOOKMARK);

// If you want to play the program from the beginning of the program
props.withPlayFrom(PlaybackProperties.PlayFrom.BEGINNING);

// If you want to play the program from a specific UNIX timestamp
props.withPlayFrom(new PlaybackProperties.PlayFrom.StartTime(START_TIME_UNIX_TIME_IN_MILISECONDS));

// If the user has limited bandwidth, it is possible to set max bitrate before playback in order to save network resources 
props.withMaxBitrate(2000000);

player.play(program, props);
```

The PlayFrom behaviour varies depending on the type of the stream:
- Live Channel: playback starts from Live Edge by default
- Live Program: playback starts from Live Edge by default
- Catchup: playback starts from Beginning by default

-> If Bookmark is enabled, playback will start from the Bookmark in the case there is one. Otherwise, default PlayFrom will apply.
-> If a custom Start Time is set, playback will start from the specified Start Time.

**Stream Navigation**

The best way to navigate the stream is by calling the **seekToTime()** method which receives a unix timestamp in milliseconds as an argument.
There are other helper methods that help you start over the current program or jump to live edge: startOver() and seekToLive().

In order to check what unix time (ms) the playhead is currently at, you can use **getPlayheadTime()**.
Other helper methods help you know important ranges related for your stream:
- **getSeekableTimeRange()** will return an array of 2 elements that represent the seekable lower and upper boundaries (seeking in this range will not require a new play request)
- **getBufferedTimeRange()** will return an array of 2 elements that represent the buffered media lower and upper boundaries (seeking in this range can be done safely in the device and it will not fetch new segments from the CDN)

**Program Boundaries**

Although you started the playback of a specific program, the player will continue playing until it reaches a program without a valid license for the user.
Program boundary crossings can occur meaning that the program being displayed has changed. When this happens, the listeners **onProgramChange(EmpProgram newProgram)** method will be called.

You, as a developer, can check at any time which program is currently being played by calling **getCurrentProgram()**.

In the event that the player tries to play a program that the user does not have the right to watch, the playback will stop and throw a NOT_ENTITLED error.
If a user seeks to a program that is not available due to a gap in the EPG, a warning message is thrown and the playback continues from the sought position. 

**Contract Restrictions**

The **getEntitlement()** method returns the entitlement of the ongoing playback. There you can check what actions you are allowed by the backend, for instance:
- ffEnabled: true -> if fast-forwarding is enabled
- rwEnabled: false -> if rewinding is disabled
- timeshiftEnabled: true -> if timeshifting is enabled. In the case where timeshift is disabled you cannot pause the playback.

More generic methods are provided by the player to check timeline restrictions: **canSeekForward()**, **canSeekBack()**, **canPause()**.

**Subtitles and Multi-Audio**

Helper methods for subtitles are as follows:
- **getSelectedTextLanguage()**: returns langugage code of selected subtitle track (null if none is selected)
- **getTextLanguages()**: returns an array of subtitle languages that are available in the stream
- **setTextLanguage(String)**: selects a subtitle language to be displayed (null to disable subtitles)
 
 Helper methods for multiple audio are as follows:
- **getSelectedAudioLanguage()**: returns langugage code of selected audio language
- **getAudioLanguages()**: returns an array of audio languages that are available in the stream
- **setAudioLanguage()**: selects an audio language to be displayed

** Logs **
All logging is currently handled using Android's native Logging capabilities, so any framework or library compatible with native logging tools should be able to gather logs from our player and send them anywhere you want (for instance crashlytics).


