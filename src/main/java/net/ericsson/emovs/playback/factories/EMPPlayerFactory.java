package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.utilities.AnalyticsPlaybackConnector;
import net.ericsson.emovs.playback.EMPAnalyticsConnector;
import net.ericsson.emovs.playback.EMPPlayer;
import net.ericsson.emovs.utilities.IEntitlementProvider;

/**
 * Created by Joao Coelho on 2017-11-17.
 */

public class EMPPlayerFactory {

    public static EMPPlayer build(Activity context, ViewGroup host) {
        return new EMPPlayer(new EMPAnalyticsConnector(), EMPEntitlementProvider.getInstance(), context, host);
    }

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, Activity context, ViewGroup host) {
        return new EMPPlayer(new EMPAnalyticsConnector(), entitlementProvider, context, host);
    }

    public static EMPPlayer build(AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host) {
        return new EMPPlayer(analytics, EMPEntitlementProvider.getInstance(), context, host);
    }

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host) {
        return new EMPPlayer(analytics, entitlementProvider, context, host);
    }

}
