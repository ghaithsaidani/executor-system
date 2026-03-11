package org.jetbrains.executor.controllers;

import lombok.RequiredArgsConstructor;
import org.jetbrains.executor.dto.JobDTO;
import org.jetbrains.executor.enums.Executor;
import org.jetbrains.executor.enums.JobStatus;
import org.jetbrains.executor.models.Job;
import org.jetbrains.executor.models.NecessaryResources;
import org.jetbrains.executor.services.JobService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@NullMarked
public class JobController {
    private final JobService jobService;

    @GetMapping("")
    public ResponseEntity<List<Job>> getAllJobs(){
        return ResponseEntity.ok(jobService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobStatus> getJobStatusByID(@PathVariable String id){
        return ResponseEntity.ok(jobService.getStatusByID(id));
    }

    @PostMapping("/pod/execute")
    public ResponseEntity<Job> executeJobOnKubernetesPod(@RequestBody JobDTO jobDTO){
        NecessaryResources necessaryResources = NecessaryResources.builder()
                .cpuRequest(jobDTO.necessaryResources().getCpuRequest())
                .cpuLimit(jobDTO.necessaryResources().getCpuLimit())
                .memoryRequest(jobDTO.necessaryResources().getMemoryRequest())
                .memoryLimit(jobDTO.necessaryResources().getMemoryLimit())
                .build();
        Job job = new Job(jobDTO.command(), necessaryResources);
        return ResponseEntity.ok(jobService.execute(job, Executor.KUBERNETES));
    }

    @PostMapping("/container/execute")
    public ResponseEntity<Job> executeJobOnDockerContainer(@RequestBody JobDTO jobDTO){
        NecessaryResources necessaryResources = NecessaryResources.builder()
                .cpu(jobDTO.necessaryResources().getCpu())
                .memory(jobDTO.necessaryResources().getMemory())
                .build();
        Job job = new Job(jobDTO.command(), necessaryResources);
        return ResponseEntity.ok(jobService.execute(job, Executor.DOCKER));
    }
}
