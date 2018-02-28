package net.ericsson.emovs.playback.services;

import android.util.Log;

import net.ericsson.emovs.utilities.errors.Warning;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IMetadataCallback;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.errors.ErrorRunnable;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.queries.EpgQueryParameters;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Random;

/**
 * <p>
 *     ProgramService should act on these scenarios:
 * </p>
 * <p>
 *     <ul>
 *         <li>Regular entitlement checks when live stream is playing</li>
 *         <li>Entitlement check when timeshiftDelay is changed</li>
 *         <li>FUZZY_ENTITLEMENT_MAX_DELAY can be set to tune a random fuzzy wait period between program change and entitlement check (useful to reduce load on server)</li>
 *     </ul>
 * </p>
 */
public class ProgramService extends Thread {
    private static final String TAG = ProgramService.class.toString();
    public static final int LONG_WAIT_TIME = 1000;
    public static final int SHORT_WAIT_TIME = 1000;
    public static final int EPG_GAP_WAIT_TIME = 30000;
    private static int FUZZY_ENTITLEMENT_MIN_MAX_DELAY = 30000;
    public static int FUZZY_ENTITLEMENT_MAX_DELAY = FUZZY_ENTITLEMENT_MIN_MAX_DELAY;

    protected Entitlement entitlement;
    protected IEntitledPlayer player;
    protected EmpProgram currentProgram;
    protected Random randomizer = new Random(System.currentTimeMillis());
    protected EntitlementCheckCache eeCache;

    public ProgramService(IEntitledPlayer player, Entitlement entitlement, EmpProgram initialProgram) {
        this.player = player;
        this.entitlement = entitlement;
        if (initialProgram != null && initialProgram.startDateTime != null && initialProgram.endDateTime != null) {
            this.currentProgram = initialProgram;
        }
        eeCache = new EntitlementCheckCache();
    }

    public EmpProgram getCurrentProgram() {
        return this.currentProgram;
    }

    public void isEntitled(final long timeToCheck, final Runnable onAllowed, final ErrorRunnable onForbidden, boolean updateCurrentProgram) {
        if (currentProgram == null) {
            checkTimeshiftAllowance(timeToCheck, onAllowed, onForbidden, updateCurrentProgram, false);
        }
        else {
            Duration dStart = new Duration(currentProgram.startDateTime, new DateTime(timeToCheck));
            Duration dEnd = new Duration(currentProgram.endDateTime, new DateTime(timeToCheck));
            if (dStart.getMillis() >= 0 && dEnd.getMillis() <= 0) {
                if (onAllowed != null) {
                    onAllowed.run();
                }
            }
            else {
                checkTimeshiftAllowance(timeToCheck, onAllowed, onForbidden, updateCurrentProgram, false);
            }
        }
    }

