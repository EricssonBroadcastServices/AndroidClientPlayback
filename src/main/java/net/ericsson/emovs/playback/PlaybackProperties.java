package net.ericsson.emovs.playback;

/**
 * Created by Joao Coelho on 2017-09-29.
 */

public class PlaybackProperties {
    public final static PlaybackProperties DEFAULT = new PlaybackProperties();

    boolean nativeControls;
    boolean autoplay;
    Long startTime;
    boolean useLastViewedOffset;

    public PlaybackProperties() {
        this.nativeControls = true;
        this.autoplay = true;
        this.useLastViewedOffset = false;
    }

    public boolean hasNativeControls() {
        return nativeControls;
    }

    public PlaybackProperties withNativeControls(boolean useNativeControls) {
        this.nativeControls = useNativeControls;
        return this;
    }

    public boolean isAutoplay() {
        return autoplay;
    }

    public boolean useLastViewedOffset() {
        return useLastViewedOffset;
    }

    public Long getStartTime() {
        return startTime;
    }

    public PlaybackProperties withAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
        return this;
    }

    public PlaybackProperties withUseLastViewedOffset(boolean useLastViewedOffset) {
        this.useLastViewedOffset = useLastViewedOffset;
        return this;
    }

    public PlaybackProperties withStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

}
