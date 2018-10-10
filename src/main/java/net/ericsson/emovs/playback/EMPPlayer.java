package net.ericsson.emovs.playback;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import net.ericsson.emovs.playback.services.ProgramService;
import net.ericsson.emovs.utilities.emp.EMPRegistry;
import net.ericsson.emovs.utilities.emp.UniversalPackagerHelper;
import net.ericsson.emovs.utilities.entitlements.EntitledRunnable;
import net.ericsson.emovs.utilities.entitlements.EntitlementCallback;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.errors.Warning;
import net.ericsson.emovs.utilities.errors.WarningCodes;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IMetadataCallback;
import net.ericsson.emovs.utilities.interfaces.IMetadataProvider;
import net.ericsson.emovs.utilities.interfaces.IMonotonicTimeService;
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
import net.ericsson.emovs.utilities.queries.EpgQueryParameters;
import net.ericsson.emovs.utilities.system.FileSerializer;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;
import net.ericsson.emovs.utilities.system.RunnableThread;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static net.ericsson.emovs.utilities.errors.Warning.SEEK_TO_UNAVAILABLE_POSITION;

/**
 * Player with EMP business logic
 */
public class EMPPlayer extends Player implements IEntitledPlayer {
    protected int START_OVER_SAFETIME_OFFSET_MS = 150;
    protected IPlayable playable;
    protected Entitlement entitlement;
    protected IEntitlementProvider entitlementProvider;
    protected IMetadataProvider metadataProvider;
    protected ProgramService programService;
    protected long lastSeekToTimeMs;

    protected static long lastPlayTimeMs = 0L;

    private final String TAG = this.getClass().getSimpleName();

    private final long BLOCKING_TIMER_DURATION = 3000L;

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
     * @param metadataProvider reference to the metadata provider that provides EPG functionality
     * @param monotonicTimeService reference to a monotonic time provider interface
     */
    public EMPPlayer(AnalyticsPlaybackConnector analyticsConnector, IEntitlementProvider entitlementProvider, TechFactory techFactory, Activity context, ViewGroup host, IMetadataProvider metadataProvider, IMonotonicTimeService monotonicTimeService) {
        super(analyticsConnector, techFactory, context, host, monotonicTimeService);
        this.entitlementProvider = entitlementProvider;
        this.metadataProvider = metadataProvider;
    }

    /**
     * <p>
     *     Plays some media available in EMP backend (if throttling is enabled, the start can be blocked)
     * </p>
     *  <p>
     *      IPlayables supported:
     *  </p>
     *  <p>
     *    <ul>
     *        <li>
 *              EmpChannel: default behaviour is to start playback from live edge
     *        </li>
     *        <li>
 *              EmpProgram: default behaviour is to start playback from beginning of program if program is NOT live, or start from live edge otherwise
     *        </li>
     *        <li>
 *              EmpAsset: default behaviour is to start playback from beginning of asset
     *        </li>
     *    </ul>
     *  </p>
     *  <p>
     *      If playFrom property set to use Bookmark, then playback starts from Bookmarked position.
     *  </p>
     *  <p>
     *      If Bookmark is not set, then default start procedure shall apply.
     *  </p>
     * @param playable the playable you want to play: asset, program or channel
     * @param properties playback properties, like autoplay, startTime, etc.. use PlaybackProperties.DEFAULT for default props
     */
    public void play(IPlayable playable, PlaybackProperties properties) {
        try {
            long currentServerTimeMs = getServerTime();
            long elapsedTime = currentServerTimeMs - lastPlayTimeMs;

            lastPlayTimeMs = currentServerTimeMs;

            if (elapsedTime < 1000L) {
                return;
            } else if (EMPRegistry.playbackThrottlingEnabled()
                       && (elapsedTime < BLOCKING_TIMER_DURATION)) {
                onWarning(Warning.PLAYBACK_START_BLOCKED);

                return;
            }

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
     * @return the entitlement of a given playback session
     */
    public Entitlement getEntitlement() {
        return entitlement;
    }

    /**
     * @return the playable from current playback session
     */
    public IPlayable getPlayable() {
        return this.playable;
    }

    /**
     * @return current playback session ID
     */
    @Override
    public String getSessionId() {
        if (entitlement == null) {
            return null;
        }
        if (playable != null && playable instanceof EmpOfflineAsset) {
            return "offline-" + playbackUUID.toString();
        }
        return entitlement.playSessionId;
    }

    /**
     * Set timeshift delay for a stream (requires reloading the asset)
     * @param timeshift sets timeshift delay (in seconds) from live point
     */
    @Override
    protected void setTimeshiftDelay(final long timeshift) {
        if (this.programService == null) {
            return;
        }
        long timeshiftUnixTime = getMonotonicTimeService().currentTime() - timeshift * 1000;
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
        }, false);
    }


