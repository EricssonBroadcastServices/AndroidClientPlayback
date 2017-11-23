package net.ericsson.emovs.playback;

import net.ericsson.emovs.playback.interfaces.ITech;

/**
 * New TechFactories must extend this class and override build method
 *
 * Created by Joao Coelho on 2017-11-22.
 */
public class TechFactory {
    /**
     * Override this method in your derived class
     * @return
     */
    public ITech build() {
        return null;  // stub
    }
}
