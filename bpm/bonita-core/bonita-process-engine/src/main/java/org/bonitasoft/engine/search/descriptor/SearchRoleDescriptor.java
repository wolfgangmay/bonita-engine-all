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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.identity.RoleSearchDescriptor;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.RoleBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Matthieu Chaffotte
 */
public class SearchRoleDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> roleKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> roleAllFields;

    SearchRoleDescriptor(final IdentityModelBuilder identityModelBuilder) {
        final RoleBuilder roleBuilder = identityModelBuilder.getRoleBuilder();
        roleKeys = new HashMap<String, FieldDescriptor>(5);
        roleKeys.put(RoleSearchDescriptor.ID, new FieldDescriptor(SRole.class, roleBuilder.getIdKey()));
        roleKeys.put(RoleSearchDescriptor.NAME, new FieldDescriptor(SRole.class, roleBuilder.getNameKey()));
        roleKeys.put(RoleSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SRole.class, roleBuilder.getDisplayNameKey()));

        roleAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> roleFields = new HashSet<String>(8);
        roleFields.add(roleBuilder.getNameKey());
        roleFields.add(roleBuilder.getDisplayNameKey());
        roleFields.add(roleBuilder.getDescriptionKey());
        roleFields.add(roleBuilder.getIconNameKey());
        roleFields.add(roleBuilder.getIconPathKey());
        roleAllFields.put(SRole.class, roleFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return roleKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return roleAllFields;
    }

}
