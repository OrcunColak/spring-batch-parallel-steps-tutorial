# Read Me

The original idea is from  
https://medium.com/@sehgal.mohit06/stepexecutionlistener-spring-batch-96756c6dde20

This project demonstrates how to use parallel steps

# Spring Batch Scalability

See https://medium.com/@techie_chandan/spring-batch-scalability-61c7fa50708b

There are five options for scaling Spring Batch jobs:

Multithreaded step
Parallel steps
Partitioning
Remote chunking
AsyncItemProcessor/AsyncItemWriter

# Multithreaded step

Multithreaded steps boost performance by running chunks concurrently, but usually sacrifice restartability.

This is because shared reader/writer state becomes inconsistent across threads, rendering it useless for restarts.
Consequently, saving state for individual components should typically be disabled, and the job marked as
non-restartable.

Example:

The job starts. The reader reads the first 40 items (4 threads * chunk size of 10). Each thread gets a chunk of 10.
The threads start processing their chunks concurrently. Let’s say Thread 1 finishes its chunk, Thread 2 finishes its
chunk, but then the job fails.
Problem: The job’s execution context might show that only 2 chunks are complete (because only 2 threads finished their
work before the failure). However, the reader’s internal state is now beyond item 40 (because other threads read further
ahead). Other complexities arise because ItemWriter usually writes in batch.

# Parallel steps

Parallel steps, executed via splits, enable concurrent execution of independent steps, improving overall throughput
without sacrificing restartability.

However, they don’t enhance the performance of individual steps or specific business logic within a step.

# Partitioning

Spring Batch partitioning improves performance and scalability by dividing data into smaller partitions. A master step
assigns these partitions to independent slave steps, enabling parallel processing. This maintains restartability, unlike
multithreading, and allows scaling beyond a single JVM by supporting remote slave execution.

The master step doesn’t transmit the actual data to be processed, instead, it provides each slave with a description or
specification of the data subset it should handle. For instance, if the task involves processing database records, the
master might instruct slave1 to process records 1–100, slave2 to process records 101–200, and so forth.

The slaves then independently query the database or access the data source using the provided description.

The master merely provides the “address” or instructions for fetching the data; it doesn’t send the data itself.

# Remote chunking

Remote chunking distributes processing across multiple JVMs and optionally writing also across multiple JVMs, enhancing
scalability.

The master retrieves data and transmits it to slave JVMs. Slaves then process and either write locally or return the
results to the master for writing.

The important difference between partitioning and remote chunking is that instead of a address going over to remote
slaves, remote chunking sends the actual data to the slaves. So instead of a single packet saying process records 1–100,
remote chunking is going to send the actual records 1–100.

This can have a large impact on the I/O profile of a step, but if the processor is enough of a bottleneck, this can be
useful.

If processing time is significantly longer than data retrieval and transfer time, remote chunking can be beneficial. By
sending data directly to slaves, you reduce contention on the shared data source and maximize parallel processing.
Slaves can work immediately without needing to fetch data, effectively distributing the processing load.

# AsyncItemProcessor/AsyncItemWriter

The AsyncItemProcessor offloads the processing of each item to a separate thread, immediately returning a Future
representing the eventual processing result.

This Future is then passed to the AsyncItemWriter.

The AsyncItemWriter manages these Futures, waiting for the processing to complete and then passing the unwrapped, fully
processed items to the underlying ItemWriter for final output.

This allows processing and writing to occur concurrently, potentially improving throughput, especially if the processing
logic is time-consuming.

However, this approach doesn’t support all listener scenarios because the ItemProcessor result isn’t known until the
ItemWriter stage. Despite this limitation, it’s a valuable tool for improving performance when processing logic is a
bottleneck.