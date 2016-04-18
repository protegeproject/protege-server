package org.protege.owl.editor.server;

import org.junit.Test;

public class ServerRunTest {

    @Test
    public void startServer() throws Exception {
        TestServerUtils.startServer("src/test/resources/server-configuration.json");
    }
}
