package org.jetbrains.executor.configs;

import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.Getter;
import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
@Getter
public class KubernetesClientManager {
    private final KubernetesClient kubernetesClient;

    public KubernetesClientManager(){
        this.kubernetesClient = new KubernetesClientBuilder().build();
    }
}
