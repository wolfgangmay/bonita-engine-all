/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.DependencyNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Elias Ricken de Medeiros
 */
public class LoginAPITest extends CommonAPITest {

    private static final String COMMAND_NAME = "deleteSession";

    private static final String COMMAND_DEPENDENCY_NAME = "deleteSessionCommand";

    private static PlatformCommandAPI platformCommandAPI;

    private static PlatformSession session;

    @Before
    public void before() throws BonitaException {
        session = APITestUtil.loginPlatform();
        platformCommandAPI = PlatformAPIAccessor.getPlatformCommandAPI(session);
    }

    @After
    public void after() throws BonitaException {
        APITestUtil.logoutPlatform(session);
    }

    @Test(expected = SessionNotFoundException.class)
    public void testSessionNotFoundExceptionIsThrownAfterSessionDeletion() throws Exception {
        // login to create a session
        login();
        final long sessionId = getSession().getId();

        // delete the session created by the login
        deleteSession(sessionId);

        // will throw SessionNotFoundException
        logout();
    }

    private void deleteSession(final long sessionId) throws IOException, AlreadyExistsException, CreationException, CreationException,
            CommandNotFoundException, CommandParameterizationException, CommandExecutionException, DeletionException, DependencyNotFoundException {
        // deploy and execute a command to delete a session
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/session-commands.jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        platformCommandAPI.addDependency(COMMAND_DEPENDENCY_NAME, byteArray);
        platformCommandAPI.register(COMMAND_NAME, "Delete a session", "org.bonitasoft.engine.command.DeleteSessionCommand");
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("sessionId", sessionId);
        platformCommandAPI.execute(COMMAND_NAME, parameters);
        platformCommandAPI.unregister(COMMAND_NAME);
        platformCommandAPI.removeDependency(COMMAND_DEPENDENCY_NAME);
    }

}
