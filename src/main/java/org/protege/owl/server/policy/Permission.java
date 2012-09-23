package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import org.protege.owl.server.api.UserId;

public class Permission {
    private Map<Operation, UserContainer> permissionMap;
    
    public Permission(Map<Operation, UserContainer> permissionMap) {
        this.permissionMap = permissionMap;
    }
    
    
    public boolean isAllowed(UserDatabase db, UserId requestingUser, Operation op) {
        UserContainer container = permissionMap.get(op);
        return container != null && container.contains(db, requestingUser);
    }

    public void write(Writer writer) throws IOException {
        writer.write("Policy (\n");
        for (Entry<Operation, UserContainer> entry : permissionMap.entrySet()) {
            Operation op = entry.getKey();
            UserContainer container = entry.getValue();
            writer.write("\tAllow [\n");
            container.write(writer);
            writer.write("\t] to ");
            writer.write(op.getName());
            writer.write(";\n");
        }
        writer.write(")\n");
        writer.flush();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Permission)) {
            return false;
        }
        Permission other = (Permission) obj;
        return permissionMap.equals(other.permissionMap);
    }
    
    @Override
    public int hashCode() {
        return permissionMap.hashCode();
    }
    
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            write(writer);
            writer.flush();
            return writer.toString();
        }
        catch (IOException e) {
            return "Unknown Permission";
        }
        finally {
            try {
                writer.close();
            }
            catch (IOException e) {
                return "Unknown Permission";
            }
        }
    }
}
