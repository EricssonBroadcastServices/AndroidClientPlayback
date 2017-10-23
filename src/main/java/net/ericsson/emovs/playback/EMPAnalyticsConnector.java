package net.ericsson.emovs.playback;

import android.content.Context;

import com.ebs.android.exposure.entitlements.Entitlement;
import com.ebs.android.utilities.RunnableThread;

import net.ericsson.emovs.analytics.EMPAnalyticsProvider;
import net.ericsson.emovs.analytics.EventParameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joao Coelho on 2017-09-27.
 */

public class EMPAnalyticsConnector extends EmptyPlaybackEventListener {
    final int CURRENT_TIME_PERIOD = 500;
    long currentTime;
    RunnableThread timeUpdater;

    public EMPAnalyticsConnector(EMPPlayer player) {
        super(player);
    }

    @Override
    public void onEntitlementChange() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        Entitlement entitlement = player.getEntitlement();

        HashMap<String, String> parameters = new HashMap<>();

        if (entitlement.channelId != null) {
            parameters.put(EventParameters.HandshakeStarted.ASSET_ID, entitlement.channelId);
        }
        else if (entitlement.assetId != null) {
            parameters.put(EventParameters.HandshakeStarted.ASSET_ID, entitlement.assetId);
        }

        if (entitlement.programId != null) {
            parameters.put(EventParameters.HandshakeStarted.PROGRAM_ID, entitlement.programId);
        }

        EMPAnalyticsProvider.getInstance(player.getContext()).handshakeStarted(sessionId, parameters);
    }

    @Override
    public void onLoadStart() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<>();

        // TODO: missing play mode
        //parameters.put(EventParameters.Created.PLAY_MODE, "");
        parameters.put(EventParameters.Created.AUTOPLAY, Boolean.toString(player.getPlaybackProperties().isAutoplay()));
        parameters.put(EventParameters.Created.VERSION, player.getVersion());
        parameters.put(EventParameters.Created.PLAYER, player.getIdentifier());

        EMPAnalyticsProvider.getInstance(player.getContext()).created(sessionId, parameters);
    }

    @Override
    public void onLoad() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put(EventParameters.PlayerReady.TECHNOLOGY, player.getTechIdentifier());
        parameters.put(EventParameters.PlayerReady.TECH_VERSION, player.getTechVersion());

        EMPAnalyticsProvider.getInstance(player.getContext()).ready(sessionId, parameters);
    }

    @Override
    public void onPlaying() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();
        long duration = player.getDuration();
        Entitlement entitlement = player.getEntitlement();

        HashMap<String, String> parameters = new HashMap<>();

        if (entitlement != null) {
            String cleanMediaLocator = entitlement.mediaLocator;
            try {
                cleanMediaLocator = entitlement.mediaLocator.replace("?" + new URL(entitlement.mediaLocator).getQuery(), "");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            parameters.put(EventParameters.Started.MEDIA_LOCATOR, cleanMediaLocator);
            parameters.put(EventParameters.Started.VIDEO_LENGTH, Long.toString(duration));
            parameters.put(EventParameters.Started.BITRATE, Integer.toString(player.getCurrentBitrate()));
        }

        // TODO: set custom attributes
        EMPAnalyticsProvider.getInstance(player.getContext()).started(sessionId, currenTime, parameters);

        clearTimeUpdater();
        timeUpdater = new RunnableThread(new Runnable() {
            @Override
            public void run() {
                updateCurrentTime();
            }
        });
    }

    @Override
    public void onPause() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();
        EMPAnalyticsProvider.getInstance(player.getContext()).paused(sessionId, currenTime, null);
    }

    @Override
    public void onSeek(long position) {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();
        EMPAnalyticsProvider.getInstance(player.getContext()).seeked(sessionId, currenTime, null);
    }

    @Override
    public void onResume() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();
        EMPAnalyticsProvider.getInstance(player.getContext()).resumed(sessionId, currenTime, null);
    }

    @Override
    public void onBitrateChange(int oldBitrate, int newBitrate) {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(EventParameters.BitrateChanged.BITRATE, Integer.toString(newBitrate));

        EMPAnalyticsProvider.getInstance(player.getContext()).bitrateChanged(sessionId, currenTime, parameters);
    }


    @Override
    public void onWaitingStart() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();
        EMPAnalyticsProvider.getInstance(player.getContext()).waitingStarted(sessionId, currenTime, null);
    }

    @Override
    public void onWaitingEnd() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();
        EMPAnalyticsProvider.getInstance(player.getContext()).waitingEnded(sessionId, currenTime, null);
    }

    @Override
    public void onPlaybackEnd() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        EMPAnalyticsProvider.getInstance(player.getContext()).completed(sessionId, null);
        clearTimeUpdater();
    }

    @Override
    public void onStop() {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();
        EMPAnalyticsProvider.getInstance(player.getContext()).aborted(sessionId, currenTime, null);
        clearTimeUpdater();
    }

    @Override
    public void onError(int errorCode, String errorMessage) {
        if (player == null) {
            return;
        }
        String sessionId = player.getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player.getCurrentTime();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(EventParameters.Error.CODE, Integer.toString(errorCode));
        if (errorMessage != null && errorMessage.equals("") == false) {
            parameters.put(EventParameters.Error.MESSAGE, errorMessage);
        }

        EMPAnalyticsProvider.getInstance(player.getContext()).error(sessionId, currenTime, parameters);
    }

    @Override
    public void onDispose() {
        clearTimeUpdater();
    }

    private void clearTimeUpdater() {
        if (timeUpdater != null && timeUpdater.isInterrupted() == false && timeUpdater.isAlive()) {
            timeUpdater.interrupt();
            timeUpdater = null;
        }
    }

    private void updateCurrentTime() {
        for(;;) {
            try {
                Thread.sleep(CURRENT_TIME_PERIOD);
                if (player.isPlaying() == false) {
                    return;
                }
                String sessionId = player.getSessionId();
                EMPAnalyticsProvider.getInstance(player.getContext()).setCurrentTime(sessionId, this.currentTime);

            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
