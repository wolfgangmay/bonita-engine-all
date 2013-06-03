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
package org.bonitasoft.engine.commons;

/**
 * Represent an enum which enum values can be convertible to other types.
 * One method: fromEnum, returns an object value, result of the conversion of that enum
 * 
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public interface EnumToObjectConvertible {

    /**
     * Convert this enum to an Object value
     * 
     * @return
     * @since 6.0
     */
    Object fromEnum();
}
