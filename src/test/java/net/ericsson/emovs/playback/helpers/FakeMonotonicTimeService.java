package net.ericsson.emovs.playback.helpers;

import net.ericsson.emovs.exposure.utils.MonotonicTimeService;

/**
 * Created by Joao Coelho on 2018-04-06.
 */

public class FakeMonotonicTimeService extends MonotonicTimeService {
    @Override
    public Long currentTime() {
        return 0L;
    }

    @Override
    public void run() {}
}
