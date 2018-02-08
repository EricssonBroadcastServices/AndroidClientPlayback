package net.ericsson.emovs.playback.helpers;

import android.app.Activity;
import android.view.View;

import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.Player;
import net.ericsson.emovs.playback.interfaces.ITech;
import net.ericsson.emovs.utilities.entitlements.Entitlement;

/**
 * Created by Joao Coelho on 2018-02-05.
 */

public class FakeTech implements ITech {
    public PlaybackProperties propsFedToTech;

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void init(Player parent, Activity ctx, String playToken, PlaybackProperties properties) {
        this.propsFedToTech = properties;
    }

    @Override
    public boolean load(String mediaId, String manifestUrl, boolean isOffline) {
        return false;
    }

    @Override
    public void play(String manifestUrl) {

    }

    @Override
    public void release() {

    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void seekTo(long positionMs) {

    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public String[] getAudioTracks() {
        return new String[0];
    }

    @Override
    public void selectAudioTrack(String language) {

    }

    @Override
    public String getSelectedAudioTrack() {
        return null;
    }

    @Override
    public String[] getTextTracks() {
        return new String[0];
    }

    @Override
    public void selectTextTrack(String language) {

    }

    @Override
    public String getSelectedTextTrack() {
        return null;
    }

    @Override
    public void mute() {

    }

    @Override
    public void unmute() {

    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public int getCurrentBitrate() {
        return 0;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public long getTimeshiftDelay() {
        return 0;
    }

    @Override
    public void setTimeshiftDelay(long timeshift) {

    }

    @Override
    public void seekToTime(long unixTimeMs) {

    }

    @Override
    public long getPlayheadTime() {
        return 0;
    }

    @Override
    public long getPlayheadPosition() {
        return 0;
    }

    @Override
    public long[] getSeekRange() {
        return new long[0];
    }

    @Override
    public long[] getSeekTimeRange() {
        return new long[0];
    }

    @Override
    public long[] getBufferedRange() {
        return new long[0];
    }

    @Override
    public long[] getBufferedTimeRange() {
        return new long[0];
    }

    @Override
    public View getSubtitlesView() {
        return null;
    }
}
