package org.jetbrains.executor.services.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.executor.enums.Executor;
import org.jetbrains.executor.enums.JobStatus;
import org.jetbrains.executor.models.Job;
import org.jetbrains.executor.services.ExecutorService;
import org.jetbrains.executor.services.JobService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ExecutorServiceImpl implements ExecutorService {
    private final JobService jobService;

    @Override
    public Job execute(Job job, Executor executor) {
        job.setStatus(JobStatus.IN_PROGRESS);
        jobService.save(job);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                Process process = executor == Executor.KUBERNETES ?
                        getPodProcess(job) :
                        getContainerProcess(job);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = process.waitFor();
                System.out.println("Command finished with exit code: " + exitCode);
                job.setStatus(JobStatus.FINISHED);
            } catch (InterruptedException | IOException e) {
                job.setStatus(JobStatus.FAILED);
                throw new RuntimeException(e);
            } finally {
                jobService.save(job);
            }
        });
        future.join();
        return job;
    }

    private static Process getPodProcess(Job job) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "kubectl", "run", "executor-" + job.getId(),
                "--image=nginx",
                "--restart=Never",
                "--rm",
                "--attach",
                "--command",
                "--",
                "sh", "-c", job.getCommand()
        );

        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    private static Process getContainerProcess(Job job) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run",
                "--rm",
                "-i",
                "--name", "executor-" + job.getId(),
                "nginx",
                "sh", "-c", job.getCommand()
        );

        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

}
