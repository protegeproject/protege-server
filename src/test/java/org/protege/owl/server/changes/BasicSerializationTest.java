package org.protege.owl.server.changes;

import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.impl.DocumentFactoryImpl;

public class BasicSerializationTest extends AbstractSerializationTest {

	@Override
	protected DocumentFactory createDocumentFactory() {
		return new DocumentFactoryImpl();
	}
}
