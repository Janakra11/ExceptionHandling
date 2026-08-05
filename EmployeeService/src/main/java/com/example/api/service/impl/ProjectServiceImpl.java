package com.example.api.service.impl;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Project;
import com.example.api.repository.ProjectRepository;
import com.example.api.service.ProjectService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "projectCache", key = "#id")
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project matrix segment empty for ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    @CachePut(cacheNames = "projectCache", key = "#id")
    public Project updateProject(Long id, Project updateData) {
        Project existing = getProjectById(id);
        existing.setName(updateData.getName());
        existing.setBudget(updateData.getBudget());
        return projectRepository.saveAndFlush(existing);
    }

    @Override
    @CacheEvict(cacheNames = "projectCache", key = "#id")
    public void deleteProject(Long id) {
        Project existing = getProjectById(id);
        projectRepository.delete(existing);
    }
}
