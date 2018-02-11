package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.playback.TechFactory;
import net.ericsson.emovs.playback.interfaces.ITech;
import net.ericsson.emovs.playback.techs.ExoPlayer.ExoPlayerTech;
import net.ericsson.emovs.playback.techs.ExoPlayer.ExoTechFactory;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.analytics.EMPAnalyticsConnector;
import net.ericsson.emovs.playback.EMPPlayer;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;

/**
 * <p>
 *     Use this factory to instantiate a player with all functionalities offered by EMP.
 * </p>
 *
 * <p>
 *     You can also tune the analytics connector, entitlement provider or tech factory
 * </p>
 */
public class EMPPlayerFactory {

    public static EMPPlayer build(Activity context, ViewGroup host) {
        return new EMPPlayer(new EMPAnalyticsConnector(), EMPEntitlementProvider.getInstance(), new ExoTechFactory(), context, host);
    }

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, Activity context, ViewGroup host) {
        return new EMPPlayer(new EMPAnalyticsConnector(), entitlementProvider, new ExoTechFactory(), context, host);
    }

    public static EMPPlayer build(AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host) {
        return new EMPPlayer(analytics, EMPEntitlementProvider.getInstance(), new ExoTechFactory(), context, host);
    }

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host) {
        return new EMPPlayer(analytics, entitlementProvider, new ExoTechFactory(), context, host);
    }

    public static EMPPlayer build(Activity context, ViewGroup host, TechFactory tech) {
        return new EMPPlayer(new EMPAnalyticsConnector(), EMPEntitlementProvider.getInstance(), tech, context, host);
    }

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, Activity context, ViewGroup host, TechFactory tech) {
        return new EMPPlayer(new EMPAnalyticsConnector(), entitlementProvider, tech, context, host);
    }

    public static EMPPlayer build(AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host, TechFactory tech) {
        return new EMPPlayer(analytics, EMPEntitlementProvider.getInstance(), tech, context, host);
    }

    public static EMPPlayer build(IEntitlementProvider entitlementProvider, AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host, TechFactory tech) {
        return new EMPPlayer(analytics, entitlementProvider, tech, context, host);
    }

}
