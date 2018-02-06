package net.ericsson.emovs.playback.helpers;

import android.app.Activity;
import android.view.View;

import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayable;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.models.EmpProgram;

/**
 * Created by Joao Coelho on 2018-02-06.
 */

public class FakeEntitledPlayer implements IEntitledPlayer {
    long playheadTime;
    boolean isPlaying;

    public void mockPlayHeadTime(long _playheadTime) {
        this.playheadTime = _playheadTime;
    }

    public void mockIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    @Override
    public void release() {

    }

    @Override
    public void pause() {

    }

    @Override
    public boolean isPaused() {
        return false;
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
    public long getServerTime() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public boolean isAutoPlay() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public int getCurrentBitrate() {
        return 0;
    }

    @Override
    public String getTechVersion() {
        return null;
    }

    @Override
    public String getTechIdentifier() {
        return null;
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
    public void mute() {

    }

    @Override
    public void unmute() {

    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public String[] getAudioTracks() {
        return new String[0];
    }

    @Override
    public String[] getTextTracks() {
        return new String[0];
    }

    @Override
    public void selectAudioTrack(String language) {

    }

    @Override
    public void selectTextTrack(String language) {

    }

    @Override
    public String getSelectedAudioTrack() {
        return null;
    }

    @Override
    public String getSelectedTextTrack() {
        return null;
    }

    @Override
    public void seekToTime(long unixTimeMs) {

    }

    @Override
    public long getPlayheadTime() {
        return playheadTime;
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
    public void fail(int errorCode, String errorMessage) {

    }

    @Override
    public Activity getActivity() {
        return null;
    }

    @Override
    public void trigger(IPlaybackEventListener.EventId eventId, Object param) {

    }

    @Override
    public View getSubtitlesView() {
        return null;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public boolean canSeekBack() {
        return false;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public void seekToLive() {

    }

    @Override
    public Entitlement getEntitlement() {
        return null;
    }

    @Override
    public IPlayable getPlayable() {
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public EmpProgram getCurrentProgram() {
        return null;
    }
}
