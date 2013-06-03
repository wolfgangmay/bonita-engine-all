/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.model.builder.impl;

import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilder;
import org.bonitasoft.engine.data.model.impl.SDataSourceImpl;

public class SDataSourceBuilderImpl implements SDataSourceBuilder {

    private SDataSourceImpl object;

    @Override
    public SDataSourceBuilder createNewInstance(final String name, final String version, final SDataSourceState state, final String implementationClassName) {
        this.object = new SDataSourceImpl(name, version, state, implementationClassName);
        return this;
    }

    @Override
    public SDataSourceBuilder setState(final SDataSourceState state) {
        this.object.setState(state);
        return this;
    }

    @Override
    public SDataSource done() {
        return object;
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getImplementationClassNameKey() {
        return "implementationClassName";
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getVersionKey() {
        return "version";
    }

    @Override
    public String getStateKey() {
        return "state";
    }

}
