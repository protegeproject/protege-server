package org.protege.owl.server.policy;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.protege.owl.server.api.User;

public interface LoginService extends Remote {
    public static String SERVICE = "LoginService";

    User login(String name, String password) throws RemoteException;
    
    boolean checkAuthentication(User user) throws RemoteException;
}
