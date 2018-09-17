package net.ericsson.emovs.playback.drm;

import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GenericDrmCallback implements MediaDrmCallback {

    private static final String TAG = GenericDrmCallback.class.getSimpleName();

    private final HttpDataSource.Factory dataSourceFactory;
    private final String licUrl;
    private final Map<String, String> keyRequestProperties;

    public GenericDrmCallback(HttpDataSource.Factory dataSourceFactory, String licUrl, Object ... obj){
        Log.d(TAG," license URL  : " + licUrl);
        this.licUrl = licUrl;
        this.dataSourceFactory = dataSourceFactory;
        this.keyRequestProperties = new HashMap<>();
    }

    public GenericDrmCallback(HttpDataSource.Factory dataSourceFactory, String licenseUrl,
                              Map<String, String> keyRequestProperties) {
        Log.d(TAG,"License URL: " + licenseUrl);
        this.licUrl = licenseUrl;
        this.dataSourceFactory = dataSourceFactory;
        this.keyRequestProperties = keyRequestProperties;
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest provisionRequest) throws Exception {
        String url = provisionRequest.getDefaultUrl() + "&signedRequest=" + new String(provisionRequest.getData());
        return executePost(url, null, null);
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest keyRequest) throws Exception {

        //Holder for additional req parameters, as per custom implementation
        Map<String, String> requestProperties = new HashMap<>();
        Uri.Builder builder = Uri.parse(licUrl).buildUpon();

        // Set content type for Widevine
        requestProperties.put("Content-Type", "text/xml");

        // Adding all other key request properties
        requestProperties.putAll(keyRequestProperties);

        Uri uri = builder.build();
        try {
            return executePost(uri.toString(), keyRequest.getData(), requestProperties);
        } catch (FileNotFoundException e) {
            throw new IOException("License not found");
        } catch (IOException e) {
            throw new IOException("Error during license acquisition", e);
        }
        /*try {
            JSONObject jsonObject = new JSONObject(new String(bytes));
            return Base64.decode(jsonObject.getString("license"), Base64.DEFAULT);
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing response: " + new String(bytes), e);
            throw new RuntimeException("Error while parsing response", e);
        }*/
    }

    private byte[] executePost(String url, byte[] data, Map<String, String> requestProperties)
            throws IOException {
        HttpDataSource dataSource = dataSourceFactory.createDataSource();
        if (requestProperties != null) {
            for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                dataSource.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
            }
        }
        DataSpec dataSpec = new DataSpec(Uri.parse(url), data, 0, 0, C.LENGTH_UNSET, null, DataSpec.FLAG_ALLOW_GZIP);
        DataSourceInputStream inputStream = new DataSourceInputStream(dataSource, dataSpec);
        try {
            return Util.toByteArray(inputStream);
        } finally {
            Util.closeQuietly(inputStream);
        }
    }
}