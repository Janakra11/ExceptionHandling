package com.example.api.service.impl;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Role;
import com.example.api.repository.RoleRepository;
import com.example.api.service.RoleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "roleCache", key = "#id")
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role entry missing for ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @CachePut(cacheNames = "roleCache", key = "#id")
    public Role updateRole(Long id, Role updateData) {
        Role existing = getRoleById(id);
        existing.setName(updateData.getName());
        return roleRepository.saveAndFlush(existing);
    }

    @Override
    @CacheEvict(cacheNames = "roleCache", key = "#id")
    public void deleteRole(Long id) {
        Role existing = getRoleById(id);
        roleRepository.delete(existing);
    }
}
