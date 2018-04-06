package net.ericsson.emovs.playback.helpers;

import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.utilities.interfaces.IMetadataCallback;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.errors.Error;
import net.ericsson.emovs.utilities.queries.EpgQueryParameters;

import java.util.ArrayList;

/**
 * Created by Joao Coelho on 2018-02-06.
 */

public  class FakeEMPMetadataProvider extends EMPMetadataProvider {
    ArrayList<EmpProgram> epg;
    boolean isBackendDown;

    public void mockEpg(ArrayList<EmpProgram> _epg){
        this.epg = _epg;
    }

    public void mockBackendAvailability(boolean availability) {
        isBackendDown = !availability;
    }

    @Override
    public void getEpg(String channelId, IMetadataCallback<ArrayList<EmpProgram>> callback, EpgQueryParameters params) {
        if (isBackendDown) {
            callback.onError(Error.NETWORK_ERROR);
        }
        else {
            callback.onMetadata(this.epg == null ? new ArrayList<EmpProgram>() : this.epg);
        }
    }

    @Override
    public void getEpgWithTime(String channelId, long epgTimeNowMs, IMetadataCallback<ArrayList<EmpProgram>> callback, EpgQueryParameters params) {
        if (isBackendDown) {
            callback.onError(Error.NETWORK_ERROR);
        }
        else {
            if (callback != null) {
                callback.onMetadata(this.epg == null ? new ArrayList<EmpProgram>() : this.epg);
            }
        }
    }

    @Override
    public void getEpgCacheFirst(final String channelId, final long epgTimeNowMs, final IMetadataCallback<ArrayList<EmpProgram>> callback, EpgQueryParameters params) {
        IMetadataCallback cacheListener = new IMetadataCallback<ArrayList<EmpProgram>>() {
            @Override
            public void onMetadata(ArrayList<EmpProgram> metadata) {
                try {
                    epgCache.update(channelId, metadata);
                }
                catch (Exception e) {}
                if (callback != null) {
                    ArrayList<EmpProgram> newMetadata = epgCache.getByTime(epgTimeNowMs);
                    callback.onMetadata(newMetadata);
                }
            }

            @Override
            public void onError(Error error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        };
        getEpgWithTime(channelId, epgTimeNowMs, cacheListener, params);
    }
}