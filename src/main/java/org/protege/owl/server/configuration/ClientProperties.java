package org.protege.owl.server.configuration;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;

import java.util.Iterator;
import java.util.Set;


public final class ClientProperties {
    private final ImmutableCollection<Property> properties;


    public ClientProperties(ImmutableCollection<Property> properties) {
        this.properties = properties;
    }


    public String getPropertyValue(String propertyName) {
        for(Property prop : properties) {
            if(prop.getName().equals(propertyName)) {
                return prop.getValue();
            }
        }
        return null;
    }


    public ImmutableCollection<Property> getProperties() {
        return properties;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(getClass() != obj.getClass()) {
            return false;
        }

        final ClientProperties other = (ClientProperties) obj;

        for(Property prop : properties) {
            if(!other.getProperties().contains(prop)) {
                return false;
            }
        }

        return true;
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(this.properties);
    }


    @Override
    public String toString() {
        String output = "";
        Iterator<Property> iter = properties.iterator();

        while(iter.hasNext()) {
            output += iter.next().toString();
            if(iter.hasNext()) {
                output += ",";
            }
        }

        return output;
    }
}
