/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.util.ipc;

import static com.github.jlangch.venice.util.ipc.AccessMode.DENY;
import static com.github.jlangch.venice.util.ipc.AccessMode.EXECUTE;
import static com.github.jlangch.venice.util.ipc.AccessMode.READ;
import static com.github.jlangch.venice.util.ipc.AccessMode.READ_WRITE;
import static com.github.jlangch.venice.util.ipc.AccessMode.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;


public class AuthenticatorAclTest {

    @Test
    public void test_queue_acl() {
        final Authenticator a = new Authenticator(false);

        assertEquals(0, a.getQueueAclsMappedByPrincipal("q1").size());

        a.setQueueAcl("q1", READ, "tom");
        a.setQueueAcl("q1", WRITE, "max");
        a.setQueueAcl("q1", READ_WRITE, "jak");

        assertEquals(3, a.getQueueAclsMappedByPrincipal("q1").size());

        assertEquals("q1", a.getQueueAclsMappedByPrincipal("q1").get("tom").getSubject());
        assertEquals("tom", a.getQueueAclsMappedByPrincipal("q1").get("tom").getPrincipal());
        assertEquals(READ, a.getQueueAclsMappedByPrincipal("q1").get("tom").getMode());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("tom").canRead());
        assertFalse(a.getQueueAclsMappedByPrincipal("q1").get("tom").canWrite());

