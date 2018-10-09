package net.ericsson.emovs.playback.drm;

public class DrmResponseEvent {
    private DrmEventType type;
    private byte[] responseData;

    public DrmResponseEvent(DrmEventType type, byte[] responseData) {
        this.type = type;
        this.responseData = responseData;
    }

    public DrmEventType getType() {
        return type;
    }

    public int getResponseLength() {
        return responseData.length;
    }
}
