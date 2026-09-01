package com.stockcheck.backend.category;

import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("category belongs to a tenant and can be retrieved by tenant ID")
    void shouldPersistAndRetrieveCategory() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Electronics Store"));
        Category category = categoryRepository.saveAndFlush(new Category(tenant, "Smartphones"));

        assertThat(category.getId()).isNotNull();
        assertThat(category.getTenant().getId()).isEqualTo(tenant.getId());

        List<Category> categories = categoryRepository.findByTenantId(tenant.getId());
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Smartphones");

        Optional<Category> found = categoryRepository.findByTenantIdAndName(tenant.getId(), "Smartphones");
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("duplicate category name within same tenant is rejected by unique constraint")
    void shouldRejectDuplicateCategoryWithinSameTenant() {
        Tenant tenant = tenantRepository.saveAndFlush(new Tenant("Grocery Store"));
        categoryRepository.saveAndFlush(new Category(tenant, "Beverages"));

        Category duplicate = new Category(tenant, "Beverages");
        assertThrows(DataIntegrityViolationException.class, () -> categoryRepository.saveAndFlush(duplicate));
    }
}
