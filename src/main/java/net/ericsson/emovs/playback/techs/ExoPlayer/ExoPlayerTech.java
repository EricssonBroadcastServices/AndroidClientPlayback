package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Pair;

import net.ericsson.emovs.download.DownloadItem;
import net.ericsson.emovs.download.GenericDrmCallback;
import net.ericsson.emovs.download.WidevineOfflineLicenseManager;
import net.ericsson.emovs.playback.EMPPlayer;
import com.ebs.android.utilities.ErrorCodes;

import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.playback.interfaces.IPlayer;
import net.ericsson.emovs.playback.interfaces.ITech;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.OfflineLicenseHelper;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.UUID;


/**
 * Created by Joao Coelho on 2017-08-29.
 */

public class ExoPlayerTech implements IPlayer, ITech {
    private final String FLUX_EXOPLAYER_WIDEVINE_KEYSTORE = "FLUX_EXOPLAYER_WIDEVINE_KEYSTORE";
    private final String KEY_OFFLINE_MEDIA_ID = "key_offline_asset_id_";

    Context ctx;
    SimpleExoPlayer player;
    SimpleExoPlayerView view;
    String playToken;
    EMPPlayer parent;

    boolean isPlaying;
    boolean isReady;
    boolean loadStarted;
    boolean seekStart;
    int currentBitrate;

    public ExoPlayerTech(EMPPlayer parent,  Context ctx) {
        this.ctx = ctx;
        this.parent = parent;
    }

    public ExoPlayerTech(EMPPlayer parent, Context ctx, boolean init, SimpleExoPlayerView view) {
        this.parent = parent;
        this.ctx = ctx;
        if (init) {
            this.init(view, "");
        }
    }

    EMPPlayer getParent() {
        return parent;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    void seekStart(boolean seekStart) {
        this.seekStart = seekStart;
    }

    public void init(SimpleExoPlayerView view, String playToken){
        this.view = view;
        this.playToken = playToken;
        this.isPlaying = false;
        this.isReady = false;
        this.loadStarted = false;
        this.seekStart = false;
    }

    public boolean load(String mediaId, String manifestUrl, boolean isOffline) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //DefaultTrackSelector.Parameters currentParameters = trackSelector.getParameters();
        //DefaultTrackSelector.Parameters newParameters = currentParameters.withMaxVideoBitrate(500000);
        //trackSelector.setParameters(newParameters);

        Pair<String, String> licenseDetails = DownloadItem.getLicenseDetails(manifestUrl, isOffline);

        if (licenseDetails != null) {
            String[] keyRequestPropertiesArray = {};
            String licenseWithToken = Uri.parse(licenseDetails.first)
                    .buildUpon()
                    .appendQueryParameter("token", "Bearer " + this.playToken)
                    .build().toString();
            licenseDetails = new Pair<>(licenseWithToken, licenseDetails.second);

            UUID drmSchemeUuid = null;
            try {
                drmSchemeUuid = getDrmUuid("widevine");
            } catch (ParserException e) {
                e.printStackTrace();
                return false;
            }
            try {
                DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;

                if (isOffline) {
                    drmSessionManager = buildOfflineDrmSessionManager(mediaId, drmSchemeUuid, licenseDetails.first, licenseDetails.second);
                }
                else {
                    drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, licenseDetails.first, keyRequestPropertiesArray);
                }

                DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(ctx, drmSessionManager, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
                this.player = HookedSimpleExoPlayer.newSimpleInstance(this, renderersFactory, trackSelector);
                this.player.setPlayWhenReady(true);
                this.player.addListener(new Player.EventListener(){
                    @Override
                    public void onTimelineChanged(Timeline timeline, Object manifest) {

                    }

                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                        int oldBitrate = currentBitrate;
                        getCurrentBitrate();
                        if (oldBitrate > 0 && oldBitrate != currentBitrate) {
                            parent.onBitrateChange(oldBitrate, currentBitrate);
                        }
                    }

                    @Override
                    public void onLoadingChanged(boolean isLoading) {
                        if (parent != null && isPlaying) {
                            if (isLoading) {
                                parent.onWaitingStart();
                            }
                            else {
                                parent.onWaitingEnd();
                            }
                        }
                    }

                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if (playbackState == Player.STATE_READY) {
                            if (isReady == false) {
                                parent.onLoad();
                                isReady = true;
                            }
                            if (player != null && playWhenReady && !isPlaying) {
                                isPlaying = true;
                                parent.onPlaying();
                            }
                            if (player != null && seekStart) {
                                seekStart = false;
                                parent.onSeek(player.getCurrentPosition());
                            }
                        }
                        else if (playbackState == Player.STATE_ENDED && isPlaying) {
                            isPlaying = false;
                            isReady = false;
                            seekStart = false;
                            parent.onPlaybackEnd();
                        }
                        else if (playbackState == Player.STATE_BUFFERING && !isReady && !isPlaying && !loadStarted) {
                            loadStarted = true;
                            parent.onLoadStart();
                        }
                        else if (playbackState == Player.STATE_IDLE) {
                            isPlaying = false;
                            isReady = false;
                            seekStart = false;
                        }
                    }

                    @Override
                    public void onRepeatModeChanged(int repeatMode) {

                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {
                        if (parent != null) {
                            parent.onError(ErrorCodes.EXO_PLAYER_INTERNAL_ERROR, error.getMessage());
                        }
                    }

                    @Override
                    public void onPositionDiscontinuity() {

                    }

                    @Override
                    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                    }
                });
            } catch (UnsupportedDrmException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            this.player = HookedSimpleExoPlayer.newSimpleInstance(this, new DefaultRenderersFactory(ctx), trackSelector);
            this.player.setPlayWhenReady(true);
        }

