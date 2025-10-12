package io.github.marciocg.payment.api;

import io.github.marciocg.payment.dto.PaymentRequest;
import io.github.marciocg.payment.dto.SummaryResponse;
import io.github.marciocg.payment.model.Payment;
import io.github.marciocg.payment.repository.PaymentRepository;
import io.github.marciocg.payment.service.PaymentWorker;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    PaymentWorker worker;

    @Inject
    PaymentRepository repository;

    @Inject
    RedisDataSource redis;

    @POST
    public Response receive(PaymentRequest request) {
        Payment payment = new Payment();
        payment.correlationId = request.correlationId();
        payment.amount = request.amount();
        worker.enqueue(payment);
        return Response.noContent().build();
    }

    @GET
    @Path("/summary")
    public Map<String, SummaryResponse> summary(@QueryParam("from") Instant from,
                                                @QueryParam("to") Instant to) {
        List<Object[]> result = repository.getSummary(from, to);
        Map<String, SummaryResponse> response = new HashMap<>();
        for (Object[] row : result) {
            String type = (String) row[0];
            long totalRequests = (Long) row[1];
            BigDecimal totalAmount = (BigDecimal) row[2];
            response.put(type, new SummaryResponse(totalRequests, totalAmount));
        }
        return response;
    }

    @GET
    @Path("/summary-redis")
    public Map<String, SummaryResponse> summaryRedis() {
        Map<String, SummaryResponse> response = new HashMap<>();
        for (String type : List.of("default", "fallback")) {
            String key = "summary:" + type;
            var hashOps = redis.hash(String.class);
            
            String totalRequestsStr = hashOps.hget(key, "totalRequests");
            String totalAmountStr = hashOps.hget(key, "totalAmount");
            
            long totalRequests = Long.parseLong(totalRequestsStr != null ? totalRequestsStr : "0");
            BigDecimal totalAmount = new BigDecimal(totalAmountStr != null ? totalAmountStr : "0");
            response.put(type, new SummaryResponse(totalRequests, totalAmount));
        }
        return response;
    }
}