package org.protege.owl.server.versioning;

import org.protege.owl.server.versioning.api.ChangeHistory;

import java.io.Serializable;
import java.util.Optional;

import edu.stanford.protege.metaproject.api.Host;

public class ServerDocument implements Serializable {

    private static final long serialVersionUID = 8705283686868339846L;

    private Host host;
    private HistoryFile historyFile;

    private ChangeHistory changeHistory;

    public ServerDocument(Host host, HistoryFile historyFile) {
        this.host = host;
        this.historyFile = historyFile;
    }

    public Host getHost() {
        return host;
    }

    public HistoryFile getHistoryFile() {
        return historyFile;
    }

    public Optional<ChangeHistory> getChangeHistory() {
        return Optional.ofNullable(changeHistory);
    }

    public void setChangeHistory(ChangeHistory changeHistory) {
        this.changeHistory = changeHistory;
    }
}
