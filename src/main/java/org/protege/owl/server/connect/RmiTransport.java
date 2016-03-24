package org.protege.owl.server.connect;

import org.protege.owl.server.api.RmiLoginService;
import org.protege.owl.server.api.server.Server;
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
    public void bind(Object remoteObject) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(registryPort);
        if (remoteObject instanceof Server) {
            RmiServer remoteServer = new RmiServer((Server) remoteObject);
            Remote remoteStub = UnicastRemoteObject.exportObject(remoteServer, serverPort);
            registry.rebind("ProtegeServer", remoteStub);
            logger.info("Server broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Server exported through RMI on port {}", serverPort);
        }
        else if (remoteObject instanceof RmiLoginService) {
            RmiLoginService remoteLoginService = (RmiLoginService) remoteObject;
            Remote remoteStub = UnicastRemoteObject.exportObject(remoteLoginService, serverPort);
            registry.rebind("LoginService", remoteStub);
            logger.info("Server broadcasted through RMI Registry on port {}", registryPort);
            logger.info("Server exported through RMI on port {}", serverPort);
        }
        else {
            logger.warn("Unknown remote object. No bind was occured: {}", remoteObject);
        }
     }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
