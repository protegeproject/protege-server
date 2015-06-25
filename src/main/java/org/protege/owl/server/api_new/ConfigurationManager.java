package org.protege.owl.server.api_new;

/**
 * @author Rafael Gonçalves
 * Stanford Center for Biomedical Informatics Research
 */
public interface ConfigurationManager {

    String getStringProperty(String propertyName);

    int getIntegerProperty(String propertyName);

    boolean getBooleanProperty(String propertyName);

    void setProperty(String propertyName, String propertyValue);

    boolean isStringProperty(String propertyName);

    boolean isIntegerProperty(String propertyName);

    boolean isBooleanProperty(String propertyName);

}
