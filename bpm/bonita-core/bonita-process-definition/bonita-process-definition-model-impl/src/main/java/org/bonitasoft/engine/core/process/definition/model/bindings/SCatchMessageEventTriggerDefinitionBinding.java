/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Elias Ricken de Medeiros
 */
public class SCatchMessageEventTriggerDefinitionBinding extends SMessageEventTriggerDefinitionBinding {

    private final List<SOperation> operations = new ArrayList<SOperation>();

    @Override
    public Object getObject() {
        final SCatchMessageEventTriggerDefinitionImpl messageEventTrigger = new SCatchMessageEventTriggerDefinitionImpl();
        fillNode(messageEventTrigger);
        return messageEventTrigger;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.CATCH_MESSAGE_EVENT_TRIGGER_NODE;
    }

    protected void fillNode(final SCatchMessageEventTriggerDefinitionImpl messageEventTrigger) {
        super.fillNode(messageEventTrigger);
        for (final SOperation operation : operations) {
            messageEventTrigger.addOperation(operation);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) throws SXMLParseException {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.OPERATION_NODE.equals(name)) {
            operations.add((SOperation) value);
        }
    }

}
