package org.protege.owl.server.api.server;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface BuilderService {

    void initialize(ServerConfiguration configuration);

    void setServerFactory(ServerFactory factory);

    void removeServerFactory(ServerFactory factory);
}
