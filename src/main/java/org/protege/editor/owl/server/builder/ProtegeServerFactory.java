package org.protege.editor.owl.server.builder;

import org.protege.editor.owl.server.api.ServerFactory;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.policy.AccessControlFilter;
import org.protege.editor.owl.server.security.AuthenticationFilter;
import org.protege.editor.owl.server.versioning.ConflictDetectionFilter;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * Responsible for building the complete Protege server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class ProtegeServerFactory implements ServerFactory {

    @Override
    public ServerLayer build(ServerConfiguration configuration) {
        ServerLayer server = addConflictDetectionLayer(createBaseServer(configuration));
        server = addAccessControlLayer(server);
        server = addAuthenticationLayer(server);
        return server;
    }

    private ServerLayer createBaseServer(ServerConfiguration configuration) {
        return new ProtegeServer(configuration);
    }

    private ServerLayer addConflictDetectionLayer(ServerLayer server) {
        return new ConflictDetectionFilter(server);
    }

    private ServerLayer addAccessControlLayer(ServerLayer server) {
        return new AccessControlFilter(server);
    }

    private ServerLayer addAuthenticationLayer(ServerLayer server) {
        return new AuthenticationFilter(server);
    }
}
