package net.ericsson.emovs.playback;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ericsson.emovs.exposure.auth.SharedPropertiesICredentialsStorage;
import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.entitlements.EntitledRunnable;
import net.ericsson.emovs.exposure.models.EmpAsset;
import net.ericsson.emovs.exposure.models.EmpChannel;
import net.ericsson.emovs.exposure.models.EmpOfflineAsset;
import net.ericsson.emovs.exposure.models.EmpProgram;
import net.ericsson.emovs.utilities.ErrorCodes;
import net.ericsson.emovs.utilities.ErrorRunnable;
import net.ericsson.emovs.utilities.FileSerializer;
import net.ericsson.emovs.utilities.RunnableThread;
import net.ericsson.emovs.exposure.entitlements.EntitlementCallback;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import net.ericsson.emovs.exposure.entitlements.Entitlement;
import net.ericsson.emovs.exposure.interfaces.IPlayable;

import net.ericsson.emovs.analytics.EMPAnalyticsProvider;
import net.ericsson.emovs.playback.techs.ExoPlayer.ExoPlayerTech;

import java.io.File;
import java.util.UUID;


/**
 * Created by Joao Coelho on 2017-08-30.
 */

public class EMPPlayer extends PlaybackEventListenerAggregator {
    ExoPlayerTech tech;

    ViewGroup host;
    SimpleExoPlayerView view;

    Activity context;
    SharedPropertiesICredentialsStorage credentialsStorage;
    EMPAnalyticsConnector analyticsConnector;
    PlaybackProperties properties;

    IPlayable playable;
    Entitlement entitlement;
    UUID playbackUUID;

    public EMPPlayer(Activity context, ViewGroup host) {
        this.context = context;
        this.credentialsStorage = SharedPropertiesICredentialsStorage.getInstance();
        this.analyticsConnector = new EMPAnalyticsConnector(this);
        //this.view = view;
        this.host = host;
        addListener(this.analyticsConnector);
        createExoView(this.host);
    }

    @Override
    public void clearListeners() {
        super.clearListeners();
        if(this.analyticsConnector != null) {
            addListener(this.analyticsConnector);
        }
    }

    public void setAnalyticsCustomAttribute(String k, String v) {
        EMPAnalyticsProvider.getInstance().setCustomAttribute(k, v);
    }

    public void clearAnalyticsCustomAttributes() {
        EMPAnalyticsProvider.getInstance().clearCustomAttributes();
    }

    private void createExoView(ViewGroup host) {
        if (this.view != null) {
            return;
        }

        if (host == null) {
            // TODO: raise proper error
            return;
        }

        View initialExoView = host.findViewById(R.id.exoview);

        if(initialExoView != null && initialExoView instanceof SimpleExoPlayerView) {
            this.view = (SimpleExoPlayerView) initialExoView;
            return;
        }

        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View exoLayout = inflater.inflate(R.layout.exoview, null);

        host.removeAllViews();
        host.addView(exoLayout);

        View exoView = host.findViewById(R.id.exoview);

        if (exoView == null) {
            //TODO: raise error - exo view not found
            return;
        }
        if (exoView instanceof SimpleExoPlayerView == false) {
            //TODO: raise error - view not of SimpleExoPlayerView type
            return;
        }

        this.view = (SimpleExoPlayerView) exoView;
    }

    private boolean init(PlaybackProperties properties) {
//        clearListeners();
        this.properties = properties;
        this.entitlement = null;
        this.playable = null;
        this.playbackUUID = null;

        if (this.tech != null) {
            this.release();
        }

        this.tech = new ExoPlayerTech(this, this.context, false, this.view, properties);
        super.onInit();
        return true;
    }

    public void play(IPlayable playable, PlaybackProperties properties) {
        init(properties);
        super.onPlay();
        if (playable == null) {
            this.onError(ErrorCodes.PLAYBACK_INVALID_EMP_PLAYABLE, "");
            return;
        }
        if (playable instanceof EmpProgram) {
            this.playable = playable;
            EmpProgram playableProgram = (EmpProgram) playable;
            if (playableProgram.liveNow()) {
                playLive(playableProgram.channelId);
            }
            else {
                playCatchup(playableProgram.assetId, playableProgram.programId);
            }
        }
        else if (playable instanceof EmpOfflineAsset) {
            this.playable = playable;
            EmpOfflineAsset offlineAsset = (EmpOfflineAsset) playable;
            playOffline(offlineAsset.localMediaPath);
        }
        else if (playable instanceof EmpChannel) {
            this.playable = playable;
            EmpChannel channel = (EmpChannel) playable;
            playLive(channel.channelId);
        }
        else if (playable instanceof EmpAsset) {
            this.playable = playable;
            EmpAsset asset = (EmpAsset) playable;
            playVod(asset.assetId);
        }
        else {
            this.onError(ErrorCodes.PLAYBACK_INVALID_EMP_PLAYABLE, "");
            return;
        }

        return;
    }

    public void release() {
        if (this.tech != null) {
            this.tech.release();
            this.tech = null;
        }
        super.clearListeners();
    }

    public void pause() {
        if (this.tech != null) {
            this.tech.pause();
        }
    }

    public void resume() {
        if (this.tech != null) {
            this.tech.resume();
        }
    }

    public void stop() {
        if (this.tech != null) {
            this.tech.stop();
        }
    }


    public void seekTo(long positionMs) {
        if (this.tech != null) {
            this.tech.seekTo(positionMs);
        }
    }

