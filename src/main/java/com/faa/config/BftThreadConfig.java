package com.faa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BftThreadConfig {
    private static final int MAX_POOL_SIZE = 50;
    private static final int CORE_POOL_SIZE = 20;

    @Bean("BftTaskExecutor")
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor bftTaskExecutor = new ThreadPoolTaskExecutor();
        bftTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        bftTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        bftTaskExecutor.setThreadNamePrefix("bft-task-");
        bftTaskExecutor.initialize();
        return bftTaskExecutor;
    }
}
