package net.ericsson.emovs.playback;

import net.ericsson.emovs.playback.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;

/**
 * This class is a bridde between the AnalyticsPlayBackConnector base class and the IPlaybackEventListener interface that is used by the player
 *
 * Created by Joao Coelho on 2017-11-20.
 */
public class AnalyticsHolder implements IPlaybackEventListener {
    AnalyticsPlaybackConnector connector;

    public AnalyticsHolder(AnalyticsPlaybackConnector connector) {
        this.connector = connector;
    }

    @Override
    public void onInit() {
        this.connector.onInit();
    }

    @Override
    public void onEntitlementLoadStart() {
        this.connector.onEntitlementLoadStart();
    }

    @Override
    public void onEntitlementChange() {
        this.connector.onEntitlementChange();
    }

    @Override
    public void onLoadStart() {
        this.connector.onLoadStart();
    }

    @Override
    public void onLoad() {
        this.connector.onLoad();
    }

    @Override
    public void onPlay() {
        this.connector.onPlay();
    }

    @Override
    public void onPlaying() {
        this.connector.onPlaying();
    }

    @Override
    public void onPause() {
        this.connector.onPause();
    }

    @Override
    public void onSeek(long position) {
        this.connector.onSeek(position);
    }

    @Override
    public void onResume() {
        this.connector.onResume();
    }

    @Override
    public void onWaitingStart() {
        this.connector.onWaitingStart();
    }

    @Override
    public void onWaitingEnd() {
        this.connector.onWaitingEnd();
    }

    @Override
    public void onBitrateChange(int oldBitrate, int newBitrate) {
        this.connector.onBitrateChange(oldBitrate, newBitrate);
    }

    @Override
    public void onPlaybackEnd() {
        this.connector.onPlaybackEnd();
    }

    @Override
    public void onDispose() {
        this.connector.onDispose();
    }

    @Override
    public void onStop() {
        this.connector.onStop();
    }

    @Override
    public void onError(int errorCode, String errorMessage) {
        this.connector.onError(errorCode, errorMessage);
    }
}
