package com.portfolio.multitenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.multitenant.benchmark.BenchmarkResult;
import com.portfolio.multitenant.benchmark.TenantBenchmark;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class MultitenantApplication {

    public static void main(String[] args) {
        boolean benchmarkMode = args.length > 0 && "benchmark".equals(args[0]);
        SpringApplication app = new SpringApplication(MultitenantApplication.class);
        if (benchmarkMode) {
            app.setWebApplicationType(WebApplicationType.NONE);
        }

        ConfigurableApplicationContext context = app.run(args);
        if (!benchmarkMode) {
            return;
        }

        try (context) {
            TenantBenchmark benchmark = context.getBean(TenantBenchmark.class);
            BenchmarkResult result = benchmark.run(
                environmentInteger("BENCHMARK_WARMUP_TENANTS", 2),
                environmentInteger("BENCHMARK_TENANTS", 6),
                environmentInteger("BENCHMARK_QUERIES_PER_TENANT", 25),
                environmentInteger("BENCHMARK_REPETITIONS", 3)
            );

            try {
                Path resultPath = Path.of(System.getenv().getOrDefault(
                    "BENCHMARK_OUTPUT", "benchmarks/results/multi-tenant-starter-v2.json"));
                Files.createDirectories(resultPath.toAbsolutePath().getParent());
                String json = context.getBean(ObjectMapper.class)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
                Files.writeString(resultPath, json + System.lineSeparator());
                System.out.println(json);
            } catch (Exception exception) {
                throw new IllegalStateException("Could not write benchmark result", exception);
            }
        }
    }

    private static int environmentInteger(String name, int fallback) {
        return Integer.parseInt(System.getenv().getOrDefault(name, Integer.toString(fallback)));
    }
}
