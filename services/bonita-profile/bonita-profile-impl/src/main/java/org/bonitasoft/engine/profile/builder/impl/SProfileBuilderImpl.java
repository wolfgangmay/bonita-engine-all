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
package org.bonitasoft.engine.profile.builder.impl;

import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProfileBuilderImpl implements SProfileBuilder {

    private SProfileImpl profile;

    @Override
    public SProfileBuilder createNewInstance(final SProfile profile) {
        this.profile = new SProfileImpl(profile);
        return this;
    }

    @Override
    public SProfileBuilder createNewInstance(final String name) {
        profile = new SProfileImpl();
        profile.setName(name);
        return this;
    }

    @Override
    public SProfileBuilder setId(final long id) {
        profile.setId(id);
        return this;
    }

    @Override
    public SProfileBuilder setDescription(final String description) {
        profile.setDescription(description);
        return this;
    }

    @Override
    public SProfileBuilder setIconPath(final String iconPath) {
        profile.setIconPath(iconPath);
        return this;
    }

    @Override
    public SProfile done() {
        return profile;
    }

}
