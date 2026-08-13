package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.CustomCategoryRequest;
import com.finance.dashboard.dto.response.CustomCategoryResponse;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.CustomCategory;
import com.finance.dashboard.repository.CustomCategoryRepository;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomCategoryService {

    private final CustomCategoryRepository repo;
    private final WorkspaceService workspaceService;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<CustomCategoryResponse> getAll() {
        Long wsId = workspaceService.getMyWorkspaceId();
        return repo.findByWorkspaceIdOrSystem(wsId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CustomCategoryResponse create(CustomCategoryRequest req) {
        Long wsId = workspaceService.getMyWorkspaceId();
        String nameTrimmed = req.getName().trim();
        if (repo.existsByNameAndWorkspaceId(nameTrimmed, wsId))
            throw new BadRequestException("Category '" + nameTrimmed + "' already exists");
        CustomCategory cat = repo.save(CustomCategory.builder()
                .name(nameTrimmed)
                .color(req.getColor())
                .type(req.getType())
                .workspaceId(wsId)
                .createdBy(securityUtils.getCurrentUser())
                .system(false)
                .build());
        return toResponse(cat);
    }

    @Transactional
    public CustomCategoryResponse update(Long id, CustomCategoryRequest req) {
        Long wsId = workspaceService.getMyWorkspaceId();
        CustomCategory cat = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (cat.isSystem())
            throw new BadRequestException("System categories cannot be modified");
        if (!wsId.equals(cat.getWorkspaceId()))
            throw new BadRequestException("You don't have access to this category");
        cat.setName(req.getName().trim());
        cat.setColor(req.getColor());
        cat.setType(req.getType());
        return toResponse(repo.save(cat));
    }

    @Transactional
    public void delete(Long id) {
        Long wsId = workspaceService.getMyWorkspaceId();
        CustomCategory cat = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (cat.isSystem())
            throw new BadRequestException("System categories cannot be deleted");
        if (!wsId.equals(cat.getWorkspaceId()))
            throw new BadRequestException("You don't have access to this category");
        repo.delete(cat);
    }

    private CustomCategoryResponse toResponse(CustomCategory c) {
        return CustomCategoryResponse.builder()
                .id(c.getId()).name(c.getName())
                .color(c.getColor()).type(c.getType())
                .system(c.isSystem()).build();
    }
}