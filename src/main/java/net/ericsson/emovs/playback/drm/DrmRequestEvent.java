package net.ericsson.emovs.playback.drm;

public class DrmRequestEvent {
    private DrmEventType type;
    private String licenseUrl;
    private int requestLength;

    public DrmRequestEvent(DrmEventType type, String licenseUrl, int requestLength) {
        this.type = type;
        this.licenseUrl = licenseUrl;
        this.requestLength = requestLength;
    }

    public DrmEventType getType() {
        return type;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public int getRequestDataLength() {
        return requestLength;
    }
}
