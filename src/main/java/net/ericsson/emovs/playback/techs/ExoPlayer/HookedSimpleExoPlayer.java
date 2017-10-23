package net.ericsson.emovs.playback.techs.ExoPlayer;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.TrackSelector;

/**
 * Created by Joao Coelho on 2017-09-28.
 */

public class HookedSimpleExoPlayer extends SimpleExoPlayer {
    ExoPlayerTech tech;

    public HookedSimpleExoPlayer(ExoPlayerTech tech, RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl) {
        super(renderersFactory, trackSelector, loadControl);
        this.tech = tech;
    }

    public static HookedSimpleExoPlayer newSimpleInstance(ExoPlayerTech tech, RenderersFactory renderersFactory, TrackSelector trackSelector) {
        return new HookedSimpleExoPlayer(tech, renderersFactory, trackSelector, new DefaultLoadControl());
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        super.setPlayWhenReady(playWhenReady);
        if (tech.getParent() != null && tech.isPlaying()) {
            if (playWhenReady) {
                tech.getParent().onResume();
            }
            else {
                tech.getParent().onPause();
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if(tech != null && tech.getParent() != null && tech.isPlaying()) {
            tech.getParent().onStop();
        }
    }

    @Override
    public void seekTo(long positionMs) {
        super.seekTo(positionMs);
        if(tech != null && tech.isPlaying()) {
            tech.seekStart(true);
        }
    }

    @Override
    public void seekToDefaultPosition() {
        super.seekToDefaultPosition();
        if(tech != null && tech.isPlaying()) {
            tech.seekStart(true);
        }
    }

    @Override
    public void seekToDefaultPosition(int windowIndex) {
        super.seekToDefaultPosition(windowIndex);
        if(tech != null && tech.isPlaying()) {
            tech.seekStart(true);
        }
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {
        super.seekTo(windowIndex, positionMs);
        if(tech != null && tech.isPlaying()) {
            tech.seekStart(true);
        }
    }

    @Override
    public void release() {
        super.release();
        if(tech != null && tech.getParent() != null) {
            tech.getParent().onDispose();
        }
    }
}
