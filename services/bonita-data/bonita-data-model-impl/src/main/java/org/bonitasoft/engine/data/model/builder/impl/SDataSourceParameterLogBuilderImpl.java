/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.model.builder.impl;

import org.bonitasoft.engine.data.model.builder.SDataSourceParameterLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Elias Ricken de Medeiros
 */
public class SDataSourceParameterLogBuilderImpl extends CRUDELogBuilder implements SDataSourceParameterLogBuilder {

    private static final String DATA_SOURCE_PARAMETER = "DATA_SOURCE_PARAMETER";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        this.queriableLogBuilder.numericIndex(SDataSourceLogIndexesMapper.DATA_SOURCE_PARAMETER_INDEX, objectId);
        return this;
    }

    @Override
    public String getObjectIdKey() {
        return SDataSourceLogIndexesMapper.DATA_SOURCE_PARAMETER_INDEX_NAME;
    }

    @Override
    protected String getActionTypePrefix() {
        return DATA_SOURCE_PARAMETER;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL) {
            if (log.getNumericIndex(SDataSourceLogIndexesMapper.DATA_SOURCE_PARAMETER_INDEX) == 0L) {
                throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "DataSourceParameter Id");
            }
        }
        if (log.getNumericIndex(SDataSourceLogIndexesMapper.DATA_SOURCE_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "DataSource Id");
        }
    }

    @Override
    public SDataSourceParameterLogBuilder dataSourceId(final long datasourceId) {
        this.queriableLogBuilder.numericIndex(SDataSourceLogIndexesMapper.DATA_SOURCE_INDEX, datasourceId);
        return this;
    }

    @Override
    public String getDataSourceIdKey() {
        return SDataSourceLogIndexesMapper.DATA_SOURCE_INDEX_NAME;
    }

}
