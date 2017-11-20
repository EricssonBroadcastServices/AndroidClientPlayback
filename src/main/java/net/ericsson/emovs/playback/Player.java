package net.ericsson.emovs.playback;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import net.ericsson.emovs.playback.techs.ExoPlayer.ExoPlayerTech;
import net.ericsson.emovs.utilities.AnalyticsPlaybackConnector;

import java.util.UUID;


/**
 * Created by Joao Coelho on 2017-08-30.
 */

public class Player extends PlaybackEventListenerAggregator {
    protected AnalyticsPlaybackConnector analyticsConnector;
    protected Activity context;
    protected ViewGroup host;
    protected PlaybackProperties properties;
    protected ExoPlayerTech tech;
    protected UUID playbackUUID;

    public Player(AnalyticsPlaybackConnector analyticsConnector, Activity context, ViewGroup host) {
        this.host = host;
        this.context = context;
        this.analyticsConnector = analyticsConnector;

        if (analyticsConnector != null) {
            this.analyticsConnector.bindPlayer(this);
            addListener(new AnalyticsHolder(this.analyticsConnector));
        }
    }

    protected boolean init(PlaybackProperties properties) throws Exception {
        this.properties = properties;
        this.playbackUUID = null;

        if (this.tech != null) {
            this.release();
        }

        this.tech = new ExoPlayerTech(this, this.context, false, properties);
        super.onInit();
        return true;
    }

    @Override
    public void clearListeners() {
        super.clearListeners();
        if (this.analyticsConnector != null) {
            addListener(new AnalyticsHolder(this.analyticsConnector));
        }
    }

    public void play(String streamUrl, PlaybackProperties properties) throws Exception {
        init(properties);
        tech.init("", properties);
        tech.load(UUID.randomUUID().toString(), streamUrl, false);
    }

    public ViewGroup getViewGroup() {
        return host;
    }

    public void release() {
        if (this.tech != null) {
            this.tech.release();
            this.tech = null;
        }
        super.clearListeners();
    }

    public void pause() {
        if (this.tech != null) {
            this.tech.pause();
        }
    }

    public void resume() {
        if (this.tech != null) {
            this.tech.resume();
        }
    }

    public void stop() {
        if (this.tech != null) {
            this.tech.stop();
        }
    }

    public void seekTo(long positionMs) {
        if (this.tech != null) {
            this.tech.seekTo(positionMs);
        }
    }

    public long getCurrentTime() {
        if (this.tech != null) {
            return this.tech.getCurrentTime();
        }
        return -1;
    }

    public long getDuration() {
        if (this.tech != null) {
            return this.tech.getDuration();
        }
        return -1;
    }

    public boolean isPlaying() {
        return this.tech != null && this.tech.isPlaying();
    }

    public PlaybackProperties getPlaybackProperties() {
        return properties;
    }

    public int getCurrentBitrate() {
        return this.tech == null ? -1 : tech.getCurrentBitrate();
    }

    public Context getContext() {
        return context;
    }

    public String getTechVersion() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getVersion();
    }

    public String getTechIdentifier() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getIdentifier();
    }

    public String getIdentifier() {
        return context.getString(R.string.emplayer_name);
    }

    public String getVersion() {
        return context.getString(R.string.emplayer_version);
    }
}