    private void onWarning(Warning warning) {
        onWarning(warning.getCode(), warning.toString());
    }

    /**
     * @return EmpProgram currently playing if stream has EPG and is Live or Catchup-as-Live. null is returned otherwise.
     */
    @Override
    public EmpProgram getCurrentProgram() {
        if (this.programService != null) {
            EmpProgram psProgram = this.programService.getCurrentProgram();
            if (psProgram != null){
                return psProgram;
            }
        }
        return null;
    }

    /**
     * Triggers an event from an external source
     * @param eventId event type to be triggered
     * @param param depending on event type: EmpProgram for ProgramChanged type, Warning for Warning type
     */
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

    /**
     * Initializes the playback resources (this method should not be publicly accessible)
     * @param _properties playbacks specific properties
     */
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

    /**
     * Seeks to a specific stream offset (range: [0..duration]
     * @param positionMs offset in milliseconds
     */
    @Override
    public void seekTo(final long positionMs) {
        if (getServerTime() - lastSeekToTimeMs < 500L) {
            return;
        }
        lastSeekToTimeMs = getServerTime();
        if (this.tech != null && this.isPlaying()) {
            long playheadPosition = getPlayheadPosition();
            if (positionMs > playheadPosition && this.entitlement.ffEnabled == false) {
                return;
            } else if (positionMs < playheadPosition && this.entitlement.rwEnabled == false) {
                return;
            }
            long[] range = getSeekTimeRange();
            if (range == null || range.length != 2 || this.programService == null) {
                this.tech.seekTo(positionMs);
            }
            else {
                long unixTimeToCheck = range[0] + positionMs;
                programService.isEntitled (unixTimeToCheck, new Runnable() {
                    @Override
                    public void run() {
                        tech.seekTo(positionMs);
                    }
                }, new ErrorRunnable() {
                    @Override
                    public void run(final int errorCode, final String errorMessage) {
                        onWarning(Warning.SEEK_NOT_POSSIBLE.getCode(), Warning.SEEK_NOT_POSSIBLE.toString());
                    }
                }, true);
            }
        }
    }

    /**
     * <p>
     *     Seeks to a specific unix time (milliseconds)
     * </p>
     * <p>
     *      Catchup-as-live Scenarios:
     * </p>
     * <p>
     *    <ul>
     *        <li>
     *          If seek time is outside of the seek range, then the player does a entitlement call automatically
     *        </li>
     *        <li>
     *          SEEK TO LIVE EDGE: get wallclock time from getServerTime() and seek to that point in time
     *        </li>
     *        <li>
     *          START-OVER: get current program from getCurrentProgram() and then seek to the start time of that program
     *        </li>
     *        <li>
     *          JUMP 30 SECONDS: get current playback time from getPlayheadTime(), add/subtract the 30000, then seek to that point in time
     *        </li>
     *    </ul>
     * </p>
     * @param _unixTimeMs unix time to seek to in milliseconds
     */
    @Override
    public void seekToTime(long _unixTimeMs) {
        this.seekToTimeInternal(_unixTimeMs, false);
    }

