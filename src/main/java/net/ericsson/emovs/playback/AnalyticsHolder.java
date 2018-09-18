package net.ericsson.emovs.playback;

import net.ericsson.emovs.utilities.interfaces.ControllerVisibility;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;

/**
 * This class is a bridde between the AnalyticsPlayBackConnector base class and the IPlaybackEventListener interface that is used by the player
 *
 * Created by Joao Coelho on 2017-11-20.
 */
public class AnalyticsHolder implements IPlaybackEventListener {
    AnalyticsPlaybackConnector connector;
    IEntitledPlayer player;
    String fallbackSessionId;

    public AnalyticsHolder(AnalyticsPlaybackConnector connector, IPlayer player) {
        this.connector = connector;
        if (player instanceof IEntitledPlayer) {
            this.player = (IEntitledPlayer) player;
        }
    }

    @Override
    public void onInit() {
        this.connector.onInit();
    }

    @Override
    public void onEntitlementLoadStart() {
        fallbackSessionId = "error-" + Double.toString(Math.random()).replace("0.", "");
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
        if (this.player == null || this.player.getSessionId() == null) {
            this.connector.onError(fallbackSessionId, errorCode, errorMessage);
        }
        else {
            this.connector.onError(errorCode, errorMessage);
        }
    }

    @Override
    public void onErrorDetailed(int code, String message, String info, String details) {
        if ((this.player == null) || (this.player.getSessionId() == null)) {
            this.connector.onErrorDetailed(fallbackSessionId, code, message, info, details);
        } else {
            this.connector.onErrorDetailed(code, message, info, details);
        }
    }

    @Override
    public void onWarning(int warningCode, String warningMessage) {

    }

    @Override
    public void onControllerVisibility(ControllerVisibility visibility) {
    }

    @Override
    public void onProgramChange(EmpProgram newProgram) {
        this.connector.onProgramChange(newProgram);
    }
}
