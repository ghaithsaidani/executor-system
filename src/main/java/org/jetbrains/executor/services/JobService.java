package org.jetbrains.executor.services;

import org.jetbrains.executor.enums.JobStatus;
import org.jetbrains.executor.models.Job;

import java.util.List;

public interface JobService {
    void save(Job job);
    JobStatus getStatusByID(String id);
    List<Job> getAll();
}
