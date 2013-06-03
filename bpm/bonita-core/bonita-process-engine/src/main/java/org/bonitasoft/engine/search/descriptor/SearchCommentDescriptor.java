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

import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 */
public class SearchCommentDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> commentKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> commentAllFields;

    public SearchCommentDescriptor(final SCommentBuilders sCommentBuilders, final SUserBuilder userBuilder) {
        final SCommentBuilder sCommentBuilder = sCommentBuilders.getSHumanCommentBuilder();
        commentKeys = new HashMap<String, FieldDescriptor>(5);
        commentKeys.put(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, new FieldDescriptor(SComment.class, sCommentBuilder.getProcessInstanceIdKey()));
        commentKeys.put(SearchCommentsDescriptor.POSTED_BY_ID, new FieldDescriptor(SComment.class, sCommentBuilder.getUserIdKey()));
        commentKeys.put(SearchCommentsDescriptor.ID, new FieldDescriptor(SComment.class, sCommentBuilder.getIdKey()));
        commentKeys.put(SearchCommentsDescriptor.POSTDATE, new FieldDescriptor(SComment.class, sCommentBuilder.getPostDateKey()));
        commentKeys.put(SearchCommentsDescriptor.USER_NAME, new FieldDescriptor(SUser.class, userBuilder.getUserNameKey()));
        commentAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> commentFields = new HashSet<String>(1);
        commentFields.add(sCommentBuilder.getContentKey());
        commentAllFields.put(SComment.class, commentFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return commentKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return commentAllFields;
    }

}
