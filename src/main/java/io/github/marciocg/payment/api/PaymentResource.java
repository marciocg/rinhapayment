package io.github.marciocg.payment.api;

import java.time.Instant;
import java.util.Map;

import io.github.marciocg.payment.dto.PaymentRequest;
import io.github.marciocg.payment.dto.SummaryResponse;
import io.github.marciocg.payment.model.Payment;
import io.github.marciocg.payment.service.PaymentService;
// import io.quarkus.redis.datasource.RedisDataSource;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    PaymentService service;

    // @Inject
    // RedisDataSource redis;

    @Transactional
    @POST
    @Path("/payments")
    // @RunOnVirtualThread
    public Response receive(PaymentRequest request) {
        Payment payment = new Payment();
        payment.correlationId = request.correlationId();
        payment.amount = request.amount();
/*         try {
            service.sendToDefaultProcessor(payment);
        } catch (Exception e) {
            return Response.noContent().build();
        } */
        service.sendToDefaultProcessor(payment);
        return Response.accepted().build();
    }

    @GET
    @Path("/payments-summary")
    // @RunOnVirtualThread
    public Map<String, SummaryResponse> summary(@QueryParam("from") Instant from, @QueryParam("to") Instant to) {
        if (from == null) {
            from = Instant.EPOCH;
        }
        if (to == null) {
            to = Instant.parse("9999-01-01T00:00:00Z");
        }

        return Payment.streamSummaryByPaymentType(from, to);
    }
}

    /*
     * @GET
     * 
     * @Path("/payments-summary-redis")
     * public Map<String, SummaryResponse> summaryRedis() {
     * Map<String, SummaryResponse> response = new HashMap<>();
     * for (String type : List.of("default", "fallback")) {
     * String key = "summary:" + type;
     * var hashOps = redis.hash(String.class);
     * 
     * String totalRequestsStr = hashOps.hget(key, "totalRequests");
     * String totalAmountStr = hashOps.hget(key, "totalAmount");
     * 
     * long totalRequests = Long.parseLong(totalRequestsStr != null ?
     * totalRequestsStr : "0");
     * BigDecimal totalAmount = new BigDecimal(totalAmountStr != null ?
     * totalAmountStr : "0");
     * response.put(type, new SummaryResponse(totalRequests, totalAmount));
     * }
     * return response;
     * }
     */
