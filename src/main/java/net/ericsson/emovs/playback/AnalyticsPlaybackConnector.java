package net.ericsson.emovs.playback;

import net.ericsson.emovs.playback.interfaces.IPlaybackEventListener;

/**
 * Created by Benjamin on 2017-11-17.
 */

public class AnalyticsPlaybackConnector implements IPlaybackEventListener {
    Player player;

    public AnalyticsPlaybackConnector() {

    }

    public void bindPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onEntitlementLoadStart() {

    }

    @Override
    public void onEntitlementChange() {

    }

    @Override
    public void onLoadStart() {

    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onPlay() {

    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onSeek(long position) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onWaitingStart() {

    }

    @Override
    public void onWaitingEnd() {

    }

    @Override
    public void onBitrateChange(int oldBitrate, int newBitrate) {

    }

    @Override
    public void onPlaybackEnd() {

    }

    @Override
    public void onDispose() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onError(int errorCode, String errorMessage) {

    }
}
