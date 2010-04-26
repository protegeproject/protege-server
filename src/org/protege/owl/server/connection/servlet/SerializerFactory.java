package org.protege.owl.server.connection.servlet;

public class SerializerFactory {
    public Serializer createSerializer() {
        return new SerializerImpl();
    }
}