        assertEquals("q1", a.getQueueAclsMappedByPrincipal("q1").get("max").getSubject());
        assertEquals("max", a.getQueueAclsMappedByPrincipal("q1").get("max").getPrincipal());
        assertEquals(WRITE, a.getQueueAclsMappedByPrincipal("q1").get("max").getMode());
        assertFalse(a.getQueueAclsMappedByPrincipal("q1").get("max").canRead());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("max").canWrite());

        assertEquals("q1", a.getQueueAclsMappedByPrincipal("q1").get("jak").getSubject());
        assertEquals("jak", a.getQueueAclsMappedByPrincipal("q1").get("jak").getPrincipal());
        assertEquals(READ_WRITE, a.getQueueAclsMappedByPrincipal("q1").get("jak").getMode());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("jak").canRead());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("jak").canWrite());

        a.removeQueueAcl("q1", "tom");

        assertEquals(2, a.getQueueAclsMappedByPrincipal("q1").size());

        a.removeQueueAcl("q1");

        assertEquals(0, a.getQueueAclsMappedByPrincipal("q1").size());
    }

    @Test
    public void test_topic_acl() {
        final Authenticator a = new Authenticator(false);

        assertEquals(0, a.getTopicAclsMappedByPrincipal("t1").size());

        a.setTopicAcl("t1", READ, "tom");
        a.setTopicAcl("t1", WRITE, "max");
        a.setTopicAcl("t1", READ_WRITE, "jak");

        assertEquals(3, a.getTopicAclsMappedByPrincipal("t1").size());

        assertEquals("t1", a.getTopicAclsMappedByPrincipal("t1").get("tom").getSubject());
        assertEquals("tom", a.getTopicAclsMappedByPrincipal("t1").get("tom").getPrincipal());
        assertEquals(READ, a.getTopicAclsMappedByPrincipal("t1").get("tom").getMode());
        assertTrue(a.getTopicAclsMappedByPrincipal("t1").get("tom").canRead());
        assertFalse(a.getTopicAclsMappedByPrincipal("t1").get("tom").canWrite());

        assertEquals("t1", a.getTopicAclsMappedByPrincipal("t1").get("max").getSubject());
        assertEquals("max", a.getTopicAclsMappedByPrincipal("t1").get("max").getPrincipal());
        assertEquals(WRITE, a.getTopicAclsMappedByPrincipal("t1").get("max").getMode());
        assertFalse(a.getTopicAclsMappedByPrincipal("t1").get("max").canRead());
        assertTrue(a.getTopicAclsMappedByPrincipal("t1").get("max").canWrite());

        assertEquals("t1", a.getTopicAclsMappedByPrincipal("t1").get("jak").getSubject());
        assertEquals("jak", a.getTopicAclsMappedByPrincipal("t1").get("jak").getPrincipal());
        assertEquals(READ_WRITE, a.getTopicAclsMappedByPrincipal("t1").get("jak").getMode());
        assertTrue(a.getTopicAclsMappedByPrincipal("t1").get("jak").canRead());
        assertTrue(a.getTopicAclsMappedByPrincipal("t1").get("jak").canWrite());

        a.removeTopicAcl("t1", "tom");

        assertEquals(2, a.getTopicAclsMappedByPrincipal("t1").size());

        a.removeTopicAcl("t1");

        assertEquals(0, a.getTopicAclsMappedByPrincipal("t1").size());
    }

    @Test
    public void test_function_acl() {
        final Authenticator a = new Authenticator(false);

        assertEquals(0, a.getFunctionAclsMappedByPrincipal("t1").size());

        a.setFunctionAcl("t1", EXECUTE, "tom");
        a.setFunctionAcl("t1", EXECUTE, "max");
        a.setFunctionAcl("t1", DENY, "jak");

        assertEquals(3, a.getFunctionAclsMappedByPrincipal("t1").size());

        assertEquals("t1", a.getFunctionAclsMappedByPrincipal("t1").get("tom").getSubject());
        assertEquals("tom", a.getFunctionAclsMappedByPrincipal("t1").get("tom").getPrincipal());
        assertEquals(EXECUTE, a.getFunctionAclsMappedByPrincipal("t1").get("tom").getMode());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("tom").canRead());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("tom").canWrite());
        assertTrue(a.getFunctionAclsMappedByPrincipal("t1").get("tom").canExecute());

        assertEquals("t1", a.getFunctionAclsMappedByPrincipal("t1").get("max").getSubject());
        assertEquals("max", a.getFunctionAclsMappedByPrincipal("t1").get("max").getPrincipal());
        assertEquals(EXECUTE, a.getFunctionAclsMappedByPrincipal("t1").get("max").getMode());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("max").canRead());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("max").canWrite());
        assertTrue(a.getFunctionAclsMappedByPrincipal("t1").get("max").canExecute());

        assertEquals("t1", a.getFunctionAclsMappedByPrincipal("t1").get("jak").getSubject());
        assertEquals("jak", a.getFunctionAclsMappedByPrincipal("t1").get("jak").getPrincipal());
        assertEquals(DENY, a.getFunctionAclsMappedByPrincipal("t1").get("jak").getMode());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("jak").canRead());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("jak").canWrite());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("jak").canExecute());

        a.removeFunctionAcl("t1", "tom");

        assertEquals(2, a.getFunctionAclsMappedByPrincipal("t1").size());

        a.removeFunctionAcl("t1");

        assertEquals(0, a.getFunctionAclsMappedByPrincipal("t1").size());
    }

    @Test
    public void test_queue_acl_load_save() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.setQueueDefaultAcl(READ);

        a.setQueueAcl("q1", READ, "tom");
        a.setQueueAcl("q1", WRITE, "max");
        a.setQueueAcl("q1", READ_WRITE, "jak");

        a.save(new FileOutputStream(file));

        final Authenticator b = new Authenticator(false);
        b.load(new FileInputStream(file));

        assertEquals(READ, a.getQueueDefaultAcl().getMode());

        assertEquals(3, a.getQueueAclsMappedByPrincipal("q1").size());

        assertEquals("q1", a.getQueueAclsMappedByPrincipal("q1").get("tom").getSubject());
        assertEquals("tom", a.getQueueAclsMappedByPrincipal("q1").get("tom").getPrincipal());
        assertEquals(READ, a.getQueueAclsMappedByPrincipal("q1").get("tom").getMode());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("tom").canRead());
        assertFalse(a.getQueueAclsMappedByPrincipal("q1").get("tom").canWrite());

        assertEquals("q1", a.getQueueAclsMappedByPrincipal("q1").get("max").getSubject());
        assertEquals("max", a.getQueueAclsMappedByPrincipal("q1").get("max").getPrincipal());
        assertEquals(WRITE, a.getQueueAclsMappedByPrincipal("q1").get("max").getMode());
        assertFalse(a.getQueueAclsMappedByPrincipal("q1").get("max").canRead());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("max").canWrite());

        assertEquals("q1", a.getQueueAclsMappedByPrincipal("q1").get("jak").getSubject());
        assertEquals("jak", a.getQueueAclsMappedByPrincipal("q1").get("jak").getPrincipal());
        assertEquals(READ_WRITE, a.getQueueAclsMappedByPrincipal("q1").get("jak").getMode());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("jak").canRead());
        assertTrue(a.getQueueAclsMappedByPrincipal("q1").get("jak").canWrite());
    }

    @Test
    public void test_topic_acl_load_save() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.setTopicDefaultAcl(READ);

        a.setTopicAcl("t1", READ, "tom");
        a.setTopicAcl("t1", READ_WRITE, "max");
        a.setTopicAcl("t1", DENY, "jak");

        a.save(new FileOutputStream(file));

        final Authenticator b = new Authenticator(false);
        b.load(new FileInputStream(file));

        assertEquals(READ, a.getTopicDefaultAcl().getMode());

        assertEquals(3, a.getTopicAclsMappedByPrincipal("t1").size());

        assertEquals("t1", a.getTopicAclsMappedByPrincipal("t1").get("tom").getSubject());
        assertEquals("tom", a.getTopicAclsMappedByPrincipal("t1").get("tom").getPrincipal());
        assertEquals(READ, a.getTopicAclsMappedByPrincipal("t1").get("tom").getMode());
        assertTrue(a.getTopicAclsMappedByPrincipal("t1").get("tom").canRead());
        assertFalse(a.getTopicAclsMappedByPrincipal("t1").get("tom").canWrite());

        assertEquals("t1", a.getTopicAclsMappedByPrincipal("t1").get("max").getSubject());
        assertEquals("max", a.getTopicAclsMappedByPrincipal("t1").get("max").getPrincipal());
        assertEquals(READ_WRITE, a.getTopicAclsMappedByPrincipal("t1").get("max").getMode());
        assertTrue(a.getTopicAclsMappedByPrincipal("t1").get("max").canRead());
        assertTrue(a.getTopicAclsMappedByPrincipal("t1").get("max").canWrite());

        assertEquals("t1", a.getTopicAclsMappedByPrincipal("t1").get("jak").getSubject());
        assertEquals("jak", a.getTopicAclsMappedByPrincipal("t1").get("jak").getPrincipal());
        assertEquals(DENY, a.getTopicAclsMappedByPrincipal("t1").get("jak").getMode());
        assertFalse(a.getTopicAclsMappedByPrincipal("t1").get("jak").canRead());
        assertFalse(a.getTopicAclsMappedByPrincipal("t1").get("jak").canWrite());
    }

    @Test
    public void test_function_acl_load_save() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.setFunctionDefaultAcl(EXECUTE);

        a.setFunctionAcl("t1", EXECUTE, "tom");
        a.setFunctionAcl("t1", EXECUTE, "max");
        a.setFunctionAcl("t1", DENY, "jak");

        a.save(new FileOutputStream(file));

        final Authenticator b = new Authenticator(false);
        b.load(new FileInputStream(file));

        assertEquals(EXECUTE, a.getFunctionDefaultAcl().getMode());

        assertEquals(3, a.getFunctionAclsMappedByPrincipal("t1").size());

        assertEquals("t1", a.getFunctionAclsMappedByPrincipal("t1").get("tom").getSubject());
        assertEquals("tom", a.getFunctionAclsMappedByPrincipal("t1").get("tom").getPrincipal());
        assertEquals(EXECUTE, a.getFunctionAclsMappedByPrincipal("t1").get("tom").getMode());
        assertTrue(a.getFunctionAclsMappedByPrincipal("t1").get("tom").canExecute());

        assertEquals("t1", a.getFunctionAclsMappedByPrincipal("t1").get("max").getSubject());
        assertEquals("max", a.getFunctionAclsMappedByPrincipal("t1").get("max").getPrincipal());
        assertEquals(EXECUTE, a.getFunctionAclsMappedByPrincipal("t1").get("max").getMode());
        assertTrue(a.getFunctionAclsMappedByPrincipal("t1").get("max").canExecute());

        assertEquals("t1", a.getFunctionAclsMappedByPrincipal("t1").get("jak").getSubject());
        assertEquals("jak", a.getFunctionAclsMappedByPrincipal("t1").get("jak").getPrincipal());
        assertEquals(DENY, a.getFunctionAclsMappedByPrincipal("t1").get("jak").getMode());
        assertFalse(a.getFunctionAclsMappedByPrincipal("t1").get("jak").canExecute());
   }

}
