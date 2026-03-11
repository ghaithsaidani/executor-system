package org.jetbrains.executor.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.executor.enums.JobStatus;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job {

    private String id;
    private String command;
    private JobStatus status = JobStatus.QUEUED;
    private NecessaryResources necessaryResources;
    private String output;

    public Job(String command, NecessaryResources necessaryResources){
        this.id = UUID.randomUUID().toString();
        this.command = command;
        this.necessaryResources=necessaryResources;
    }

}
