package net.ericsson.emovs.playback.drm;

import net.ericsson.emovs.analytics.EMPAnalyticsProvider;
import net.ericsson.emovs.analytics.EventParameters;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsDrmCallbackListener implements IDrmCallbackListener {
    private static final class Request {
        private static final String WIDEVINE_LICENSE_REQUEST = "WIDEVINE_LICENSE_REQUEST";
    }
    private static final class Response {
        private static final String WIDEVINE_LICENSE_RESPONSE = "WIDEVINE_LICENSE_RESPONSE";
    }
    private static final class Error {
        private static final String WIDEVINE_LICENSE_ERROR = "WIDEVINE_LICENSE_ERROR";
    }

    private IEntitledPlayer player;

    public AnalyticsDrmCallbackListener(IEntitledPlayer player) {
        this.player = player;
    }


    @Override
    public void onDrmRequest(DrmRequestEvent event) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(EventParameters.Drm.MESSAGE, Request.WIDEVINE_LICENSE_REQUEST);
        parameters.put(EventParameters.Drm.INFO, event.getLicenceUrl());
        parameters.put(EventParameters.Drm.DRM_REQUEST_TYPE, event.getType().name());
        parameters.put(EventParameters.Drm.DRM_DATA_LENGTH, String.valueOf(event.getRequestDataLength()));
        sendPlaybackDrm(parameters);
    }

    @Override
    public void onDrmRequestResponse(DrmResponseEvent event) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(EventParameters.Drm.MESSAGE, Response.WIDEVINE_LICENSE_RESPONSE);
        parameters.put(EventParameters.Drm.DRM_REQUEST_TYPE, event.getType().name());
        parameters.put(EventParameters.Drm.DRM_DATA_LENGTH, String.valueOf(event.getResponseLength()));
        sendPlaybackDrm(parameters);
    }

    @Override
    public void onDrmRequestException(DrmRequestExceptionEvent event) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(EventParameters.Drm.MESSAGE, Error.WIDEVINE_LICENSE_ERROR);

        StringWriter stringWriter = new StringWriter();
        event.getException().printStackTrace(new PrintWriter(stringWriter, true));
        parameters.put(EventParameters.Drm.INFO, stringWriter.getBuffer().toString());

        parameters.put(EventParameters.Drm.DRM_REQUEST_TYPE, event.getType().name());
        sendPlaybackDrm(parameters);
    }

    private void sendPlaybackDrm(Map<String, String> parameters) {
        EMPAnalyticsProvider.getInstance().playbackDrm(player.getSessionId(), parameters);
    }
}
