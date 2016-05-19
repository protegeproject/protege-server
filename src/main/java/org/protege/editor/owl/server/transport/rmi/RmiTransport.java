package org.protege.editor.owl.server.transport.rmi;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.api.Server;
import org.protege.editor.owl.server.api.TransportHandler;

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

    /*
     * Keep the server and remote services as static fields to keep a strong reference to
     * the objects so that they remain reachable, i.e., avoiding garbage collection.
     */
    private static RmiServer remoteServer;
    private static RmiLoginService remoteLoginService;
    private static RmiChangeService remoteChangeService;

    private int registryPort;
    private int serverPort;

    private Registry registry;

    public RmiTransport(int registryPort, int serverPort) {
        this.registryPort = registryPort;
        this.serverPort = serverPort;
    }

    @Override
    public void bind(Object object) throws RemoteException {
        initialize();
        if (object instanceof Server) {
            remoteServer = new RmiServer((Server) object);
            final Remote remoteStub = UnicastRemoteObject.exportObject(remoteServer, serverPort);
            registry.rebind(RmiServer.SERVER_SERVICE, remoteStub);
            logger.info("Server broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Server exported through RMI on port {}", serverPort);
        }
        else if (object instanceof LoginService) {
            remoteLoginService = new RmiLoginService((LoginService) object);
            final Remote remoteStub = UnicastRemoteObject.exportObject(remoteLoginService, serverPort);
            registry.rebind(RmiLoginService.LOGIN_SERVICE, remoteStub);
            logger.info("Login service broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Login service exported through RMI on port {}", serverPort);
        }
        else if (object instanceof ChangeService) {
            remoteChangeService = new RmiChangeService((ChangeService) object);
            final Remote remoteStub = UnicastRemoteObject.exportObject(remoteChangeService, serverPort);
            registry.rebind(RmiChangeService.CHANGE_SERVICE, remoteStub);
            logger.info("Change service broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Change service exported through RMI on port {}", serverPort);
        }
     }

    private void initialize() throws RemoteException {
        if (registry == null) {
            registry = LocateRegistry.createRegistry(registryPort);
        }
    }

    @Override
    public void close() {
        registry = null;
    }
}
