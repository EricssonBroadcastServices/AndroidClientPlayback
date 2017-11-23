package net.ericsson.emovs.playback.interfaces;

import android.app.Activity;

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
    long getCurrentTime();

    /**
     * Returns the duration of the media displayed by the tech
     *
     * @return
     */
    long getDuration();

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
}
