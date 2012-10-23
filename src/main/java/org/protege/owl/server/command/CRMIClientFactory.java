package org.protege.owl.server.command;

import java.io.Console;
import java.io.PrintWriter;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.connect.rmi.AbstractRMIClientFactory;
import org.semanticweb.owlapi.model.IRI;

public class CRMIClientFactory extends AbstractRMIClientFactory {

    @Override
    protected AuthToken login(IRI serverLocation) {
        try {
            String username;
            String password;
            Console console = System.console();
            PrintWriter writer = console.writer();
            writer.print("User: ");
            writer.flush();
            username = console.readLine();
            password = new String(console.readPassword("%s", "Password: "));
            return login(serverLocation, username, password);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
