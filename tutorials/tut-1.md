# Integrating Simple Activity for Playback

The most basic integration of EMPPlayer is by extending the **SimplePlaybackActivity** class provided by the library.
In order to specify playback properties like autoplay or startTime, use class PlaybackProperties.
 
``
public class MyVideoPlayer extends SimplePlaybackActivity {

    public MyVideoPlayer() {
        super(PlaybackProperties.DEFAULT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
``

Once the derived activity is created and registered in **AndroidManifest.xml**, the player can be started by sending the playable in the Intent:

** Asset **

``
EmpAsset asset = new EmpAsset();
asset.assetId = "MY_ASSET_ID";

Intent intent = new Intent(ctx, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", asset);
context.startActivity(intent);
``

** Catchup **

``
EmpProgram program = new EmpProgram();
asset.channelId = "MY_CHANNEL_ID";
asset.programId = "MY_PROGRAM_ID";

Intent intent = new Intent(ctx, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", program);
context.startActivity(intent);
``

** Channel **

``
EmpChannel asset = new EmpChannel();
asset.channelId = "MY_CHANNEL_ID";

Intent intent = new Intent(ctx, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", playable);
context.startActivity(intent);
``