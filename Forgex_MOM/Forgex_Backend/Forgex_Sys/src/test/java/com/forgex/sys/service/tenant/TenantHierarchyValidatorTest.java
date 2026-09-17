package com.forgex.sys.service.tenant;

import com.forgex.common.enums.TenantTypeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantHierarchyValidatorTest {

    @Test
    void mainTenantMayOmitParent() {
        assertDoesNotThrow(() -> TenantHierarchyValidator.validate(
                TenantTypeEnum.MAIN_TENANT, null, false));
    }

    @Test
    void childTenantRequiresParent() {
        assertThrows(IllegalArgumentException.class, () -> TenantHierarchyValidator.validate(
                TenantTypeEnum.CUSTOMER_TENANT, null, false));
    }

    @Test
    void childTenantParentMustBeMainTenant() {
        assertThrows(IllegalArgumentException.class, () -> TenantHierarchyValidator.validate(
                TenantTypeEnum.CUSTOMER_TENANT, 2L, false));
    }
}
