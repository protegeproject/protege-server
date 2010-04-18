package org.protege.owl.server.connection.servlet.serialize;

public class SerializerFactory {
    public Serializer createSerializer() {
        return new SerializerImpl();
    }
}
