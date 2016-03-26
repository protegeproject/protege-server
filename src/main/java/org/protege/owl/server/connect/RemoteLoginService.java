package org.protege.owl.server.connect;

import org.protege.owl.server.api.LoginService;

import java.rmi.Remote;

public interface RemoteLoginService extends LoginService, Remote {
    // NO-OP
}
