package net.ericsson.emovs.playback.helpers;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.errors.ErrorRunnable;

/**
 * Created by Joao Coelho on 2018-02-06.
 */

public class FakeEMPEntitlementProvider extends EMPEntitlementProvider {
    boolean isEntitled;
    public boolean wasEntitlementCheckDone;

    public void forgetEntitlementCheck() {
        this.wasEntitlementCheckDone = false;
    }

    public void mockIsEntitled(boolean allowEntitlement) {
        this.isEntitled = allowEntitlement;
    }

    @Override
    public void isEntitledAsync(String mediaId, final Runnable onEntitled, final ErrorRunnable onNotEntitled) {
        this.wasEntitlementCheckDone = true;
        if (isEntitled && onEntitled != null) {
            onEntitled.run();
        }
        else if (!isEntitled && onNotEntitled != null) {
            onNotEntitled.run(ErrorCodes.PLAYBACK_NOT_ENTITLED, "USER_NOT_ENTITLED");
        }
    }

    @Override
    public boolean isEntitled(String mediaId) {
        this.wasEntitlementCheckDone = true;
        return isEntitled;
    }
}
