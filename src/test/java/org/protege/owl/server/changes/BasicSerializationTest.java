package org.protege.owl.server.changes;

import org.protege.owl.server.changes.api.DocumentFactory;

public class BasicSerializationTest extends AbstractSerializationTest {

	@Override
	protected DocumentFactory createDocumentFactory() {
		return new DocumentFactoryImpl();
	}
}
