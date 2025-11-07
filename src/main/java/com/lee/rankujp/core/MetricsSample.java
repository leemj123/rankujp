//package com.lee.rankujp.core;
//
//
//import io.micrometer.core.instrument.MeterRegistry;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.ArrayDeque;
//import java.util.Deque;
//
//@Component
//@RequiredArgsConstructor
//public class MetricsSample {
//    private final MeterRegistry reg;
//
//    // 10초 간격 수집, 6시간치 보관(1h * 60m * 6 = 2160 포인트)
//    private static final int MAX_POINTS = 360;
//    private final Deque<Point> ring = new ArrayDeque<>(MAX_POINTS);
//
//    public record Point(long ts, double active, double idle, double pending,
//                        double timeoutTotal, double execActive, double execQueue) {}
//
//    @Scheduled(fixedRate = 5_000) // 10s
//    public synchronized void sample() {
//        double active  = gauge("hikaricp.connections", "pool","HikariPool-1","state","active");
//        double idle    = gauge("hikaricp.connections", "pool","HikariPool-1","state","idle");
//        double pending = gauge("hikaricp.connections", "pool","HikariPool-1","state","pending");
//        double timeout = counter("hikaricp.connections.timeout");
//        // executor 메트릭은 이름/태그를 네 환경에 맞게 조정
//        double execActive = gauge("executor.active", "name","crawl");
//        double execQueue  = gauge("executor.queue.size", "name","crawl");
//
//        var p = new Point(Instant.now().toEpochMilli(), active, idle, pending, timeout, execActive, execQueue);
//        if (ring.size() >= MAX_POINTS) ring.removeFirst();
//        ring.addLast(p);
//    }
//
//    public synchronized Point[] snapshot() {
//        return ring.toArray(Point[]::new);
//    }
//
//    private double gauge(String name, String... tags) {
//        var g = reg.find(name).tags(tags).gauge();
//        return g == null ? Double.NaN : g.value();
//    }
//    private double counter(String name) {
//        var c = reg.find(name).counter();
//        return c == null ? 0 : c.count();
//    }
//}
