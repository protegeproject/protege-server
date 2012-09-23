package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.protege.owl.server.api.UserId;

public interface UserContainer {
    public static final UserContainer EVERYONE = new UserContainer() {
        public boolean contains(UserDatabase db, UserId requestingUser) {
            return true;
        }
        
        public void write(Writer writer) throws IOException {
            writer.write("\t\tAll\n");
        }
    };
    
    
    boolean contains(UserDatabase db, UserId requestingUser);
    
    void write(Writer writer) throws IOException; 

}
