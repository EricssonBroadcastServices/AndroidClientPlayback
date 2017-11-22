package net.ericsson.emovs.playback.interfaces;

import android.app.Activity;

import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.Player;


/**
 * Created by Joao Coelho on 2017-08-29.
 */

public interface ITech {
    boolean isPlaying();
    void init(Player parent, Activity ctx, String playToken, PlaybackProperties properties);
    boolean load(String mediaId, String manifestUrl, boolean isOffline);
    void play(String dashManifestUrl);
    void release();
    void pause();
    void resume();
    void stop();
    void seekTo(long positionMs);
    long getCurrentTime();
    long getDuration();
    int getCurrentBitrate();
    String getIdentifier();
    String getVersion();
}
