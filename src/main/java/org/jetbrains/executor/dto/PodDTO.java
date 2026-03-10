package org.jetbrains.executor.dto;

public record PodDTO(
        String name, String namespace, String phase
) {
}
