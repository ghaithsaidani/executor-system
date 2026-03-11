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

                if (executor == Executor.KUBERNETES) {
                    deletePod("executor-" + job.getId());
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            } finally {
                job.setStatus(JobStatus.FINISHED);
                jobService.save(job);
            }
        });
        future.join();
        return job;
    }

    private static Process getPodProcess(Job job) throws IOException {

        String overrides = """
                {
                  "spec": {
                    "containers": [{
                      "name": "executor-%s",
                      "image": "alpine",
                      "command": ["sh","-c","%s"],
                      "resources": {
                        "requests": {
                          "cpu": "%s",
                          "memory": "%s"
                        },
                        "limits": {
                          "cpu": "%s",
                          "memory": "%s"
                        }
                      }
                    }]
                  }
                }
                """.formatted(
                job.getId(),
                job.getCommand().replace("\"", "\\\""),
                job.getNecessaryResources().getCpuRequest(),
                job.getNecessaryResources().getMemoryRequest(),
                job.getNecessaryResources().getCpuLimit(),
                job.getNecessaryResources().getMemoryLimit()
        );

        ProcessBuilder processBuilder = new ProcessBuilder(
                "kubectl", "run", "executor-" + job.getId(),
                "--image=alpine",
                "--restart=Never",
                "--rm",
                "--attach",
                "--overrides", overrides
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
                "--cpus=" + job.getNecessaryResources().getCpu(),
                "--memory=" + job.getNecessaryResources().getMemory(),
                "nginx",
                "sh", "-c", job.getCommand()
        );

        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    private static void deletePod(String podName) throws IOException, InterruptedException {

        ProcessBuilder deleteBuilder = new ProcessBuilder(
                "kubectl", "delete", "pod", podName
        );

        deleteBuilder.redirectErrorStream(true);

        Process deleteProcess = deleteBuilder.start();
        deleteProcess.waitFor();
    }

}
