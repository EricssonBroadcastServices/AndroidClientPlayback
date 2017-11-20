package net.ericsson.emovs.playback;


import net.ericsson.emovs.exposure.interfaces.IPlayable;
import net.ericsson.emovs.exposure.models.EmpAsset;
import net.ericsson.emovs.exposure.models.EmpChannel;
import net.ericsson.emovs.exposure.models.EmpOfflineAsset;
import net.ericsson.emovs.exposure.models.EmpProgram;
import net.ericsson.emovs.utilities.Entitlement;
import net.ericsson.emovs.utilities.RunnableThread;

import net.ericsson.emovs.analytics.EMPAnalyticsProvider;
import net.ericsson.emovs.analytics.EventParameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Joao Coelho on 2017-09-27.
 */

public class EMPAnalyticsConnector extends AnalyticsPlaybackConnector {
    final int CURRENT_TIME_PERIOD = 500;
    long currentTime;
    RunnableThread timeUpdater;

    public EMPAnalyticsConnector() {
    }

    @Override
    public void onEntitlementChange() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        Entitlement entitlement = player().getEntitlement();
        IPlayable playable = player().getPlayable();
        boolean isOffline = false;

        if (playable instanceof EmpOfflineAsset) {
            isOffline = true;
        }

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

        EMPAnalyticsProvider.getInstance().handshakeStarted(sessionId, isOffline, parameters);
    }

    @Override
    public void onLoadStart() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<>();

        IPlayable playable = player().getPlayable();
        String mode = null;

        if (playable instanceof EmpOfflineAsset) {
            mode = "offline";
        }
        else if (playable instanceof EmpAsset) {
            mode = "vod";
        }
        else if (playable instanceof EmpChannel) {
            mode = "live";
        }
        else if (playable instanceof EmpProgram) {
            // TODO: check if program is live
            mode = "vod";
        }

        parameters.put(EventParameters.Created.PLAY_MODE, mode);
        parameters.put(EventParameters.Created.AUTOPLAY, Boolean.toString(player().getPlaybackProperties().isAutoplay()));
        parameters.put(EventParameters.Created.VERSION, player.getVersion());
        parameters.put(EventParameters.Created.PLAYER, player.getIdentifier());

        EMPAnalyticsProvider.getInstance().created(sessionId, parameters);
    }

    @Override
    public void onLoad() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put(EventParameters.PlayerReady.TECHNOLOGY, player.getTechIdentifier());
        parameters.put(EventParameters.PlayerReady.TECH_VERSION, player.getTechVersion());

        EMPAnalyticsProvider.getInstance().ready(sessionId, parameters);
    }

    @Override
    public void onPlaying() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();
        long duration = player().getDuration();
        Entitlement entitlement = player().getEntitlement();

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
        EMPAnalyticsProvider.getInstance().started(sessionId, currenTime, parameters);

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
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();
        EMPAnalyticsProvider.getInstance().paused(sessionId, currenTime, null);
    }

    @Override
    public void onSeek(long position) {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();
        EMPAnalyticsProvider.getInstance().seeked(sessionId, currenTime, null);
    }

    @Override
    public void onResume() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();
        EMPAnalyticsProvider.getInstance().resumed(sessionId, currenTime, null);
    }

    @Override
    public void onBitrateChange(int oldBitrate, int newBitrate) {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(EventParameters.BitrateChanged.BITRATE, Integer.toString(newBitrate));

        EMPAnalyticsProvider.getInstance().bitrateChanged(sessionId, currenTime, parameters);
    }


    @Override
    public void onWaitingStart() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();
        EMPAnalyticsProvider.getInstance().waitingStarted(sessionId, currenTime, null);
    }

    @Override
    public void onWaitingEnd() {
        if (player == null) {
            player();
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();
        EMPAnalyticsProvider.getInstance().waitingEnded(sessionId, currenTime, null);
    }

    @Override
    public void onPlaybackEnd() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        EMPAnalyticsProvider.getInstance().completed(sessionId, null);
        clearTimeUpdater();
    }

    @Override
    public void onStop() {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();
        EMPAnalyticsProvider.getInstance().aborted(sessionId, currenTime, null);
        clearTimeUpdater();
    }

    @Override
    public void onError(int errorCode, String errorMessage) {
        if (player() == null) {
            return;
        }
        String sessionId = player().getSessionId();
        if (sessionId == null) {
            return;
        }

        long currenTime = player().getCurrentTime();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(EventParameters.Error.CODE, Integer.toString(errorCode));
        if (errorMessage != null && errorMessage.equals("") == false) {
            parameters.put(EventParameters.Error.MESSAGE, errorMessage);
        }

        EMPAnalyticsProvider.getInstance().error(sessionId, currenTime, parameters);
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
                String sessionId = player().getSessionId();
                EMPAnalyticsProvider.getInstance().setCurrentTime(sessionId, this.currentTime);

            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }


    private EMPPlayer player() {
        if (player instanceof EMPPlayer) {
            return (EMPPlayer) player;
        }
        return null;
    }
}
