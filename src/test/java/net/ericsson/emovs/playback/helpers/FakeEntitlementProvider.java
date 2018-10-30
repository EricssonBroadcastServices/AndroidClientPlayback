package net.ericsson.emovs.playback.helpers;

import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.entitlements.IEntitlementCallback;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;
import net.ericsson.emovs.utilities.errors.ErrorRunnable;

/**
 * Created by Joao Coelho on 2018-02-05.
 */

public class FakeEntitlementProvider implements IEntitlementProvider {
    Entitlement e;
    String requestId;

    public void setEntitlement(Entitlement e) {
        this.e = e;
    }

    public void setRequestId(String requestId){
        this.requestId = requestId;
    }

    @Override
    public boolean isEntitled(String mediaId) {
        return false;
    }

    @Override
    public void isEntitledAsync(String mediaId, Runnable onEntitled, ErrorRunnable onNotEntitled) {

    }

    @Override
    public void playVod(String assetId, IEntitlementCallback listener) {
        if (listener != null) {
            listener.onEntitlement(this.e == null ? new Entitlement() : e, this.requestId==null ? "" : requestId);
        }
    }

    @Override
    public void playCatchup(String channelId, String programId, IEntitlementCallback listener) {
        if (listener != null) {
            listener.onEntitlement(this.e == null ? new Entitlement() : e, this.requestId==null ? "" : requestId);
        }
    }

    @Override
    public void playLive(String channelId, IEntitlementCallback listener) {
        if (listener != null) {
            listener.onEntitlement(this.e == null ? new Entitlement() : e, this.requestId==null ? "" : requestId);
        }
    }
}