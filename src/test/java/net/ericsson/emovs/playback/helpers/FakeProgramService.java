package net.ericsson.emovs.playback.helpers;

import net.ericsson.emovs.playback.services.ProgramService;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;

/**
 * Created by Joao Coelho on 2018-02-26.
 */

public class FakeProgramService extends ProgramService {
    public FakeProgramService(IEntitledPlayer player, Entitlement entitlement, EmpProgram initialProgram) {
        super(player, entitlement, initialProgram);
        this.eeCache = new FakeCache();
    }

    public void clear() {
        ((FakeCache) eeCache).cacheCalled = false;
    }

    public boolean wasTimeAllowedCalled() {
        return ((FakeCache) eeCache).cacheCalled;
    }

    private class FakeCache extends EntitlementCheckCache {
        public boolean cacheCalled = false;

        @Override
        public Boolean isTimeAllowed(long playheadTime) {
            Boolean outcome = super.isTimeAllowed(playheadTime);
            if (outcome != null) {
                cacheCalled = true;
            }
            return outcome;
        }
    }
}
