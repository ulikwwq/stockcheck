package com.stockcheck.backend.category;

import com.stockcheck.backend.category.dto.CreateCategoryRequest;
import com.stockcheck.backend.category.dto.CategoryResponse;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;

    public CategoryService(CategoryRepository categoryRepository, TenantRepository tenantRepository) {
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found: " + tenantId));

        if (categoryRepository.findByTenantIdAndName(tenantId, request.getName().trim()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category '" + request.getName() + "' already exists for this business"
            );
        }

        Category category = categoryRepository.save(new Category(tenant, request.getName().trim()));
        return CategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesForCurrentTenant() {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        return categoryRepository.findByTenantId(tenantId).stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }
}
