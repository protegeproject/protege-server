package org.protege.editor.owl.server;

import static org.junit.Assert.assertNotNull;

import org.protege.editor.owl.server.transport.rmi.RemoteChangeService;
import org.protege.editor.owl.server.transport.rmi.RemoteLoginService;
import org.protege.editor.owl.server.transport.rmi.RemoteServer;
import org.protege.editor.owl.server.transport.rmi.RmiChangeService;
import org.protege.editor.owl.server.transport.rmi.RmiLoginService;
import org.protege.editor.owl.server.transport.rmi.RmiServer;

import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerRunTest {

    private static final String HOST_NAME = "localhost";
    private static final int REGISTRY_PORT = 5200;

    @BeforeClass
    public static void startServer() throws Exception {
        TestServerUtils.startServer("src/test/resources/server-configuration.json");
    }

    @Test
    public void serverBroadcastTest() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST_NAME, REGISTRY_PORT);
        RemoteServer remoteServer = (RemoteServer) registry.lookup(RmiServer.SERVER_SERVICE);
        assertNotNull(remoteServer);
    }

    @Test
    public void loginServiceBroadcastTest() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST_NAME, REGISTRY_PORT);
        RemoteLoginService remoteService = (RemoteLoginService) registry.lookup(RmiLoginService.LOGIN_SERVICE);
        assertNotNull(remoteService);
    }

    @Test
    public void changeServiceBroadcastTest() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST_NAME, REGISTRY_PORT);
        RemoteChangeService remoteService = (RemoteChangeService) registry.lookup(RmiChangeService.CHANGE_SERVICE);
        assertNotNull(remoteService);
    }
}
