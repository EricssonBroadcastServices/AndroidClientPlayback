package net.ericsson.emovs.playback.helpers;

import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.exposure.metadata.IMetadataCallback;
import net.ericsson.emovs.exposure.metadata.queries.EpgQueryParameters;
import net.ericsson.emovs.utilities.models.EmpProgram;

import java.util.ArrayList;

/**
 * Created by Joao Coelho on 2018-02-06.
 */

public  class FakeEMPMetadataProvider extends EMPMetadataProvider {
    ArrayList<EmpProgram> epg;

    public void mockEpg(ArrayList<EmpProgram> _epg){
        this.epg = _epg;
    }

    @Override
    public void getEpg(String channelId, IMetadataCallback<ArrayList<EmpProgram>> callback, EpgQueryParameters params) {
        callback.onMetadata(this.epg == null ? new ArrayList<EmpProgram>() : this.epg);
    }

    @Override
    public void getEpgWithTime(String channelId, long epgTimeNowMs, IMetadataCallback<ArrayList<EmpProgram>> callback, EpgQueryParameters params) {
        callback.onMetadata(this.epg == null ? new ArrayList<EmpProgram>() : this.epg);
    }
}