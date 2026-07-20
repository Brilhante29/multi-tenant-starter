package com.portfolio.multitenant.benchmark;

import java.time.Instant;

public record BenchmarkResult(
    String project,
    String metric,
    double value,
    String unit,
    int iterations,
    String timestamp,
    String os,
    String arch,
    int cpus
) {
    public String toJson() {
        return "{" +
            "\"project\":\"" + escape(project) + "\"," +
            "\"metric\":\"" + escape(metric) + "\"," +
            "\"value\":" + value + "," +
            "\"unit\":\"" + escape(unit) + "\"," +
            "\"iterations\":" + iterations + "," +
            "\"timestamp\":\"" + escape(timestamp) + "\"," +
            "\"environment\":{" +
                "\"os\":\"" + escape(os) + "\"," +
                "\"arch\":\"" + escape(arch) + "\"," +
                "\"cpus\":" + cpus +
            "}," +
            "\"command\":\"docker run --rm multi-tenant-starter benchmark\"" +
            "}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
