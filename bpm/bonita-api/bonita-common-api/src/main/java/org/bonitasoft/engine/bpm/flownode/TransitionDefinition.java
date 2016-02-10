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
package org.bonitasoft.engine.bpm.flownode;

import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.bpm.process.Visitable;
import org.bonitasoft.engine.expression.Expression;

/**
 * Component of a process definition. It connects 2 {@link FlowNodeDefinition} between them.
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public interface TransitionDefinition extends NamedElement, Visitable {

    /**
     * @deprecated from 6.5.0 on, name is not used anymore on TransitionDefinition. It is kept for retro-compatibility.
     */
    @Override
    @Deprecated
    public String getName();

    /**
     * @return The source of the transition
     */
    long getSource();

    /**
     * @return The target of the transition
     */
    long getTarget();

    /**
     * @return The condition of the transition
     */
    Expression getCondition();

}
