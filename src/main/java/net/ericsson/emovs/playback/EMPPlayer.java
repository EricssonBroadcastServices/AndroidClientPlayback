package net.ericsson.emovs.playback;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.exposure.metadata.IMetadataCallback;
import net.ericsson.emovs.exposure.metadata.queries.EpgQueryParameters;
import net.ericsson.emovs.exposure.utils.MonotonicTimeService;
import net.ericsson.emovs.playback.services.ProgramService;
import net.ericsson.emovs.utilities.emp.UniversalPackagerHelper;
import net.ericsson.emovs.utilities.entitlements.EntitledRunnable;
import net.ericsson.emovs.utilities.entitlements.EntitlementCallback;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.errors.Warning;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayable;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.models.EmpAsset;
import net.ericsson.emovs.utilities.models.EmpChannel;
import net.ericsson.emovs.utilities.models.EmpOfflineAsset;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.errors.ErrorRunnable;
import net.ericsson.emovs.utilities.system.FileSerializer;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;
import net.ericsson.emovs.utilities.system.RunnableThread;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Joao Coelho on 2017-11-17.
 */

public class EMPPlayer extends Player implements IEntitledPlayer {
    protected IPlayable playable;
    protected Entitlement entitlement;
    protected IEntitlementProvider entitlementProvider;
    protected ProgramService programService;
    protected long lastPlayTimeMs;

    private EmptyPlaybackEventListener empPlaybackListener = new EmptyPlaybackEventListener(this) {
        @Override
        public void onPlaybackEnd() {
            disposeProgrameService();
        }

        @Override
        public void onDispose() {
            disposeProgrameService();
        }

        @Override
        public void onStop() {
            disposeProgrameService();
        }
    };

    /**
     * Constructor of a EMPPlayer instance - must be instanciated from a PlayerFactory
     *
     * @param analyticsConnector reference to an analytics connector instance
     * @param entitlementProvider reference to the entitlement provider
     * @param techFactory reference to a factory of specific techs that will play the media
     * @param context activity that holds the player
     * @param host reference to the ViewGroup that wraps the player (can have several players per activity)
     */
    public EMPPlayer(AnalyticsPlaybackConnector analyticsConnector, IEntitlementProvider entitlementProvider, TechFactory techFactory, Activity context, ViewGroup host) {
        super(analyticsConnector, techFactory, context, host);
        this.entitlementProvider = entitlementProvider;
    }

