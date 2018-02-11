package net.ericsson.emovs.playback;

import net.ericsson.emovs.exposure.utils.MonotonicTimeService;

import java.io.Serializable;

/**
 * Holder class of the playback properties
 */
public class PlaybackProperties implements Serializable {
    public final static PlaybackProperties DEFAULT = new PlaybackProperties();

    String preferredTextLanguage;
    String preferredAudioLanguage;
    Integer maxBitrate;
    boolean nativeControls;
    boolean autoplay;
    PlayFromItem playFrom;
    DRMProperties drmProperties;

    public PlaybackProperties() {
        this.nativeControls = true;
        this.autoplay = true;
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
     * @return this
     */
    public PlaybackProperties withNativeControls(boolean useNativeControls) {
        this.nativeControls = useNativeControls;
        return this;
    }

    /**
     * @return whether the playback is in autoplay mode or not
     */
    public boolean isAutoplay() {
        return autoplay;
    }

    /**
     * Sets the autoplay property of the playback
     * @param autoplay
     * @return this
     */
    public PlaybackProperties withAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
        return this;
    }

    /**
     * Sets the DRM properties
     * @param drmProperties drm properties holder
     * @return
     */
    public PlaybackProperties withDRMProperties(DRMProperties drmProperties) {
        this.drmProperties = drmProperties;
        return this;
    }

    /**
     * @return drm properties holder
     */
    public DRMProperties getDRMProperties() {
        return this.drmProperties;
    }

    /**
     * When set to true it will instruct the player to play from one of these positions: Beginning, Live Edge, Bookmark, Start Time
     * @param playFrom
     * @return this
     */
    public PlaybackProperties withPlayFrom(PlayFromItem playFrom) {
        this.playFrom = playFrom;
        return this;
    }

    /**
     * @return playFrom property
     */
    public PlayFromItem getPlayFrom() {
        return this.playFrom;
    }

    /**
     * When set, it will limit playback's max bitrate
     * @param maxBitrate
     * @return this
     */
    public PlaybackProperties withMaxBitrate(Integer maxBitrate) {
        this.maxBitrate = maxBitrate;
        return this;
    }

    /**
     * Gets max bitrate limit
     */
    public Integer getMaxBitrate() {
        return this.maxBitrate;
    }

    /**
     * Gets playback's preferred text langugage code
     */
    public String getPreferredTextLanguage() {
        return preferredTextLanguage;
    }

    /**
     * Sets preferred text langugage code
     */
    public PlaybackProperties withPreferredTextLanguage(String preferredTextLanguage) {
        this.preferredTextLanguage = preferredTextLanguage;
        return this;
    }

    /**
     * Gets playback's preferred audio langugage code
     */
    public String getPreferredAudioLanguage() {
        return preferredAudioLanguage;
    }

    /**
     * Sets preferred audio langugage code
     */
    public PlaybackProperties withPreferredAudioLanguage(String preferredAudioLanguage) {
        this.preferredAudioLanguage = preferredAudioLanguage;
        return this;
    }

    @Override
    public PlaybackProperties clone() throws CloneNotSupportedException {
        PlaybackProperties newProps = new PlaybackProperties();
        newProps.nativeControls = this.nativeControls;
        newProps.autoplay = this.autoplay;
        newProps.playFrom = this.playFrom != null ? (PlayFromItem) this.playFrom.clone() : null;
        newProps.drmProperties = this.drmProperties != null ? this.drmProperties.clone() : null;
        newProps.maxBitrate = this.maxBitrate;
        newProps.preferredAudioLanguage = this.getPreferredAudioLanguage();
        newProps.preferredTextLanguage = this.getPreferredTextLanguage();
        return newProps;
    }

    public static class DRMProperties implements Serializable {
        public String licenseServerUrl;
        public String initDataBase64;

        @Override
        public DRMProperties clone() throws CloneNotSupportedException {
            DRMProperties newDRMProps = new DRMProperties();
            newDRMProps.licenseServerUrl = this.licenseServerUrl;
            newDRMProps.initDataBase64 = this.initDataBase64;
            return newDRMProps;
        }
    }

    public interface IPlayFrom {};

    public static class PlayFromItem implements IPlayFrom, Serializable {
        public Integer type;

        PlayFromItem(Integer type) {
            this.type = type;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public static class PlayFrom {
        public static PlayFromItem START_TIME_DEFAULT = new StartTime(0L);
        public static PlayFromItem BOOKMARK = new Bookmark();
        public static PlayFromItem BEGINNING = new Beginning();
        public static PlayFromItem LIVE_EDGE = new LiveEdge();

        public static class LiveEdge extends StartTime {
            public LiveEdge() {
                super(1);
            }

            @Override
            public Object clone() throws CloneNotSupportedException {
                return new LiveEdge().withStartTime(this.startTime);
            }
        }

        public static class Beginning extends StartTime {
            public Beginning() {
                super(0);
            }

            @Override
            public Object clone() throws CloneNotSupportedException {
                return new Beginning().withStartTime(this.startTime);
            }
        }

        public static class Bookmark extends StartTime {
            public Bookmark() {
                super(2);
            }

            @Override
            public Object clone() throws CloneNotSupportedException {
                return new Bookmark().withStartTime(this.startTime);
            }
        }

        public static class StartTime extends PlayFromItem {
            public long startTime;

            protected StartTime(int type) {
                super(type);
                this.startTime = 0;
            }

            public StartTime(long startTime) {
                super(3);
                this.startTime = startTime;
            }

            public Object withStartTime(long startTime) {
                this.startTime = startTime;
                return this;
            }

            @Override
            public Object clone() throws CloneNotSupportedException {
                return new StartTime(this.startTime);
            }
        };

        public static boolean isBeginning(PlayFromItem candidate) {
            return candidate != null && candidate.type == BEGINNING.type;
        }

        public static boolean isLiveEdge(PlayFromItem candidate) {
            return candidate != null && candidate.type == LIVE_EDGE.type;
        }

        public static boolean isBookmark(PlayFromItem candidate) {
            return candidate != null && candidate.type == BOOKMARK.type;
        }

        public static boolean isStartTime(PlayFromItem candidate) {
            return candidate != null && candidate.type == START_TIME_DEFAULT.type;
        }
    };
}
