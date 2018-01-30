package net.ericsson.emovs.playback.interfaces;

import android.app.Activity;
import android.view.View;

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

    /**
     * Returns timeshift delay of the stream
     *
     * @return
     */
    long getTimeshiftDelay();

    /**
     * Sets timeshift delay property in manifest URL and reloads stream
     *
     * @return
     */
    void setTimeshiftDelay(long timeshift);

    /**
     * Seeks to a specific unix timestamp (milliseconds)
     *
     * @return
     */
    void seekToTime(long unixTimeMs);

    /**
     * Returns current playback time (unix timestamp in milliseconds)
     *
     * @return
     */
    long getPlayheadTime();

    /**
     * Returns current playback position within the seek range (0..MAX_SEEKABLE_POSITION)
     *
     * @return
     */
    long getPlayheadPosition();

    /**
     * Returns an array of 2 entries that is the overall seekable range without a new stream reload
     *
     * @return
     */
    long[] getSeekRange();

    /**
     * Returns an array of 2 entries that is the overall seekable time range without a new stream reload
     *
     * @return
     */
    long[] getSeekTimeRange();

    /**
     * Returns an array of 2 entries that is the range of currently buffered video
     *
     * @return
     */
    long[] getBufferedRange();

    /**
     * Returns an array of 2 entries that is the unix time range of currently buffered video
     *
     * @return
     */
    long[] getBufferedTimeRange();

    /**
     * Returns the subtitles view
     *
     * @return
     */
    View getSubtitlesView();
}
