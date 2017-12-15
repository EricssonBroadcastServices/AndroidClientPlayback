package net.ericsson.emovs.playback;

/**
 * Holder class of the playback properties
 *
 * Created by Joao Coelho on 2017-09-29.
 */
public class PlaybackProperties {
    public final static PlaybackProperties DEFAULT = new PlaybackProperties();

    boolean nativeControls;
    boolean autoplay;
    Long startTime;
    boolean useLastViewedOffset;
    DRMProperties drmProperties;

    public PlaybackProperties() {
        this.nativeControls = true;
        this.autoplay = true;
        this.useLastViewedOffset = false;
    }

    /**
     * When set, it shows native UI controls during playback. When hidden returns false
     * @return
     */
    public boolean hasNativeControls() {
        return nativeControls;
    }

    /**
     * When set, it shows native UI controls during playback. To hide them pass false
     * @param useNativeControls
     * @return
     */
    public PlaybackProperties withNativeControls(boolean useNativeControls) {
        this.nativeControls = useNativeControls;
        return this;
    }

    /**
     * Returns if the playback is in autoplay mode
     * @return
     */
    public boolean isAutoplay() {
        return autoplay;
    }

    /**
     * If returns true, then playback will start from last watched position
     * @return
     */
    public boolean useLastViewedOffset() {
        return useLastViewedOffset;
    }

    /**
     * Returns the startTime property of the playback
     * @return
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets the autoplay property of the playback
     * @param autoplay
     * @return
     */
    public PlaybackProperties withAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
        return this;
    }

    public PlaybackProperties withDRMProperties(DRMProperties drmProperties) {
        this.drmProperties = drmProperties;
        return this;
    }

    public DRMProperties getDRMProperties() {
        return this.drmProperties;
    }

    /**
     * When set to true it will instruct the player to resume from last watched position
     * @param useLastViewedOffset
     * @return
     */
    public PlaybackProperties withUseLastViewedOffset(boolean useLastViewedOffset) {
        this.useLastViewedOffset = useLastViewedOffset;
        return this;
    }

    /**
     * Sets the start time of the new playback
     * @param startTime
     * @return
     */
    public PlaybackProperties withStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public static class DRMProperties {
        public String licenseServerUrl;
        public String initDataBase64;
    }
}
