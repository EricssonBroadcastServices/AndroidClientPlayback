package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.playback.TechFactory;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.playback.EMPPlayer;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;
import net.ericsson.emovs.utilities.interfaces.IMetadataProvider;
import net.ericsson.emovs.utilities.interfaces.IMonotonicTimeService;

/**
 * Use this factory if you only want to instantiate a player with your own analytics or entitlement provider or tech factory
 */
public class EntitlementPlayerFactory {

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host, TechFactory tech, IMetadataProvider metadataProvider, IMonotonicTimeService monotonicTimeService) {
        return new EMPPlayer(analytics, entitlementProvider, tech, context, host, metadataProvider, monotonicTimeService);
    }

}
