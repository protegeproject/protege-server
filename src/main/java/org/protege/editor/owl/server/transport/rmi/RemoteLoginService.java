package org.protege.editor.owl.server.transport.rmi;

import org.protege.editor.owl.server.api.LoginService;

import java.rmi.Remote;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface RemoteLoginService extends LoginService, Remote {

    // NO-OP
}
