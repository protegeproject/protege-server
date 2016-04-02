package org.protege.owl.server.connect;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.api.LoginService;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.server.TransportHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class RmiTransport implements TransportHandler {

    private static final Logger logger = LoggerFactory.getLogger(RmiTransport.class);

    private int registryPort;
    private int serverPort;

    public RmiTransport(int registryPort, int serverPort) {
        this.registryPort = registryPort;
        this.serverPort = serverPort;
    }

    @Override
    public void bind(Object object) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(registryPort);
        if (object instanceof Server) {
            RmiServer remoteServer = new RmiServer((Server) object);
            Remote remoteStub = UnicastRemoteObject.exportObject(remoteServer, serverPort);
            registry.rebind(RmiServer.SERVER_SERVICE, remoteStub);
            logger.info("Server broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Server exported through RMI on port {}", serverPort);
        }
        else if (object instanceof LoginService) {
            RmiLoginService remoteLoginService = new RmiLoginService((LoginService) object);
            Remote remoteStub = UnicastRemoteObject.exportObject(remoteLoginService, serverPort);
            registry.rebind(RmiLoginService.LOGIN_SERVICE, remoteStub);
            logger.info("Login service broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Login service exported through RMI on port {}", serverPort);
        }
        else if (object instanceof ChangeService) {
            RmiChangeService remoteChangeService = new RmiChangeService((ChangeService) object);
            Remote remoteStub = UnicastRemoteObject.exportObject(remoteChangeService, serverPort);
            registry.rebind(RmiChangeService.CHANGE_SERVICE, remoteStub);
            logger.info("Change service broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Change service exported through RMI on port {}", serverPort);
        }
     }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }
}
