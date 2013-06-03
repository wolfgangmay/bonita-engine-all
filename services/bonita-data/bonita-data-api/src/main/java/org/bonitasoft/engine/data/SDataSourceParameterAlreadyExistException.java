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
package org.bonitasoft.engine.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class SDataSourceParameterAlreadyExistException extends SBonitaException {

    private static final long serialVersionUID = 6773573707218746656L;

    private final String name;

    private final long datasourceId;

    public SDataSourceParameterAlreadyExistException(final String name, final long datasourceId) {
        super("A datasource parameter with name: " + name + ", and datasourceId: " + datasourceId + " already exists.");
        this.name = name;
        this.datasourceId = datasourceId;
    }

    public String getName() {
        return name;
    }

    public long getDatasourceId() {
        return datasourceId;
    }
}
