package org.jetbrains.executor.services;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;

public interface PodExecutorService {
    //public void execute();
    public PodList getPods();
}
