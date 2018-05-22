package net.ericsson.emovs.playback;

import net.ericsson.emovs.utilities.interfaces.ControllerVisibility;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.models.EmpProgram;

import java.util.HashMap;

/**
 * This class is an event listener aggregator which receives one event from the player and passes it on to all the listeners it aggregates
 *
 * Created by Joao Coelho on 2017-09-27.
 */
class PlaybackEventListenerAggregator implements IPlaybackEventListener {
    private HashMap<IPlaybackEventListener, IPlaybackEventListener> eventListeners;

    public PlaybackEventListenerAggregator() {
        this.eventListeners = new HashMap<>();
    }

    public void addListener(IPlaybackEventListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.put(listener, listener);
        }
    }

    public HashMap<IPlaybackEventListener, IPlaybackEventListener> cloneEventListeners() {
        synchronized (eventListeners) {
            return (HashMap<IPlaybackEventListener, IPlaybackEventListener>) this.eventListeners.clone();
        }
    }

    public void removeListener(IPlaybackEventListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.remove(listener);
        }
    }

    public void clearListeners() {
        synchronized (eventListeners) {
            this.eventListeners.clear();
        }
    }

    @Override
    public void onInit() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
               return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onInit();
            }
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
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onEntitlementLoadStart();
            }
        }
    }

    @Override
    public void onEntitlementChange() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onEntitlementChange();
            }
        }
    }

    @Override
    public void onLoadStart() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onLoadStart();
            }
        }
    }

    @Override
    public void onLoad() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onLoad();
            }
        }
    }

    @Override
    public void onPlay() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onPlay();
            }
        }
    }

    @Override
    public void onPlaying() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onPlaying();
            }
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
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onPause();
            }
        }
    }

    @Override
    public void onSeek(long position) {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onSeek(position);
            }
        }
    }

    @Override
    public void onResume() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onResume();
            }
        }
    }

    @Override
    public void onWaitingStart() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onWaitingStart();
            }
        }
    }

    @Override
    public void onWaitingEnd() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onWaitingEnd();
            }
        }
    }

    @Override
    public void onBitrateChange(int oldBitrate, int newBitrate) {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onBitrateChange(oldBitrate, newBitrate);
            }
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
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onPlaybackEnd();
            }
        }
    }

    @Override
    public void onDispose() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onDispose();
            }
        }
    }

    @Override
    public void onStop() {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onStop();
            }
        }
    }

    @Override
    public void onError(int errorCode, String errorMessage) {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onError(errorCode, errorMessage);
            }
        }
    }

    @Override
    public void onWarning(int warningCode, String warningMessage) {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onWarning(warningCode, warningMessage);
            }
        }
    }

    @Override
    public void onControllerVisibility(ControllerVisibility visibility) {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onControllerVisibility(visibility);
            }
        }
    }

    @Override
    public void onProgramChange(EmpProgram newProgram) {
        synchronized (eventListeners) {
            if (eventListeners == null) {
                return;
            }
            for (IPlaybackEventListener listener : eventListeners.keySet()) {
                listener.onProgramChange(newProgram);
            }
        }
    }
}
