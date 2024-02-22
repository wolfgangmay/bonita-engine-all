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
package org.bonitasoft.engine.business.data.converter;

import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;
import org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataModelConverter {

    public static SimpleBusinessDataReference toSimpleBusinessDataReference(
            final SSimpleRefBusinessDataInstance sReference) {
        return new SimpleBusinessDataReferenceImpl(sReference.getName(), sReference.getDataClassName(),
                sReference.getDataId());
    }

    public static MultipleBusinessDataReference toMultipleBusinessDataReference(
            final SProcessMultiRefBusinessDataInstance sReference) {
        return new MultipleBusinessDataReferenceImpl(sReference.getName(), sReference.getDataClassName(),
                sReference.getDataIds());
    }

}
