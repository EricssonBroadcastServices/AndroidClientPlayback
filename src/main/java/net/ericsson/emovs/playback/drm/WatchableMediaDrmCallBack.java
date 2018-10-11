package net.ericsson.emovs.playback.drm;

import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;

import java.util.UUID;

/**
 *  This {@link MediaDrmCallback} is used to intercept the calls to
 *  another wrapped MediaDrmCallback and send events to a {@link IDrmCallbackListener}.
 */
public class WatchableMediaDrmCallBack implements MediaDrmCallback {
    private MediaDrmCallback wrapped;
    private String licenseUrl;
    private IDrmCallbackListener mediaDrmCallbackListener;

    public WatchableMediaDrmCallBack(MediaDrmCallback wrapped, String licenseUrl, IDrmCallbackListener mediaDrmCallbackListener) {
        this.wrapped = wrapped;
        this.licenseUrl = licenseUrl;
        this.mediaDrmCallbackListener = mediaDrmCallbackListener;
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest provisionRequest) throws Exception {
        DrmEventType type = DrmEventType.PROVISION;
        mediaDrmCallbackListener.onDrmRequest(new DrmRequestEvent(type, licenseUrl, provisionRequest.getData().length));
        try {
            byte[] response = wrapped.executeProvisionRequest(uuid, provisionRequest);
            mediaDrmCallbackListener.onDrmRequestResponse(new DrmResponseEvent(type, response));
            return response;
        } catch (Exception e) {
            mediaDrmCallbackListener.onDrmRequestException(new DrmRequestExceptionEvent(type, e));
            throw e;
        }
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest keyRequest) throws Exception {
        DrmEventType type = DrmEventType.KEY;
        mediaDrmCallbackListener.onDrmRequest(new DrmRequestEvent(type, licenseUrl, keyRequest.getData().length));
        try {
            byte[] response = wrapped.executeKeyRequest(uuid, keyRequest);
            mediaDrmCallbackListener.onDrmRequestResponse(new DrmResponseEvent(type, response));
            return response;
        } catch (Exception e){
            mediaDrmCallbackListener.onDrmRequestException(new DrmRequestExceptionEvent(type, e));
            throw e;
        }
    }
}
