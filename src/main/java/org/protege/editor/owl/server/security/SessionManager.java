package org.protege.editor.owl.server.security;

import edu.stanford.protege.metaproject.api.AuthToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class SessionManager {

    private Logger logger = LoggerFactory.getLogger(SessionManager.class);

    public List<AuthToken> tokenList = new ArrayList<>();

    public boolean check(AuthToken token) {
        int index = tokenList.indexOf(token);
        if (index == -1) {
            return false;
        }
//        return (token.getTimestamp() > currentTimestamp()) ? false : true;
        return true;
    }

    public void add(AuthToken token) {
        String userId = token.getUser().getId().get();
        String userName = token.getUser().getName().get();
        logger.info(String.format("Register %s (%s) to gain server access", userId, userName));
        tokenList.add(token);
    }

    public void remove(AuthToken token) {
        String userId = token.getUser().getId().get();
        String userName = token.getUser().getName().get();
        logger.info(String.format("Unregister %s (%s) from the server", userId, userName));
        tokenList.remove(token);
    }

    public void maintenance() {
        long currentTimestamp = currentTimestamp();
        for (AuthToken token : tokenList) {
//            if (token.getTimestamp() > currentTimestamp) {
//                remove(token);
//            }
        }
    }

    public void removeAll() {
        logger.info(String.format("Unregister all users from the server"));
        tokenList.clear();
    }

    protected static long currentTimestamp() {
        return System.currentTimeMillis();
    }
}
