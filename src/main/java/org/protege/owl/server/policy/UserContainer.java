package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.Writer;

import org.protege.owl.server.api.User;

public interface UserContainer {
    public static final UserContainer EVERYONE = new UserContainer() {
        public boolean contains(UserDatabase db, User requestingUser) {
            return true;
        }
        
        public void write(Writer writer) throws IOException {
            writer.write("\t\tAll\n");
        }
    };
    
    
    boolean contains(UserDatabase db, User requestingUser);
    
    void write(Writer writer) throws IOException; 

}
