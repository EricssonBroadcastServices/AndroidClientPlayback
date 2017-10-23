package net.ericsson.emovs.playback;

/**
 * Created by Joao Coelho on 2017-09-29.
 */

public class PlaybackProperties {
    public final static PlaybackProperties DEFAULT = new PlaybackProperties();

    boolean nativeControls;
    boolean autoplay;

    public PlaybackProperties() {
        this.nativeControls = true;
        this.autoplay = true;
    }

    public boolean isNativeControls() {
        return nativeControls;
    }

    public PlaybackProperties withUseNativeControls(boolean useNativeControls) {
        this.nativeControls = useNativeControls;
        return this;
    }

    public boolean isAutoplay() {
        return autoplay;
    }

    public PlaybackProperties withAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
        return this;
    }
}
