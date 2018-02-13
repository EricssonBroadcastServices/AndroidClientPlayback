# Integrating Simple Activity for Playback

The most basic integration of the EMPPlayer is by extending the **SimplePlaybackActivity** class provided by the library.
In order to specify playback properties like autoplay or startTime, use the PlaybackProperties class.
 
```java
import android.os.Bundle;
import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.ui.activities.SimplePlaybackActivity;

public class MyVideoPlayer extends SimplePlaybackActivity {

    public MyVideoPlayer() {
        super(PlaybackProperties.DEFAULT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
```

Once the derived activity is created and registered in **AndroidManifest.xml**, the player can be started by sending the playable to the Intent:

**Asset**

```java
EmpAsset asset = new EmpAsset();
asset.assetId = "MY_ASSET_ID";

Intent intent = new Intent(context, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", asset);
context.startActivity(intent);
```

**Offline Asset**

```java
EmpOfflineAsset asset = new EmpOfflineAsset();
asset.localMediaPath = "PATH_TO_MEDIA_PARENT_FOLDER";

Intent intent = new Intent(context, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", asset);
context.startActivity(intent);
```

**Program**

```java
EmpProgram program = new EmpProgram();
program.channelId = "MY_CHANNEL_ID";
program.programId = "MY_PROGRAM_ID";

Intent intent = new Intent(context, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", program);
context.startActivity(intent);
```

**Channel**

```java
EmpChannel channel = new EmpChannel();
channel.channelId = "MY_CHANNEL_ID";

Intent intent = new Intent(context, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", channel);
context.startActivity(intent);
```

**NOTE:** in order for the entitlement to be loaded from the backend, the **ApiUrl**, **CustomerId** and **BusinessId** need to be set when the Application is created. 
Also, the user has to be logged in. For authentication flows, please read Exposure library tutorials [here](https://github.com/EricssonBroadcastServices/AndroidClientExposure/tree/master/tutorials) or check our reference app [here](https://github.com/EricssonBroadcastServices/AndroidClientReferenceApp). 

```java

// ...
import net.ericsson.emovs.utilities.ContextRegistry;
import net.ericsson.emovs.exposure.auth.EMPAuthProviderWithStorage;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EMPRegistry.bindApplicationContext(this);
        EMPRegistry.bindExposureContext(Constants.API_URL, Constants.CUSTOMER, Constants.BUSSINESS_UNIT);
		// ...
	}
	
	// ...
}
```
