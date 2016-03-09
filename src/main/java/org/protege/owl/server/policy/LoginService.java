package org.protege.owl.server.policy;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.protege.owl.server.api.AuthToken;

@Deprecated
public interface LoginService extends Remote {
    public static String SERVICE = "LoginService";

    AuthToken login(String name, String password) throws RemoteException;
    
    boolean checkAuthentication(AuthToken user) throws RemoteException;
}
