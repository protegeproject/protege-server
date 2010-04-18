package org.protege.owl.server.connection.servlet.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;

public class ServletDocumentSource implements OWLOntologyDocumentSource {
    private URL url;
    
    public ServletDocumentSource(URL url) {
        this.url = url;
    }

    @Override
    public IRI getDocumentIRI() {
        try {
            return IRI.create(url);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Servlet URI is invalid " + url, e);
        }
    }

    @Override
    public boolean isInputStreamAvailable() {
        return true;
    }

    @Override
    public InputStream getInputStream() {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            }
            else if (code == HttpURLConnection.HTTP_CONFLICT) {
                throw new UnsupportedOperationException("Not implemented yet");
            }
            else { //TODO fix me...
                throw new UnsupportedOperationException("Not implemented yet");
            }
        }
        catch (IOException ioe) {
            throw new RuntimeException("Unexpected IO error", ioe);
        }
    }

    @Override
    public boolean isReaderAvailable() {
        return false;
    }

    @Override
    public Reader getReader() {
        throw new IllegalStateException("Reader not available");
    }

}
