package com.colak.springtutorial.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job job(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("job1", jobRepository)
                .start(parallelFlow())
                .next(step3())
                .end()
                .build();
    }

    @Bean
    public Flow parallelFlow() {
        return new FlowBuilder<SimpleFlow>("parallelFlow")
                .split(taskExecutor()) // This is where the parallelism happens
                .add(flow1(), flow2()) // Define the flows that will run in parallel
                .build();
    }

    @Bean
    public Flow flow1() {
        return new FlowBuilder<SimpleFlow>("flow1")
                .start(step1())
                .build();
    }

    @Bean
    public Flow flow2() {
        return new FlowBuilder<SimpleFlow>("flow2")
                .start(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step_one", jobRepository)
                .tasklet((_, _) -> {
                    log.info("STEP1 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("step_two", jobRepository)
                .tasklet((_, _) -> {
                    log.info("STEP2 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("three", jobRepository)
                .tasklet((_, _) -> {
                    log.info("STEP3 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        // fires up a new Thread for each task
        return new SimpleAsyncTaskExecutor();
    }


}
