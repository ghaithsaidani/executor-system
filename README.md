# Executor System Documentation

## Overview

The Executor system is a Spring Boot application that provides a REST API for executing commands on both Kubernetes pods and Docker containers. It uses a three-tier architecture with asynchronous execution powered by Java 21 virtual threads. [1](#0-0)

## Architecture

### Three-Tier Design

The system follows a classic three-tier architecture pattern:

[![](https://mermaid.ink/img/pako:eNqFU01P3DAQ_SvRHBCVsqts4rAhSD0UDm1VpKpwoubg3QxZq4kd2Q7qdtn_zjgfdMOC8Glm_J7f80u8g7UuEHIojWg2we0XrgJatl31g58GLSonnNTqh9ii-c3hcBZ0Qw73Pc-v73p1qZUzuqo6-KR_QaIq-uKV4A2aR7nGUWto35YZNnuNEXkqlUPzINb46R38t7qpJhw_-NDYlXBidOXrty39wkZb6bTZ9gr_-wmSAlm3xlCKX4XdXIuG0Eezk8pd3DgjVRn6o09Kd_Gey0nGwWz2-YlDgRWWwqENnObwdBDAC2fMbDbvGJJywJoMYBGstlNOl5E6DnIQa9BYaZ0NHqUYiAdXV0fpDDRLNRmUynOOAoCQfktZQO5MiyHUaGrhW9j5Ezm4DbnlkFNZCPOHA1d74jRC3WldjzSj23IzNm1TUCRXUtBHJcSDqKyHUJxoLnWrHOSL6Lw7A_Id_IU8iZI5y-IoThfsjGWMhbAlUJLOo2UaLZOYxYtsme5D-NeJRvMzlmYsOs9YkrAkS-IQsPCXvu5fWvfg9s-YECLk?type=png)](https://mermaid.live/edit#pako:eNqFU01P3DAQ_SvRHBCVsqts4rAhSD0UDm1VpKpwoubg3QxZq4kd2Q7qdtn_zjgfdMOC8Glm_J7f80u8g7UuEHIojWg2we0XrgJatl31g58GLSonnNTqh9ii-c3hcBZ0Qw73Pc-v73p1qZUzuqo6-KR_QaIq-uKV4A2aR7nGUWto35YZNnuNEXkqlUPzINb46R38t7qpJhw_-NDYlXBidOXrty39wkZb6bTZ9gr_-wmSAlm3xlCKX4XdXIuG0Eezk8pd3DgjVRn6o09Kd_Gey0nGwWz2-YlDgRWWwqENnObwdBDAC2fMbDbvGJJywJoMYBGstlNOl5E6DnIQa9BYaZ0NHqUYiAdXV0fpDDRLNRmUynOOAoCQfktZQO5MiyHUaGrhW9j5Ezm4DbnlkFNZCPOHA1d74jRC3WldjzSj23IzNm1TUCRXUtBHJcSDqKyHUJxoLnWrHOSL6Lw7A_Id_IU8iZI5y-IoThfsjGWMhbAlUJLOo2UaLZOYxYtsme5D-NeJRvMzlmYsOs9YkrAkS-IQsPCXvu5fWvfg9s-YECLk)

### Layer Responsibilities

#### Presentation Layer
`JobController` exposes REST endpoints at `/api/v1/jobs/*` and handles HTTP request/response transformation.

#### Service Layer
`JobServiceImpl` contains core business logic including job persistence, asynchronous execution, and platform abstraction.

#### Data Layer
`JobRepository` provides in-memory persistence using `ConcurrentHashMap` for thread-safe job storage.

## API Endpoints

| Endpoint | Method | Purpose | Executor Type |
|----------|--------|---------|---------------|
| `GET /api/v1/jobs` | GET | List all jobs | N/A |
| `GET /api/v1/jobs/{id}` | GET | Get job status | N/A |
| `POST /api/v1/jobs/pod/execute` | POST | Execute on Kubernetes | `KUBERNETES` |
| `POST /api/v1/jobs/container/execute` | POST | Execute on Docker | `DOCKER` |


## Job Model

### Job Structure
```java
public class Job {
    private String id;                    // UUID identifier
    private String command;               // Command to execute
    private JobStatus status = JobStatus.QUEUED;  // Current state
    private NecessaryResources necessaryResources;  // Resource specs
    private String output;                // Execution output
}
```


### Job Status
Jobs progress through three states: `QUEUED` → `IN_PROGRESS` → `FINISHED`.

### Resource Model
`NecessaryResources` supports both Kubernetes (request/limit pairs) and Docker (simple values) resource specifications.

## Asynchronous Execution

The system uses Java 21 virtual threads for non-blocking job execution:

[![](https://mermaid.ink/img/pako:eNqdlG2vmjAUx79K01eaIKCCOpLdZFPuxh68zoe92FiWCmfChJZbWqczfve1ojc6uVkyXjS0Pf_f-Z9T6B5HLAbs4RIeJdAIRilZcZKHFKmnIFykUVoQKtAwS4GK2_V3bDlkVHCWZcBrt2fAN2kEQV5kt_ufUy4kyeYJBxL7W4ikYPxriM-vJzFqnAJRFVk2Q_ztluZvBXBKsglnEZSl4qzlEiKRWTGL1sDR8EPwJKzGqq7W3d1VIR4K8eRhNkcWKVJr07Z-smVpFSy24GgMQlzJr1QV5aJejTkJGopgoPeL1_507M_9WfOCcKGoEFMoWJmq8neaQOJYrWlAE7VQKYiQ5ctPC3_hj56F1PZVw4ZMBYAgywzupZAcTC7pq3JHo8ZzlhSu6pLWT0GJqA5Bv1KRoGCEGleWnijVOGYCENuo3tdaMv46Mp3i6CbhjDJZoiowZfSMrcXUtW1RxETlPpkLxt8n04c3U382-xepxtLpM0KqV8g0TdRqESFIlPyvKXWWJpOikAIRGp893gfjYPZWnyo28IqnMfYEl2DgHHhO9BTvdb4QiwRy9Q1qYkz4Wts4KI36Cb4wlp9lnMlVcp7IY-bT_429HyQrdQjQGPiQSSqw17bdIwN7e7zFXtfums6gY3fcttNzBo5j4J0K6rqm3XftfrfjdNqDvnsw8O9jUtvsOe7Aadt2p_fCcbp2x8AQ69I_VpfM8a45_AEJF4HX?type=png)](https://mermaid.live/edit#pako:eNqdlG2vmjAUx79K01eaIKCCOpLdZFPuxh68zoe92FiWCmfChJZbWqczfve1ojc6uVkyXjS0Pf_f-Z9T6B5HLAbs4RIeJdAIRilZcZKHFKmnIFykUVoQKtAwS4GK2_V3bDlkVHCWZcBrt2fAN2kEQV5kt_ufUy4kyeYJBxL7W4ikYPxriM-vJzFqnAJRFVk2Q_ztluZvBXBKsglnEZSl4qzlEiKRWTGL1sDR8EPwJKzGqq7W3d1VIR4K8eRhNkcWKVJr07Z-smVpFSy24GgMQlzJr1QV5aJejTkJGopgoPeL1_507M_9WfOCcKGoEFMoWJmq8neaQOJYrWlAE7VQKYiQ5ctPC3_hj56F1PZVw4ZMBYAgywzupZAcTC7pq3JHo8ZzlhSu6pLWT0GJqA5Bv1KRoGCEGleWnijVOGYCENuo3tdaMv46Mp3i6CbhjDJZoiowZfSMrcXUtW1RxETlPpkLxt8n04c3U382-xepxtLpM0KqV8g0TdRqESFIlPyvKXWWJpOikAIRGp893gfjYPZWnyo28IqnMfYEl2DgHHhO9BTvdb4QiwRy9Q1qYkz4Wts4KI36Cb4wlp9lnMlVcp7IY-bT_429HyQrdQjQGPiQSSqw17bdIwN7e7zFXtfums6gY3fcttNzBo5j4J0K6rqm3XftfrfjdNqDvnsw8O9jUtvsOe7Aadt2p_fCcbp2x8AQ69I_VpfM8a45_AEJF4HX)

## Execution Strategies

### Kubernetes Execution
Executes commands using `kubectl run` with resource requests and limits:

```bash
kubectl run executor-{job.id} 
  --image=alpine 
  --restart=Never 
  --rm 
  --attach 
  --overrides={json_spec}
```


### Docker Execution
Executes commands using `docker run` with simple resource allocation:

```bash
docker run 
  --rm 
  -i 
  --name executor-{job.id} 
  --cpus={cpu} 
  --memory={memory} 
  nginx sh -c {command}
```

## Example Usage

### Kubernetes Execution
```http
POST http://localhost:8080/api/v1/jobs/pod/execute
Content-Type: application/json

{
  "command": "echo Hello from Kubernetes Pod && sleep 20 && echo Finished",
  "necessaryResources": {
    "cpuRequest": "500m",
    "cpuLimit": "1",
    "memoryRequest": "128Mi",
    "memoryLimit": "256Mi"
  }
}
```

### Docker Execution
```http
POST http://localhost:8080/api/v1/jobs/container/execute
Content-Type: application/json

{
  "command": "echo Hello from Docker container && sleep 5 && echo Finished",
  "necessaryResources": {
    "cpu": "1",
    "memory": "512m"
  }
}
```

## Technical Implementation Details

### Process Execution
Both strategies use `ProcessBuilder` to invoke external CLI tools with error stream redirection and output capture via `BufferedReader`.

### Thread Safety
- `ConcurrentHashMap` for thread-safe job storage 
- Virtual threads for lightweight concurrency
- Linear state transitions prevent race conditions

### Key Architectural Decisions
| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| In-memory storage | Simplicity, fast access | No persistence across restarts |
| Virtual threads | High concurrency, low overhead | Requires Java 21+ |
| CLI invocation | No SDK dependencies | Requires CLI tools installed |
