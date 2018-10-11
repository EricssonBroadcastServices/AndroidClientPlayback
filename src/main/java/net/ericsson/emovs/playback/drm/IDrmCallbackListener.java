package net.ericsson.emovs.playback.drm;

public interface IDrmCallbackListener {
    void onDrmRequest(DrmRequestEvent event);
    void onDrmRequestResponse(DrmResponseEvent event);
    void onDrmRequestException(DrmRequestExceptionEvent event);
}
