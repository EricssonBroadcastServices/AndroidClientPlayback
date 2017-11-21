package net.ericsson.emovs.playback.factories;

import android.app.Activity;
import android.view.ViewGroup;

import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.playback.Player;

/**
 * Created by Joao Coelho on 2017-11-17.
 */

public class PlayerFactory {

    public static Player build(Activity context, ViewGroup host) {
        return new Player(null, context, host);
    }

    public static Player build(AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host) {
        return new Player(analytics, context, host);
    }
}
