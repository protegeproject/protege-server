package org.protege.editor.owl.server.api;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface BuilderService {

    void initialize(ServerConfiguration configuration);

    void setServerFactory(ServerFactory factory);

    void setTransportFactory(TransportFactory factory);

    void activate();
}
