package com.portfolio.multitenant;

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
        boolean isBenchmark = args.length > 0 && "benchmark".equals(args[0]);

        SpringApplication app = new SpringApplication(MultitenantApplication.class);
        if (isBenchmark) {
            app.setWebApplicationType(WebApplicationType.NONE);
        }

        ConfigurableApplicationContext ctx = app.run(args);

        if (isBenchmark) {
            int tenantCount = 100;
            if (args.length > 1) {
                try {
                    tenantCount = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }

            TenantBenchmark benchmark = ctx.getBean(TenantBenchmark.class);
            BenchmarkResult result = benchmark.run(tenantCount);
            String json = result.toJson();
            System.out.println(json);

            try {
                Path resultPath = Path.of("benchmarks", "results", "benchmark.json");
                Files.createDirectories(resultPath.getParent());
                Files.writeString(resultPath, json);
            } catch (Exception e) {
                System.err.println("Warning: could not write benchmark file: " + e.getMessage());
            }

            SpringApplication.exit(ctx, () -> 0);
        }
    }
}
