package net.ericsson.emovs.playback.helpers;

import android.app.Activity;
import android.view.View;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.playback.ui.activities.SimplePlaybackActivity;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;
import net.ericsson.emovs.utilities.errors.Warning;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IMetadataProvider;
import net.ericsson.emovs.utilities.interfaces.IPlayable;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.models.EmpProgram;

import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Joao Coelho on 2018-02-06.
 */

public class FakeEntitledPlayer implements IEntitledPlayer {
    long playheadTime;
    boolean isPlaying;

    public Warning lastWarning;
    public int lastErrorCode;
    public String lastErrorMessage;

    public FakeEntitledPlayer() {
    }

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
        isPlaying = false;
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
    public String[] getAudioLanguages() {
        return new String[0];
    }

    @Override
    public String[] getTextLanguages() {
        return new String[0];
    }

    @Override
    public void selectAudioLanguage(String language) {

    }

    @Override
    public void selectTextLanguage(String language) {

    }

    @Override
    public String getSelectedAudioLanguage() {
        return null;
    }

    @Override
    public String getSelectedTextLanguage() {
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
        this.lastErrorCode = errorCode;
        this.lastErrorMessage = errorMessage;
    }

    @Override
    public Activity getActivity() {
        return null;
    }

    @Override
    public void trigger(IPlaybackEventListener.EventId eventId, Object param) {
        if (eventId == IPlaybackEventListener.EventId.WARNING) {
            this.lastWarning = (Warning) param;
        }
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
    public void runOnUiThread(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    public void startOver() {

    }

    @Override
    public Entitlement getEntitlement() {
        return null;
    }

    @Override
    public String getRequestId() { return null; }

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

    @Override
    public IMetadataProvider getMetadataProvider() {
        return EMPMetadataProvider.getInstance();
    }

    @Override
    public IEntitlementProvider getEntitlementProvider() {
        return EMPEntitlementProvider.getInstance();
    }
}
