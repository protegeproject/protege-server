package org.protege.editor.owl.server.versioning.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        this.registryPort = registryPort;
        this.historyFile = historyFile;
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

    public static void writeDocument(ServerDocument document, String workspaceDir) throws IOException {
        File parentDir = new File(workspaceDir, FileNaming.VERSIONING_DIR);
        File destination = new File(parentDir, FileNaming.CONFIG_FILE);
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));
        try {
            oos.writeObject(document);
        }
        finally {
            oos.flush();
            oos.close();
        }
    }

    public static ServerDocument readDocument(String workspaceDir) throws IOException {
        File parentDir = new File(workspaceDir, FileNaming.VERSIONING_DIR);
        File source = new File(parentDir, FileNaming.CONFIG_FILE);
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(source)));
        try {
            return (ServerDocument) ois.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unknown source file format", e);
        }
        finally {
            ois.close();
        }
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
