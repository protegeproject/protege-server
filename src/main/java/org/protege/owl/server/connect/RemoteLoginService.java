package org.protege.owl.server.connect;

import org.protege.owl.server.api.LoginService;

import java.rmi.Remote;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface RemoteLoginService extends LoginService, Remote {

    // NO-OP
}
