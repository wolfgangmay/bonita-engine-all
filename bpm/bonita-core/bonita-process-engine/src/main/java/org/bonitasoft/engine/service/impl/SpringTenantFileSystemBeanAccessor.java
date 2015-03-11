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
package org.bonitasoft.engine.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.MissingServiceException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author Matthieu Chaffotte
 */
public class SpringTenantFileSystemBeanAccessor {

    protected static final String TENANT_ID = "tenantId";

    protected AbsoluteFileSystemXmlApplicationContext context;

    private final long tenantId;

    public SpringTenantFileSystemBeanAccessor(final long tenantId) {
        this.tenantId = tenantId;
    }

    private String[] getResources() {
        final BonitaHomeServer homeServer = BonitaHomeServer.getInstance();
        try {
            return homeServer.getTenantConfigurationFiles(tenantId);
        } catch (final BonitaHomeNotSetException e) {
            throw new RuntimeException("Bonita home not set");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CustomPropertySource getCustomPropertySource() {
        try {
            return new CustomPropertySource("tenant", BonitaHomeServer.getInstance().getTenantProperties(tenantId));
        } catch (final BonitaHomeNotSetException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getService(final Class<T> serviceClass) {
        try {

            return getContext().getBean(serviceClass);
        } catch (NoSuchBeanDefinitionException e) {
            throw new MissingServiceException("Service not found: " + serviceClass.getName());
        }
    }

    protected <T> T getService(final String name, final Class<T> serviceClass) {
        return getContext().getBean(name, serviceClass);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getService(final String name) {
        return (T) getContext().getBean(name);
    }

    protected FileSystemXmlApplicationContext getContext() {
        if (context == null) {
            initializeContext(null);
        }
        return context;
    }

    public synchronized void initializeContext(final ClassLoader classLoader) {
        if (context == null) {// synchronized null check
            SpringPlatformFileSystemBeanAccessor.initializeContext(classLoader);
            final FileSystemXmlApplicationContext platformContext = SpringPlatformFileSystemBeanAccessor.getContext();
            // Delay the refresh so we can set our BeanFactoryPostProcessor to be able to resolve the placeholder.
            final AbsoluteFileSystemXmlApplicationContext localContext = new AbsoluteFileSystemXmlApplicationContext(getResources(), false /* refresh */, platformContext);
            localContext.getEnvironment().getPropertySources().addLast(getCustomPropertySource());
            localContext.refresh();
            this.context = localContext;
        }
    }

    public void destroy() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

}
