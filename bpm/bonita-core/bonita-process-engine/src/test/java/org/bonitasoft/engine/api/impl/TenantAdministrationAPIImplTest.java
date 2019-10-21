/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.tenant.TenantResourceType.BDM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.resources.STenantResource;
import org.bonitasoft.engine.resources.STenantResourceState;
import org.bonitasoft.engine.resources.TenantResourceType;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.tenant.TenantManager;
import org.bonitasoft.engine.tenant.TenantResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantAdministrationAPIImplTest {

    @Mock
    private TenantResourcesService tenantResourcesService;
    @Mock
    private PlatformServiceAccessor platformServiceAccessor;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    @Mock
    private TenantManager tenantManager;

    @Spy
    @InjectMocks
    private TenantAdministrationAPIImpl tenantManagementAPI;

    @Before
    public void before() throws Exception {
        doReturn(platformServiceAccessor).when(tenantManagementAPI).getPlatformAccessorNoException();
        doReturn(17L).when(tenantManagementAPI).getTenantId();
        doReturn(tenantServiceAccessor).when(tenantManagementAPI).getTenantAccessor();
        doReturn(tenantResourcesService).when(tenantServiceAccessor).getTenantResourcesService();

        when(platformServiceAccessor.getTenantServiceAccessor(17)).thenReturn(tenantServiceAccessor);

        when(tenantServiceAccessor.getBusinessArchiveArtifactsManager()).thenReturn(businessArchiveArtifactsManager);
        when(tenantServiceAccessor.getTenantManager()).thenReturn(tenantManager);
    }

    @Test
    public void resume_should_have_annotation_available_when_tenant_is_paused() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("resume");

        final boolean present = method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                || TenantAdministrationAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class);

        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused should be present on API method 'resume' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    @Test
    public void pause_should_have_annotation_available_when_tenant_is_paused() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("pause");

        final boolean present = method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                || TenantAdministrationAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class);

        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused should be present on API method 'pause' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    @Test
    public void pageApi_should_be_available_in_maintenance_mode() {
        // given:
        final Class<PageAPIImpl> classPageApiExt = PageAPIImpl.class;

        // then:
        assertThat(classPageApiExt.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as(
                "Annotation @AvailableOnMaintenanceTenant should be present on PageAPIIml");
    }

    @Test
    public void resume_should_resolve_dependencies_for_deployed_processes() throws Exception {
        tenantManagementAPI.resume();

        verify(businessArchiveArtifactsManager).resolveDependenciesForAllProcesses(tenantServiceAccessor);
    }



    @Test
    public void installBDR_should_be_available_when_tenant_is_paused_ONLY() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("installBusinessDataModel", byte[].class);
        final AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);

        final boolean present = annotation != null && annotation.onlyAvailableWhenPaused();
        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'installBusinessDataModel(byte[])'")
                .isTrue();
    }

    @Test
    public void uninstallBDR_should_be_available_when_tenant_is_paused_ONLY() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("uninstallBusinessDataModel");
        final AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);

        final boolean present = annotation != null && annotation.onlyAvailableWhenPaused();
        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'uninstallBusinessDataModel()'").isTrue();
    }

    @Test
    public void uninstallBusinessDataModel_should_work() throws Exception {
        // Given
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        when(tenantServiceAccessor.getBusinessDataModelRepository()).thenReturn(repository);

        // When
        tenantManagementAPI.uninstallBusinessDataModel();

        // Then
        verify(repository).uninstall(anyLong());
    }

    @Test(expected = BusinessDataRepositoryException.class)
    public void uninstallBusinessDataModel_should_throw_BusinessDataRepositoryException() throws Exception {
        // Given
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        when(tenantServiceAccessor.getBusinessDataModelRepository()).thenReturn(repository);
        doThrow(new SBusinessDataRepositoryException("error")).when(repository).uninstall(anyLong());

        // When
        tenantManagementAPI.uninstallBusinessDataModel();
    }

    @Test
    public void getTenantResource_should_retrieve_resource_from_tenantResourceService() throws Exception {
        // Given
        final STenantResource toBeReturned = new STenantResource("some name",
                TenantResourceType.BDM, null, 111L, 222L, STenantResourceState.INSTALLED);
        doReturn(toBeReturned).when(tenantResourcesService).getSingleLightResource(TenantResourceType.BDM);

        // When
        tenantManagementAPI.getTenantResource(BDM);

        // Then
        verify(tenantResourcesService).getSingleLightResource(TenantResourceType.BDM);
    }

    @Test
    public void getTenantResource_should_return_NONE_if_exception() throws Exception {
        // Given
        doThrow(SBonitaReadException.class).when(tenantResourcesService).getSingleLightResource(any(TenantResourceType.class));

        // When
        final TenantResource tenantResource = tenantManagementAPI.getTenantResource(BDM);

        // Then
        assertThat(tenantResource).isEqualTo(TenantResource.NONE);
    }

    @Test
    public void getBusinessDataModelResource_should_get_resource_for_BDM_type() {
        // given:
        doReturn(mock(TenantResource.class)).when(tenantManagementAPI)
                .getTenantResource(any(org.bonitasoft.engine.tenant.TenantResourceType.class));

        // when:
        tenantManagementAPI.getBusinessDataModelResource();

        // then:
        verify(tenantManagementAPI).getTenantResource(BDM);
    }
}
