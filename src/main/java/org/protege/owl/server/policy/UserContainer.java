package org.protege.owl.server.policy;

import org.protege.owl.server.api.UserId;

public interface UserContainer {
    public static final UserContainer EVERYONE = new UserContainer() {
        public boolean contains(UserDatabase db, UserId owner, UserId requestingUser) {
            return true;
        }
    };
    
    
    public boolean contains(UserDatabase db, UserId owner, UserId requestingUser);

}
