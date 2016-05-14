package org.protege.editor.owl.server.versioning.api;

import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;

import java.io.File;

/**
 * Represents the binary history file used by the Protege server to track changes.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class HistoryFile extends File {

    private static final long serialVersionUID = 5138690689632511640L;

    public static final String EXTENSION = ".history";

    private HistoryFile(String pathname) {
        super(pathname);
    }

    public static HistoryFile createNew(String rootDir, String filename) {
        if (!rootDir.endsWith(File.separator)) {
            rootDir = rootDir + File.separator;
        }
        if (!filename.endsWith(EXTENSION)) {
            filename = filename + EXTENSION;
        }
        return createNew(rootDir + filename);
    }

    public static HistoryFile createNew(String pathname) {
        return new HistoryFile(pathname);
    }

    public static HistoryFile openExisting(String rootDir, String filename) throws InvalidHistoryFileException {
        if (!rootDir.endsWith(File.separator)) {
            rootDir = rootDir + File.separator;
        }
        if (!filename.endsWith(EXTENSION)) {
            filename = filename + EXTENSION;
        }
        return openExisting(rootDir + filename);
    }

    public static HistoryFile openExisting(String pathname) throws InvalidHistoryFileException {
        HistoryFile f = new HistoryFile(pathname);
        if (!f.exists()) {
            throw new InvalidHistoryFileException("Cannot found history file at path " + pathname);
        }
        if (!pathname.endsWith(EXTENSION)) {
            String template = "Invalid history file extension: %s";
            throw new InvalidHistoryFileException(String.format(template, EXTENSION, pathname));
        }
        return f;
    }
}
