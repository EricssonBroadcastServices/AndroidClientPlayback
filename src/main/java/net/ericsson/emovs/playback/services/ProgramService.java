package net.ericsson.emovs.playback.services;

import android.util.Log;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.exposure.metadata.IMetadataCallback;
import net.ericsson.emovs.exposure.metadata.queries.EpgQueryParameters;
import net.ericsson.emovs.exposure.utils.MonotonicTimeService;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;

/**
 * ProgramService should act on these scenarios:
 *  - Regular entitlement checks when live stream is playing
 *  - Entitlement check when timeshiftDelay is changed
 *  - Check licenseExpiration in entitlement?
 */
public class ProgramService extends Thread {
    private static final String TAG = ProgramService.class.toString();
    private static final int WAIT_TIME = 30000;

    Entitlement entitlement;
    IPlayer player;
    MonotonicTimeService timeService;
    EmpProgram currentProgram;

    public ProgramService(IPlayer player, Entitlement entitlement) {
        this.player = player;
        this.entitlement = entitlement;
    }

    public void isSeekEntitled(long timeToCheck, final Runnable onAllowed, final Runnable onForbidden) {
        if (currentProgram == null) {
            // TODO: throw error maybe?
            if (onForbidden != null) {
                onForbidden.run();
            }
        }
        else {
            Duration dStart = new Duration(new DateTime(timeToCheck), currentProgram.startDateTime);
            Duration dEnd = new Duration(new DateTime(timeToCheck), currentProgram.endDateTime);

            if (dStart.getMillis() > 0 && dEnd.getMillis() < 0) {
                if (onAllowed != null) {
                    onAllowed.run();
                }
            }
            else {
                checkTimeshiftAllowance(timeToCheck, onAllowed, onForbidden, false);
            }
        }
    }

    public void checkTimeshiftAllowance(final long timeToCheck, final Runnable onAllowed, final Runnable onForbidden, final boolean updateProgram) {
        EpgQueryParameters epgParams = EpgQueryParameters.DEFAULT;
        epgParams.setFutureTimeFrame(0);
        epgParams.setPastTimeFrame(0);

        EMPMetadataProvider.getInstance().getEpgWithTime(this.entitlement.channelId, timeToCheck, new IMetadataCallback<ArrayList<EmpProgram>>() {
            @Override
            public void onMetadata(ArrayList<EmpProgram> programs) {
                for (EmpProgram program : programs) {
                    Duration dStart = new Duration(new DateTime(timeToCheck), program.startDateTime);
                    Duration dEnd = new Duration(new DateTime(timeToCheck), program.endDateTime);

                    if (dStart.getMillis() > 0 && dEnd.getMillis() < 0) {
                        if (updateProgram) {
                            currentProgram = program;
                        }
                        boolean isEntitled = EMPEntitlementProvider.getInstance().isEntitled(program.assetId);
                        if (isEntitled == false) {
                            if (onForbidden != null) {
                                onForbidden.run();
                            }
                            return;
                        }
                    }
                }
                if (onAllowed != null) {
                    onAllowed.run();
                }
            }

            @Override
            public void onError(Error error) {
                player.fail(ErrorCodes.EXO_PLAYER_INTERNAL_ERROR, error.toString());
                player.stop();
            }
        }, epgParams);
    }

    public void run () {
        this.timeService = new MonotonicTimeService();
        this.timeService.start();

        for(;;) {
            try {
                if (this.player == null || this.entitlement == null || this.entitlement.channelId == null) {
                    return;
                }

                if (!this.entitlement.isLive && !this.entitlement.isUnifiedStream) {
                    return;
                }

                if (this.player.isPlaying()) {
                    long currentTime = this.player.getCurrentTime();
                    Log.d("PlaybackCurrentTime", Long.toString(currentTime));
                    checkTimeshiftAllowance(currentTime, null, new Runnable() {
                        @Override
                        public void run() {
                            player.fail(ErrorCodes.PLAYBACK_NOT_ENTITLED, "");
                            player.stop();
                        }
                    }, true);
                }

                Thread.sleep(WAIT_TIME);
            }
            catch (InterruptedException e) {
                Log.d(TAG, "Program service interrupted.");
                break;
            }
        }

        if (this.timeService.isAlive() && this.timeService.isInterrupted() == false) {
            this.timeService.interrupt();
        }
    }

}
