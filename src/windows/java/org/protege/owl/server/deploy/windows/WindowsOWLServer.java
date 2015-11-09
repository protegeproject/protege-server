package org.protege.owl.server.deploy.windows;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.osgi.framework.Launcher;
import org.xml.sax.SAXException;


public class WindowsOWLServer {
    private static WindowsOWLServer instance;
    private Logger logger = Logger.getLogger(WindowsOWLServer.class.getCanonicalName());
    private Framework framework;
    
    
    private WindowsOWLServer() {
        ;
    }
    
    public static WindowsOWLServer getInstance() {
        if (instance == null) {
            instance = new WindowsOWLServer();
        }
        return instance;
    }
    
    public void start() throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, InterruptedException {
        logger.info("Entering apache daemon start method");
        Launcher launcher = new Launcher(new File("config.xml"));
        launcher.start(true);
        framework = launcher.getFramework();
    }
    
    public void stop() throws BundleException {
    	logger.info("Entering apache daemon stop method");
        framework.stop();
    }
    
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, InterruptedException {
        String cmd = "start";
        if(args.length > 0) {
           cmd = args[0];
        }
      
        if("start".equals(cmd)) {
           getInstance().start();
        }
        else {
           getInstance().stop();
        }
    }

}
