package org.protege.owl.server.api;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public abstract class AbstractServerFilter extends ServerLayer {

    private ServerLayer delegate;

    public AbstractServerFilter(ServerLayer delegate) {
        this.delegate = delegate;
    }

    protected ServerLayer getDelegate() {
        return delegate;
    }

    @Override
    protected ServerConfiguration getConfiguration() {
        return getDelegate().getConfiguration();
    }
}
