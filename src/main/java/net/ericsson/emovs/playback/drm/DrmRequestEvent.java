package net.ericsson.emovs.playback.drm;

public class DrmRequestEvent {
    private DrmEventType type;
    private String licenceUrl;
    private int requestLength;

    public DrmRequestEvent(DrmEventType type, String licenceUrl, int requestLength) {
        this.type = type;
        this.licenceUrl = licenceUrl;
        this.requestLength = requestLength;
    }

    public DrmEventType getType() {
        return type;
    }

    public String getLicenceUrl() {
        return licenceUrl;
    }

    public int getRequestDataLength() {
        return requestLength;
    }
}
