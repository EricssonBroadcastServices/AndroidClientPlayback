package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.playback.TechFactory;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.playback.Player;

/**
 * Use this factory if you want to instantiate a player without EMP integration and just to play a stream from manifest/playlist
 */
public class PlayerFactory {

    public static Player build(Activity context, ViewGroup host, TechFactory tech) {
        return new Player(null, tech, context, host);
    }

    public static Player build(AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host, TechFactory tech) {
        return new Player(analytics, tech, context, host);
    }
}
