package net.ericsson.emovs.playback.techs.ExoPlayer;

import net.ericsson.emovs.playback.TechFactory;
import net.ericsson.emovs.playback.interfaces.ITech;

/**
 * Created by Benjamin on 2017-11-22.
 */

public class ExoTechFactory extends TechFactory {
    @Override
    public ITech build() {
        return new ExoPlayerTech();
    }
}
