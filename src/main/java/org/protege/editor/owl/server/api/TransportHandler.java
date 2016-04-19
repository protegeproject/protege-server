package org.protege.editor.owl.server.api;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface TransportHandler {

    /**
     * Bind the target remote object to this transport handler.
     *
     * @param server
     *            The target server where this transport medium will be used.
     */
    void bind(Object remoteObject) throws Exception;

    /**
     * Closes the transport mechanism.
     */
    void close();
}
