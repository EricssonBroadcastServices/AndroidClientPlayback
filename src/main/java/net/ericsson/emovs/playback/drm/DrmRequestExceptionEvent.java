package net.ericsson.emovs.playback.drm;

public class DrmRequestExceptionEvent {
    private DrmEventType type;
    private Exception exception;

    public DrmRequestExceptionEvent(DrmEventType type, Exception exception) {
        this.type = type;
        this.exception = exception;
    }

    public DrmEventType getType() {
        return type;
    }

    public Exception getException() {
        return exception;
    }
}
