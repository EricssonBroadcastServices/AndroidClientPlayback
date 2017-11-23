package net.ericsson.emovs.playback;


import net.ericsson.emovs.playback.interfaces.IPlaybackEventListener;

/**
 * This is an empty declaration of a IPlayBackListener - you can override this class if you want to handle just a small subset of all events that the player can trigger
 *
 * Created by Joao Coelho on 2017-09-27.
 */
public class EmptyPlaybackEventListener implements IPlaybackEventListener {
    protected EMPPlayer player;

    public EmptyPlaybackEventListener(EMPPlayer player) {
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

/*
    @Override
    public void onAudioTrackChange() {

    }

    @Override
    public void onTextTrackChange() {

    }
*/

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

/*
    @Override
    public void onBitrateChange() {

    }

    @Override
    public void onProgramChange() {

    }

    @Override
    public void onCastingStart() {

    }

    @Override
    public void onCastingStop() {

    }
*/

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
