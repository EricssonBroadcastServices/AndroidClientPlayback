# Customizing Player UI controls


One way of customizing the activity UI and it is not possible to append UI elements to the content view, then the method **bindContentView** shall be used to create a custom layout.
If you also want to create video controls (play/pause, seekbar, etc..), then it is possible to disable native controls by setting **nativeControls** property in **PlaybackProperties** class.

```java
public class MyVideoPlayer extends SimplePlaybackActivity {

    public MyVideoPlayer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
		this.properties = PlaybackProperties.DEFAULT.withNativeControls(false);
        this.bindContentView(R.layout.mycustomlayout);
        super.onCreate(savedInstanceState);
    }
}
```

A custom XML layout should look like this:

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
		android:id="@+id/empplayer_layout_1"
		android:layout_width="match_parent"
		android:layout_height="0dp"
		android:layout_weight="1">
	</net.ericsson.emovs.playback.ui.views.EMPPlayerView>
</LinearLayout>
```

Note that it is possible to have several **EMPPlayerView** in one layout in order to achieve a multiview experience.