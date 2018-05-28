package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.ericsson.emovs.utilities.emp.UniversalPackagerHelper;
import net.ericsson.emovs.playback.drm.GenericDrmCallback;
import net.ericsson.emovs.playback.Player;
import net.ericsson.emovs.playback.drm.WidevinePlaybackLicenseManager;
import net.ericsson.emovs.utilities.errors.Warning;
import net.ericsson.emovs.utilities.interfaces.ControllerVisibility;
import net.ericsson.emovs.utilities.drm.DashDetails;
import net.ericsson.emovs.utilities.errors.ErrorCodes;

import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.playback.interfaces.ITech;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.system.ParameterizedRunnable;
import net.ericsson.emovs.utilities.system.RunnableThread;
import net.ericsson.emovs.utilities.system.ServiceUtils;
import net.ericsson.emovs.utilities.time.DateTimeParser;
import net.ericsson.emovs.utilities.ui.ViewHelper;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
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
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static net.ericsson.emovs.utilities.errors.Error.NETWORK_ERROR;


/**
 * Created by Joao Coelho on 2017-08-29.
 */

public class ExoPlayerTech implements ITech, PlaybackPreparer {
    private final String FLUX_EXOPLAYER_WIDEVINE_KEYSTORE = "FLUX_EXOPLAYER_WIDEVINE_KEYSTORE";
    private final String KEY_OFFLINE_MEDIA_ID = "key_offline_asset_id_";

    private static final int TRACK_GROUP_VIDEO = 0;
    private static final int TRACK_GROUP_AUDIO = 1;
    private static final int TRACK_GROUP_TEXT = 2;

    private static final long TIMESHIFT_VAL = 30;

    ViewGroup host;
    protected Activity ctx;
    SimpleExoPlayer player;
    SimpleExoPlayerView view;
    String playToken;
    Player parent;

    float lastVolume;

    int currentBitrate;
    DefaultTrackSelector.ParametersBuilder trackSelectorParamsBuilder;

    PlaybackProperties properties;
    Uri manifestUrl;

    protected ExoPlayerEventListener playerEventListener;
    private Handler mainHandler;
    private EventLogger eventLogger;
    protected DefaultBandwidthMeter bandwidthMeter;

    public ExoPlayerTech() {
        mainHandler = new Handler();
        bandwidthMeter = new DefaultBandwidthMeter(mainHandler, new BandwidthMeter.EventListener() {
            @Override
            public void onBandwidthSample(int i, long l, long l1) {
                Log.d("BandwidthEstimate", Integer.toString(i) + "," + Long.toString(l) + "," + Long.toString(l1));
            }
        });
    }

    Player getParent() {
        return parent;
    }

    public boolean isPlaying() {
        if (playerEventListener == null) {
            return false;
        }
        return playerEventListener.isPlaying;
    }

    /**
     * Method that checks if ExoPlayer is paused
     * @return
     */
    @Override
    public boolean isPaused() {
        if (this.isPlaying()) {
            return !this.player.getPlayWhenReady();
        }
        return false;
    }

    void seekStart(boolean seekStart) {
        if (playerEventListener == null) {
            return;
        }
        playerEventListener.seekStart = seekStart;
    }

    public void init(Player parent, Activity ctx, String playToken, PlaybackProperties properties) {
        this.parent = parent;
        this.ctx = ctx;

        createExoView(parent.getViewGroup());

        this.playToken = playToken;
        this.properties = properties;
    }

    DefaultTrackSelector trackSelector = null;

