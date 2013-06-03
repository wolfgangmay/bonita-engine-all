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
package org.bonitasoft.engine.command.model;

/**
 * @author Matthieu Chaffotte
 */
public class SCommandBuilderImpl implements SCommandBuilder {

    private static final String ID = "id";

    static final String NAME = "name";

    static final String DESCRIPTION = "description";

    static final String IMPLEMENTATION = "implementation";
    
    static final String SYSTEM = "system";

    private SCommandImpl command;

    @Override
    public SCommandBuilder createNewInstance(final SCommand command) {
        this.command = new SCommandImpl(command);
        return this;
    }

    @Override
    public SCommandBuilder createNewInstance(final String name, final String description, final String implementationClass) {
        this.command = new SCommandImpl(name, description, implementationClass);
        return this;
    }

    @Override
    public SCommand done() {
        return command;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getNameKey() {
        return NAME;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

    @Override
    public String getImplementationClassKey() {
        return IMPLEMENTATION;
    }

    @Override
    public SCommandBuilder setSystem(boolean system) {
        this.command.setSystem(system);
        return this;
    }

    @Override
    public String getSystemKey() {
        return SYSTEM;
    }

}
