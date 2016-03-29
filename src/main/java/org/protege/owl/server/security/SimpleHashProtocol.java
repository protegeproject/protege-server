package org.protege.owl.server.security;

import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.UserId;

public interface SimpleHashProtocol {

    public Salt getSalt(UserId userId) throws Exception;
}