    public void overrideExoControls() {
        // FastForward: exo_ffwd
        // FastRewing: exo_rew
        // Pause: exo_pause
        // Play: exo_play
        // Next: exo_next
        // Previous: exo_prev
        // Shuffle: exo_shuffle
        // TODO: what about VOD/catchup? get use cases for VOD and implement them
        // TODO: if future code shows stuff, do not forget to check if props enable the default controller
        final View ff = (View) view.findViewById(R.id.exo_ffwd);
        View rw = (View) view.findViewById(R.id.exo_rew);
        View next = (View) view.findViewById(R.id.exo_next);
        View prev = (View) view.findViewById(R.id.exo_prev);
        View pause = (View) view.findViewById(R.id.exo_pause);
        View play = (View) view.findViewById(R.id.exo_play);
        View timeline = (View) view.findViewById(R.id.exo_progress);
        View duration = (View) view.findViewById(R.id.exo_duration);
        View position = (View) view.findViewById(R.id.exo_position);

        pause.setEnabled(parent.canPause());

        if (UniversalPackagerHelper.isUniversalPackager(this.manifestUrl.toString()) == true) {
            //timeline.setVisibility(View.INVISIBLE);
            //duration.setVisibility(View.INVISIBLE);
            //position.setVisibility(View.INVISIBLE);
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parent.pause();
                }
            });

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parent.seekToLive();
                }
            });

            ff.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(ctx, "Seeking to live...", Toast.LENGTH_SHORT).show();
                    parent.seekToLive();
                    return true;
                }
            });

            ff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View btn) {
                    parent.seekToTime(parent.getPlayheadTime() + 30000);
                }
            });

            rw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View btn) {
                    parent.seekToTime(parent.getPlayheadTime() - 30000);
                }
            });

            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parent.startOver();
                }
            });
        }
    }

    public boolean load(final String mediaId, final String manifestUrl, final boolean isOffline) {
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        this.trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        if (properties != null) {
            this.trackSelectorParamsBuilder = new DefaultTrackSelector.ParametersBuilder();

            // Setting max bitrate
            if (properties.getMaxBitrate() != null) {
                this.trackSelectorParamsBuilder = this.trackSelectorParamsBuilder.setMaxVideoBitrate(properties.getMaxBitrate());
            }

            // Setting preferred audio langugage
            if (properties.getPreferredAudioLanguage() != null) {
                this.trackSelectorParamsBuilder = this.trackSelectorParamsBuilder.setPreferredAudioLanguage(properties.getPreferredAudioLanguage());
            }

            // Setting preferred text language
            if (properties.getPreferredTextLanguage() != null) {
                this.trackSelectorParamsBuilder = this.trackSelectorParamsBuilder.setPreferredTextLanguage(properties.getPreferredTextLanguage());
            }

            trackSelector.setParameters(this.trackSelectorParamsBuilder.build());
        }

        eventLogger = new EventLogger(trackSelector);
        final Runnable onValid = new Runnable() {
            @Override
            public void run() {
                proceedWithPlayback(manifestUrl, isOffline, mediaId);
            }
        };
        final Runnable onHttpRequired = new Runnable() {
            @Override
            public void run() {
                proceedWithPlayback(manifestUrl.replace("https://", "http://"), isOffline, mediaId);
            }
        };
        final ParameterizedRunnable<String> onError = new ParameterizedRunnable<String>() {
            @Override
            public void run(final String s) {
                parent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder b = new StringBuilder("INVALID_MANIFEST");
                        if (s != null) {
                            b.append("\n" + s);
                        }
                        parent.fail(ErrorCodes.INVALID_MANIFEST, b.toString());
                        stop();
                    }
                });
            }
        };

        DashDetails.isValidManifest(manifestUrl, isOffline, onValid, onError, onHttpRequired);

        return true;
    }

    protected void proceedWithPlayback(final String manifestUrl, final boolean isOffline, final String mediaId) {
        final ExoPlayerTech self = this;
        DashDetails.getLicenseDetails(manifestUrl, isOffline, new ParameterizedRunnable<Pair<String, String>>() {
            @Override
            public void run(Pair<String, String> licenseDetails) {
                if (licenseDetails == null && properties.getDRMProperties() != null) {
                    PlaybackProperties.DRMProperties drmProps = properties.getDRMProperties();
                    licenseDetails = new Pair<>(drmProps.licenseServerUrl, drmProps.initDataBase64);
                }

                if (licenseDetails != null) {
                    String[] keyRequestPropertiesArray = {};
                    String licenseWithToken = licenseDetails.first;
                    if (self.playToken != null) {
                        licenseWithToken = Uri.parse(licenseDetails.first)
                                .buildUpon()
                                .appendQueryParameter("token", "Bearer " + self.playToken)
                                .build().toString();
                    }

                    licenseDetails = new Pair<>(licenseWithToken, licenseDetails.second);

                    UUID drmSchemeUuid = null;
                    try {
                        drmSchemeUuid = getDrmUuid("widevine");
                    } catch (ParserException e) {
                        e.printStackTrace();
                        return;
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
                        self.player = HookedSimpleExoPlayer.newSimpleInstance(self, renderersFactory, trackSelector);
                        self.player.setPlayWhenReady(self.properties == null ? PlaybackProperties.DEFAULT.isAutoplay() : self.properties.isAutoplay());
                    } catch (UnsupportedDrmException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                else {
                    self.player = HookedSimpleExoPlayer.newSimpleInstance(self, new DefaultRenderersFactory(ctx), trackSelector);
                    self.player.setPlayWhenReady(self.properties == null ? PlaybackProperties.DEFAULT.isAutoplay() : self.properties.isAutoplay());
                }

                if (ctx != null) {
                    if (self.playerEventListener != null) {
                        self.player.removeListener(self.playerEventListener);
                    }
                    self.playerEventListener = new ExoPlayerEventListener(self, isOffline);
                    self.player.addListener(self.playerEventListener);

                    ctx.runOnUiThread(new Runnable() {
                        public void run() {
                            play(manifestUrl);
                        }
                    });
                }
                return;
            }
        });
    }

    @Override
    public void preparePlayback() {

    }

    public void play(String dashManifestUrl) {
        if (this.player == null) {
            return;
        }
        this.manifestUrl = Uri.parse(dashManifestUrl);
        this.view.setPlayer(this.player);

        this.view.setUseController(this.properties == null ? PlaybackProperties.DEFAULT.hasNativeControls() : this.properties.hasNativeControls());
        this.view.requestFocus();

        if (this.properties != null && this.properties.getNativeControlsShowTimeoutMs() != null) {
            this.view.setControllerShowTimeoutMs(this.properties.getNativeControlsShowTimeoutMs());
        }

        if (this.properties != null && this.properties.isNativeControlsHideOnTouch() != null) {
            this.view.setControllerHideOnTouch(this.properties.isNativeControlsHideOnTouch());
        }

        this.view.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == 0) {
                    parent.onControllerVisibility(ControllerVisibility.Visible);
                }
                else if(visibility == 8) {
                    parent.onControllerVisibility(ControllerVisibility.Hidden);
                }
            }
        });
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this.ctx, Util.getUserAgent(this.ctx, "EMP-Player"), bandwidthMeter);
        //MediaSource mediaSource = new DashMediaSource(this.manifestUrl, dataSourceFactory, new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);

        MediaSource mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                .createMediaSource(this.manifestUrl, mainHandler, eventLogger);

        player.prepare(mediaSource);
        overrideExoControls();
    }

    public void release() {
        if (player == null) {
            return;
        }
        if (playerEventListener != null) {
            player.removeListener(playerEventListener);
        }
        this.player.release();
        this.player = null;
    }

    public void pause() {
        if (player != null) {
            this.player.setPlayWhenReady(false);
        }
    }

    public void resume() {
        if (player != null) {
            this.player.setPlayWhenReady(true);
        }
    }

    public void stop() {
        if (player != null) {
            this.player.stop();
        }
    }

    public void seekTo(long positionMs) {
        if (player != null && positionMs >= 0) {
            if (positionMs >= getDuration()) {
                positionMs = getDuration();
            }
            this.player.seekTo(positionMs);
        }
    }

    public void seekToTime(long unixTimeMs) {
        if (player != null) {
            if (unixTimeMs >= parent.getMonotonicTimeService().currentTime()) {
                unixTimeMs = parent.getMonotonicTimeService().currentTime();
            }
            long windowStartTime = getWindowStartTime();
            this.player.seekTo(unixTimeMs - windowStartTime);
        }
    }

    public long getPlayheadTime() {
        if (this.player != null) {
            long windowStartTime = getWindowStartTime();
            return windowStartTime + getPlayheadPosition();
        }
        return -1;
    }

    public long getPlayheadPosition() {
        if (this.player != null) {
            return this.player.getCurrentPosition();
        }
        return -1;
    }

    public long[] getBufferedRange() {
        if (this.player == null) {
            return null;
        }
        long[] range = new long[2];
        range[0] = this.getPlayheadPosition();
        range[1] = this.player.getBufferedPosition();
        return range;
    }

    public long[] getBufferedTimeRange() {
        if (this.player == null) {
            return null;
        }
        long windowStartTime = getWindowStartTime();
        long[] range = getBufferedRange();
        range[0] += windowStartTime;
        range[1] += windowStartTime;
        return range;
    }

    public long[] getSeekRange() {
        if (this.player == null) {
            return null;
        }
        long[] range = new long[2];
        range[0] = 0;
        range[1] = getDuration();
        return range;
    }

    public long[] getSeekTimeRange() {
        if (this.player == null) {
            return null;
        }
        long windowStartTime = getWindowStartTime();
        long[] range = getSeekRange();
        range[0] += windowStartTime;
        range[1] += windowStartTime;
        return range;
    }

    public long getDuration() {
        if (this.player != null) {
            return this.player.getDuration();
        }
        return -1;
    }

    private long getWindowStartTime() {
        if (this.playerEventListener == null) {
            return 0;
        }
        return this.playerEventListener.windowStartTimeMs;
    }

    /**
     * Returns a list of audio languages available to be chosen
     *
     * @return
     */
    @Override
    public String[] getAudioLanguages() {
        if (trackSelector == null) {
            return null;
        }
        MappingTrackSelector.MappedTrackInfo tracksInfo = trackSelector.getCurrentMappedTrackInfo();

        if (tracksInfo == null) {
            return null;
        }

        TrackGroupArray audioTracks = tracksInfo.getTrackGroups(TRACK_GROUP_AUDIO);

        if (audioTracks.length == 0) {
            return null;
        }

        HashMap<String, String> memory = new HashMap<>();
        ArrayList<String> audioTracksOutput = new ArrayList<>();
        for (int i = 0; i < audioTracks.length; ++i) {
            if (audioTracks.get(i).length > 0) {
                String lang = audioTracks.get(i).getFormat(0).language;
                if(memory.containsKey(lang) == false && lang != null) {
                    audioTracksOutput.add(lang);
                    memory.put(lang, lang);
                }
            }
        }

        return audioTracksOutput.toArray(new String[audioTracksOutput.size()]);
    }

    /**
     * Selects audio track
     *
     * @param language language code to select the audio track (e.g.: en, pt, es, fr)
     */
    @Override
    public void selectAudioLanguage(String language) {
        if (this.trackSelector == null || this.trackSelectorParamsBuilder == null) {
            return;
        }
        this.trackSelectorParamsBuilder = this.trackSelectorParamsBuilder.setPreferredAudioLanguage(language);
        trackSelector.setParameters(this.trackSelectorParamsBuilder.build());
    }

    /**
     * Returns selected audio track
     *
     * @return selected audio language
     */
    @Override
    public String getSelectedAudioLanguage() {
        return trackSelector.getParameters().preferredAudioLanguage;
    }

    /**
     * Returns a list of text languages available to be chosen
     *
     * @return
     */
    @Override
    public String[] getTextLanguages() {
        if (trackSelector == null) {
            return null;
        }

        MappingTrackSelector.MappedTrackInfo tracksInfo = trackSelector.getCurrentMappedTrackInfo();

        if (tracksInfo == null) {
            return null;
        }

        TrackGroupArray textTracks = tracksInfo.getTrackGroups(TRACK_GROUP_TEXT);

        if (textTracks.length == 0) {
            return null;
        }

        ArrayList<String> textTracksOutput = new ArrayList<>();
        HashMap<String, String> memory = new HashMap<>();
        for (int i = 0; i < textTracks.length; ++i) {
            if (textTracks.get(i).length > 0) {
                String lang = textTracks.get(i).getFormat(0).language;
                if (memory.containsKey(lang) == false && lang != null) {
                    textTracksOutput.add(lang);
                    memory.put(lang, lang);
                }
            }
        }

        return textTracksOutput.toArray(new String[textTracksOutput.size()]);
    }

    /**
     * Selects text track
     *
     * @param language language code to select the text track (e.g.: en, pt, es, fr)
     */
    @Override
    public void selectTextLanguage(String language) {
        if (this.trackSelector == null || this.trackSelectorParamsBuilder == null) {
            return;
        }
        this.trackSelectorParamsBuilder = this.trackSelectorParamsBuilder.setPreferredTextLanguage(language);
        trackSelector.setParameters(this.trackSelectorParamsBuilder.build());
    }

    /**
     * Returns selected text track
     *
     * @return selected text language
     */
    @Override
    public String getSelectedTextLanguage() {
        return trackSelector.getParameters().preferredTextLanguage;
    }

    /**
     * Mutes the audio
     */
    @Override
    public void mute() {
        this.lastVolume = this.player.getVolume();
        this.player.setVolume(0);
    }

    /**
     * Unmutes the audio (volume will be last heard volume [0..1]
     */
    @Override
    public void unmute() {
        if (this.lastVolume == 0f) {
            this.lastVolume = 1.0f;
        }
        this.player.setVolume(this.lastVolume);
    }

    /**
     * Sets the audio volume level
     *
     * @param volume volume level [0..1]
     */
    @Override
    public void setVolume(float volume) {
        if (this.player == null) {
            return;
        }
        this.player.setVolume(volume);
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

    public long tParamStartTime() {
        if (this.manifestUrl == null) {
            return -1;
        }
        String t = this.manifestUrl.getQueryParameter("t");
        if (t == null) {
            return -1;
        }
        String[] tParts = t.replace("%3A", ":").split("-");
        if (tParts.length < 4) {
            return -1;
        }
        String tStart = tParts[0] + "-" + tParts[1] + "-" + tParts[2] + "Z";
        try {
            return DateTimeParser.parseUtcDateTime(tStart, true).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long getTimeshiftDelay() {
        if (this.manifestUrl == null) {
            return 0;
        }
        String timeshiftOldValue = this.manifestUrl.getQueryParameter("time_shift");
        if (timeshiftOldValue == null || "".equals(timeshiftOldValue)) {
            return 0;
        }
        return Long.parseLong(timeshiftOldValue);
    }

    public void setTimeshiftDelay(long timeshift) {
        if (isPlaying()) {
            String newManifestUrl = null;
            String timeshiftOldValue = this.manifestUrl.getQueryParameter("time_shift");
            if (timeshiftOldValue == null) {
                newManifestUrl = this.manifestUrl.buildUpon()
                        .appendQueryParameter("time_shift", Long.toString(timeshift))
                        .build().toString();
            }
            else {
                newManifestUrl = this.manifestUrl.buildUpon().build().toString()
                        .replace("time_shift=" + timeshiftOldValue, "time_shift=" + Long.toString(timeshift));
            }
            if (newManifestUrl != null) {
                // TODO: what to do with analytics? Also very slow solution
                play(newManifestUrl);
            }
        }
    }

    public View getSubtitlesView() {
        if (this.view != null) {
          return this.view.findViewById(R.id.exo_subtitles);
        }
        return null;
    }


    private DrmSessionManager<FrameworkMediaCrypto> buildOfflineDrmSessionManager(String mediaId, UUID uuid, String licenseUrl, String initDataB64/*, Map<String, String> keyRequestProperties*/) {
        try {
            WidevinePlaybackLicenseManager licenseDownloadManager = new WidevinePlaybackLicenseManager(ctx);
            byte[] offlineAssetKeyId = licenseDownloadManager.get(licenseUrl, mediaId);

            if (offlineAssetKeyId == null) {
                return null;
            }

            GenericDrmCallback customDrmCallback = new GenericDrmCallback(buildHttpDataSourceFactory(true), licenseUrl);
            DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), customDrmCallback, null, null, null);
            OfflineLicenseHelper offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(licenseUrl, buildHttpDataSourceFactory(true));
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
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, buildHttpDataSourceFactory(true));
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger, false);
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
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

    private void createExoView(final ViewGroup host) {
        if (this.view != null) {
            return;
        }

        if (host == null) {
            // TODO: raise proper error
            return;
        }

        this.ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View initialExoView = host.findViewById(R.id.exoview);

                if(initialExoView != null && initialExoView instanceof SimpleExoPlayerView) {
                    view = (SimpleExoPlayerView) initialExoView;
                    return;
                }

                LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

                view = (SimpleExoPlayerView) exoView;
                registerPlayer();
            }
        });
    }

    private void registerPlayer() {
        View timebar = view.findViewById(R.id.exo_progress);
        if (timebar != null && timebar instanceof HookedDefaultTimeBar) {
            HookedDefaultTimeBar hookedTimebar = (HookedDefaultTimeBar) timebar;
            hookedTimebar.bindPlayer(getParent());

            View liveLine = view.findViewById(R.id.live_divisory_line);
            if (liveLine != null && liveLine instanceof View) {
                hookedTimebar.bindLiveLine(liveLine);
            }
        }

        ArrayList<HookedImageButton> hookedBtns = ViewHelper.getViewsFromViewGroup(view, HookedImageButton.class);
        if (hookedBtns != null) {
            for (HookedImageButton btn : hookedBtns) {
                btn.bindPlayer(getParent());
            }
        }

        View durationView = view.findViewById(R.id.exo_duration);
        if (durationView != null && durationView instanceof HookedDurationTextView) {
            HookedDurationTextView hookedDurationView = (HookedDurationTextView) durationView;
            hookedDurationView.bindPlayer(getParent());
        }

        View positionView = view.findViewById(R.id.exo_position);
        if (positionView != null && positionView instanceof HookedPositionTextView) {
            HookedPositionTextView hookedDurationView = (HookedPositionTextView) positionView;
            hookedDurationView.bindPlayer(getParent());
        }
    }
}
