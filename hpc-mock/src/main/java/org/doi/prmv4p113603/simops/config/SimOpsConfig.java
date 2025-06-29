package org.doi.prmv4p113603.simops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SimOpsConfig {

    /*
     * NOTE: This hpc-mock application is nothing but a mini-job scheduler inside a
     *  Spring application. It essentially deals with asynchronous execution in Spring;
     *  where instead of blocking the main thread (usually handling HTTP requests), it offloads
     *  long-running tasks to a thread pool using ThreadPoolTaskExecutor.
     *
     * NOTE: ThreadPoolTaskExecutor is a Spring abstraction over Java’s ThreadPoolExecutor,
     *  offering flexible and configurable multi-threaded task execution.
     */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // number of concurrent "HPC nodes"; TODO: from application.properties
        executor.setMaxPoolSize(2); // max number of jobs running at the same time in each "HPC node"; TODO: from application.properties
        executor.setQueueCapacity(100); // queue size before rejecting tasks; TODO: how to deal with in MlOps?
        executor.setThreadNamePrefix("SimOpsJob-");
        executor.initialize();

        return executor;

    }

}
