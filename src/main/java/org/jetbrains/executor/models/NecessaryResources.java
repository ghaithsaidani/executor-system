package org.jetbrains.executor.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class NecessaryResources {
    private Integer cpu;
    private Integer memory;
}
