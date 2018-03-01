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
    }
}
