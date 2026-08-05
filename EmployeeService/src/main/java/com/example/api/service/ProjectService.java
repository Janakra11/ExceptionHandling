package com.example.api.service;

import com.example.api.model.Project;
import java.util.List;

public interface ProjectService {
    Project createProject(Project project);
    Project getProjectById(Long id);
    List<Project> getAllProjects();
    Project updateProject(Long id, Project project);
    void deleteProject(Long id);
}