    /**
     * Plays some media available in EMP backend
     *
     * IPlayables supported:
     * - EmpChannel: default behaviour is to start playback from live edge
     * - EmpProgram: default behaviour is to start playback from beginning of program if program is NOT live, or start from live edge otherwise
     * - EmpAsset: default behaviour is to start playback from beginning of asset
     *
     * If playFrom property set to use Bookmark, then playback starts from Bookmarked position.
     * If Bookmark is not set, then default start procedure shall apply.
     *
     * @param playable the playable you want to play: asset, program or channel
     * @param properties playback properties, like autoplay, startTime, etc.. use PlaybackProperties.DEFAULT for default props
     */
    public void play(IPlayable playable, PlaybackProperties properties) {
        try {
            if (getServerTime() - lastPlayTimeMs < 1000L) {
                return;
            }
            lastPlayTimeMs = getServerTime();

            init(properties);

            super.onPlay();
            if (playable == null) {
                this.onError(ErrorCodes.PLAYBACK_INVALID_EMP_PLAYABLE, "");
                return;
            }
            if (playable instanceof EmpProgram) {
                this.playable = playable;
                EmpProgram playableProgram = (EmpProgram) playable;
                playProgram(playableProgram);
            }
            else if (playable instanceof EmpOfflineAsset) {
                this.playable = playable;
                EmpOfflineAsset offlineAsset = (EmpOfflineAsset) playable;
                playOffline(offlineAsset);
            }
            else if (playable instanceof EmpChannel) {
                this.playable = playable;
                EmpChannel channel = (EmpChannel) playable;
                playLive(channel);
            }
            else if (playable instanceof EmpAsset) {
                this.playable = playable;
                EmpAsset asset = (EmpAsset) playable;
                playVod(asset);
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

    /**
     * Returns the entitlement of a given playback session
     *
     * @return
     */
    public Entitlement getEntitlement() {
        return entitlement;
    }

    /**
     * Returns the playable from current playback session
     *
     * @return
     */
    public IPlayable getPlayable() {
        return this.playable;
    }

    /**
     * Returns current playback session ID
     *
     * @return
     */
    public String getSessionId() {
        if (entitlement == null) {
            return null;
        }
        if (playable != null && playable instanceof EmpOfflineAsset) {
            return "offline-" + playbackUUID.toString();
        }
        return entitlement.playSessionId;
    }

    @Override
    protected void setTimeshiftDelay(final long timeshift) {
        if (this.programService == null) {
            return;
        }
        long timeshiftUnixTime = MonotonicTimeService.getInstance().currentTime() - timeshift * 1000;
        this.programService.isEntitled (timeshiftUnixTime, new Runnable() {
            @Override
            public void run() {
                if (tech != null) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tech.setTimeshiftDelay(timeshift);
                        }
                    });
                }
            }
        }, new ErrorRunnable() {
            @Override
            public void run(final int errorCode, final String errorMessage) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fail(errorCode, errorMessage);
                        stop();
                    }
                });
            }
        });
    }

    @Override
    public EmpProgram getCurrentProgram() {
        if (this.programService != null) {
            return this.programService.getCurrentProgram();
        }
        return null;
    }

    @Override
    public void trigger(EventId eventId, Object param) {
        super.trigger(eventId, param);

        if (eventId == EventId.PROGRAM_CHANGED) {
            EmpProgram paramProgram = null;
            if (param instanceof EmpProgram) {
                paramProgram = (EmpProgram) param;
            }
            onProgramChange(paramProgram);
        }
        else if (eventId == EventId.WARNING) {
            Warning warningParam = null;
            if (param instanceof Warning) {
                warningParam = (Warning) param;
            }
            onWarning(warningParam.getCode(), warningParam.toString());
        }
    }

    @Override
    protected boolean init(PlaybackProperties _properties) throws Exception {
        super.init(_properties.clone());
        if (getEntitlementProvider() == null) {
            throw new Exception("Do not use default constructor on EMPPlayer.");
        }
        this.entitlement = null;
        this.playable = null;

        return true;
    }

    @Override
    public void seekTo(final long positionMs) {
        if (this.tech != null && this.isPlaying()) {
            long playheadPosition = getPlayheadPosition();
            if (positionMs > playheadPosition && this.entitlement.ffEnabled == false) {
                return;
            } else if (positionMs < playheadPosition && this.entitlement.rwEnabled == false) {
                return;
            }
            this.tech.seekTo(positionMs);
        }
    }

    /**
     * Seeks to a specific unix time (milliseconds)
     * Catchup-as-live Scenarios:
     * - If seek time is outside of the seek range, then the player does a entitlement call automatically
     * - SEEK TO LIVE EDGE: get wallclock time from getServerTime() and seek to that point in time
     * - START-OVER: get current program from getCurrentProgram() and then seek to the start time of that program
     * - JUMP 30 SECONDS: get current playback time from getPlayheadTime(), add/subtract the 30000, then seek to that point in time
     */
    @Override
    public void seekToTime(long _unixTimeMs) {
        if (this.tech != null) {
            if (this.isPlaying()) {
                long playheadTime = getPlayheadTime();
                if (_unixTimeMs > playheadTime && this.entitlement.ffEnabled == false) {
                    return;
                }
                else if (_unixTimeMs < playheadTime && this.entitlement.rwEnabled == false) {
                    return;
                }
            }

            long[] range = getSeekTimeRange();
            if (range != null && _unixTimeMs >= range[0] && _unixTimeMs <= range[1]) {
                this.tech.seekToTime(_unixTimeMs);
            }
            else if (this.entitlement != null && this.entitlement.channelId != null) {
                if (range != null) {
                    long[] rangeDiffs = { _unixTimeMs - range[0], range[1] - _unixTimeMs };
                    if (rangeDiffs[1] < 0 && UniversalPackagerHelper.isDynamicCatchup(this.entitlement.mediaLocator)) {
                        _unixTimeMs = range[1] - SAFETY_LIVE_DELAY;
                    }
                }
                final long unixTimeMs = _unixTimeMs;

                EpgQueryParameters epgParams = new EpgQueryParameters();
                epgParams.setFutureTimeFrame(0);
                epgParams.setPastTimeFrame(0);

                EMPMetadataProvider.getInstance().getEpgWithTime(this.entitlement.channelId, unixTimeMs, new IMetadataCallback<ArrayList<EmpProgram>>() {
                    @Override
                    public void onMetadata(ArrayList<EmpProgram> programs) {
                        try {
                            if (programs.size() > 0) {
                                final EmpProgram program = programs.get(0);
                                final PlaybackProperties newProps = properties.clone();
                                newProps.playFrom = new PlaybackProperties.PlayFrom.StartTime(unixTimeMs);
                                newProps.withAutoplay(isPaused() == false);
                                final HashMap<IPlaybackEventListener, IPlaybackEventListener> listeners = (HashMap<IPlaybackEventListener, IPlaybackEventListener>) eventListeners.clone();
                                programService.isEntitled (unixTimeMs, new Runnable() {
                                    @Override
                                    public void run() {
                                        if(isPlaying()) {
                                            stop();
                                        }
                                        play(program, newProps);
                                        for(IPlaybackEventListener listener : listeners.keySet()) {
                                            addListener(listener);
                                        }
                                    }
                                }, new ErrorRunnable() {
                                    @Override
                                    public void run(final int errorCode, final String errorMessage) {
                                        onWarning(Warning.SEEK_NOT_POSSIBLE.getCode(), Warning.SEEK_NOT_POSSIBLE.toString());
                                    }
                                });
                            }
                            else {
                                fail (ErrorCodes.PLAYBACK_PROGRAM_NOT_FOUND, Error.PROGRAM_NOT_FOUND.toString());
                            }
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(final Error error) {
                        fail (ErrorCodes.GENERIC_PLAYBACK_FAILED, error.toString());
                    }
                }, epgParams);
            }
        }
    }

    @Override
    public boolean canSeekForward() {
        if (this.entitlement != null) {
            return this.entitlement.ffEnabled;
        }
        return true;
    }

    @Override
    public boolean canSeekBack() {
        if (this.entitlement != null) {
            return this.entitlement.rwEnabled;
        }
        return true;
    }

    @Override
    public boolean canPause() {
        if (this.entitlement != null) {
            return this.entitlement.timeshiftEnabled;
        }
        return true;
    }

    @Override
    public void startOver() {
        if (this.tech == null || this.playable == null) {
            return;
        }
        IPlayable currentPlayable = null;
        if (this.programService != null) {
            currentPlayable = this.programService.getCurrentProgram();
        }
        if (currentPlayable == null) {
            currentPlayable = this.playable;
        }
        if (currentPlayable instanceof EmpProgram) {
            EmpProgram program = (EmpProgram) currentPlayable;
            if (program.startDateTime != null) {
                seekToTime(program.startDateTime.getMillis());
            }
            else {
                seekTo(0);
            }
        }
        else {
            seekTo(0);
        }
    }

    @Override
    public void seekToLive() {
        if (this.tech == null || this.entitlement == null || this.entitlement.channelId == null) {
            return;
        }
        long[] seekTimeRange = getSeekTimeRange();
        if (seekTimeRange == null) {
            return;
        }
        long nowMs = getServerTime();
        if (UniversalPackagerHelper.isStaticCatchup(this.entitlement.mediaLocator)) {
            EpgQueryParameters epgParams = new EpgQueryParameters();
            epgParams.setFutureTimeFrame(0);
            epgParams.setPastTimeFrame(0);
            epgParams.setPageSize(10);
            EMPMetadataProvider.getInstance().getEpgWithTime(this.entitlement.channelId, nowMs, new IMetadataCallback<ArrayList<EmpProgram>>() {
                @Override
                public void onMetadata(ArrayList<EmpProgram> programs) {
                    try {
                        if (programs.size() > 0) {
                            EmpProgram program = programs.get(0);
                            PlaybackProperties newProps = properties.clone();
                            newProps.playFrom = PlaybackProperties.PlayFrom.LIVE_EDGE;
                            newProps.withAutoplay(isPaused() == false);
                            HashMap<IPlaybackEventListener, IPlaybackEventListener> listeners = (HashMap<IPlaybackEventListener, IPlaybackEventListener>) eventListeners.clone();
                            play(program, newProps);
                            for(IPlaybackEventListener listener : listeners.keySet()) {
                                addListener(listener);
                            }
                        }
                        else {
                            fail (ErrorCodes.PLAYBACK_PROGRAM_NOT_FOUND, Error.PROGRAM_NOT_FOUND.toString());
                        }
                    }
                    catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(final Error error) {
                    fail (ErrorCodes.GENERIC_PLAYBACK_FAILED, error.toString());
                }
            }, epgParams);
        }
        else {
            seekToTime(Math.min(nowMs, seekTimeRange[1] - SAFETY_LIVE_DELAY));
        }
    }


    private void prepareBookmark(IPlayable playable, Entitlement entitlement) {
        if (this.properties != null &&
                this.properties.getPlayFrom() != null &&
                this.properties.getPlayFrom() instanceof PlaybackProperties.PlayFrom.Bookmark) {
            if (entitlement.lastViewedOffset != null && entitlement.lastViewedTime != null) {
                if (UniversalPackagerHelper.isUniversalPackager(entitlement.mediaLocator)) {
                    ((PlaybackProperties.PlayFrom.StartTime) this.properties.getPlayFrom()).startTime = entitlement.liveTime;
                }
                else {
                    ((PlaybackProperties.PlayFrom.StartTime) this.properties.getPlayFrom()).startTime = entitlement.lastViewedOffset;
                }
            }
            else {
                this.properties.withPlayFrom(null);
            }
        }
    }

    private void preparePlayback(String mediaId, final Entitlement entitlement) {
        if (empPlaybackListener != null) {
            addListener(empPlaybackListener);
        }

        this.entitlement = entitlement;
        this.onEntitlementChange();

        if (this.properties != null && entitlement != null) {
            if (entitlement.licenseServerUrl != null) {
                PlaybackProperties.DRMProperties drmProps = new PlaybackProperties.DRMProperties();
                drmProps.licenseServerUrl = entitlement.licenseServerUrl;
                drmProps.initDataBase64 = entitlement.drmInitDataBase64;
                this.properties.withDRMProperties(drmProps);
            }
            else {
                this.properties.withDRMProperties(null);
            }

            if (entitlement.maxBitrate != null &&
                    (this.properties.getMaxBitrate() == null || entitlement.maxBitrate < this.properties.getMaxBitrate())) {
                this.properties.withMaxBitrate(entitlement.maxBitrate);
            }
        }

        // TODO: remove hack that is only for test purposes - this should be done on the backend
        /*if (entitlement.programId != null && dvrWindow > 0) {
            String dvrWindowOldValue = Uri.parse(entitlement.mediaLocator).getQueryParameter("dvr_window_length");
            String timeshiftOld = Uri.parse(entitlement.mediaLocator).getQueryParameter("time_shift");
            entitlement.mediaLocator = entitlement.mediaLocator
                    .replace("dvr_window_length=" + dvrWindowOldValue, "dvr_window_length=" + Long.toString(dvrWindow))
                    .replace("time_shift=" + timeshiftOld, "time_shift=" + Long.toString(ts));
            if (timeshiftOld == null) {
                entitlement.mediaLocator += "&time_shift=" + Long.toString(ts);
            }
        }*/
        //entitlement.mediaLocator = "https://nl-hvs-dev-cache2.cdn.ebsd.ericsson.net/L24/nautical/nautical.isml/live.mpd?t=2018-01-17T13%3A30%3A00.000-2018-01-17T14%3A00%3A00.000";
        //entitlement.mediaLocator = "https://nl-hvs-dev-cache2.cdn.ebsd.ericsson.net/L24/cgi-bin/proxy.py?t=2018-01-25T17:00:00.000";


        Log.d("EMP MEDIA LOCATOR", entitlement.mediaLocator);
        tech.init(this, context, entitlement.playToken, this.properties);
        tech.load(mediaId, entitlement.mediaLocator, false);
        context.runOnUiThread(new Runnable() {
            public void run() {
                if (tech != null) {
                    tech.play(entitlement.mediaLocator);
                }
            }
        });
    }

    private void prepareProgramService() {
        disposeProgrameService();
        this.programService = new ProgramService(this, getEntitlement());
        this.programService.start();
    }

    private void disposeProgrameService() {
        if (this.programService != null && this.programService.isAlive() && this.programService.isInterrupted() == false) {
            this.programService.interrupt();
        }
    }

    private ErrorRunnable getErrorRunnable() {
        final ErrorRunnable onErrorRunnable = new ErrorRunnable() {
            @Override
            public void run(int errorCode, String errorMessage) {
                onError(errorCode, errorMessage);
            }
        };
        return onErrorRunnable;
    }

    private void playLive(final EmpChannel channel) {
        final EntitledRunnable onEntitlementRunnable = new EntitledRunnable() {
            @Override
            public void run() {
                if (PlaybackProperties.PlayFrom.isBookmark(properties.getPlayFrom())) {
                    prepareBookmark(channel, entitlement);
                }

                if (PlaybackProperties.PlayFrom.isBeginning(properties.getPlayFrom())) {
                    if (channel.programs != null) {
                        boolean foundLive = false;
                        for (EmpProgram program : channel.programs) {
                            if (program.liveNow()) {
                                PlaybackProperties.PlayFrom.Beginning beginningOpt = new PlaybackProperties.PlayFrom.Beginning();
                                beginningOpt.withStartTime(program.startDateTime.getMillis());
                                properties.withPlayFrom(beginningOpt);
                                foundLive = true;
                                break;
                            }
                        }
                        if (!foundLive) {
                            properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                        }
                    }
                    else {
                        EpgQueryParameters epgParams = new EpgQueryParameters();
                        epgParams.setFutureTimeFrame(0);
                        epgParams.setPastTimeFrame(0);
                        epgParams.setPageSize(10);
                        EMPMetadataProvider.getInstance().getEpgWithTime(this.entitlement.channelId, getServerTime(), new IMetadataCallback<ArrayList<EmpProgram>>() {
                            @Override
                            public void onMetadata(ArrayList<EmpProgram> programs) {
                                if (programs.size() > 0) {
                                    EmpProgram program = programs.get(0);
                                    PlaybackProperties.PlayFrom.Beginning beginningOpt = new PlaybackProperties.PlayFrom.Beginning();
                                    beginningOpt.withStartTime(program.startDateTime.getMillis());
                                    properties.withPlayFrom(beginningOpt);
                                }
                                else {
                                    properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                                }

                                preparePlayback(entitlement.channelId, entitlement);
                                prepareProgramService();
                            }

                            @Override
                            public void onError(final Error error) {
                                properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                                preparePlayback(entitlement.channelId, entitlement);
                                prepareProgramService();
                            }
                        }, epgParams);
                        return;
                    }
                }

                if (properties.getPlayFrom() == null) {
                    properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                }

                preparePlayback(entitlement.channelId, entitlement);
                prepareProgramService();
            }
        };
        super.onEntitlementLoadStart();
        getEntitlementProvider().playLive(channel.channelId, new EntitlementCallback(null, channel.channelId, null, onEntitlementRunnable, getErrorRunnable()));
    }

    private void playProgram(final EmpProgram program) {
        final EntitledRunnable onEntitlementRunnable = new EntitledRunnable() {
            @Override
            public void run() {
                if (PlaybackProperties.PlayFrom.isBookmark(properties.getPlayFrom())) {
                    prepareBookmark(program, entitlement);
                }

                if (PlaybackProperties.PlayFrom.isBeginning(properties.getPlayFrom()) ||
                    PlaybackProperties.PlayFrom.isLiveEdge(properties.getPlayFrom()) ||
                    properties.getPlayFrom() == null) {
                    if (program.startDateTime != null && program.endDateTime != null) {
                        if (program.liveNow() && properties.getPlayFrom() == null) {
                            properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                        }
                        if (!program.liveNow() && properties.getPlayFrom() == null) {
                            properties.withPlayFrom(PlaybackProperties.PlayFrom.BEGINNING);
                        }
                        if (PlaybackProperties.PlayFrom.isBeginning(properties.getPlayFrom())) {
                            ((PlaybackProperties.PlayFrom.StartTime) properties.getPlayFrom()).startTime = program.startDateTime.getMillis();
                        }
                        preparePlayback(entitlement.programId, entitlement);
                        prepareProgramService();
                    }
                    else {
                        EMPMetadataProvider.getInstance().getProgramDetails(program.channelId, program.programId, new IMetadataCallback<EmpProgram>() {
                            @Override
                            public void onMetadata(EmpProgram fullProgram) {
                                if (fullProgram.liveNow() && properties.getPlayFrom() == null) {
                                    properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                                }
                                if (!fullProgram.liveNow() && properties.getPlayFrom() == null) {
                                    properties.withPlayFrom(PlaybackProperties.PlayFrom.BEGINNING);
                                }
                                if (PlaybackProperties.PlayFrom.isBeginning(properties.getPlayFrom())) {
                                    ((PlaybackProperties.PlayFrom.StartTime) properties.getPlayFrom()).startTime = fullProgram.startDateTime.getMillis();
                                }
                                preparePlayback(entitlement.programId, entitlement);
                                prepareProgramService();
                            }

                            @Override
                            public void onError(Error error) {
                                properties.withPlayFrom(null);
                                preparePlayback(entitlement.programId, entitlement);
                                prepareProgramService();
                            }
                        });
                    }
                }
                else {
                    preparePlayback(entitlement.programId, entitlement);
                    prepareProgramService();
                }
            }
        };
        super.onEntitlementLoadStart();
        getEntitlementProvider().playCatchup(program.channelId, program.programId, new EntitlementCallback(null, program.channelId, program.programId, onEntitlementRunnable, getErrorRunnable()));
    }

    private void playVod(final EmpAsset asset) {
        final EntitledRunnable onEntitlementRunnable = new EntitledRunnable() {
            @Override
            public void run() {
                prepareBookmark(asset, entitlement);
                preparePlayback(entitlement.assetId, entitlement);
            }
        };
        super.onEntitlementLoadStart();
        getEntitlementProvider().playVod(asset.assetId, new EntitlementCallback(asset.assetId, null, null, onEntitlementRunnable, getErrorRunnable()));
    }

    private boolean playOffline(final EmpOfflineAsset offlineAsset) {
        // TODO: missing eventListeners.onEntitlementLoadStart();
        final EMPPlayer self = this;
        new RunnableThread(new Runnable() {
            @Override
            public void run() {
                final String manifestPath = offlineAsset.localMediaPath;
                File manifestUrl = new File(manifestPath);
                File manifestFolder = manifestUrl.getParentFile();
                File entitlementFile = new File(manifestFolder, "entitlement.ser");
                Entitlement entitlement = new Entitlement();
                self.entitlement = FileSerializer.readJson(entitlement, entitlementFile.getAbsolutePath());
                self.playbackUUID = UUID.randomUUID();
                self.onEntitlementChange();
                Log.d("EMP MEDIA LOCATOR", manifestPath);
                tech.init(self, context, self.entitlement.playToken, self.properties);
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

}