    public long getCurrentTime() {
        if (this.tech != null) {
            return this.tech.getCurrentTime();
        }
        return -1;
    }

    public long getDuration() {
        if (this.tech != null) {
            return this.tech.getDuration();
        }
        return -1;
    }

    public Entitlement getEntitlement() {
        return entitlement;
    }

    public IPlayable getPlayable() {
        return this.playable;
    }

    public String getSessionId() {
        if (entitlement == null) {
            return null;
        }
        if (playable != null && playable instanceof EmpOfflineAsset) {
            return "offline-" + playbackUUID.toString();
        }
        return entitlement.playSessionId;
    }

    public boolean isPlaying() {
        return this.tech != null && this.tech.isPlaying();
    }

    public PlaybackProperties getPlaybackProperties() {
        return properties;
    }

    public int getCurrentBitrate() {
        return this.tech == null ? -1 : tech.getCurrentBitrate();
    }

    public Context getContext() {
        return context;
    }

    private void playLive(final String channelId) {
        final EMPPlayer self = this;
        final EntitledRunnable onEntitlementRunnable = new EntitledRunnable() {
            @Override
            public void run() {
                self.entitlement = entitlement;
                self.onEntitlementChange();
                Log.d("EMP MEDIA LOCATOR", entitlement.mediaLocator);
                tech.init(view, entitlement.playToken, self.properties);
                tech.load(entitlement.channelId, entitlement.mediaLocator, false);
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        tech.play(entitlement.mediaLocator);
                    }
                });
            }
        };
        final ErrorRunnable onErrorRunnable = new ErrorRunnable() {
            @Override
            public void run(int errorCode, String errorMessage) {
                if (self != null) {
                    self.onError(errorCode, errorMessage);
                }
            }
        };
        super.onEntitlementLoadStart();
        EMPEntitlementProvider.getInstance().playLive(channelId, new EntitlementCallback(null, channelId, null, onEntitlementRunnable, onErrorRunnable));
    }

    private void playCatchup(final String channelId, final String programId) {
        final EMPPlayer self = this;
        final EntitledRunnable onEntitlementRunnable = new EntitledRunnable() {
            @Override
            public void run() {
                self.entitlement = entitlement;
                self.onEntitlementChange();
                Log.d("EMP MEDIA LOCATOR", entitlement.mediaLocator);
                tech.init(view, entitlement.playToken, self.properties);
                tech.load(entitlement.programId, entitlement.mediaLocator, false);
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        tech.play(entitlement.mediaLocator);
                    }
                });
            }
        };
        final ErrorRunnable onErrorRunnable = new ErrorRunnable() {
            @Override
            public void run(int errorCode, String errorMessage) {
                if (self != null) {
                    self.onError(errorCode, errorMessage);
                }
            }
        };
        super.onEntitlementLoadStart();
        EMPEntitlementProvider.getInstance().playCatchup(channelId, programId, new EntitlementCallback(null, channelId, programId, onEntitlementRunnable, onErrorRunnable));
    }

    private void playVod(final String assetId) {
        final EMPPlayer self = this;
        final EntitledRunnable onEntitlementRunnable = new EntitledRunnable() {
            @Override
            public void run() {
                self.entitlement = entitlement;
                self.onEntitlementChange();
                if(self.properties != null && self.properties.useLastViewedOffset()) {
                    self.properties.withStartTime(entitlement.lastViewedOffset);
                }
                Log.d("EMP MEDIA LOCATOR", entitlement.mediaLocator);
                tech.init(view, entitlement.playToken, self.properties);
                tech.load(entitlement.assetId, entitlement.mediaLocator, false);
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        if (tech != null) {
                            tech.play(entitlement.mediaLocator);
                        }
                        else {
                            // TODO: handle
                        }
                    }
                });
            }
        };
        final ErrorRunnable onErrorRunnable = new ErrorRunnable() {
            @Override
            public void run(int errorCode, String errorMessage) {
                if (self != null) {
                    self.onError(errorCode, errorMessage);
                }
            }
        };
        super.onEntitlementLoadStart();
        EMPEntitlementProvider.getInstance().playVod(assetId, new EntitlementCallback(assetId, null, null, onEntitlementRunnable, onErrorRunnable));
    }

    private boolean playOffline(final String manifestPath) {
        // TODO: missing eventListeners.onEntitlementLoadStart();
        final EMPPlayer self = this;
        new RunnableThread(new Runnable() {
            @Override
            public void run() {
                File manifestUrl = new File(manifestPath);
                File manifestFolder = manifestUrl.getParentFile();
                File entitlementFile = new File(manifestFolder, "entitlement.ser");
                self.entitlement = FileSerializer.read(entitlementFile.getAbsolutePath());
                self.playbackUUID = UUID.randomUUID();
                self.onEntitlementChange();
                Log.d("EMP MEDIA LOCATOR", manifestPath);
                tech.init(view, self.entitlement.playToken, self.properties);
                tech.load(self.entitlement.assetId, manifestPath, true);
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        tech.play(manifestPath);
                    }
                });
            }
        }).start();
        return true;
    }


    public String getTechVersion() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getVersion();
    }

    public String getTechIdentifier() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getIdentifier();
    }

    public String getIdentifier() {
        return context.getString(R.string.emplayer_name);
    }

    public String getVersion() {
        return context.getString(R.string.emplayer_version);
    }
}
