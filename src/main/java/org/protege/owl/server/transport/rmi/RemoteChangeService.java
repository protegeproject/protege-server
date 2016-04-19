package org.protege.owl.server.transport.rmi;

import org.protege.owl.server.api.ChangeService;

import java.rmi.Remote;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface RemoteChangeService extends ChangeService, Remote {

    // NO-OP
}
