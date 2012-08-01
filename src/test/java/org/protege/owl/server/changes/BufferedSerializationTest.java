package org.protege.owl.server.changes;

import org.protege.owl.server.api.DocumentFactory;

public class BufferedSerializationTest extends AbstractSerializationTest {

    @Override
    protected DocumentFactory createDocumentFactory() {
        return new BufferedDocumentFactory(new DocumentFactoryImpl());
    }

}
