package net.ericsson.emovs.playback;

import android.app.Activity;
import android.view.ViewGroup;

/**
 * Created by Joao Coelho on 2017-11-17.
 */

public class EMPPlayerFactory {

    public static EMPPlayer build(Activity context, ViewGroup host) {
        return new EMPPlayer(new EMPAnalyticsConnector(), context, host);
    }

    public static EMPPlayer build(AnalyticsPlaybackConnector analytics, Activity context, ViewGroup host) {
        return new EMPPlayer(analytics, context, host);
    }
}
