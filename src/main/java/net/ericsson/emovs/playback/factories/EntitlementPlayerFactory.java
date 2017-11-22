package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.playback.TechFactory;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.playback.EMPPlayer;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;

/**
 * Created by Joao Coelho on 2017-11-20.
 */

public class EntitlementPlayerFactory {

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host, TechFactory tech) {
        return new EMPPlayer(analytics, entitlementProvider, tech, context, host);
    }

}
