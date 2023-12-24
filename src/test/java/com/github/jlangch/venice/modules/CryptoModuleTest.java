/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class CryptoModuleTest {

    @Test
    public void test_PBKDEF2_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "  (load-module :crypt)                             \n" +
                "  (str/bytebuf-to-hex                              \n" +
                "    (crypt/pbkdf2-hash \"hello world\" \"-salt-\") \n" +
                "    :upper))                                         ";

        assertEquals(
            "54F2B4411E8817C2A0743B2A7DD7EAE5AA3F748D1DDDCE00766380914AFFE995",
            venice.eval(script));
    }

    @Test
    public void test_PBKDEF2_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "  (load-module :crypt)                               \n" +
                "  (str/bytebuf-to-hex                                \n" +
                "    (crypt/pbkdf2-hash \"hello world\" \"-salt-\")))   ";

        assertEquals(
            "54f2b4411e8817c2a0743b2a7dd7eae5aa3f748d1dddce00766380914affe995",
            venice.eval(script));
    }

    @Test
    public void test_PBKDEF2_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "  (load-module :crypt)                                      \n" +
                "  (str/bytebuf-to-hex                                       \n" +
                "    (crypt/pbkdf2-hash \"hello world\" \"-salt-\" 1000 256) \n" +
                "    :upper))                                                 ";

        assertEquals(
            "54F2B4411E8817C2A0743B2A7DD7EAE5AA3F748D1DDDCE00766380914AFFE995",
            venice.eval(script));
    }

    @Test
    public void test_PBKDEF2_4() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         \n" +
                "  (load-module :crypt)                                      \n" +
                "  (str/bytebuf-to-hex                                       \n" +
                "    (crypt/pbkdf2-hash \"hello world\" \"-salt-\" 1000 384) \n" +
                "    :upper))                                                 ";

        assertEquals(
            "54F2B4411E8817C2A0743B2A7DD7EAE5AA3F748D1DDDCE00766380914AFFE995281C8170AE437EC63B13BB50FD1A480E",
            venice.eval(script));
    }

    @Test
    public void test_SHA_512_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "  (load-module :crypt)                             \n" +
                "  (str/bytebuf-to-hex                              \n" +
                "    (crypt/sha512-hash \"hello world\" \"-salt-\") \n" +
                "    :upper))                                         ";

        assertEquals(
            "316EBB70239D9480E91089D5D5BC6428879DF6E5CFB651B39D7AFC27DFF259418105C6D78F307FC6197531FBD37C4E8103095F186B19FC33C93D60282F3314A2",
            venice.eval(script));
    }

    @Test
    public void test_SHA_512_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "  (load-module :crypt)                               \n" +
                "  (str/bytebuf-to-hex                                \n" +
                "    (crypt/sha512-hash \"hello world\" \"-salt-\")))   ";

        assertEquals(
            "316ebb70239d9480e91089d5d5bc6428879df6e5cfb651b39d7afc27dff259418105c6d78f307fc6197531fbd37c4e8103095f186b19fc33c93d60282f3314a2",
            venice.eval(script));
    }

    @Test
    public void test_SHA_512_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                     \n" +
                "  (load-module :crypt)                                  \n" +
                "  (str/bytebuf-to-hex                                   \n" +
                "    (crypt/sha512-hash (bytebuf [54 78 99]) \"-salt-\") \n" +
                "    :upper))                                              ";

        assertEquals(
            "02621CADC0EA2E051EFCBE77A5BDEDC6AC77ECA0A06D97801485A9AC2BC9DFBE08D0671FE03D6B249F954C890FF812D2FA345FE6B8BF54DB3D2DCD3EDE3B9351",
            venice.eval(script));
    }

    @Test
    public void test_MD5_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "  (load-module :crypt)                             \n" +
                "  (str/bytebuf-to-hex                              \n" +
                "    (crypt/md5-hash \"hello world\")               \n" +
                "    :upper))                                         ";

        assertEquals(
            "5EB63BBBE01EEED093CB22BB8F5ACDC3",
            venice.eval(script));
    }

    @Test
    public void test_MD5_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "  (load-module :crypt)                             \n" +
                "  (str/bytebuf-to-hex                              \n" +
                "    (crypt/md5-hash \"hello world\")))               ";

        assertEquals(
            "5eb63bbbe01eeed093cb22bb8f5acdc3",
            venice.eval(script));
    }

    @Test
    public void test_MD5_3() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "  (load-module :crypt)                               \n" +
                "  (str/bytebuf-to-hex                                \n" +
                "    (crypt/md5-hash (bytebuf [54 78 99]))            \n" +
                "    :upper))                                           ";

        assertEquals(
            "7F83326444205E182B3E80D1C65C902D",
            venice.eval(script));
    }

    @Test
    public void test_DES_encrypt_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                             \n" +
                "  (load-module :crypt)                                                          \n" +
                "  (def encrypt (crypt/encrypt \"DES\" \"secret\" :url-safe true))               \n" +
                "  (assert (== \"QdxpapAEjgI=\" (encrypt \"hello\")))                             \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_DES_encrypt_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                             \n" +
                "  (load-module :crypt)                                                          \n" +
                "  (def encrypt (crypt/encrypt \"DES\" \"secret\" :url-safe true))               \n" +
                "  (def decrypt (crypt/decrypt \"DES\" \"secret\" :url-safe true))               \n" +
                "  (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                         \n" +
                "  (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5]))))) \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_DES_encrypt_custom_salt_string() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                 \n" +
                "  (load-module :crypt)                                                              \n" +
                "  (def encrypt (crypt/encrypt \"DES\" \"secret\" :url-safe true :salt \"-salt-\"))  \n" +
                "  (def decrypt (crypt/decrypt \"DES\" \"secret\" :url-safe true :salt \"-salt-\"))  \n" +
                "  (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                             \n" +
                "  (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5])))))     \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_DES_encrypt_custom_salt_bytes() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                   \n" +
                "  (load-module :crypt)                                                                \n" +
                "  (let [salt (bytebuf [0x20 0x21 0x22 0x23 0x24 0x25 0x26 0x27])]                     \n" +
                "    (def encrypt (crypt/encrypt \"DES\" \"secret\" :url-safe true :salt salt))        \n" +
                "    (def decrypt (crypt/decrypt \"DES\" \"secret\" :url-safe true :salt salt))        \n" +
                "    (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                             \n" +
                "    (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5])))))))" ;

        venice.eval(script);
    }

    @Test
    public void test_3DES_encrypt_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                             \n" +
                "  (load-module :crypt)                                                          \n" +
                "  (def encrypt (crypt/encrypt \"3DES\" \"secret\" :url-safe true))              \n" +
                "  (assert (== \"ndmW1NLsDHA=\" (encrypt \"hello\")))                             \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_3DES_encrypt_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                             \n" +
                "  (load-module :crypt)                                                          \n" +
                "  (def encrypt (crypt/encrypt \"3DES\" \"secret\" :url-safe true))              \n" +
                "  (def decrypt (crypt/decrypt \"3DES\" \"secret\" :url-safe true))              \n" +
                "  (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                         \n" +
                "  (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5]))))) \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_3DES_encrypt_custom_salt_string() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                 \n" +
                "  (load-module :crypt)                                                              \n" +
                "  (def encrypt (crypt/encrypt \"3DES\" \"secret\" :url-safe true :salt \"-salt-\")) \n" +
                "  (def decrypt (crypt/decrypt \"3DES\" \"secret\" :url-safe true :salt \"-salt-\")) \n" +
                "  (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                             \n" +
                "  (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5])))))     \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_3DES_encrypt_custom_salt_bytes() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                   \n" +
                "  (load-module :crypt)                                                                \n" +
                "  (let [salt (bytebuf [0x20 0x21 0x22 0x23 0x24 0x25 0x26 0x27])]                     \n" +
                "    (def encrypt (crypt/encrypt \"3DES\" \"secret\" :url-safe true :salt salt))       \n" +
                "    (def decrypt (crypt/decrypt \"3DES\" \"secret\" :url-safe true :salt salt))       \n" +
                "    (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                             \n" +
                "    (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5])))))))" ;

        venice.eval(script);
    }

    @Test
    public void test_AES256_CBC_encrypt_1() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                             \n" +
                "  (load-module :crypt)                                                          \n" +
                "  (def encrypt (crypt/encrypt \"AES256\" \"secret\" :url-safe true))            \n" +
                "  (assert (== \"e4m1qe6Fyx3Rr7NTIZe97g==\" (encrypt \"hello\")))                  \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_AES256_CBC_encrypt_2() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                             \n" +
                "  (load-module :crypt)                                                          \n" +
                "  (def encrypt (crypt/encrypt \"AES256\" \"secret\" :url-safe true))            \n" +
                "  (def decrypt (crypt/decrypt \"AES256\" \"secret\" :url-safe true))            \n" +
                "  (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                          \n" +
                "  (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5]))))) \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_AES256_CBC_encrypt_custom_salt_string() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                    \n" +
                "  (load-module :crypt)                                                                 \n" +
                "  (def encrypt (crypt/encrypt \"AES256\" \"secret\" :url-safe true :salt \"-salt-\"))  \n" +
                "  (def decrypt (crypt/decrypt \"AES256\" \"secret\" :url-safe true :salt \"-salt-\"))  \n" +
                "  (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                                \n" +
                "  (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5])))))        \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_AES256_CBC_encrypt_custom_salt_bytes() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                                   \n" +
                "  (load-module :crypt)                                                                \n" +
                "  (let [salt (bytebuf [0x20 0x21 0x22 0x23 0x24 0x25 0x26 0x27])]                     \n" +
                "    (def encrypt (crypt/encrypt \"AES256\" \"secret\" :url-safe true :salt salt))     \n" +
                "    (def decrypt (crypt/decrypt \"AES256\" \"secret\" :url-safe true :salt salt))     \n" +
                "    (assert (== \"hello\" (decrypt (encrypt \"hello\"))))                             \n" +
                "    (assert (== (bytebuf [1 2 3 4 5]) (decrypt (encrypt (bytebuf [1 2 3 4 5])))))))" ;

        venice.eval(script);
    }

    @Test
    public void test_ciphers_default() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                \n" +
                "  (load-module :crypt)                             \n" +
                "  (assert (> (count (crypt/ciphers :default)) 1))  \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_ciphers_available() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                  \n" +
                "  (load-module :crypt)                               \n" +
                "  (assert (> (count (crypt/ciphers :available)) 1))  \n" +
                ")";

        venice.eval(script);
    }

    @Test
    public void test_hash_file_MD5_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                     \n" +
                "  (load-module :crypt)                                  \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")        \n" +
                "        data \"1234567890\"]                            \n" +
                "    (io/delete-file-on-exit file)                       \n" +
                "    (io/spit file data)                                 \n" +
                "    (crypt/hash-file \"MD5\" \"salt\" file)))           ";

        assertEquals("kUucF7TqNzvEmBu/hn3xhg==", venice.eval(script));
    }

    @Test
    public void test_hash_file_MD5_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                             \n" +
                "  (load-module :crypt)                                          \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                \n" +
                "        data \"1234567890\"]                                    \n" +
                "    (io/delete-file-on-exit file)                               \n" +
                "    (io/spit file data)                                         \n" +
                "    (crypt/hash-file \"MD5\" \"salt\" (io/file-path file))))    ";

        assertEquals("kUucF7TqNzvEmBu/hn3xhg==", venice.eval(script));
    }

    @Test
    public void test_hash_file_MD5_3() {
        final Venice venice = new Venice();

        // file-in-stream
        final String script =
                "(do                                                                  \n" +
                "  (load-module :crypt)                                               \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                     \n" +
                "        data \"1234567890\"]                                         \n" +
                "    (io/delete-file-on-exit file)                                    \n" +
                "    (io/spit file data)                                              \n" +
                "    (crypt/hash-file \"MD5\" \"salt\"(io/file-in-stream file))))    ";

        assertEquals("kUucF7TqNzvEmBu/hn3xhg==", venice.eval(script));
    }

    @Test
    public void test_hash_file_MD5_4() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                    \n" +
                "  (load-module :crypt)                                                 \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                       \n" +
                "        data \"1234567890\"]                                           \n" +
                "    (io/delete-file-on-exit file)                                      \n" +
                "    (io/spit file data)                                                \n" +
                "    (crypt/hash-file \"MD5\" \"salt\" (io/slurp file :binary true))))  ";

        assertEquals("kUucF7TqNzvEmBu/hn3xhg==", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA1_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                     \n" +
                "  (load-module :crypt)                                  \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")        \n" +
                "        data \"1234567890\"]                            \n" +
                "    (io/delete-file-on-exit file)                       \n" +
                "    (io/spit file data)                                 \n" +
                "    (crypt/hash-file \"SHA-1\" \"salt\" file)))         ";

        assertEquals("uhAVLkBSJ7kyuYmYQfREs6A7+/A=", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA1_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                             \n" +
                "  (load-module :crypt)                                          \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                \n" +
                "        data \"1234567890\"]                                    \n" +
                "    (io/delete-file-on-exit file)                               \n" +
                "    (io/spit file data)                                         \n" +
                "    (crypt/hash-file \"SHA-1\" \"salt\" (io/file-path file))))  ";

        assertEquals("uhAVLkBSJ7kyuYmYQfREs6A7+/A=", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA1_3() {
        final Venice venice = new Venice();

        // file-in-stream
        final String script =
                "(do                                                                  \n" +
                "  (load-module :crypt)                                               \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                     \n" +
                "        data \"1234567890\"]                                         \n" +
                "    (io/delete-file-on-exit file)                                    \n" +
                "    (io/spit file data)                                              \n" +
                "    (crypt/hash-file \"SHA-1\" \"salt\" (io/file-in-stream file))))  ";

        assertEquals("uhAVLkBSJ7kyuYmYQfREs6A7+/A=", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA1_4() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                    \n" +
                "  (load-module :crypt)                                                 \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                       \n" +
                "        data \"1234567890\"]                                           \n" +
                "    (io/delete-file-on-exit file)                                      \n" +
                "    (io/spit file data)                                                \n" +
                "    (crypt/hash-file \"SHA-1\" \"salt\" (io/slurp file :binary true))))";

        assertEquals("uhAVLkBSJ7kyuYmYQfREs6A7+/A=", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA512_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                     \n" +
                "  (load-module :crypt)                                  \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")        \n" +
                "        data \"1234567890\"]                            \n" +
                "    (io/delete-file-on-exit file)                       \n" +
                "    (io/spit file data)                                 \n" +
                "    (crypt/hash-file \"SHA-512\" \"salt\" file)))       ";

        assertEquals("qFUlDmCuGAA8Y8qKrjMd4mUdQYau4Cs6gTcYs39oRF0wuOG46oLfi/7nUbHd0IH2uiBe+xUzjcsAT4CImO/liw==", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA512_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                             \n" +
                "  (load-module :crypt)                                          \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                \n" +
                "        data \"1234567890\"]                                    \n" +
                "    (io/delete-file-on-exit file)                               \n" +
                "    (io/spit file data)                                         \n" +
                "    (crypt/hash-file \"SHA-512\" \"salt\" (io/file-path file))))";

        assertEquals("qFUlDmCuGAA8Y8qKrjMd4mUdQYau4Cs6gTcYs39oRF0wuOG46oLfi/7nUbHd0IH2uiBe+xUzjcsAT4CImO/liw==", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA512_3() {
        final Venice venice = new Venice();

        // file-in-stream
        final String script =
                "(do                                                                  \n" +
                "  (load-module :crypt)                                               \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                     \n" +
                "        data \"1234567890\"]                                         \n" +
                "    (io/delete-file-on-exit file)                                    \n" +
                "    (io/spit file data)                                              \n" +
                "    (crypt/hash-file \"SHA-512\" \"salt\" (io/file-in-stream file))))";

        assertEquals("qFUlDmCuGAA8Y8qKrjMd4mUdQYau4Cs6gTcYs39oRF0wuOG46oLfi/7nUbHd0IH2uiBe+xUzjcsAT4CImO/liw==", venice.eval(script));
    }

    @Test
    public void test_hash_file_SHA512_4() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                    \n" +
                "  (load-module :crypt)                                                 \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                       \n" +
                "        data \"1234567890\"]                                           \n" +
                "    (io/delete-file-on-exit file)                                      \n" +
                "    (io/spit file data)                                                \n" +
                "    (crypt/hash-file \"SHA-512\" \"salt\" (io/slurp file :binary true))))";

        assertEquals("qFUlDmCuGAA8Y8qKrjMd4mUdQYau4Cs6gTcYs39oRF0wuOG46oLfi/7nUbHd0IH2uiBe+xUzjcsAT4CImO/liw==", venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_MD5_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                             \n" +
                "  (load-module :crypt)                                          \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                \n" +
                "        data \"1234567890\"                                     \n" +
                "        salt \"-salt-\"]                                        \n" +
                "    (io/delete-file-on-exit file)                               \n" +
                "    (io/spit file data)                                         \n" +
                "    (let [hash (crypt/hash-file \"MD5\" salt file)]             \n" +
                "      (crypt/verify-file-hash \"MD5\" salt file hash))))        ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_MD5_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                                     \n" +
                "  (load-module :crypt)                                                  \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                        \n" +
                "        data \"1234567890\"                                             \n" +
                "        salt \"-salt-\"]                                                \n" +
                "    (io/delete-file-on-exit file)                                       \n" +
                "    (io/spit file data)                                                 \n" +
                "    (let [hash (crypt/hash-file \"MD5\" salt file)]                     \n" +
                "      (crypt/verify-file-hash \"MD5\" salt (io/file-path file) hash)))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_MD5_3() {
        final Venice venice = new Venice();

        // file-in-stream
        final String script =
                "(do                                                                          \n" +
                "  (load-module :crypt)                                                       \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                             \n" +
                "        data \"1234567890\"                                                  \n" +
                "        salt \"-salt-\"]                                                     \n" +
                "    (io/delete-file-on-exit file)                                            \n" +
                "    (io/spit file data)                                                      \n" +
                "    (let [hash (crypt/hash-file \"MD5\" salt file)]                          \n" +
                "      (crypt/verify-file-hash \"MD5\" salt (io/file-in-stream file) hash))))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_MD5_4() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                             \n" +
                "  (load-module :crypt)                                                          \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                                \n" +
                "        data \"1234567890\"                                                     \n" +
                "        salt \"-salt-\"]                                                        \n" +
                "    (io/delete-file-on-exit file)                                               \n" +
                "    (io/spit file data)                                                         \n" +
                "    (let [hash (crypt/hash-file \"MD5\" salt file)]                             \n" +
                "      (crypt/verify-file-hash \"MD5\" salt (io/slurp file :binary true) hash))))";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA1_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                             \n" +
                "  (load-module :crypt)                                          \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                \n" +
                "        data \"1234567890\"                                     \n" +
                "        salt \"-salt-\"]                                        \n" +
                "    (io/delete-file-on-exit file)                               \n" +
                "    (io/spit file data)                                         \n" +
                "    (let [hash (crypt/hash-file \"SHA-1\" salt file)]           \n" +
                "      (crypt/verify-file-hash \"SHA-1\" salt file hash))))      ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA1_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                                       \n" +
                "  (load-module :crypt)                                                    \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                          \n" +
                "        data \"1234567890\"                                               \n" +
                "        salt \"-salt-\"]                                                  \n" +
                "    (io/delete-file-on-exit file)                                         \n" +
                "    (io/spit file data)                                                   \n" +
                "    (let [hash (crypt/hash-file \"SHA-1\" salt file)]                     \n" +
                "      (crypt/verify-file-hash \"SHA-1\" salt (io/file-path file) hash)))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA1_3() {
        final Venice venice = new Venice();

        // file-in-stream
        final String script =
                "(do                                                                            \n" +
                "  (load-module :crypt)                                                         \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                               \n" +
                "        data \"1234567890\"                                                    \n" +
                "        salt \"-salt-\"]                                                       \n" +
                "    (io/delete-file-on-exit file)                                              \n" +
                "    (io/spit file data)                                                        \n" +
                "    (let [hash (crypt/hash-file \"SHA-1\" salt file)]                          \n" +
                "      (crypt/verify-file-hash \"SHA-1\" salt (io/file-in-stream file) hash)))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA1_4() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                \n" +
                "  (load-module :crypt)                                                             \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                                   \n" +
                "        data \"1234567890\"                                                        \n" +
                "        salt \"-salt-\"]                                                           \n" +
                "    (io/delete-file-on-exit file)                                                  \n" +
                "    (io/spit file data)                                                            \n" +
                "    (let [hash (crypt/hash-file \"SHA-1\" salt file)]                              \n" +
                "      (crypt/verify-file-hash \"SHA-1\" salt (io/slurp file :binary true) hash)))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA512_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                             \n" +
                "  (load-module :crypt)                                          \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                \n" +
                "        data \"1234567890\"                                     \n" +
                "        salt \"-salt-\"]                                        \n" +
                "    (io/delete-file-on-exit file)                               \n" +
                "    (io/spit file data)                                         \n" +
                "    (let [hash (crypt/hash-file \"SHA-512\" salt file)]         \n" +
                "      (crypt/verify-file-hash \"SHA-512\" salt file hash))))    ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA512_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                                        \n" +
                "  (load-module :crypt)                                                     \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                           \n" +
                "        data \"1234567890\"                                                \n" +
                "        salt \"-salt-\"]                                                   \n" +
                "    (io/delete-file-on-exit file)                                          \n" +
                "    (io/spit file data)                                                    \n" +
                "    (let [hash (crypt/hash-file \"SHA-512\" salt file)]                    \n" +
                "      (crypt/verify-file-hash \"SHA-512\" salt (io/file-path file) hash)))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA512_3() {
        final Venice venice = new Venice();

        // file-in-stream
        final String script =
                "(do                                                                              \n" +
                "  (load-module :crypt)                                                           \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                                 \n" +
                "        data \"1234567890\"                                                      \n" +
                "        salt \"-salt-\"]                                                         \n" +
                "    (io/delete-file-on-exit file)                                                \n" +
                "    (io/spit file data)                                                          \n" +
                "    (let [hash (crypt/hash-file \"SHA-512\" salt file)]                          \n" +
                "      (crypt/verify-file-hash \"SHA-512\" salt (io/file-in-stream file) hash)))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_verify_file_hash_SHA512_4() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                  \n" +
                "  (load-module :crypt)                                                               \n" +
                "  (let [file (io/temp-file \"test-\", \".data\")                                     \n" +
                "        data \"1234567890\"                                                          \n" +
                "        salt \"-salt-\"]                                                             \n" +
                "    (io/delete-file-on-exit file)                                                    \n" +
                "    (io/spit file data)                                                              \n" +
                "    (let [hash (crypt/hash-file \"SHA-512\" salt file)]                              \n" +
                "      (crypt/verify-file-hash \"SHA-512\" salt (io/slurp file :binary true) hash)))) ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_GCM_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                                  \n" +
                "  (load-module :crypt)                                               \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")               \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")           \n" +
                "        passphrase \"42\"]                                           \n" +
                "    (io/delete-file-on-exit file-in)                                 \n" +
                "    (io/delete-file-on-exit file-out)                                \n" +
                "    (io/spit file-in \"1234567890\")                                 \n" +
                "    (crypt/encrypt-file \"AES256-GCM\" passphrase file-in file-out)  \n" +
                "    (-> (crypt/decrypt-file \"AES256-GCM\" passphrase file-out)      \n" +
                "        (bytebuf-to-string :UTF-8))))                                ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_GCM_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                                                                \n" +
                "  (load-module :crypt)                                                                             \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                             \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                         \n" +
                "        passphrase \"42\"]                                                                         \n" +
                "    (io/delete-file-on-exit file-in)                                                               \n" +
                "    (io/delete-file-on-exit file-out)                                                              \n" +
                "    (io/spit file-in \"1234567890\")                                                               \n" +
                "    (crypt/encrypt-file \"AES256-GCM\" passphrase (io/file-path file-in) (io/file-path file-out))  \n" +
                "    (-> (crypt/decrypt-file \"AES256-GCM\" passphrase (io/file-path file-out))                     \n" +
                "        (bytebuf-to-string :UTF-8))))                                                              ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_GCM_3() {
        final Venice venice = new Venice();

        // file-in-stream, file-out-stream
        final String script =
                "(do                                                                                                           \n" +
                "  (load-module :crypt)                                                                                        \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                                        \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                                    \n" +
                "        passphrase \"42\"]                                                                                    \n" +
                "    (io/delete-file-on-exit file-in)                                                                          \n" +
                "    (io/delete-file-on-exit file-out)                                                                         \n" +
                "    (io/spit file-in \"1234567890\")                                                                          \n" +
                "    (crypt/encrypt-file \"AES256-GCM\" passphrase (io/file-in-stream file-in) (io/file-out-stream file-out))  \n" +
                "    (-> (crypt/decrypt-file \"AES256-GCM\" passphrase (io/file-in-stream file-out))                                          \n" +
                "        (bytebuf-to-string :UTF-8))))                                                                         ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_GCM_4a() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                          \n" +
                "  (load-module :crypt)                                                                       \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                       \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                   \n" +
                "        passphrase \"42\"]                                                                   \n" +
                "    (io/delete-file-on-exit file-in)                                                         \n" +
                "    (io/delete-file-on-exit file-out)                                                        \n" +
                "    (io/spit file-in \"1234567890\")                                                         \n" +
                "    (crypt/encrypt-file \"AES256-GCM\" passphrase (io/slurp file-in :binary true) file-out)  \n" +
                "    (-> (crypt/decrypt-file \"AES256-GCM\" passphrase (io/slurp file-out :binary true))      \n" +
                "        (bytebuf-to-string :UTF-8))))                                                        ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_GCM_4b() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                          \n" +
                "  (load-module :crypt)                                                                       \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                       \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                   \n" +
                "        passphrase \"42\"]                                                                   \n" +
                "    (io/delete-file-on-exit file-in)                                                         \n" +
                "    (io/delete-file-on-exit file-out)                                                        \n" +
                "    (io/spit file-in \"1234567890\")                                                         \n" +
                "    (->> (crypt/encrypt-file \"AES256-GCM\" passphrase (io/slurp file-in :binary true))      \n" +
                "         (io/spit file-out))                                                                 \n" +
                "    (-> (crypt/decrypt-file \"AES256-GCM\" passphrase (io/slurp file-out :binary true))      \n" +
                "        (bytebuf-to-string :UTF-8))))                                                        ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_GCM_4c() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                     \n" +
                "  (load-module :crypt)                                                                  \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                  \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                              \n" +
                "        passphrase \"42\"]                                                              \n" +
                "    (io/delete-file-on-exit file-in)                                                    \n" +
                "    (io/delete-file-on-exit file-out)                                                   \n" +
                "    (io/spit file-in \"1234567890\")                                                    \n" +
                "    (-<> (crypt/encrypt-file \"AES256-GCM\" passphrase (io/slurp file-in :binary true)) \n" +
                "         (crypt/decrypt-file \"AES256-GCM\" passphrase <>)                              \n" +
                "         (bytebuf-to-string <> :UTF-8))))                                               ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_CBC_1() {
        final Venice venice = new Venice();

        // file
        final String script =
                "(do                                                                 \n" +
                "  (load-module :crypt)                                              \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")              \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")          \n" +
                "        passphrase \"42\"]                                          \n" +
                "    (io/delete-file-on-exit file-in)                                \n" +
                "    (io/delete-file-on-exit file-out)                               \n" +
                "    (io/spit file-in \"1234567890\")                                \n" +
                "    (crypt/encrypt-file \"AES256-CBC\" passphrase file-in file-out) \n" +
                "    (-> (crypt/decrypt-file \"AES256-CBC\" passphrase file-out)     \n" +
                "        (bytebuf-to-string :UTF-8))))                               ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_CBC_2() {
        final Venice venice = new Venice();

        // string
        final String script =
                "(do                                                                                                \n" +
                "  (load-module :crypt)                                                                             \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                             \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                         \n" +
                "        passphrase \"42\"]                                                                         \n" +
                "    (io/delete-file-on-exit file-in)                                                               \n" +
                "    (io/delete-file-on-exit file-out)                                                              \n" +
                "    (io/spit file-in \"1234567890\")                                                               \n" +
                "    (crypt/encrypt-file \"AES256-CBC\" passphrase (io/file-path file-in) (io/file-path file-out))  \n" +
                "    (-> (crypt/decrypt-file \"AES256-CBC\" passphrase (io/file-path file-out))                     \n" +
                "        (bytebuf-to-string :UTF-8))))                                                              ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_CBC_3() {
        final Venice venice = new Venice();

        // file-in-stream, file-out-stream
        final String script =
                "(do                                                                                                           \n" +
                "  (load-module :crypt)                                                                                        \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                                        \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                                    \n" +
                "        passphrase \"42\"]                                                                                    \n" +
                "    (io/delete-file-on-exit file-in)                                                                          \n" +
                "    (io/delete-file-on-exit file-out)                                                                         \n" +
                "    (io/spit file-in \"1234567890\")                                                                          \n" +
                "    (crypt/encrypt-file \"AES256-CBC\" passphrase (io/file-in-stream file-in) (io/file-out-stream file-out))  \n" +
                "    (-> (crypt/decrypt-file \"AES256-CBC\" passphrase (io/file-in-stream file-out))                           \n" +
                "        (bytebuf-to-string :UTF-8))))                                                                         ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_CBC_4a() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                          \n" +
                "  (load-module :crypt)                                                                       \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                       \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                   \n" +
                "        passphrase \"42\"]                                                                   \n" +
                "    (io/delete-file-on-exit file-in)                                                         \n" +
                "    (io/delete-file-on-exit file-out)                                                        \n" +
                "    (io/spit file-in \"1234567890\")                                                         \n" +
                "    (crypt/encrypt-file \"AES256-CBC\" passphrase (io/slurp file-in :binary true) file-out)  \n" +
                "    (-> (crypt/decrypt-file \"AES256-CBC\" passphrase (io/slurp file-out :binary true))      \n" +
                "        (bytebuf-to-string :UTF-8))))                                                        ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_CBC_4b() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                       \n" +
                "  (load-module :crypt)                                                                    \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                    \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                                \n" +
                "        passphrase \"42\"]                                                                \n" +
                "    (io/delete-file-on-exit file-in)                                                      \n" +
                "    (io/delete-file-on-exit file-out)                                                     \n" +
                "    (io/spit file-in \"1234567890\")                                                      \n" +
                "    (->> (crypt/encrypt-file \"AES256-CBC\" passphrase (io/slurp file-in :binary true))   \n" +
                "         (io/spit file-out))                                                              \n" +
                "    (-> (crypt/decrypt-file \"AES256-CBC\" passphrase (io/slurp file-out :binary true))  \n" +
                "        (bytebuf-to-string :UTF-8))))                                                     ";

        assertEquals("1234567890", venice.eval(script));
    }

    @Test
    public void test_file_encrypt_decrypt_AES256_CBC_4c() {
        final Venice venice = new Venice();

        // bytebuf
        final String script =
                "(do                                                                                     \n" +
                "  (load-module :crypt)                                                                  \n" +
                "  (let [file-in    (io/temp-file \"test-\", \".data\")                                  \n" +
                "        file-out   (io/temp-file \"test-\", \".data.enc\")                              \n" +
                "        passphrase \"42\"]                                                              \n" +
                "    (io/delete-file-on-exit file-in)                                                    \n" +
                "    (io/delete-file-on-exit file-out)                                                   \n" +
                "    (io/spit file-in \"1234567890\")                                                    \n" +
                "    (-<> (crypt/encrypt-file \"AES256-CBC\" passphrase (io/slurp file-in :binary true)) \n" +
                "         (crypt/decrypt-file \"AES256-CBC\" passphrase <>)                              \n" +
                "         (bytebuf-to-string <> :UTF-8))))                                               ";

        assertEquals("1234567890", venice.eval(script));
    }
}

