package org.jetbrains.executor.services.impl;

import io.fabric8.kubernetes.api.model.PodList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.executor.configs.KubernetesClientManager;
import org.jetbrains.executor.services.PodExecutorService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PodExecutorServiceImpl implements PodExecutorService {
    private final KubernetesClientManager clientManager;
    @Override
    public PodList getPods() {
        return clientManager.getKubernetesClient().pods().list();
    }
}
