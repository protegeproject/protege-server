package org.protege.owl.editor.server;

import org.protege.osgi.framework.Launcher;
import org.protege.editor.owl.server.ServerActivator;

import org.osgi.framework.launch.Framework;

import java.io.File;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class TestServerUtils {

    private static final File OSGI_CONFIGURATION = new File("src/test/resources/server-basic-config.xml");

    public static Framework startServer(String serverConfiguration) throws Exception {
        System.setProperty(ServerActivator.SERVER_CONFIGURATION_PROPERTY, serverConfiguration);
        Launcher launcher = new Launcher(OSGI_CONFIGURATION);
        launcher.start(false);
        return launcher.getFramework();
    }
}
