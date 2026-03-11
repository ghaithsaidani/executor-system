package org.jetbrains.executor.repositories;

import org.jetbrains.executor.enums.JobStatus;
import org.jetbrains.executor.models.Job;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Repository

public class JobRepository {
    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    public void addJob(Job job){
        jobs.put(job.getId(), job);
    }

    public JobStatus getJobStatusById(String id){
        return jobs.get(id).getStatus();
    }

    public Collection<Job> getAllJobs(){
        return jobs.values();
    }
}
