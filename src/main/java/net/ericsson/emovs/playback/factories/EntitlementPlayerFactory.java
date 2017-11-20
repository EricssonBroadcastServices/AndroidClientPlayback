package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.utilities.AnalyticsPlaybackConnector;
import net.ericsson.emovs.playback.EMPPlayer;
import net.ericsson.emovs.utilities.IEntitlementProvider;

/**
 * Created by Joao Coelho on 2017-11-20.
 */

public class EntitlementPlayerFactory {

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host) {
        return new EMPPlayer(analytics, entitlementProvider, context, host);
    }

}
