package org.jetbrains.executor.controllers;


import io.fabric8.kubernetes.api.model.PodList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.executor.services.PodExecutorService;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pods")
@RequiredArgsConstructor
@NullMarked
public class PodExecutorController {
    private final PodExecutorService podExecutorService;

    @GetMapping("")
    public ResponseEntity<PodList> getPods() {
        return ResponseEntity.ok(podExecutorService.getPods());
    }
}
