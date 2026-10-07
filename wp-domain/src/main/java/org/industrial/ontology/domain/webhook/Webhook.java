package org.industrial.ontology.domain.webhook;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.webhook.Webhook}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 4 Feb 2018
 */
public interface Webhook {

    /**
     * Get the payload Url for the webhook
     * @return The payload Url
     */
    @Nonnull
    String getPayloadUrl();
}
