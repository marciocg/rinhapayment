package io.github.marciocg.payment.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.github.marciocg.payment.dto.PaymentsProcessorRequest;
import io.github.marciocg.payment.dto.PaymentsProcessorResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
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
@RegisterRestClient(baseUri = "http://localhost:8001")
public interface DefaultPaymentsProcessor {

    @POST
    @Path("/payments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PaymentsProcessorResponse processPayment(PaymentsProcessorRequest request);

    
}
