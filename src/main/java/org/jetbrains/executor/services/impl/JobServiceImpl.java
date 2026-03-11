package org.jetbrains.executor.services.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.executor.enums.JobStatus;
import org.jetbrains.executor.models.Job;
import org.jetbrains.executor.repositories.JobRepository;
import org.jetbrains.executor.services.JobService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    @Override
    public void save(Job job) {
        //Job job = new Job(command, necessaryResources);
        jobRepository.addJob(job);
    }

    @Override
    public JobStatus getStatusByID(String id) {
        return jobRepository.getJobStatusById(id);
    }

    @Override
    public List<Job> getAll() {
        return jobRepository.getAllJobs().stream().toList();
    }
}
