package net.ericsson.emovs.playback;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.ericsson.emovs.playback.interfaces.ITech;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.utilities.interfaces.IMonotonicTimeService;
import net.ericsson.emovs.utilities.interfaces.IPlayer;

import java.util.UUID;


/**
 * Base class for a player. This class only handles basic playback flows.
 */
public class Player extends PlaybackEventListenerAggregator implements IPlayer {
    public static long SAFETY_LIVE_DELAY = 10000L;
    protected AnalyticsPlaybackConnector analyticsConnector;
    protected IMonotonicTimeService monotonicTimeService;
    protected Activity context;
    protected ViewGroup host;
    protected PlaybackProperties properties;
    protected TechFactory techFactory;
    protected ITech tech;
    protected UUID playbackUUID;

    /**
     * Constructor of a player instance - must be instanciated from a PlayerFactory or extended
     *
     * @param analyticsConnector reference to an analytics connector instance
     * @param techFactory reference to a factory of specific techs that will play the media
     * @param context activity that holds the player
     * @param host reference to the ViewGroup that wraps the player (can have several players per activity)
     */
    public Player(AnalyticsPlaybackConnector analyticsConnector, TechFactory techFactory, Activity context, ViewGroup host, IMonotonicTimeService monotonicTimeService) {
        this.host = host;
        this.context = context;
        this.analyticsConnector = analyticsConnector;
        this.monotonicTimeService = monotonicTimeService;
        this.techFactory = techFactory;

        if (analyticsConnector != null) {
            this.analyticsConnector.bindPlayer(this);
            addListener(new AnalyticsHolder(this.analyticsConnector, this));
        }
    }

    /**
     * Plays media given a playlist/manifest URL
     *
     * @param streamUrl stream locator URL
     * @param properties playback properties
     * @throws Exception
     */
    public void play(String streamUrl, PlaybackProperties properties) throws Exception {
        init(properties);
        this.tech.init(this, this.context, "", properties);
        tech.load(UUID.randomUUID().toString(), streamUrl, false);
    }

    /**
     * Unregisters and clears all listeners of a player
     */
    @Override
    public void clearListeners() {
        super.clearListeners();
        if (this.analyticsConnector != null) {
            addListener(new AnalyticsHolder(this.analyticsConnector, this));
        }
    }

    /**
     * Returns wrapper ViewGroup of a specific player
     *
     * @return ViewGroup that holds contains all player-related views
     */
    public ViewGroup getViewGroup() {
        return host;
    }

    /**
     * Release a player instance and its tech if instanciated
     */
    public void release() {
        if (this.tech != null) {
            if (this.tech.isPlaying()) {
                this.stop();
            }
            this.tech.release();
            this.tech = null;
        }
        this.clearListeners();
    }

    /**
     * Pauses an ongoing playback
     */
    public void pause() {
        if (!canPause()) {
            return;
        }
        if (this.tech != null) {
            if (this.tech.isPaused() == false) {
                this.tech.pause();
            }
        }
    }

    /**
     * Resumes an ongoing playback
     */
    public void resume() {
        if (this.tech != null) {
            this.tech.resume();
        }
    }

    /**
     * Stops ongoing playback
     */
    public void stop() {
        if (this.tech != null) {
            this.tech.stop();
        }
    }

    /**
     * Seeks the media to a position and continues playback from there
     *
     * @param positionMs position in milliseconds
     */
    public void seekTo(long positionMs) {
        if (this.tech != null) {
            this.tech.seekTo(positionMs);
        }
    }

    /**
     * Returns the current time of the media being displayed
     *
     * @return
     */
    public long getServerTime() {
        return getMonotonicTimeService().currentTime();
    }

    /**
     * @return duration of the media in milliseconds
     */
    public long getDuration() {
        if (this.tech != null) {
            return this.tech.getDuration();
        }
        return -1;
    }

    /**
     * @return boolean to indicate if the media is currently playing or paused
     */
    public boolean isPlaying() {
        return this.tech != null && this.tech.isPlaying();
    }

    /**
     * @return current playback properties
     */
    public PlaybackProperties getPlaybackProperties() {
        return properties;
    }

    /**
     * @return current bitrate being displayed
     */
    public int getCurrentBitrate() {
        return this.tech == null ? -1 : tech.getCurrentBitrate();
    }

    /**
     * @return application context associated with the player
     */
    public Context getContext() {
        return context;
    }

