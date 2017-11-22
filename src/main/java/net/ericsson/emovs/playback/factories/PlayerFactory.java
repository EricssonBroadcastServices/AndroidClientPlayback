package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.playback.TechFactory;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.playback.Player;

/**
 * Created by Joao Coelho on 2017-11-17.
 */

public class PlayerFactory {

    public static Player build(Activity context, ViewGroup host, TechFactory tech) {
        return new Player(null, tech, context, host);
    }

    public static Player build(AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host, TechFactory tech) {
        return new Player(analytics, tech, context, host);
    }
}
