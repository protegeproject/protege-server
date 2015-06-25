package org.protege.owl.server.configuration;

import com.google.common.base.Objects;

public final class Property {
    private final String name, value;


    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof Property)) {
            return false;
        }
        final Property other = (Property) obj;
        return Objects.equal(this.name, other.name) && Objects.equal(this.value, other.value);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(this.name, this.value);
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("value", value)
                .toString();
    }
}
