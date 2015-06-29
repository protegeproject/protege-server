package org.protege.owl.server.server_config.configuration.idgeneration;

/**
 * A strategy for automatically generating unique term (classes, properties, ...) identifiers
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface TermIdentifierGenerationStrategy {

    /**
     * Get the class identifier generation strategy type
     *
     * @return Class identifier generation strategy type
     */
    TermIdentifierGenerationStrategyType getClassIdentifierGenerationStrategy();

    /**
     * Set the class identifier generation strategy type
     *
     * @param strategy  Identifier generation strategy type
     */
    void setClassIdentifierGenerationStrategy(TermIdentifierGenerationStrategyType strategy);

}