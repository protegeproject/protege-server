package org.protege.editor.owl.server.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;



public class SSLContextFactory {
	// generate a self signed cert for testing
	// keytool -genkey -alias my_foo_cert -keyalg RSA -sigalg MD5withRSA -keystore my_foo.jks -storepass maotoing  -keypass maotoing -validity 9999

    public final static TrustManager[] TRUST_ALL_CERTS = new X509TrustManager[] { new DummyTrustManager() };   
    
    
    private static String PROPERTY_SSL_KEYSTORE = "org.protege.editor.owl.server.security.ssl.keystore";
	private static String PROPERTY_SSL_KEYSTORE_TYPE = "org.protege.editor.owl.server.security.ssl.keystore.type";
	private static String PROPERTY_SSL_PASSWORD = "org.protege.editor.owl.server.security.ssl.password";

    
    public SSLContextFactory() {
    }

    public SSLContext createSslContext() throws IOException {
    	
        String keyStoreName = System.getProperty(PROPERTY_SSL_KEYSTORE);
        String keyStoreType = System.getProperty(PROPERTY_SSL_KEYSTORE_TYPE);
        String keyStorePassword = System.getProperty(PROPERTY_SSL_PASSWORD);
        
        KeyStore keyStore = loadKeyStore(keyStoreName, keyStoreType, keyStorePassword);

        KeyManager[] keyManagers = buildKeyManagers(keyStore, keyStorePassword.toCharArray());
        TrustManager[] trustManagers = buildTrustManagers(null);

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(keyManagers, trustManagers, null);
        }
        catch (NoSuchAlgorithmException | KeyManagementException exc) {
            throw new IOException("Unable to create and initialise the SSLContext", exc);
        }

        return sslContext;
    }

    private KeyStore loadKeyStore(final String location, String type, String storePassword)
        throws IOException {
        String url = location;
        if (url.indexOf(':') == -1) {
            url = "file:" + location;
        }

        final InputStream stream = new URL(url).openStream();
        try {
            KeyStore loadedKeystore = KeyStore.getInstance(type);
            loadedKeystore.load(stream, storePassword.toCharArray());
            return loadedKeystore;
        }
        catch (KeyStoreException | NoSuchAlgorithmException | CertificateException exc) {
            throw new IOException(String.format("Unable to load KeyStore %s", location), exc);
        }
        finally {
            stream.close();
        }
    }

    private static TrustManager[] buildTrustManagers(final KeyStore trustStore) throws IOException {
        TrustManager[] trustManagers = null;
        if (trustStore == null) {
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            }
            catch (NoSuchAlgorithmException | KeyStoreException exc) {
                throw new IOException("Unable to initialise TrustManager[]", exc);
            }
        }
        else {
            trustManagers = TRUST_ALL_CERTS;
        }
        return trustManagers;
    }

    private static KeyManager[] buildKeyManagers(final KeyStore keyStore, char[] storePassword)
        throws IOException {
        KeyManager[] keyManagers;
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, storePassword);
            keyManagers = keyManagerFactory.getKeyManagers();
        }
        catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException exc) {
            throw new IOException("Unable to initialise KeyManager[]", exc);
        }
        return keyManagers;
    }

}
