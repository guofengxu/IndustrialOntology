/**
 * HTTP surface: {@code /api/v1} controllers, the legacy {@code /data} compatibility layer, SSE
 * and security configuration. Controllers stay thin and delegate to {@code wp-app}; errors are
 * rendered as RFC 7807 problem details by a single advice.
 */
package org.industrial.ontology.api;