    public void checkTimeshiftAllowance(final long timeToCheck, final Runnable onAllowed, final ErrorRunnable onForbidden, final boolean updateProgram, final boolean shouldCacheOutcome) {
        // Check if entitlement check was done already and cached
        Boolean isAllowed = eeCache.isTimeAllowed(timeToCheck);
        if (isAllowed != null) {
            if (isAllowed == true) {
                if (onAllowed != null) {
                    onAllowed.run();
                }
                if (updateProgram) {
                    if (shouldCacheOutcome) {
                        // Should not happen!
                    }
                    else {
                        if (player != null && eeCache.getProgram() != null) {
                            player.trigger(IPlaybackEventListener.EventId.PROGRAM_CHANGED, eeCache.getProgram());
                        }
                        currentProgram = eeCache.getProgram();
                    }
                }
                if (eeCache.hasGapInEpg()) {
                    player.trigger(IPlaybackEventListener.EventId.WARNING, Warning.PROGRAM_SERVICE_GAPS_IN_EPG_OR_NO_EPG);
                    currentProgram = null;
                }
                if (eeCache.isCheckNotPossible()) {
                    player.trigger(IPlaybackEventListener.EventId.WARNING, Warning.PROGRAM_SERVICE_ENTITLEMENT_CHECK_NOT_POSSIBLE);
                }
            }
            else if (onForbidden != null) {
                onForbidden.run(ErrorCodes.PLAYBACK_NOT_ENTITLED, "USER_NOT_ENTITLED");
            }
            eeCache.clear();
            return;
        }
        EpgQueryParameters epgParams = new EpgQueryParameters();
        epgParams.setFutureTimeFrame(0);
        epgParams.setPastTimeFrame(0);
        epgParams.setPageSize(5);

        if (currentProgram != null) {
            Duration dStart = new Duration(currentProgram.startDateTime, new DateTime(timeToCheck));
            Duration dEnd = new Duration(currentProgram.endDateTime, new DateTime(timeToCheck));

            if (dStart.getMillis() >= 0 && dEnd.getMillis() <= 0) {
                if (onAllowed != null) {
                    onAllowed.run();
                }
                return;
            }
        }

        player.getMetadataProvider().getEpgWithTime(this.entitlement.channelId, timeToCheck, new IMetadataCallback<ArrayList<EmpProgram>>() {
            @Override
            public void onMetadata(ArrayList<EmpProgram> programs) {
                if(programs != null) {
                    for (final EmpProgram program : programs) {
                        // Ignoring programs that are almost ending
                        if (programs.size() > 1 && timeToCheck - program.endDateTime.getMillis() > -1000L) {
                            continue;
                        }
                        if (updateProgram == false || currentProgram == null || program.assetId.equals(currentProgram.assetId) == false) {
                            player.getEntitlementProvider().isEntitledAsync(program.assetId, new Runnable() {
                                @Override
                                public void run() {
                                    if (onAllowed != null) {
                                        onAllowed.run();
                                    }

                                    if (updateProgram) {
                                        if (shouldCacheOutcome) {
                                            eeCache.registerProgramChanged(program);
                                        }
                                        else {
                                            if (player != null && currentProgram != null) {
                                                player.trigger(IPlaybackEventListener.EventId.PROGRAM_CHANGED, program);
                                            }
                                            currentProgram = program;
                                        }
                                    }
                                }
                            }, onForbidden);
                        }
                        else if (onAllowed != null) {
                            onAllowed.run();
                        }
                        return;
                    }
                }
                if (programs == null || programs.size() == 0) {
                    if (shouldCacheOutcome) {
                        eeCache.registerGapInEpg();
                    }
                    else {
                        player.trigger(IPlaybackEventListener.EventId.WARNING, Warning.PROGRAM_SERVICE_GAPS_IN_EPG_OR_NO_EPG);
                        currentProgram = null;
                    }
                }
                if (onAllowed != null) {
                    onAllowed.run();
                }
            }

            @Override
            public void onError(final Error error) {
                if (onAllowed != null) {
                    onAllowed.run();
                }
                if (player != null) {
                    if (shouldCacheOutcome) {
                        eeCache.registerCheckNotPossible();
                    }
                    else {
                        player.trigger(IPlaybackEventListener.EventId.WARNING, Warning.PROGRAM_SERVICE_ENTITLEMENT_CHECK_NOT_POSSIBLE);
                    }
                }
            }
        }, epgParams);
    }

