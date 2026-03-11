package org.jetbrains.executor.controllers;

import lombok.RequiredArgsConstructor;
import org.jetbrains.executor.dto.JobDTO;
import org.jetbrains.executor.enums.Executor;
import org.jetbrains.executor.models.Job;
import org.jetbrains.executor.services.JobService;
import org.jetbrains.executor.services.ExecutorService;
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
    private final ExecutorService kubernetesPodExecutorService;

    @GetMapping("")
    public ResponseEntity<List<Job>> getAllJobs(){
        return ResponseEntity.ok(jobService.getAll());
    }

    @PostMapping("/pod/execute")
    public ResponseEntity<Job> executeJobOnKubernetesPod(@RequestBody JobDTO jobDTO){
        Job job = new Job(jobDTO.command(), jobDTO.necessaryResources());
        return ResponseEntity.ok(kubernetesPodExecutorService.execute(job, Executor.KUBERNETES));
    }

    @PostMapping("/container/execute")
    public ResponseEntity<Job> executeJobOnDockerContainer(@RequestBody JobDTO jobDTO){
        Job job = new Job(jobDTO.command(), jobDTO.necessaryResources());
        return ResponseEntity.ok(kubernetesPodExecutorService.execute(job, Executor.DOCKER));
    }
}
