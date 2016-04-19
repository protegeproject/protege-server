package org.protege.owl.server.versioning;

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

    public HistoryFile(File file) throws InvalidHistoryFileException {
        this(file.getPath());
    }

    public HistoryFile(String pathname) throws InvalidHistoryFileException {
        super(pathname);
        if (!this.exists()) {
            throw new InvalidHistoryFileException("Cannot found history file at path " + pathname);
        }
        if (!pathname.endsWith(EXTENSION)) {
            String template = "History file does not end with %s extension: %s";
            throw new InvalidHistoryFileException(String.format(template, EXTENSION, pathname));
        }
    }
}
