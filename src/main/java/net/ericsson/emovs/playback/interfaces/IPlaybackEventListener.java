package net.ericsson.emovs.playback.interfaces;

/**
 * Created by Joao Coelho on 2017-09-27.
 */

public interface IPlaybackEventListener {
    void onInit();
    void onEntitlementLoadStart();
    void onEntitlementChange();
    void onLoadStart();
    void onLoad();
    void onPlay();
    void onPlaying();
//    void onAudioTrackChange();
//    void onTextTrackChange();
    void onPause();
    void onSeek(long position);
    void onResume();
    void onWaitingStart();
    void onWaitingEnd();
    void onBitrateChange(int oldBitrate, int newBitrate);
//    void onProgramChange();
//    void onCastingStart();
//    void onCastingStop();
    void onPlaybackEnd();
    void onDispose();
    void onStop();
    void onError(int errorCode, String errorMessage);
}
