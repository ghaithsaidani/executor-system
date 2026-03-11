package org.jetbrains.executor.services.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.executor.enums.Executor;
import org.jetbrains.executor.enums.JobStatus;
import org.jetbrains.executor.models.Job;
import org.jetbrains.executor.repositories.JobRepository;
import org.jetbrains.executor.services.JobService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final ExecutorService executorService =
            Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void save(Job job) {
        jobRepository.addJob(job);
    }

    @Override
    public Job execute(Job job, Executor executor) {

        this.save(job);
        CompletableFuture.runAsync(() -> {
            try {
                job.setStatus(JobStatus.IN_PROGRESS);
                this.save(job);

                Process process = executor == Executor.KUBERNETES ?
                        getPodProcess(job) :
                        getContainerProcess(job);

                StringBuilder output = new StringBuilder();

                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }
                job.setOutput(output.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                job.setStatus(JobStatus.FINISHED);
                this.save(job);
            }
        }, executorService);
        return job;
    }


    @Override
    public JobStatus getStatusByID(String id) {
        return jobRepository.getJobStatusById(id);
    }

    @Override
    public List<Job> getAll() {
        return jobRepository.getAllJobs().stream().toList();
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
}
