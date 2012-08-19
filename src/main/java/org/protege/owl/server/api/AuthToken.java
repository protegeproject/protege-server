package org.protege.owl.server.api;

public interface AuthToken extends Comparable<AuthToken> {
	UserId getUserId();
}
