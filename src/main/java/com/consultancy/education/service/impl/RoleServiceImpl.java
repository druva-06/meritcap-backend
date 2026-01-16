package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.role.RoleRequestDto;
import com.consultancy.education.DTOs.responseDTOs.role.RoleResponseDto;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.BadRequestException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.Role;
import com.consultancy.education.repository.RoleRepository;
import com.consultancy.education.service.RoleService;
import com.consultancy.education.transformer.RoleTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleRequestDto requestDto, Long createdBy) {
        log.info("Creating new role: {}", requestDto.getName());

        // Check if role already exists
        if (roleRepository.existsByNameIgnoreCase(requestDto.getName())) {
            throw new AlreadyExistException(List.of("Role with name '" + requestDto.getName() + "' already exists"));
        }

        Role role = RoleTransformer.requestDtoToRole(requestDto);
        role.setCreatedBy(createdBy);
        role.setUpdatedBy(createdBy);

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", savedRole.getId());

        return RoleTransformer.roleToResponseDto(savedRole);
    }

    @Override
    @Transactional
    public RoleResponseDto updateRole(Long roleId, RoleRequestDto requestDto, Long updatedBy) {
        log.info("Updating role with ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId));

        // Check if trying to update a system role
        if (role.getIsSystemRole()) {
            throw new BadRequestException("System roles cannot be modified");
        }

        // Check if new name conflicts with existing role
        if (requestDto.getName() != null && !requestDto.getName().equalsIgnoreCase(role.getName())) {
            if (roleRepository.existsByNameIgnoreCase(requestDto.getName())) {
                throw new AlreadyExistException(
                        List.of("Role with name '" + requestDto.getName() + "' already exists"));
            }
        }

        RoleTransformer.updateRoleFromDto(role, requestDto);
        role.setUpdatedBy(updatedBy);

        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", roleId);

        return RoleTransformer.roleToResponseDto(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId, Long deletedBy) {
        log.info("Deleting role with ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId));

        // Check if trying to delete a system role
        if (role.getIsSystemRole()) {
            throw new BadRequestException("System roles cannot be deleted");
        }

        // TODO: Check if any users are assigned this role before deletion
        // Could implement soft delete instead of hard delete

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", roleId);
    }

    @Override
    public RoleResponseDto getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId));
        return RoleTransformer.roleToResponseDto(role);
    }

    @Override
    public RoleResponseDto getRoleByName(String name) {
        Role role = roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Role not found with name: " + name));
        return RoleTransformer.roleToResponseDto(role);
    }

    @Override
    public List<RoleResponseDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleTransformer::roleToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleResponseDto> getActiveRoles() {
        return roleRepository.findByIsActiveTrue().stream()
                .map(RoleTransformer::roleToResponseDto)
                .collect(Collectors.toList());
    }
}
