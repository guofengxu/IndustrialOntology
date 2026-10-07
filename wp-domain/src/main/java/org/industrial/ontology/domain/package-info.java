/**
 * Serializable data shapes shared by every layer (frames, form descriptors, match criteria,
 * settings, events). Kept as plain records with explicit Jackson field names so the JSON
 * contract in docs/02 does not depend on Spring or on the OWL API runtime.
 */
package org.industrial.ontology.domain;
