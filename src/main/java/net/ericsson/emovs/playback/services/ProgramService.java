package net.ericsson.emovs.playback.services;

import android.util.Log;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.exposure.metadata.IMetadataCallback;
import net.ericsson.emovs.exposure.metadata.queries.EpgQueryParameters;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.errors.ErrorRunnable;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.system.RunnableThread;

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
    private static final int LONG_WAIT_TIME = 30000;
    private static final int SHORT_WAIT_TIME = 1000;

    Entitlement entitlement;
    IPlayer player;
    EmpProgram currentProgram;

    public ProgramService(IPlayer player, Entitlement entitlement) {
        this.player = player;
        this.entitlement = entitlement;
    }

    public void isEntitled(final long timeToCheck, final Runnable onAllowed, final ErrorRunnable onForbidden) {
        if (currentProgram == null) {
            if (onForbidden != null) {
                onForbidden.run(ErrorCodes.PLAYBACK_NOT_ENTITLED, "ProgramID is null");
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
                new RunnableThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isEntitled = EMPEntitlementProvider.getInstance().isEntitled(currentProgram.assetId);
                        if (isEntitled == false) {
                            if (onForbidden != null) {
                                onForbidden.run(ErrorCodes.PLAYBACK_NOT_ENTITLED, "User not entitled");
                            }
                            return;
                        }
                        else {
                            if (onAllowed != null) {
                                onAllowed.run();
                            }
                        }
                    }
                }).start();
            }
        }
    }

    public void checkTimeshiftAllowance(final long timeToCheck, final Runnable onAllowed, final ErrorRunnable onForbidden, final boolean updateProgram) {
        EpgQueryParameters epgParams = EpgQueryParameters.DEFAULT;
        epgParams.setFutureTimeFrame(0);
        epgParams.setPastTimeFrame(0);

        EMPMetadataProvider.getInstance().getEpgWithTime(this.entitlement.channelId, timeToCheck, new IMetadataCallback<ArrayList<EmpProgram>>() {
            @Override
            public void onMetadata(ArrayList<EmpProgram> programs) {
                for (EmpProgram program : programs) {
                    Duration dStart = new Duration(program.startDateTime, new DateTime(timeToCheck));
                    Duration dEnd = new Duration(program.endDateTime, new DateTime(timeToCheck));

                    if (dStart.getMillis() > 0 && dEnd.getMillis() < 0) {
                        if (updateProgram) {
                            currentProgram = program;
                        }
                        EMPEntitlementProvider.getInstance().isEntitledAsync(program.assetId, onAllowed, onForbidden);
                    }
                }
                if (onAllowed != null) {
                    onAllowed.run();
                }
            }

            @Override
            public void onError(final Error error) {
                player.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        player.fail(ErrorCodes.EXO_PLAYER_INTERNAL_ERROR, error.toString());
                        player.stop();
                    }
                });
            }
        }, epgParams);
    }

    public void run () {
        for(;;) {
            try {
                if (this.player == null || this.entitlement == null || this.entitlement.channelId == null) {
                    return;
                }

                if (!this.entitlement.isLive && !this.entitlement.isUnifiedStream) {
                    return;
                }

                if (this.player.isPlaying()) {
                    long currentTime = this.player.getPlayheadTime();
                    Log.d("PlaybackCurrentTime", Long.toString(currentTime));
                    checkTimeshiftAllowance(currentTime, null, new ErrorRunnable() {
                        @Override
                        public void run(int code, final String message) {
                            player.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    player.fail(ErrorCodes.PLAYBACK_NOT_ENTITLED, message);
                                    player.stop();
                                }
                            });
                        }
                    }, true);
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

}
