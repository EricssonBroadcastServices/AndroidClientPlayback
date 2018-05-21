package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.errors.Warning;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.system.ServiceUtils;

import java.lang.reflect.Field;

import static net.ericsson.emovs.utilities.errors.Error.NETWORK_ERROR;

/**
 * Created by Joao Coelho on 2018-03-07.
 */

public class ExoPlayerEventListener implements Player.EventListener {
    long windowStartTimeMs;
    boolean startTimeSeekDone;
    boolean waitingStarted;
    boolean isReady;
    boolean loadStarted;
    boolean seekStart;
    boolean isPlaying;
    boolean isOffline;

    ExoPlayerTech tech;

    public ExoPlayerEventListener(ExoPlayerTech tech, boolean isOffline) {
        this.tech = tech;
        this.isPlaying = false;
        this.isReady = false;
        this.loadStarted = false;
        this.seekStart = false;
        this.startTimeSeekDone = false;
        this.windowStartTimeMs = 0;
        this.isOffline = isOffline;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int i) {
        if (!isInitiated()) {
            return;
        }
        windowStartTimeMs = getWindowStartFromTimeline(timeline);
        if (windowStartTimeMs < 0) {
            windowStartTimeMs = 0;
            long tParamStartTime = tech.tParamStartTime();
            if (tParamStartTime >= 0) {
                windowStartTimeMs = tParamStartTime;
            }
        }
        if (startTimeSeekDone == false && tech.properties != null && tech.properties.getPlayFrom() != null) {
            if (tech.properties.getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge) {
                long[] seekTimeRange = tech.parent.getSeekTimeRange();
                if (seekTimeRange != null) {
                    tech.seekToTime(seekTimeRange[1] - net.ericsson.emovs.playback.Player.SAFETY_LIVE_DELAY);
                }
                else {
                    long startTime = tech.parent.getMonotonicTimeService().currentTime() - tech.getTimeshiftDelay() * 1000 - net.ericsson.emovs.playback.Player.SAFETY_LIVE_DELAY;
                    tech.seekToTime(startTime);
                }
                startTimeSeekDone = true;
            }
            else if (tech.properties.getPlayFrom() instanceof PlaybackProperties.PlayFrom.StartTime) {
                long startTime = ((PlaybackProperties.PlayFrom.StartTime) tech.properties.getPlayFrom()).startTime;
                long[] range = tech.getSeekTimeRange();
                if (range != null && (startTime < range[0] || startTime > range[1])) {
                    tech.parent.trigger(IPlaybackEventListener.EventId.WARNING, Warning.INVALID_START_TIME);
                }
                else {
                    tech.seekToTime(startTime);
                    startTimeSeekDone = true;
                }
            }
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (!isInitiated()) {
            return;
        }
        int oldBitrate = tech.currentBitrate;
        int newBitrate = tech.getCurrentBitrate();
        if (oldBitrate > 0 && oldBitrate != newBitrate) {
            tech.parent.onBitrateChange(oldBitrate, newBitrate);
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (!isInitiated()) {
            return;
        }
        if (playbackState == com.google.android.exoplayer2.Player.STATE_READY) {
            if (isReady == false) {
                tech.parent.onLoad();
                isReady = true;
            }
            if (tech.player != null && playWhenReady && !isPlaying) {
                tech.view.setVisibility(View.VISIBLE);
                isPlaying = true;
                tech.parent.onPlaying();
            }
            if (tech.player != null && seekStart) {
                seekStart = false;
                tech.parent.onSeek(tech.player.getCurrentPosition());
            }
            if (waitingStarted) {
                waitingStarted = false;
                if (tech.parent != null && isPlaying) {
                    tech.parent.onWaitingEnd();
                }
            }
        }
        else if (playbackState == com.google.android.exoplayer2.Player.STATE_ENDED && isPlaying) {
            isPlaying = false;
            isReady = false;
            seekStart = false;
            waitingStarted = false;
            tech.parent.onPlaybackEnd();
        }
        else if (playbackState == com.google.android.exoplayer2.Player.STATE_BUFFERING) {
            if (!isReady && !isPlaying && !loadStarted) {
                loadStarted = true;
                tech.parent.onLoadStart();
            }
            else if (isPlaying) {
                waitingStarted = true;
                if (tech.parent != null) {
                    tech.parent.onWaitingStart();
                }
            }
        }
        else if (playbackState == com.google.android.exoplayer2.Player.STATE_IDLE) {
            if (isPlaying) {
                tech.parent.onStop();
            }
            isPlaying = false;
            isReady = false;
            seekStart = false;
            waitingStarted = false;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean b) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (!isInitiated()) {
            return;
        }
        if (tech.parent != null) {
            if (!this.isOffline && isPlaying && !ServiceUtils.haveNetworkConnection(tech.ctx)) {
                this.tech.parent.onError(ErrorCodes.NETWORK_ERROR, NETWORK_ERROR.toString());
            }
            else {
                Log.d("ExoPlayerEventListener", error.toString());
                StringBuilder builder = new StringBuilder();
                builder.append("Message: " + error.toString() + "\n");
                try {
                    if (error.getSourceException() != null) {
                        for (StackTraceElement element : error.getSourceException().getStackTrace()) {
                            builder.append(element.toString() + "\n");
                        }
                    }
                    Log.d("ExoPlayerEventListener", builder.toString());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                this.tech.parent.onError(ErrorCodes.EXO_PLAYER_INTERNAL_ERROR, builder.toString());
            }
        }
    }

    @Override
    public void onPositionDiscontinuity(int i) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    protected long getWindowStartFromTimeline(Timeline timeline) {
        if (!isInitiated()) {
            return 0;
        }

        Field field = null;
        try {
            field = timeline.getClass().getDeclaredField("windowStartTimeMs");
            field.setAccessible(true);
            long lwindowStartTimeMs = (Long) field.get(timeline);
            return lwindowStartTimeMs;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return  0;
    }

    private boolean isInitiated() {
        return this.tech != null && this.tech.parent != null && this.tech.view != null && this.tech.player != null;
    }
}