    protected void seekToTimeInternal(long _unixTimeMs, boolean avoidContract) {
        if (this.tech != null) {
            if (avoidContract == false && this.isPlaying()) {
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
                final long seekToTime = _unixTimeMs;
                if (programService == null) {
                    tech.seekToTime(seekToTime);
                }
                else {
                    programService.isEntitled (_unixTimeMs, new Runnable() {
                        @Override
                        public void run() {
                            tech.seekToTime(seekToTime);
                        }
                    }, new ErrorRunnable() {
                        @Override
                        public void run(final int errorCode, final String errorMessage) {
                            onWarning(Warning.SEEK_NOT_POSSIBLE.getCode(), Warning.SEEK_NOT_POSSIBLE.toString());
                        }
                    }, true);
                }
            }
            else if (this.entitlement != null && this.entitlement.channelId != null) {
                if (range != null) {
                    long[] rangeDiffs = { _unixTimeMs - range[0], range[1] - _unixTimeMs };
                    if (rangeDiffs[1] < 0 && UniversalPackagerHelper.isDynamicCatchup(this.entitlement.mediaLocator)) {
                        trigger(EventId.WARNING, SEEK_TO_UNAVAILABLE_POSITION);
                        return;
                    }
                }
                if (_unixTimeMs >= getMonotonicTimeService().currentTime()) {
                    trigger(EventId.WARNING, SEEK_TO_UNAVAILABLE_POSITION);
                    return;
                }
                final long unixTimeMs = _unixTimeMs;

                EpgQueryParameters epgParams = new EpgQueryParameters();
                epgParams.setFutureTimeFrame(0);
                epgParams.setPastTimeFrame(0);

                getMetadataProvider().getEpgWithTime(this.entitlement.channelId, unixTimeMs, new IMetadataCallback<ArrayList<EmpProgram>>() {
                    @Override
                    public void onMetadata(ArrayList<EmpProgram> programs) {
                        try {
                            if (programs.size() > 0) {
                                final EmpProgram program = programs.get(0);
                                final PlaybackProperties newProps = properties.clone();
                                newProps.playFrom = new PlaybackProperties.PlayFrom.StartTime(unixTimeMs);
                                newProps.withAutoplay(isPaused() == false);
                                final HashMap<IPlaybackEventListener, IPlaybackEventListener> listeners = cloneEventListeners();
                                programService.isEntitled (unixTimeMs, new Runnable() {
                                    @Override
                                    public void run() {
                                        if(isPlaying()) {
                                            stop();
                                        }
                                        play(program, newProps);
                                        for(IPlaybackEventListener listener : listeners.keySet()) {
                                            if (listener instanceof AnalyticsHolder) {
                                                continue;
                                            }
                                            addListener(listener);
                                        }
                                    }
                                }, new ErrorRunnable() {
                                    @Override
                                    public void run(final int errorCode, final String errorMessage) {
                                        onWarning(Warning.SEEK_NOT_POSSIBLE.getCode(), Warning.SEEK_NOT_POSSIBLE.toString());
                                    }
                                }, true);
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

    /**
     * Use this method to check if stream can be seeked forward
     */
    @Override
    public boolean canSeekForward() {
        if (this.entitlement != null) {
            return this.entitlement.ffEnabled;
        }
        return true;
    }

    /**
     * Use this method to check if stream can be seeked backwards
     */
    @Override
    public boolean canSeekBack() {
        if (this.entitlement != null) {
            return this.entitlement.rwEnabled;
        }
        return true;
    }

    /**
     * Use this method to check if stream can be paused
     */
    @Override
    public boolean canPause() {
        if (this.entitlement != null) {
            return this.entitlement.timeshiftEnabled;
        }
        return true;
    }

    /**
     * Use this method to start playback from start (0 for VOD or Program Start Time for Catchup
     */
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
                seekToTime(program.startDateTime.getMillis() + START_OVER_SAFETIME_OFFSET_MS);
            }
            else {
                seekTo(0);
            }
        }
        else {
            seekTo(0);
        }
    }

    /**
     * Use this method to seek to live edge (a reload is made if live edge belongs to another program)
     */
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
            getMetadataProvider().getEpgWithTime(this.entitlement.channelId, nowMs, new IMetadataCallback<ArrayList<EmpProgram>>() {
                @Override
                public void onMetadata(ArrayList<EmpProgram> programs) {
                    try {
                        if (programs.size() > 0) {
                            EmpProgram program = programs.get(0);
                            PlaybackProperties newProps = properties.clone();
                            newProps.playFrom = PlaybackProperties.PlayFrom.LIVE_EDGE;
                            newProps.withAutoplay(isPaused() == false);
                            HashMap<IPlaybackEventListener, IPlaybackEventListener> listeners = cloneEventListeners();
                            play(program, newProps);
                            for(IPlaybackEventListener listener : listeners.keySet()) {
                                if (listener instanceof AnalyticsHolder) {
                                    continue;
                                }
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
            seekToTimeInternal(Math.min(nowMs, seekTimeRange[1] - SAFETY_LIVE_DELAY), true);
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

    private boolean preparePlayback(String mediaId, final Entitlement entitlement) {
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

        Log.d("EMP MEDIA LOCATOR", entitlement.mediaLocator);

        if (this.tech != null) {
            tech.init(this, context, entitlement.playToken, this.properties);
            tech.load(mediaId, entitlement.mediaLocator, false);
            return true;
        }

        return false;
    }

    private void prepareProgramService(EmpProgram program) {
        disposeProgrameService();
        this.programService = new ProgramService(this, getEntitlement(), program);
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
                        getMetadataProvider().getEpgWithTime(this.entitlement.channelId, getServerTime(), new IMetadataCallback<ArrayList<EmpProgram>>() {
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

                                boolean ret = preparePlayback(entitlement.channelId, entitlement);
                                if (ret) prepareProgramService(programs.size() > 0 ? programs.get(0) : null);
                            }

                            @Override
                            public void onError(final Error error) {
                                properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                                boolean ret = preparePlayback(entitlement.channelId, entitlement);
                                if (ret) prepareProgramService(null);
                            }
                        }, epgParams);
                        return;
                    }
                }

                if (properties.getPlayFrom() == null) {
                    properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                }

                boolean ret = preparePlayback(entitlement.channelId, entitlement);
                if (ret) prepareProgramService(null);
            }
        };
        super.onEntitlementLoadStart();
        getEntitlementProvider().playLive(channel.channelId, new EntitlementCallback(null, channel.channelId, null, onEntitlementRunnable, getErrorRunnable()));
    }

    private void playProgram(final EmpProgram program) {
        final EntitledRunnable onEntitlementRunnable = new EntitledRunnable() {
            @Override
            public void run() {
                if (PlaybackProperties.PlayFrom.isStartTime(properties.getPlayFrom()) && program.startDateTime != null && program.endDateTime != null) {
                    long startTime = ((PlaybackProperties.PlayFrom.StartTime) properties.getPlayFrom()).startTime;
                    if (startTime < program.startDateTime.getMillis() || startTime > program.endDateTime.getMillis()) {
                        trigger(EventId.WARNING, Warning.INVALID_START_TIME);
                        if (program.liveNow()) {
                            properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                        }
                        else {
                            properties.withPlayFrom(PlaybackProperties.PlayFrom.BEGINNING);
                        }
                    }
                }

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
                        boolean ret = preparePlayback(entitlement.programId, entitlement);
                        if (ret) {
                            prepareProgramService(program);
                        }
                    }
                    else {
                        getMetadataProvider().getProgramDetails(program.channelId, program.programId, new IMetadataCallback<EmpProgram>() {
                            @Override
                            public void onMetadata(EmpProgram fullProgram) {
                                if (fullProgram == null) {
                                    return;
                                }
                                if (fullProgram.liveNow() && properties.getPlayFrom() == null) {
                                    properties.withPlayFrom(PlaybackProperties.PlayFrom.LIVE_EDGE);
                                }
                                if (!fullProgram.liveNow() && properties.getPlayFrom() == null) {
                                    properties.withPlayFrom(PlaybackProperties.PlayFrom.BEGINNING);
                                }
                                if (PlaybackProperties.PlayFrom.isBeginning(properties.getPlayFrom())) {
                                    ((PlaybackProperties.PlayFrom.StartTime) properties.getPlayFrom()).startTime = fullProgram.startDateTime.getMillis();
                                }
                                boolean ret = preparePlayback(entitlement.programId, entitlement);
                                if (ret) prepareProgramService(fullProgram);
                            }

                            @Override
                            public void onError(Error error) {
                                properties.withPlayFrom(null);
                                boolean ret = preparePlayback(entitlement.programId, entitlement);
                                if (ret) prepareProgramService(program);
                            }
                        });
                    }
                }
                else {
                    boolean ret = preparePlayback(entitlement.programId, entitlement);
                    if (ret) prepareProgramService(program);
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
                if (tech != null) {
                    tech.init(self, context, self.entitlement.playToken, self.properties);
                    tech.load(self.entitlement.assetId, manifestPath, true);
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            tech.play(manifestPath);
                        }
                    });
                }
            }
        }).start();
        return true;
    }

    public IEntitlementProvider getEntitlementProvider() {
        return entitlementProvider;
    }

    public IMetadataProvider getMetadataProvider() {
        return metadataProvider;
    }
}
