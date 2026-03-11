package org.jetbrains.executor.services;

import org.jetbrains.executor.enums.Executor;
import org.jetbrains.executor.models.Job;

public interface ExecutorService {
    Job execute(Job job, Executor executor) ;
}
