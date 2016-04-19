package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.OWLServerException;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface TransportFactory {

    TransportHandler build(ServerConfiguration configuration) throws OWLServerException;
}
