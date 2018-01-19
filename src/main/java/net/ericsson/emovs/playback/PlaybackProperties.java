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
     * When set to true it will instruct the player to play from one of these positions: Beginning, Live Edge, Bookmark, Start Time
     * @param playFrom
     * @return
     */
    public PlaybackProperties withPlayFrom(PlayFromItem playFrom) {
        this.playFrom = playFrom;
        return this;
    }

    public PlayFromItem getPlayFrom() {
        return this.playFrom;
    }

    public static class DRMProperties {
        public String licenseServerUrl;
        public String initDataBase64;
    }

    public interface IPlayFrom {};

    public static class PlayFromItem implements IPlayFrom {
        public Integer type;

        PlayFromItem(Integer type) {
            this.type = type;
        }
    }

    public static class PlayFrom {
        public static PlayFromItem LIVE_EDGE = new PlayFromItem(1);
        public static PlayFromItem START_TIME_DEFAULT = new StartTime(0);
        public static PlayFromItem BOOKMARK = new Bookmark();
        public static PlayFromItem BEGINNING = new Beginning();

        public static class Beginning extends StartTime {
            public Beginning() {
                super(0);
            }
        }

        public static class Bookmark extends StartTime {
            public Bookmark() {
                super(2);
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
        };

        public static boolean isBeginning(PlayFromItem candidate) {
            return candidate.type == BEGINNING.type;
        }

        public static boolean isLiveEdge(PlayFromItem candidate) {
            return candidate.type == LIVE_EDGE.type;
        }

        public static boolean isBookmark(PlayFromItem candidate) {
            return candidate.type == BOOKMARK.type;
        }

        public static boolean isStartTime(PlayFromItem candidate) {
            return candidate.type == START_TIME_DEFAULT.type;
        }
    };
}
