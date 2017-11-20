package net.ericsson.emovs.playback;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import net.ericsson.emovs.analytics.EMPAnalyticsProvider;
import net.ericsson.emovs.exposure.entitlements.EntitledRunnable;
import net.ericsson.emovs.exposure.entitlements.EntitlementCallback;
import net.ericsson.emovs.exposure.interfaces.IPlayable;
import net.ericsson.emovs.exposure.models.EmpAsset;
import net.ericsson.emovs.exposure.models.EmpChannel;
import net.ericsson.emovs.exposure.models.EmpOfflineAsset;
import net.ericsson.emovs.exposure.models.EmpProgram;
import net.ericsson.emovs.utilities.AnalyticsPlaybackConnector;
import net.ericsson.emovs.utilities.Entitlement;
import net.ericsson.emovs.utilities.ErrorCodes;
import net.ericsson.emovs.utilities.ErrorRunnable;
import net.ericsson.emovs.utilities.FileSerializer;
import net.ericsson.emovs.utilities.IEntitlementProvider;
import net.ericsson.emovs.utilities.RunnableThread;

import java.io.File;
import java.util.UUID;

/**
 * Created by Joao Coelho on 2017-11-17.
 */

public class EMPPlayer extends Player {
    private IPlayable playable;
    private Entitlement entitlement;
    private IEntitlementProvider entitlementProvider;

    public EMPPlayer(AnalyticsPlaybackConnector analyticsConnector, IEntitlementProvider entitlementProvider, Activity context, ViewGroup host) {
        super(analyticsConnector, context, host);
        this.entitlementProvider = entitlementProvider;
    }

    @Override
    protected boolean init(PlaybackProperties properties) throws Exception {
        super.init(properties);
        if (getEntitlementProvider() == null) {
            throw new Exception("Do not use default constructor on EMPPlayer.");
        }
        this.entitlement = null;
        this.playable = null;

        return true;
    }

    public void play(IPlayable playable, PlaybackProperties properties) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(ErrorCodes.GENERIC_PLAYBACK_FAILED, e.getMessage());
        }
    }

    private void playLive(final String channelId) {
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
                tech.init(entitlement.playToken, self.properties);
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
        getEntitlementProvider().playLive(channelId, new EntitlementCallback(null, channelId, null, onEntitlementRunnable, onErrorRunnable));
    }

    private void playCatchup(final String channelId, final String programId) {
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
                tech.init(entitlement.playToken, self.properties);
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
        getEntitlementProvider().playCatchup(channelId, programId, new EntitlementCallback(null, channelId, programId, onEntitlementRunnable, onErrorRunnable));
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
                tech.init(entitlement.playToken, self.properties);
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
        getEntitlementProvider().playVod(assetId, new EntitlementCallback(assetId, null, null, onEntitlementRunnable, onErrorRunnable));
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
                Entitlement entitlement = new Entitlement();
                self.entitlement = FileSerializer.readJson(entitlement, entitlementFile.getAbsolutePath());
                self.playbackUUID = UUID.randomUUID();
                self.onEntitlementChange();
                Log.d("EMP MEDIA LOCATOR", manifestPath);
                tech.init(self.entitlement.playToken, self.properties);
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

    private IEntitlementProvider getEntitlementProvider() {
        return entitlementProvider;
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

    public void setAnalyticsCustomAttribute(String k, String v) {
        EMPAnalyticsProvider.getInstance().setCustomAttribute(k, v);
    }

    public void clearAnalyticsCustomAttributes() {
        EMPAnalyticsProvider.getInstance().clearCustomAttributes();
    }


}
