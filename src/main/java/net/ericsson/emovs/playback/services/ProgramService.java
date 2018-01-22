package net.ericsson.emovs.playback.services;

import android.util.Log;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.exposure.metadata.IMetadataCallback;
import net.ericsson.emovs.exposure.metadata.queries.EpgQueryParameters;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.errors.ErrorRunnable;
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
    private static final int LONG_WAIT_TIME = 1000;
    private static final int SHORT_WAIT_TIME = 1000;

    Entitlement entitlement;
    IEntitledPlayer player;
    EmpProgram currentProgram;

    public ProgramService(IEntitledPlayer player, Entitlement entitlement) {
        this.player = player;
        this.entitlement = entitlement;
    }

    public EmpProgram getCurrentProgram() {
        return this.currentProgram;
    }

    public void isEntitled(final long timeToCheck, final Runnable onAllowed, final ErrorRunnable onForbidden) {
        if (currentProgram == null) {
            checkTimeshiftAllowance(timeToCheck, onAllowed, onForbidden, false);
        }
        else {
            Duration dStart = new Duration(currentProgram.startDateTime, new DateTime(timeToCheck));
            Duration dEnd = new Duration(currentProgram.endDateTime, new DateTime(timeToCheck));

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

    public void checkTimeshiftAllowance(final long timeToCheck, final Runnable onAllowed, final ErrorRunnable onForbidden, final boolean updateProgram) {
        EpgQueryParameters epgParams = new EpgQueryParameters();
        epgParams.setFutureTimeFrame(0);
        epgParams.setPastTimeFrame(0);

        if (currentProgram != null) {
            Duration dStart = new Duration(currentProgram.startDateTime, new DateTime(timeToCheck));
            Duration dEnd = new Duration(currentProgram.endDateTime, new DateTime(timeToCheck));

            if (dStart.getMillis() > 0 && dEnd.getMillis() < 0) {
                if (onAllowed != null) {
                    onAllowed.run();
                }
                return;
            }
        }

        EMPMetadataProvider.getInstance().getEpgWithTime(this.entitlement.channelId, timeToCheck, new IMetadataCallback<ArrayList<EmpProgram>>() {
            @Override
            public void onMetadata(ArrayList<EmpProgram> programs) {
                for (EmpProgram program : programs) {
                    if (updateProgram == false || currentProgram == null || program.assetId.equals(currentProgram.assetId) == false) {
                        EMPEntitlementProvider.getInstance().isEntitledAsync(program.assetId, onAllowed, onForbidden);
                    }
                    if (updateProgram) {
                        currentProgram = program;
                        if (player != null) {
                            player.trigger(IPlaybackEventListener.EventId.PROGRAM_CHANGED, program);
                        }
                    }
                    break;
                }
                if (onAllowed != null) {
                    onAllowed.run();
                }
            }

            @Override
            public void onError(final Error error) {
                if (onForbidden != null) {
                    onForbidden.run(ErrorCodes.EXO_PLAYER_INTERNAL_ERROR, error.toString());
                }
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
                                    currentProgram = null;
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
