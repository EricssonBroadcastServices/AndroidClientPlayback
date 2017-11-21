package net.ericsson.emovs.playback.drm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.OfflineLicenseHelper;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;


import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

/**
 * Created by Joao Coelho on 2017-09-21.
 */

public class WidevinePlaybackLicenseManager {
    private final String EMP_WIDEVINE_KEYSTORE = "EMP_WIDEVINE_KEYSTORE";
    private final String KEY_OFFLINE_MEDIA_ID  = "OFFLINE_KEY_";

    private Context ctx;

    public WidevinePlaybackLicenseManager(Context ctx) {
        this.ctx = ctx;
    }

    public byte[] get(String licenseUrl, String mediaId) {

        String offlineAssetKeyIdStr = getSharedPreferences().getString(KEY_OFFLINE_MEDIA_ID + mediaId, null);

        if (offlineAssetKeyIdStr != null) {
            byte[] offlineAssetKeyId = Base64.decode(offlineAssetKeyIdStr, Base64.DEFAULT);

            if (offlineAssetKeyId == null) {
                return null;
            }

            try {
                GenericDrmCallback customDrmCallback = new GenericDrmCallback(buildHttpDataSourceFactory(true), licenseUrl);
                OfflineLicenseHelper offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(customDrmCallback, null);
                Pair<Long, Long> remainingTime = offlineLicenseHelper.getLicenseDurationRemainingSec(offlineAssetKeyId);

                Log.e(TAG, "Widevine license : " + Base64.encodeToString (offlineAssetKeyId, Base64.DEFAULT));
                Log.e(TAG, "Widevine license expiration: " + remainingTime.toString());

                if (remainingTime.first == 0 || remainingTime.second == 0) {
                    return null;
                }
            }
            catch (DrmSession.DrmSessionException | UnsupportedDrmException e) {
                e.printStackTrace();
                return null;
            }

            return offlineAssetKeyId;
        }

        return null;
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this.ctx, "EMP-Player"), useBandwidthMeter ? bandwidthMeter : null);
    }

    private SharedPreferences getSharedPreferences() {
        return this.ctx.getSharedPreferences(EMP_WIDEVINE_KEYSTORE, Context.MODE_PRIVATE);
    }
}
