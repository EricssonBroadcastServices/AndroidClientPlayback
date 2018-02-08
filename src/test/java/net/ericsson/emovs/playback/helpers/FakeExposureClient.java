package net.ericsson.emovs.playback.helpers;

import net.ericsson.emovs.exposure.clients.exposure.ExposureClient;
import net.ericsson.emovs.exposure.interfaces.IExposureCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Joao Coelho on 2018-02-08.
 */

public class FakeExposureClient extends ExposureClient {
    public FakeExposureClient() {
    }

    @Override
    public void getSync(String endpoint, IExposureCallback callback) {
        if ("/time".equals(endpoint)) {
            JSONObject response = new JSONObject();
            try {
                response.put("epochMillis", System.currentTimeMillis());
                callback.onCallCompleted(response, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
