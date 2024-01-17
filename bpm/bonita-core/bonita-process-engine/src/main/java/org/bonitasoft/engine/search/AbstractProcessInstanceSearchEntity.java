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
package org.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractProcessInstanceSearchEntity
        extends AbstractSearchEntity<ProcessInstance, SProcessInstance> {

    private final ProcessDefinitionService processDefinitionService;

    public AbstractProcessInstanceSearchEntity(final SearchEntityDescriptor searchDescriptor,
            final SearchOptions options,
            final ProcessDefinitionService processDefinitionService) {
        super(searchDescriptor, options);
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public List<ProcessInstance> convertToClientObjects(final List<SProcessInstance> serverObjects) {
        return ModelConvertor.toProcessInstances(serverObjects, processDefinitionService);
    }

}
