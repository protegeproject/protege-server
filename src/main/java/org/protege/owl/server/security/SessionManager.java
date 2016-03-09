package org.protege.owl.server.security;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.protege.metaproject.api.AuthToken;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class SessionManager { // TODO Put it in the Metaproject API

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
        tokenList.add(token);
    }

    public void remove(AuthToken token) {
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
        tokenList.clear();
    }

    protected static long currentTimestamp() {
        return System.currentTimeMillis();
    }
}
