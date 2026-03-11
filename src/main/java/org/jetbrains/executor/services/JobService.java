package org.jetbrains.executor.services;

import org.jetbrains.executor.enums.Executor;
import org.jetbrains.executor.enums.JobStatus;
import org.jetbrains.executor.models.Job;

import java.util.List;

public interface JobService {
    void save(Job job);
    Job execute(Job job, Executor executor);
    JobStatus getStatusByID(String id);
    List<Job> getAll();
}
