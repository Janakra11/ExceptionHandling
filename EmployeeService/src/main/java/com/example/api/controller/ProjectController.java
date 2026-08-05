package com.example.api.controller;

import com.example.api.model.Project;
import com.example.api.service.ProjectService;
import com.example.api.service.IdempotencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final IdempotencyService idempotencyService;

    public ProjectController(ProjectService projectService, IdempotencyService idempotencyService) {
        this.projectService = projectService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Project project) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return new ResponseEntity<>(projectService.createProject(project), HttpStatus.CREATED);
        }

        idempotencyService.validateKey(idempotencyKey);
        try {
            Project saved = projectService.createProject(project);
            idempotencyService.saveResponse(idempotencyKey, saved);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            idempotencyService.removeKey(idempotencyKey);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Project project) {

        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            idempotencyService.validateKey(idempotencyKey);
        }
        try {
            Project updated = projectService.updateProject(id, project);
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                idempotencyService.saveResponse(idempotencyKey, updated);
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                idempotencyService.removeKey(idempotencyKey);
            }
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("Project context profile removed successfully.");
    }
}
