/**
 * Copyright (C) 2010 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SProfileMetadataDefinition;

/**
 * @author Anthony Birembaut
 * @author Matthieu Chaffotte
 */
public class SProfileMetadataDefinitionImpl extends SNamedElementImpl implements SProfileMetadataDefinition {

    private static final long serialVersionUID = 1L;

    public SProfileMetadataDefinitionImpl() {
    }

    @Override
    public String getDiscriminator() {
        return SProfileMetadataDefinition.class.getName();
    }

    @Override
    public String toString() {
        return "SProfileMetadataDefinitionImpl [getDescription()=" + this.getDescription() + ", getLabel()=" + this.getDisplayName() + ", getName()="
                + this.getName() + ", getId()=" + this.getId() + "]";
    }

}
