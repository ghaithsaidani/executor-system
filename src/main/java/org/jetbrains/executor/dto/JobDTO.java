package org.jetbrains.executor.dto;

import lombok.Builder;
import org.jetbrains.executor.models.NecessaryResources;

@Builder
public record JobDTO(
        String command,
        NecessaryResources necessaryResources
) {
}
