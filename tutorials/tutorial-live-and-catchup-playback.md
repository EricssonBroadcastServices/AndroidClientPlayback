# Live & Catchup Playback

This tutorial shows how to play live and catchup seamlessly with the help of an EMPPlayer instance.
If you are not familiar with the basic EMPPlayer API, please check the first 2 tutorials on Github before reading this one.


** Playing a Live program **

In order to play a Live Channel, just construct an EmpChannel object and pass it as an argument to **play()** method.
  
```java
EmpChannel channel = new EmpChannel();
channel.channelId = "MY_CHANNEL_ID";
player.play(channel, PlaybackProperties.DEFAULT);
```

If you play an EmpChannel, you do not have to know explicitly which EmpProgram is live at the current moment.
However, it might not be possible to store or play from bookmark. For that purpose, it is advised that you play the live EmpProgram instead.

```java
EmpProgram program = new EmpProgram();
program.channelId = "MY_CHANNEL_ID";
program.programId = "MY_LIVE_PROGRAM_ID";
player.play(program, PlaybackProperties.DEFAULT);
```


** Playing a Catchup **

Any program that belongs to a Channel and is not live anymore is considered to be a Catchup.
You can use the EmpProgram object to play a catchup. The code sintax is the same, regardless the program is live or not.

```java
EmpProgram program = new EmpProgram();
program.channelId = "MY_CHANNEL_ID";
program.programId = "MY_CATCHUP_ID";
player.play(program, PlaybackProperties.DEFAULT);
```


** Custom Playback Properties **

If you want a custom playback, like specifying a start time or defining autoplay mode, you can achieve that by setting those values in a **PlaybackProperties** object and passing it as a second argument of the play() call.
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

-> If the Bookmark is enabled, then playback will start from Bookmark in case there is one. Otherwise, default PlayFrom should apply.
-> If custom Start Time is set, then playback will start from Start Time.

** Stream Navigation **

The best way to navigate the stream is by calling **seekToTime()** method, which receives a unix timestamp in milliseconds as an argument.
There are other helper methods that help you start over the current program or jump to live edge: startOver() and seekToLive().

In order to check what unix time (ms) is currently being played, you can use **getPlayheadTime()**.
Other helper methods help you know importante ranges related for your stream:
- **getSeekableTimeRange()** will return an array of 2 elements that represent the seekable lower and upper bounds (seeking in this range should not require a new play request)
- **getBufferedTimeRange()** will return an array of 2 elements that represent the buffered media lower and upper bounds (seeking in this range can be done safely in the device and it will not fetch new segments from the CDN)

** Program Boundaries **

Although you specificly set playback for a specific program, the player will play until it reaches a program without a valid license for the user.
Program boundary crossings can occur meaning that the program being displayed has changed. When this happens, the listener's method **onProgramChange(EmpProgram newProgram)** will be called.

You, as a developer, can check at any time which program is currently being played by calling **getCurrentProgram()**.

In the event that the player tries to play a program that the user does not have right to watch, then the playback will stop with NOT_ENTITLED error.
If a user seeks to a program that is not available due to a gap in the EPG, a warning message is thrown and the playback continues from the sought position. 

** Contract Restrictions **

The **getEntitlement()** method return the entitlement of ongoing playback. There you can check what actions you are allowed by the backend, for instance:
- ffEnabled: if fast-forward is enabled
- rwEnabled: if rewing is enabled
- timeshiftEnabled: if timeshift is enabled. In case timeshift is disabled, then you cannot pause

More generic methods are provided by the player to check timeline restrictions: **canSeekForward()**, **canSeekBack()**, **canPause()**.

** Subtitles and Multi-Audio **

Helper methods for subtitles are as follows:
- **getSelectedTextLanguage()**: returns langugage code of selected subs language (null if none is selected)
- **getTextLanguages()**: returns an array of subtitles' languages available in the stream
- **setTextLanguage(String)**: selects current subs language to be displayed (null to disable subs)
 
 Helper methods for subtitles are as follows:
- **getSelectedAudioLanguage()**: returns langugage code of selected audio language
- **getAudioLanguages()**: returns an array of audio languages available in the stream
- **setAudioLanguage()**: selects audio language to be displayed

** Logs **
All logging is currently handled using Android's native Logging capabilities, so any framework or library compatible with native logging tools should be able to gather logs from our player and send them anywhere you want (for instance crashlytics).


