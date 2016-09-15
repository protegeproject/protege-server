package org.protege.editor.owl.server.versioning.api;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;

/**
 * Represents the binary history file used by the Protege server to track changes.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class HistoryFile extends File {

    private static final long serialVersionUID = 5138690689632511640L;

    public static final String EXTENSION = ".history";

    /*
     * Avoid external initialization
     */
    private HistoryFile(@Nonnull String filePath) {
        super(checkNotNull(filePath));
    }

    /**
     * Returns a history file object and creates a new file located at the given parent directory
     * and input file name.
     *
     * @param parentDir
     *          The parent directory
     * @param filename
     *          The name of the history file, must be followed by .history extension
     * @return A <code>HistoryFile</code> object.
     * @throws IOException If an I/O problem occurs.
     */
    public static HistoryFile createNew(@Nonnull String parentDir, @Nonnull String filename) throws IOException {
        return createNew(parentDir, filename, true);
    }

    /**
     * Returns a history file object with an option to create the file at the given parent
     * directory and input file name.
     *
     * @param parentDir
     *          The parent directory
     * @param filename
     *          The name of the history file, must be followed by .history extension
     * @param doCreate
     *          Creates a new file in the file system if set <code>true</code>.
     * @return A <code>HistoryFile</code> object.
     * @throws IOException If an I/O problem occurs.
     */
    public static HistoryFile createNew(@Nonnull String parentDir, @Nonnull String filename, boolean doCreate) throws IOException {
        checkNotNull(parentDir);
        checkNotNull(filename);
        String filepath = constructFilePath(parentDir, filename);
        return createNew(filepath, doCreate);
    }

    /**
     * Returns a history file object and creates a new file located at the given file path.
     *
     * @param filepath
     *          A valid file location
     * @return A <code>HistoryFile</code> object.
     * @throws IOException If an I/O problem occurs
     */
    public static HistoryFile createNew(@Nonnull String filepath) throws IOException {
        return createNew(filepath, true);
    }

    /**
     * Returns a history file object with an option to create the file at the given file path.
     *
     * @param filepath
     *          A valid file location
     * @param doCreate
     *          Creates a new file in the file system if set <code>true</code>.
     * @return A <code>HistoryFile</code> object.
     * @throws IOException If an I/O problem occurs
     */
    public static HistoryFile createNew(@Nonnull String filepath, boolean doCreate) throws IOException {
        checkNotNull(filepath);
        HistoryFile f = new HistoryFile(filepath);
        if (doCreate) {
            FileUtils.touch(f); // Create an empty file in the file system
        }
        return f;
    }

    /**
     * Returns a history file object by opening an existing history file at the given parent
     * directory and input file name.
     *
     * @param parentDir
     *          The parent directory
     * @param filename
     *          The name of the history file, must be followed by .history extension
     * @return A <code>HistoryFile</code> object.
     * @throws InvalidHistoryFileException If the input file name is not followed by .history extension
     */
    public static HistoryFile openExisting(String parentDir, String filename) throws InvalidHistoryFileException {
        String filepath = constructFilePath(parentDir, filename);
        return openExisting(filepath);
    }

    /**
     * Returns a history file object by opening an existing history file at the given parent
     * directory and input file name.
     *
     * @param filepath
     *          A valid file location
     * @return A <code>HistoryFile</code> object.
     * @throws InvalidHistoryFileException If the input file name is not followed by .history extension
     */
    public static HistoryFile openExisting(String filepath) throws InvalidHistoryFileException {
        return checkAndReturnHistoryFileWhenValid(filepath);
    }

    /*
     * Private helper methods
     */

    private static String constructFilePath(String parentDir, String filename) {
        parentDir = appendEndFileSeparatorWhenMissing(parentDir);
        return parentDir + filename;
    }

    private static HistoryFile checkAndReturnHistoryFileWhenValid(String filepath) throws InvalidHistoryFileException {
        if (!filepath.endsWith(EXTENSION)) {
            String message = String.format("File name must end with %s extension", EXTENSION);
            throw new InvalidHistoryFileException(message);
        }
        HistoryFile f = new HistoryFile(filepath);
        if (!f.exists()) {
            String message = String.format("Cannot found history file at path %s", filepath);
            throw new InvalidHistoryFileException(message);
        }
        return f;
    }

    private static String appendEndFileSeparatorWhenMissing(String parentDir) {
        if (!parentDir.endsWith(File.separator)) {
            parentDir = parentDir + File.separator;
        }
        return parentDir;
    }
}