    /**
     * @return current tech version
     */
    public String getTechVersion() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getVersion();
    }

    /**
     * @return an identifier of the type of the tech being used - null if no tech is loaded yet
     */
    public String getTechIdentifier() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getIdentifier();
    }

    /**
     * @return an identifier of the type of the player being used
     */
    public String getIdentifier() {
        return context.getString(R.string.emplayer_name);
    }

    /**
     * @return the version of the player
     */
    public String getVersion() {
        return context.getString(R.string.emplayer_version);
    }

    /**
     * Mutes the audio
     */
    @Override
    public void mute() {
        if (this.tech == null) {
            return;
        }
        this.tech.mute();
    }

    /**
     * Unmutes the audio (volume will be last heard volume [0..1]
     */
    @Override
    public void unmute() {
        if (this.tech == null) {
            return;
        }
        this.tech.unmute();
    }

    /**
     * Sets the audio volume level
     *
     * @param volume volume level [0..1]
     */
    @Override
    public void setVolume(float volume) {
        if (this.tech == null) {
            return;
        }
        this.tech.setVolume(volume);
    }

    /**
     * @return if the playback is in auto play mode or not
     */
    @Override
    public boolean isAutoPlay() {
        return getPlaybackProperties().isAutoplay();
    }

    /**
     * @return a list of audio languages available
     */
    @Override
    public String[] getAudioLanguages() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getAudioLanguages();
    }

    /**
     * @return a list of available text languages
     */
    @Override
    public String[] getTextLanguages() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getTextLanguages();
    }

    /**
     * @param language language code to select the audio track (e.g.: en, pt, es, fr)
     */
    @Override
    public void selectAudioLanguage(String language) {
        if (this.tech == null) {
            return;
        }
        this.tech.selectAudioLanguage(language);
    }

    /**
     * @param language language code to select the text track (e.g.: en, pt, es, fr)
     */
    @Override
    public void selectTextLanguage(String language) {
        if (this.tech == null) {
            return;
        }
        this.tech.selectTextLanguage(language);
    }

    /**
     * @return selected audio language
     */
    @Override
    public String getSelectedAudioLanguage() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getSelectedAudioLanguage();
    }

    /**
     * @return selected text language
     */
    @Override
    public String getSelectedTextLanguage() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getSelectedTextLanguage();
    }

    public void seekToTime(long unixTimeMs) {
        if (this.tech != null) {
            this.tech.seekToTime(unixTimeMs);
        }
    }

    /**
     * @return unix time in milliseconds of current playback position
     */
    public long getPlayheadTime() {
        if (this.tech != null) {
            return this.tech.getPlayheadTime();
        }
        return 0;
    }

    /**
     * @return offset of current playback position
     */
    public long getPlayheadPosition() {
        if (this.tech != null) {
            return this.tech.getPlayheadPosition();
        }
        return 0;
    }

    /**
     * @return array[2] with seekable offsets [0, duration]
     */
    public long[] getSeekRange() {
        if (this.tech != null) {
            return this.tech.getSeekRange();
        }
        return null;
    }

    /**
     * @return array[2] with seekable unix times [lowerBound, upperBound]
     */
    public long[] getSeekTimeRange() {
        if (this.tech != null) {
            return this.tech.getSeekTimeRange();
        }
        return null;
    }

    /**
     * @return array[2] with buffered offsets [lowerBound, upperBound]
     */
    public long[] getBufferedRange() {
        if (this.tech != null) {
            return this.tech.getBufferedRange();
        }
        return null;
    }

    /**
     * @return array[2] with buffered unix times [lowerBound, upperBound]
     */
    public long[] getBufferedTimeRange() {
        if (this.tech != null) {
            return this.tech.getBufferedTimeRange();
        }
        return null;
    }

    /**
     * Calls onError
     * @param errorCode
     * @param errorMessage
     */
    public void fail(final int errorCode, final String errorMessage) {
        this.onError(errorCode, errorMessage);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @return Activity the player belongs to
     */
    public Activity getActivity() {
        return context;
    }

    @Override
    public void trigger(EventId eventId, Object param) {
    }

    /**
     * @return boolean stating if stream is paused or not
     */
    @Override
    public boolean isPaused() {
        if (this.tech != null) {
            return this.tech.isPaused();
        }
        return false;
    }

    /**
     * @return subtitles view for styling
     */
    @Override
    public View getSubtitlesView() {
        if (this.tech != null) {
            return this.tech.getSubtitlesView();
        }

        return null;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public boolean canSeekBack() {
        return true;
    }

    @Override
    public boolean canPause() {
        return true;
    }


    @Override
    public void seekToLive() {}

    /**
     * Starts stream from beginning
     */
    @Override
    public void startOver() {
        if (this.tech == null) {
            return;
        }
        seekTo(0);
    }

    /**
     * Run some task on player's UI thread
     * @param runnable runnable to be executed
     */
    @Override
    public void runOnUiThread(Runnable runnable) {
        if (this.context != null) {
            this.context.runOnUiThread(runnable);
        }
    }

    protected boolean init(PlaybackProperties properties) throws Exception {
        this.properties = properties;
        this.playbackUUID = null;

        if (this.tech != null) {
            this.release();
        }

        this.tech = techFactory.build();

        super.onInit();
        return true;
    }

    protected long getTimehisftDelay() {
        if (this.tech != null) {
            return this.tech.getTimeshiftDelay();
        }
        return 0;
    }

    protected void setTimeshiftDelay(long timeshift) {
        if (this.tech != null) {
            this.tech.setTimeshiftDelay(timeshift);
        }
    }

    public IMonotonicTimeService getMonotonicTimeService() {
        return monotonicTimeService;
    }
}
