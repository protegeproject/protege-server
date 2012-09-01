package org.protege.owl.server.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.connect.rmi.AbstractRMIClientFactory;
import org.semanticweb.owlapi.model.IRI;

public class CRMIClientFactory extends AbstractRMIClientFactory {

    @Override
    protected AuthToken login(IRI serverLocation) {
        try {
            String username;
            String password;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("User: ");
            username = reader.readLine();
            System.out.print("Password: ");
            password = reader.readLine();
            return login(serverLocation, username, password);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
