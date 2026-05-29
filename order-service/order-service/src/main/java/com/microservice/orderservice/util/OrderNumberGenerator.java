package com.microservice.orderservice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class OrderNumberGenerator {

    private OrderNumberGenerator() {}

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    /**
     * Generates an order number like ORD-20240521-00042.
     * For production use a database sequence or UUID approach instead.
     */
    public static String generate() {
        int seq = SEQUENCE.incrementAndGet() % 100_000;
        return String.format("ORD-%s-%05d", LocalDateTime.now().format(FMT), seq);
    }
}
