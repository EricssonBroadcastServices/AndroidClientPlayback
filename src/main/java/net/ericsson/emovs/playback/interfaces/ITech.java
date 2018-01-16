package net.ericsson.emovs.playback.interfaces;

import android.app.Activity;

import net.ericsson.emovs.exposure.utils.MonotonicTimeService;
import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.Player;


/**
 * Implement this interface to create your own playback Tech (ExoTech, MRRTech, VisualOnTech, etc..)
 *
 * Created by Joao Coelho on 2017-08-29.
 */
public interface ITech {
    /**
     *
     * @return
     */
    boolean isPlaying();

    /**
     *
     * @param parent
     * @param ctx
     * @param playToken
     * @param properties
     */
    void init(Player parent, Activity ctx, String playToken, PlaybackProperties properties);

    /**
     *
     * @param mediaId
     * @param manifestUrl
     * @param isOffline
     * @return
     */
    boolean load(String mediaId, String manifestUrl, boolean isOffline);

    /**
     * Plays a stream from a manifest/playlist url
     *
     * @param manifestUrl
     */
    void play(String manifestUrl);

    /**
     * Releases the tech and underlying resources
     */
    void release();

    /**
     * Method that checks if ExoPlayer is paused
     * @return
     */
    boolean isPaused();

    /**
     * Pauses ongoing playback
     */
    void pause();

    /**
     * Resumes paused playback
     */
    void resume();

    /**
     * Stops playback
     */
    void stop();

    /**
     * Seeks the media to the desired display position
     *
     * @param positionMs
     */
    void seekTo(long positionMs);

    /**
     * Returns the current time of the media being displayed
     *
     * @return
     */
    long getServerTime();

    /**
     * Returns the duration of the media displayed by the tech
     *
     * @return
     */
    long getDuration();

    /**
     * Returns a list of audio languages available to be chosen
     *
     * @return
     */
    String[] getAudioTracks();

    /**
     * Selects audio track
     *
     * @param language language code to select the audio track (e.g.: en, pt, es, fr)
     */
    void selectAudioTrack(String language);

    /**
     * Returns selected audio track
     *
     * @return selected audio language
     */
    String getSelectedAudioTrack();

    /**
     * Returns a list of text languages available to be chosen
     *
     * @return
     */
    String[] getTextTracks();

    /**
     * Selects text track
     *
     * @param language language code to select the text track (e.g.: en, pt, es, fr)
     */
    void selectTextTrack(String language);

    /**
     * Returns selected text track
     *
     * @return selected text language
     */
    String getSelectedTextTrack();

    /**
     * Mutes the audio
     */
    void mute();

    /**
     * Unmutes the audio (volume will be last heard volume [0..1]
     */
    void unmute();

    /**
     * Sets the audio volume level
     *
     * @param volume volume level [0..1]
     */
    void setVolume(float volume);

    /**
     * Returns the current bitrate being displayed by the tech
     *
     * @return
     */
    int getCurrentBitrate();

    /**
     * Returns a unique identifier of the tech
     *
     * @return
     */
    String getIdentifier();

    /**
     * Returns the version of the tech
     *
     * @return
     */
    String getVersion();

    long getTimeshiftDelay();

    void setTimeshiftDelay(long timeshift);

    void seekToTime(long unixTimeMs);
    long getPlayheadTime();
    long getPlayheadPosition();
    long[] getSeekRange();
    long[] getSeekTimeRange();
    long[] getBufferedRange();
    long[] getBufferedTimeRange();
}
