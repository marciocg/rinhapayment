package io.github.marciocg.payment.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import io.github.marciocg.payment.dto.PaymentsProcessorHealthCheckResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


/**
 * To use it via injection.
 *
 * {@code
 *     @Inject
 *     @RestClient
 *     MyRemoteService myRemoteService;
 *
 *     public void doSomething() {
 *         Set<MyRemoteService.Extension> restClientExtensions = myRemoteService.getExtensionsById("io.quarkus:quarkus-hibernate-validator");
 *     }
 * }
 */

// @RegisterRestClient(baseUri = "https://httpbin.org/post")
@RegisterRestClient(baseUri = "http://localhost:8002/payments/service-health")
public interface FallbackHealthCheckPaymentsProcessor {

    @GET
    @Path("/payments/service-health")
    @Produces(MediaType.APPLICATION_JSON)
    public PaymentsProcessorHealthCheckResponse getHealth();

    
}
