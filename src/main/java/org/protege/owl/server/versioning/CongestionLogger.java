package org.protege.owl.server.versioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CongestionLogger {
    private static final int LOGGING_TIMEOUT = 3000;
    private static final int LOGGING_UNIT    = 100000;
    private Logger   logger         = LoggerFactory.getLogger(CongestionLoggerInputStream.class.getCanonicalName());
    private long    startTime      = System.currentTimeMillis();
    private boolean loggingStarted = false;
    private int      counter        = 0;
    private String doingWhat;
    
    public CongestionLogger(String doingWhat) {
        this.doingWhat = doingWhat;
    }

    public void logBytes(int count) {
        counter += count;
        if (isLogging()) {
            int oldUnitCount = counter / LOGGING_UNIT;
            int newUnitCount = (counter + count) / LOGGING_UNIT;
            if (newUnitCount > oldUnitCount) {
                logger.info("" + (newUnitCount * LOGGING_UNIT / 1000) + "K bytes " + doingWhat + ".");
            }
        }
        counter += count;
    }
    
    public void close() {
        if (isLogging()) {
            logger.info("A total of " + counter + " bytes " +doingWhat + ".");
        }
    }
    
    private boolean isLogging() {
        if (!loggingStarted && (System.currentTimeMillis() - startTime) > LOGGING_TIMEOUT) {
            loggingStarted = true;
        }
        return loggingStarted;
    }
}
