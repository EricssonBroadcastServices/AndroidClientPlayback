package net.ericsson.emovs.playback.drm;

public class EmptyDrmCallbackListener implements IDrmCallbackListener {
    @Override
    public void onDrmRequest(DrmRequestEvent event) {
    }

    @Override
    public void onDrmRequestResponse(DrmResponseEvent event) {
    }

    @Override
    public void onDrmRequestException(DrmRequestExceptionEvent event) {
    }
}
