package org.protege.editor.owl.server.versioning.api;

import java.io.Serializable;
import java.net.URI;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerDocument implements Serializable {

    private static final long serialVersionUID = 8705283686868339846L;

    private URI serverAddress;
    private HistoryFile historyFile;
    private int registryPort = -1;

    public ServerDocument(URI serverAddress, int registryPort, HistoryFile historyFile) {
        this.serverAddress = serverAddress;
        this.historyFile = historyFile;
        this.registryPort = registryPort;
    }

    public ServerDocument(URI serverAddress, HistoryFile historyFile) {
        this.serverAddress = serverAddress;
        this.historyFile = historyFile;
    }

    public URI getServerAddress() {
        return serverAddress;
    }

    /**
     * Returns the registry port number. The method will return <code>-1</code>
     * if such port does not exist.
     */
    public int getRegistryPort() {
        return registryPort;
    }

    public boolean hasRegistryPort() {
        return (registryPort != -1) ? true : false;
    }

    public HistoryFile getHistoryFile() {
        return historyFile;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Server address: ").append(getServerAddress());
        sb.append("\n");
        if (hasRegistryPort()) {
            sb.append("Registry port: ").append(getRegistryPort());
            sb.append("\n");
        }
        sb.append("Remote resource :").append(historyFile.getPath());
        return sb.toString();
    }
}
