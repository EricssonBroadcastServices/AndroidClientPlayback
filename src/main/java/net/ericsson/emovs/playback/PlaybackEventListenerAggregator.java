package net.ericsson.emovs.playback;

import net.ericsson.emovs.playback.interfaces.IPlaybackEventListener;

import java.util.HashMap;

/**
 * This class is an event listener aggregator which receives one event from the player and passes it on to all the listeners it aggregates
 *
 * Created by Joao Coelho on 2017-09-27.
 */
class PlaybackEventListenerAggregator implements IPlaybackEventListener {
    HashMap<IPlaybackEventListener, IPlaybackEventListener> eventListeners;

    public PlaybackEventListenerAggregator() {
        this.eventListeners = new HashMap<>();
    }

    public void addListener(IPlaybackEventListener listener) {
        this.eventListeners.put(listener, listener);
    }

    public void removeListener(IPlaybackEventListener listener) {
        this.eventListeners.remove(listener);
    }

    public void clearListeners() {
        this.eventListeners.clear();
    }

    @Override
    public void onInit() {
        if (eventListeners == null) {
           return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onInit();
        }
    }

/*
    @Override
    public void onTechChange() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onTechChange();
        }
    }
*/

    @Override
    public void onEntitlementLoadStart() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onEntitlementLoadStart();
        }
    }

    @Override
    public void onEntitlementChange() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onEntitlementChange();
        }
    }

    @Override
    public void onLoadStart() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onLoadStart();
        }
    }

    @Override
    public void onLoad() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onLoad();
        }
    }

    @Override
    public void onPlay() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onPlay();
        }
    }

    @Override
    public void onPlaying() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onPlaying();
        }
    }

/*
    @Override
    public void onAudioTrackChange() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onAudioTrackChange();
        }
    }

    @Override
    public void onTextTrackChange() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onTextTrackChange();
        }
    }
*/

    @Override
    public void onPause() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onPause();
        }
    }

    @Override
    public void onSeek(long position) {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onSeek(position);
        }
    }

    @Override
    public void onResume() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onResume();
        }
    }

    @Override
    public void onWaitingStart() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onWaitingStart();
        }
    }

    @Override
    public void onWaitingEnd() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onWaitingEnd();
        }
    }

    @Override
    public void onBitrateChange(int oldBitrate, int newBitrate) {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onBitrateChange(oldBitrate, newBitrate);
        }
    }

/*
    @Override
    public void onBitrateChange() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onBitrateChange();
        }
    }

    @Override
    public void onProgramChange() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onProgramChange();
        }
    }

    @Override
    public void onCastingStart() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onCastingStart();
        }
    }

    @Override
    public void onCastingStop() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onCastingStop();
        }
    }
*/

    @Override
    public void onPlaybackEnd() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onPlaybackEnd();
        }
    }

    @Override
    public void onDispose() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onDispose();
        }
    }

    @Override
    public void onStop() {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onStop();
        }
    }

    @Override
    public void onError(int errorCode, String errorMessage) {
        if (eventListeners == null) {
            return;
        }
        for (IPlaybackEventListener listener : eventListeners.keySet()) {
            listener.onError(errorCode, errorMessage);
        }
    }
}