    public void run () {
        boolean firstCycle = true;
        for(;;) {
            try {
                int fuzzySleep = 0;
                if (FUZZY_ENTITLEMENT_MAX_DELAY > 0) {
                    fuzzySleep = randomizer.nextInt(FUZZY_ENTITLEMENT_MAX_DELAY);
                }

                if (this.player == null || this.entitlement == null || this.entitlement.channelId == null) {
                    return;
                }

                if (this.entitlement.isUnifiedStream == false) {
                    return;
                }

                if (this.player.isPlaying()) {
                    // If gap in EPG then wait a bit longer
                    if (firstCycle == false && this.currentProgram == null) {
                        Thread.sleep(EPG_GAP_WAIT_TIME);
                    }
                    firstCycle = false;

                    final long playheadTime = this.player.getPlayheadTime();
                    Log.d("PlaybackCurrentTime", Long.toString(playheadTime));
                    checkTimeshiftAllowance(playheadTime, null, new ErrorRunnable() {
                        @Override
                        public void run(int code, final String message) {
                            player.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentProgram = null;
                                    player.fail(ErrorCodes.PLAYBACK_NOT_ENTITLED, message);
                                    player.stop();
                                }
                            });
                        }
                    }, true, false);
                    if (FUZZY_ENTITLEMENT_MAX_DELAY > 0 &&
                            this.currentProgram != null &&
                            this.currentProgram.endDateTime != null) {
                        final long futureTimeCheck = this.currentProgram.endDateTime.getMillis() + 1;
                        long timeToEnd = futureTimeCheck - playheadTime;
                        if (timeToEnd >= 0 && timeToEnd <= fuzzySleep) {
                            checkTimeshiftAllowance(futureTimeCheck, new Runnable() {
                                @Override
                                public void run() {
                                    eeCache.register(playheadTime, futureTimeCheck, true);
                                }
                            }, new ErrorRunnable() {
                                @Override
                                public void run(int code, final String message) {
                                    eeCache.register(playheadTime, futureTimeCheck, false);
                                }
                            }, true, true);
                            Thread.sleep(timeToEnd);
                            fuzzySleep = randomizer.nextInt(FUZZY_ENTITLEMENT_MAX_DELAY);
                        }
                    }
                    Thread.sleep(LONG_WAIT_TIME);
                }
                else {
                    Thread.sleep(SHORT_WAIT_TIME);
                }
            }
            catch (InterruptedException e) {
                Log.d(TAG, "Program service interrupted.");
                break;
            }
        }
    }

    public static void setEntitlementFuzzyMaxDelay(int delay) {
        if (delay < FUZZY_ENTITLEMENT_MIN_MAX_DELAY) {
            FUZZY_ENTITLEMENT_MAX_DELAY = FUZZY_ENTITLEMENT_MIN_MAX_DELAY;
        }
        else {
            FUZZY_ENTITLEMENT_MAX_DELAY = delay;
        }
    }

    public class EntitlementCheckCache {
        protected Long playheadToDeny;
        protected Long whenWasDenied;
        protected Long denyTimeValidity;
        protected Boolean isAllowed;
        protected EmpProgram program;
        protected Boolean gapInEpg;
        protected Boolean checkNotPossible;

        public EntitlementCheckCache() {

        }

        public void clear(){
            this.playheadToDeny = null;
            this.whenWasDenied = null;
            this.denyTimeValidity = null;
            this.isAllowed = null;
            this.program = null;
            this.gapInEpg = null;
            this.checkNotPossible = null;
        }

        public void register(long playheadFromDeny, long playheadToDeny, boolean isAllowed) {
            if (player == null) {
                clear();
                return;
            }
            this.playheadToDeny = playheadToDeny;
            this.whenWasDenied = player.getServerTime();
            this.denyTimeValidity = playheadToDeny - playheadFromDeny;
            this.isAllowed = isAllowed;
        }

        public void registerCheckNotPossible() {
            this.checkNotPossible = true;
        }

        public boolean isCheckNotPossible() {
            return this.checkNotPossible != null && this.checkNotPossible == true;
        }

        public void registerProgramChanged(EmpProgram newProgram) {
            program = newProgram;
        }

        public void registerGapInEpg() {
            gapInEpg = true;
        }

        public boolean hasGapInEpg() {
            return gapInEpg != null && gapInEpg == true;
        }

        public EmpProgram getProgram() {
            return program;
        }

        public Boolean isTimeAllowed(long playheadTime) {
            if (player == null || playheadToDeny == null || whenWasDenied == null || denyTimeValidity == null || isAllowed == null) {
                clear();
                return null;
            }
            long nowMs = player.getServerTime();
            if (nowMs - this.whenWasDenied > 2 * this.denyTimeValidity) {
                clear();
                return null;
            }
            if (playheadTime >= this.playheadToDeny && playheadTime <= this.playheadToDeny + this.denyTimeValidity) {
                return this.isAllowed;
            }
            return null;
        }
    }
}
