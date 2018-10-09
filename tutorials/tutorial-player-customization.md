# Customizing Player UI


To create a new content view for your activity use **bindContentView**.
If you want to create controls for the player (play/pause, seekbar, etc..) it is possible by disabling native controls when setting **nativeControls** property in the **PlaybackProperties** class.

```java
public class MyVideoPlayer extends SimplePlaybackActivity {

    public MyVideoPlayer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		this.properties = new PlaybackProperties().withNativeControls(false);
        this.bindContentView(R.layout.mycustomlayout);
        super.onCreate(savedInstanceState);
    }
}
```

A custom XML layout should look like this:
Any type of Layout is allowed, what is important is to include **net.ericsson.emovs.playback.ui.views.EMPPlayerView** instance(s).
Note that it is possible to have several **EMPPlayerView** in one layout in order to achieve a multiview experience.

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
	xmlns:android="http://schemas.android.com/apk/res/android"
	android:id="@+id/empplayer_layout"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:orientation="vertical">
	<net.ericsson.emovs.playback.ui.views.EMPPlayerView 
		xmlns:android="http://schemas.android.com/apk/res/android"
		android:id="@+id/myempplayer"
		android:layout_width="match_parent"
		android:layout_height="0dp"
		android:layout_weight="1">
	</net.ericsson.emovs.playback.ui.views.EMPPlayerView>
</LinearLayout>
```

To play an asset, the reference code is as follows:

```java
	final Context myContext = getApplicationContext();
	EMPPlayerView view = (EMPPlayerView) findViewById(R.id.myempplayer);	// the id will probably change in your implementation
	EMPPlayer player = view.getPlayer();
	player.clearListeners();
	player.addListener(new EmptyPlaybackEventListener(player) {
		@Override
		public void onError(int errorCode, String errorMessage) {
			Toast.makeText(myContext, errorMessage, Toast.LENGTH_SHORT).show();
		}

	});
	
	EmpAsset asset = new EmpAsset();
	asset.assetId = 'your_asset_id';
	player.play(asset, PlaybackProperties.DEFAULT);
```

If one wants to implement custom controls, then all that has to be done is fetch the **EMPPlayerView** object in the activity and register a listener in the associated **EMPPlayer** instance.
This listener must implement the **IPlaybackEventListener** interface or extend the **EmptyPlaybackEventListener** class.

```java
EMPPlayerView view = (EMPPlayerView) findViewById(R.id.myempplayer);
view.getPlayer().addListener(new EmptyPlaybackEventListener(view.getPlayer()) {
	@Override
	public void onError(int errorCode, String errorMessage) {
		// Your logic here ...
	}
	
	// ...
});
```

The **EMPPlayer** class also exposes more methods to fully enable control of the playback: pause, resume, stop, play, seekTo, etc..
For more information, check the documentation.

### Playback Throttling note

The EMPPlayer.play() method has a throttling mechanism that prevents spamming play() calls.
This feature is disabled by default. It can be enabled byt calling the following method on the EMPRegistry:

```java
// Enable the playback throttling on the app
EMPRegistry.enablePlaybackThrottling();
```

If the EMPPlayer.play() method is called again (one or multiple times) in less than 3 seconds,
the player does not start playback again and nothing happens.
This is put in place to prevent several internal resources allocation,
which can degrade performance of the player and libraries.

Affected method is shown below:
```java
public class EMPPlayer extends Player implements IEntitledPlayer {
...
    public void play(IPlayable playable, PlaybackProperties properties) {
        ...
    }
...
}
```
