/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.search.process;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractArchivedProcessInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class SearchArchivedProcessInstancesSupervisedBy extends AbstractArchivedProcessInstanceSearchEntity {

    private final long userId;

    private final ProcessInstanceService processInstanceService;

    public SearchArchivedProcessInstancesSupervisedBy(final long userId,
            final ProcessInstanceService processInstanceService,
            final ProcessDefinitionService processDefinitionService, final SearchEntityDescriptor searchDescriptor,
            final SearchOptions options) {
        super(searchDescriptor, options, processDefinitionService);
        this.userId = userId;
        this.processInstanceService = processInstanceService;

    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return processInstanceService.getNumberOfArchivedProcessInstancesSupervisedBy(userId, searchOptions);
    }

    @Override
    public List<SAProcessInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return processInstanceService.searchArchivedProcessInstancesSupervisedBy(userId, searchOptions);
    }

}
