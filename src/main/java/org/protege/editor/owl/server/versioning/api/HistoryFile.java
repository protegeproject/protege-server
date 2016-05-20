package org.protege.editor.owl.server.versioning.api;

import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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

    public static HistoryFile createNew(String parentDir, String filename) throws IOException {
        return createNew(parentDir, filename, true);
    }

    public static HistoryFile createNew(String parentDir, String filename, boolean doCreate) throws IOException {
        if (!parentDir.endsWith(File.separator)) {
            parentDir = parentDir + File.separator;
        }
        if (!filename.endsWith(EXTENSION)) {
            filename = filename + EXTENSION;
        }
        return createNew(parentDir + filename, doCreate);
    }

    public static HistoryFile createNew(String filepath) throws IOException {
        return createNew(filepath, true);
    }

    public static HistoryFile createNew(String filepath, boolean doCreate) throws IOException {
        HistoryFile f = new HistoryFile(filepath);
        if (doCreate) {
            FileUtils.touch(f); // Create an empty file in the file system
        }
        return f;
    }

    public static HistoryFile openExisting(String parentDir, String filename) throws InvalidHistoryFileException {
        if (!parentDir.endsWith(File.separator)) {
            parentDir = parentDir + File.separator;
        }
        if (!filename.endsWith(EXTENSION)) {
            filename = filename + EXTENSION;
        }
        return openExisting(parentDir + filename);
    }

    public static HistoryFile openExisting(String filepath) throws InvalidHistoryFileException {
        HistoryFile f = new HistoryFile(filepath);
        if (!f.exists()) {
            throw new InvalidHistoryFileException("Cannot found history file at path " + filepath);
        }
        if (!filepath.endsWith(EXTENSION)) {
            String template = "Invalid history file extension: %s";
            throw new InvalidHistoryFileException(String.format(template, EXTENSION, filepath));
        }
        return f;
    }
}