        return true;
    }

    public void play(String dashManifestUrl) {
        this.view.setPlayer(this.player);
        this.view.requestFocus();

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this.ctx, Util.getUserAgent(this.ctx, "EMP-Player"), bandwidthMeter);
        MediaSource mediaSource = new DashMediaSource(Uri.parse(dashManifestUrl), dataSourceFactory, new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        this.player.prepare(mediaSource);

    }

    public void release() {
        if (player == null) {
            return;
        }
        this.player.release();
        this.player = null;
    }

    public void pause() {
        if(player != null) {
            this.player.setPlayWhenReady(false);
        }
    }

    public void resume() {
        if(player != null) {
            this.player.setPlayWhenReady(true);
        }
    }

    public void stop() {
        if(player != null) {
            this.player.stop();
        }
    }

    public void seekTo(long positionMs) {
        if(player != null) {
            this.player.seekTo(positionMs);
        }
    }

    public long getCurrentTime() {
        if (this.player != null) {
            return this.player.getCurrentPosition();
        }
        return -1;
    }

    public long getDuration() {
        if (this.player != null) {
            return this.player.getDuration();
        }
        return -1;
    }

    public int getCurrentBitrate() {
        if (this.player == null) {
            return -1;
        }
        for (TrackSelection selection : player.getCurrentTrackSelections().getAll()) {
            if (selection instanceof  AdaptiveTrackSelection) {
                AdaptiveTrackSelection adaptiveSelection = (AdaptiveTrackSelection) selection;
                currentBitrate = adaptiveSelection.getSelectedFormat().bitrate / 1024;
                return currentBitrate;
            }
        }
        return -1;
    }

    public String getIdentifier() {
        return "ExoPlayer2";
    }

    public String getVersion() {
        return ctx.getString(R.string.exoplayer_version);
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildOfflineDrmSessionManager(String mediaId, UUID uuid, String licenseUrl, String initDataB64/*, Map<String, String> keyRequestProperties*/) {
        try {
            WidevineOfflineLicenseManager licenseDownloadManager = new WidevineOfflineLicenseManager(ctx);
            byte[] offlineAssetKeyId = licenseDownloadManager.get(licenseUrl, mediaId);

            if (offlineAssetKeyId == null) {
                return null;
            }

            GenericDrmCallback customDrmCallback = new GenericDrmCallback(buildHttpDataSourceFactory(true), licenseUrl);
            DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), customDrmCallback, null, null, null);
            OfflineLicenseHelper offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(customDrmCallback, null);
            Pair<Long, Long> remainingTime = offlineLicenseHelper.getLicenseDurationRemainingSec(offlineAssetKeyId);

            if (remainingTime.first == 0 || remainingTime.second == 0) {
                return null;
            }

            drmSessionManager.setMode(DefaultDrmSessionManager.MODE_QUERY, offlineAssetKeyId);
            return drmSessionManager;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray) throws UnsupportedDrmException {
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, buildHttpDataSourceFactory(false));
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback, null, null, null);
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this.ctx, "EMP-Player"), useBandwidthMeter ? bandwidthMeter : null);
    }

    private UUID getDrmUuid(String typeString) throws ParserException {
        switch (Util.toLowerInvariant(typeString)) {
            case "widevine":
                return C.WIDEVINE_UUID;
            case "playready":
                return C.PLAYREADY_UUID;
            case "cenc":
                return C.CLEARKEY_UUID;
            default:
                try {
                    return UUID.fromString(typeString);
                } catch (RuntimeException e) {
                    throw new ParserException("Unsupported drm type: " + typeString);
                }
        }
    }

    private SharedPreferences getSharedPreferences() {
        return ctx.getSharedPreferences(FLUX_EXOPLAYER_WIDEVINE_KEYSTORE, Context.MODE_PRIVATE);
    }

}
