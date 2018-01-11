package net.ericsson.emovs.playback.services;

import android.util.Log;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.exposure.metadata.IMetadataCallback;
import net.ericsson.emovs.exposure.metadata.queries.EpgQueryParameters;
import net.ericsson.emovs.exposure.utils.MonotonicTimeService;
import net.ericsson.emovs.playback.interfaces.ITech;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.models.EmpProgram;

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
    ITech tech;

    public ProgramService(ITech tech, Entitlement entitlement) {
        this.tech = tech;
        this.entitlement = entitlement;
    }

    public boolean isTimeshiftAllowed(long seekWallclockTime) {
        // TODO: implement
        return true;
    }

    public void run () {
        //MonotonicTimeService timeservice = new MonotonicTimeService();
        //timeservice.start();

        for(;;) {
            try {
                if (tech == null || this.entitlement == null || this.entitlement.channelId == null) {
                    return;
                }

                if (!this.entitlement.isLive && !this.entitlement.isUnifiedStream) {
                    return;
                }

                if (tech.isPlaying()) {
                    long currentTime = tech.getCurrentTime();
                    Log.d("PlaybackCurrentTime", Long.toString(currentTime));

                    EpgQueryParameters epgParams = EpgQueryParameters.DEFAULT;
                    epgParams.setFutureTimeFrame(0);
                    epgParams.setPastTimeFrame(0);

                    EMPMetadataProvider.getInstance().getEpgWithTime(this.entitlement.channelId, currentTime, new IMetadataCallback<ArrayList<EmpProgram>>() {
                        @Override
                        public void onMetadata(ArrayList<EmpProgram> programs) {
                            for (EmpProgram program : programs) {
                                if (program.liveNow()) {
                                    boolean isEntitled = EMPEntitlementProvider.getInstance().isEntitled(program.assetId);
                                    if (isEntitled == false) {
                                        // TODO: trigger error?
                                        //tech.error();
                                        tech.stop();
                                        return;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            // TODO: trigger error?
                            //tech.error();
                            tech.stop();
                            return;
                        }
                    }, epgParams);
                }

                Thread.sleep(WAIT_TIME);
            }
            catch (InterruptedException e) {
                Log.d(TAG, "Program service interrupted.");
                break;
            }
        }

        //if (timeservice.isAlive() && timeservice.isInterrupted() == false) {
        //    timeservice.interrupt();
        //}

    }

}
