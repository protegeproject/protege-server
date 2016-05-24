package org.protege.editor.owl.server.security;

import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.api.exception.ServerServiceException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

public interface SaltedChallengeLoginService extends LoginService {

    AuthToken login(UserId username, SaltedPasswordDigest password) throws ServerServiceException;

    Salt getSalt(UserId userId) throws ServerServiceException;
}
