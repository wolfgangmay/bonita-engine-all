/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.instance.model.archive.impl;

import java.io.Serializable;

import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public class SAIntegerDataInstanceImpl extends SADataInstanceImpl {

    private static final long serialVersionUID = 1339182108197167117L;

    private Integer value;

    public SAIntegerDataInstanceImpl() {
        super();
    }

    public SAIntegerDataInstanceImpl(final SDataInstance sDataInstance) {
        super(sDataInstance);
        value = (Integer) sDataInstance.getValue();
    }

    @Override
    public String getDiscriminator() {
        return SAIntegerDataInstanceImpl.class.getSimpleName();
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(final Serializable value) {
        this.value = (Integer) value;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SDataInstance.class;
    }

}
