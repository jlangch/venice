/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package example.scripts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


/**
 * Parsifal 'expr-parser.venice' example test
 */
public class ExprParserTest {

	@Test
	public void test_tokenizer_empty_1() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \"\")))                     ";

		assertEquals("[]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_empty_2() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \"  \")))                     ";

		assertEquals("[]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_op_add() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" + \")))                     ";

		assertEquals("[[:op \"+\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_op_sub() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" - \")))                     ";

		assertEquals("[[:op \"-\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_op_mul() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" * \")))                     ";

		assertEquals("[[:op \"*\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_op_div() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" / \")))                     ";

		assertEquals("[[:op \"/\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_lparen() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" ( \")))                     ";

		assertEquals("[[:lparen \"(\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_rparen() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" ) \")))                     ";

		assertEquals("[[:rparen \")\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_int_0() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 0 \")))                     ";

		assertEquals("[[:int \"0\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_int_5() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 5 \")))                     ";

		assertEquals("[[:int \"5\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_int_123() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 123 \")))                     ";

		assertEquals("[[:int \"123\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_float_0_0() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 0.0 \")))                     ";

		assertEquals("[[:float \"0.0\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_float_0_01234() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 0.01234 \")))                     ";

		assertEquals("[[:float \"0.01234\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_float_250_01234() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 250.01234 \")))                     ";

		assertEquals("[[:float \"250.01234\" (1,2)]]", new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_expr_1() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 1 + 2 \")))              ";

		assertEquals(
			"[[:int \"1\" (1,2)] [:op \"+\" (1,4)] [:int \"2\" (1,6)]]", 
			new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_expr_2() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 1 - 2 \")))              ";

		assertEquals(
			"[[:int \"1\" (1,2)] [:op \"-\" (1,4)] [:int \"2\" (1,6)]]", 
			new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_expr_3() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 1 * 2 \")))              ";

		assertEquals(
			"[[:int \"1\" (1,2)] [:op \"*\" (1,4)] [:int \"2\" (1,6)]]", 
			new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_expr_4() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" 1 / 2 \")))              ";

		assertEquals(
			"[[:int \"1\" (1,2)] [:op \"/\" (1,4)] [:int \"2\" (1,6)]]", 
			new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_expr_5() {
		final String script =
				"(do                                              \n" +
				"   (load-classpath-file \"expr-parser.venice\")  \n" +
				"                                                 \n" +
				"   (pr-str (tokenize \" (1 + 2) * 4 + 1\")))     ";

		assertEquals(
			"[[:lparen \"(\" (1,2)] " +
			 "[:int \"1\" (1,3)] "    +
			 "[:op \"+\" (1,5)] "     +
			 "[:int \"2\" (1,7)] "    +
			 "[:rparen \")\" (1,8)] " +
			 "[:op \"*\" (1,10)] "    +
			 "[:int \"4\" (1,12)] "   +
			 "[:op \"+\" (1,14)] "    +
			 "[:int \"1\" (1,16)]]", 
			new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_expr_6() {
		final String script =
				"(do                                                  \n" +
				"   (load-classpath-file \"expr-parser.venice\")      \n" +
				"                                                     \n" +
				"   (pr-str (tokenize \" (1.1 + 2.2) * 4.4 + 1.1\"))) ";

		assertEquals(
			"[[:lparen \"(\" (1,2)] "   +
			 "[:float \"1.1\" (1,3)] "  +
			 "[:op \"+\" (1,7)] "       +
			 "[:float \"2.2\" (1,9)] "  +
			 "[:rparen \")\" (1,12)] "  +
			 "[:op \"*\" (1,14)] "      +
			 "[:float \"4.4\" (1,16)] " +
			 "[:op \"+\" (1,20)] "      +
			 "[:float \"1.1\" (1,22)]]", 
			new Venice().eval(script));
	}

	@Test
	public void test_tokenizer_token_expr_7() {
		final String script =
				"(do                                                  \n" +
				"   (load-classpath-file \"expr-parser.venice\")      \n" +
				"                                                     \n" +
				"   (pr-str (tokenize \"(((1.1 + 2.2) * 4.4) + 1.1)\"))) ";

		assertEquals(
			"[[:lparen \"(\" (1,1)] "   +
			 "[:lparen \"(\" (1,2)] "   +
			 "[:lparen \"(\" (1,3)] "   +
			 "[:float \"1.1\" (1,4)] "  +
			 "[:op \"+\" (1,8)] "       +
			 "[:float \"2.2\" (1,10)] " +
			 "[:rparen \")\" (1,13)] "  +
			 "[:op \"*\" (1,15)] "      +
			 "[:float \"4.4\" (1,17)] " +
			 "[:rparen \")\" (1,20)] "  +
			 "[:op \"+\" (1,22)] "      +
			 "[:float \"1.1\" (1,24)] " +
			 "[:rparen \")\" (1,27)]]", 
			new Venice().eval(script));
	}


	@Test
	public void test_evaluate_empty() {
		final String script =
				"(do                                                      \n" +
				"   (load-classpath-file \"expr-parser.venice\")          \n" +
				"                                                         \n" +
				"   (assert (= nil (evaluate \"\")))                      \n" +
				"   (assert (= nil (evaluate \" \")))                     \n" +
				"   (assert (= nil (evaluate \"  \")))                    \n" +
				")";

		new Venice().eval(script);
	}

	@Test
	public void test_evaluate_int_value() {
		final String script =
				"(do                                                      \n" +
				"   (load-classpath-file \"expr-parser.venice\")          \n" +
				"                                                         \n" +
				"   (assert (= 0 (evaluate \"0\")))                       \n" +
				"   (assert (= 0 (evaluate \" 0\")))                      \n" +
				"   (assert (= 0 (evaluate \"0 \")))                      \n" +
				"   (assert (= 0 (evaluate \" 0 \")))                     \n" +
				"   (assert (= 0 (evaluate \"-0 \")))                     \n" +
				"   (assert (= 0 (evaluate \" -0 \")))                    \n" +
				"                                                         \n" +
				"   (assert (= 1 (evaluate \"1\")))                       \n" +
				"   (assert (= 1 (evaluate \" 1\")))                      \n" +
				"   (assert (= 1 (evaluate \"1 \")))                      \n" +
				"   (assert (= 1 (evaluate \" 1 \")))                     \n" +
				"   (assert (= -1 (evaluate \"-1 \")))                    \n" +
				"   (assert (= -1 (evaluate \" -1 \")))                   \n" +
				"                                                         \n" +
				"   (assert (= 123 (evaluate \"123\")))                   \n" +
				"   (assert (= 123 (evaluate \" 123\")))                  \n" +
				"   (assert (= 123 (evaluate \"123 \")))                  \n" +
				"   (assert (= 123 (evaluate \" 123 \")))                 \n" +
				"   (assert (= -123 (evaluate \"-123 \")))                \n" +
				"   (assert (= -123 (evaluate \" -123 \")))               \n" +
				")";

		new Venice().eval(script);
	}

	@Test
	public void test_evaluate_float_value() {
		final String script =
				"(do                                                      \n" +
				"   (load-classpath-file \"expr-parser.venice\")          \n" +
				"                                                         \n" +
				"   (assert (= 0.0 (evaluate \"0.0\")))                   \n" +
				"   (assert (= 0.0 (evaluate \" 0.0\")))                  \n" +
				"   (assert (= 0.0 (evaluate \"0.0 \")))                  \n" +
				"   (assert (= 0.0 (evaluate \" 0.0 \")))                 \n" +
				"   (assert (= 0.0 (evaluate \"-0.0 \")))                 \n" +
				"   (assert (= 0.0 (evaluate \" -0.0 \")))                \n" +
				"                                                         \n" +
				"   (assert (= 1.0 (evaluate \"1.0\")))                   \n" +
				"   (assert (= 1.0 (evaluate \" 1.0\")))                  \n" +
				"   (assert (= 1.0 (evaluate \"1.0 \")))                  \n" +
				"   (assert (= 1.0 (evaluate \" 1.0 \")))                 \n" +
				"   (assert (= -1.0 (evaluate \"-1.0 \")))                \n" +
				"   (assert (= -1.0 (evaluate \" -1.0 \")))               \n" +
				"                                                         \n" +
				"   (assert (= 123.45 (evaluate \"123.45\")))             \n" +
				"   (assert (= 123.45 (evaluate \" 123.45\")))            \n" +
				"   (assert (= 123.45 (evaluate \"123.45 \")))            \n" +
				"   (assert (= 123.45 (evaluate \" 123.45 \")))           \n" +
				"   (assert (= -123.45 (evaluate \"-123.45 \")))          \n" +
				"   (assert (= -123.45 (evaluate \" -123.45 \")))         \n" +
				")";

		new Venice().eval(script);
	}

	@Test
	public void test_evaluate_expression_int() {
		final String script =
				"(do                                                            \n" +
				"   (load-classpath-file \"expr-parser.venice\")                \n" +
				"                                                               \n" +
				"   (assert (= 0  (evaluate \"0 + 0 - 0\")))                    \n" +
				"   (assert (= 0  (evaluate \"0+0-0\")))                        \n" +
				"   (assert (= 11 (evaluate \" 1 + 2 * 4 + 2 - 6 / 3 + 2 \")))  \n" +
				"   (assert (= 11 (evaluate \" 1+2*4+2-6/3+2\")))               \n" +
				")";

		new Venice().eval(script);
	}
	
	@Test
	public void test_evaluate_expression_float() {
		final String script =
				"(do                                                                           \n" +
				"   (load-classpath-file \"expr-parser.venice\")                               \n" +
				"                                                                              \n" +
				"   (assert (= 0.0  (evaluate \"0.0 + 0.0 - 0.0\")))                           \n" +
				"   (assert (= 0.0  (evaluate \"0.0+0.0-0.0\")))                               \n" +
				"   (assert (= 12.1 (evaluate \" 1.2 + 2.2 * 4.0 + 2 - 6.4 / 3.2 + 2.1 \")))   \n" +
				"   (assert (= -5.5 (evaluate \" 1.2 + -2.2 * 4.0 + 2 - 6.4 / 3.2 + 2.1 \")))  \n" +
				"   (assert (= 12.1 (evaluate \"1.2+2.2*4.0+2-6.4/3.2+2.1\")))                 \n" +
				")";

		new Venice().eval(script);
	}
	
	@Test
	public void test_evaluate_expression_float_int() {
		final String script =
				"(do                                                                           \n" +
				"   (load-classpath-file \"expr-parser.venice\")                               \n" +
				"                                                                              \n" +
				"   (assert (= 0.0  (evaluate \"0.0 + 0 - 0.0\")))                             \n" +
				"   (assert (= 3.2  (evaluate \"1 + 2.2\")))                                   \n" +
				"   (assert (= 3.2  (evaluate \"1.2 + 2\")))                                   \n" +
				"   (assert (= -1.1 (evaluate \"1 - 2.1\")))                                   \n" +
				"   (assert (= -0.8 (evaluate \"1.2 - 2\")))                                   \n" +
				"   (assert (= 2.2  (evaluate \"1 * 2.2\")))                                   \n" +
				"   (assert (= 2.4  (evaluate \"1.2 * 2\")))                                   \n" +
				"   (assert (= 5.0  (evaluate \"10 / 2.0\")))                                  \n" +
				"   (assert (= 5.0  (evaluate \"15.0 / 3\")))                                  \n" +
				")";

		new Venice().eval(script);
	}
	
	@Test
	public void test_evaluate_expression_1() {
		final String script =
				"(do                                                                           \n" +
				"   (load-classpath-file \"expr-parser.venice\")                               \n" +
				"                                                                              \n" +
				"   (assert (= 1     (evaluate \"(1)\")))                                      \n" +
				"   (assert (= 1     (evaluate \"(+1)\")))                                     \n" +
				"   (assert (= -1    (evaluate \"(-1)\")))                                     \n" +
				"   (assert (= 1.1   (evaluate \"(1.1)\")))                                    \n" +
				"   (assert (= 1.1   (evaluate \"(+1.1)\")))                                   \n" +
				"   (assert (= -1.1  (evaluate \"(-1.1)\")))                                   \n" +
				"                                                                              \n" +
				"   (assert (= -1    (evaluate \"(-1)\")))                                     \n" +
				"   (assert (= -1    (evaluate \"-(1)\")))                                     \n" +
				"   (assert (= 1     (evaluate \"-(-1)\")))                                    \n" +
				"   (assert (= -1.1  (evaluate \"(-1.1)\")))                                   \n" +
				"   (assert (= -1.1  (evaluate \"-(1.1)\")))                                   \n" +
				"   (assert (= 1.1   (evaluate \"-(-1.1)\")))                                  \n" +
				"                                                                              \n" +
				"                                                                              \n" +
				"   (assert (= 1     (evaluate \"((1))\")))                                    \n" +
				"   (assert (= 1     (evaluate \"((+1))\")))                                   \n" +
				"   (assert (= -1    (evaluate \"((-1))\")))                                   \n" +
				"   (assert (= 1.1   (evaluate \"((1.1))\")))                                  \n" +
				"   (assert (= 1.1   (evaluate \"((+1.1))\")))                                 \n" +
				"   (assert (= -1.1  (evaluate \"((-1.1))\")))                                 \n" +
				"                                                                              \n" +
				"   (assert (= -1    (evaluate \"((-1))\")))                                   \n" +
				"   (assert (= -1    (evaluate \"(-(1))\")))                                   \n" +
				"   (assert (= -1    (evaluate \"-((1))\")))                                   \n" +
				"   (assert (= 1     (evaluate \"-((-1))\")))                                  \n" +
				"   (assert (= -1    (evaluate \"-(-(-1))\")))                                 \n" +
				"   (assert (= -1.1  (evaluate \"((-1.1))\")))                                 \n" +
				"   (assert (= -1.1  (evaluate \"(-(1.1))\")))                                 \n" +
				"   (assert (= -1.1  (evaluate \"-((1.1))\")))                                 \n" +
				"   (assert (= 1.1   (evaluate \"-((-1.1))\")))                                \n" +
				"   (assert (= -1.1  (evaluate \"-(-(-1.1))\")))                               \n" +
				")";

		new Venice().eval(script);
	}
	
	@Test
	public void test_evaluate_expression_2() {
		final String script =
				"(do                                                                           \n" +
				"   (load-classpath-file \"expr-parser.venice\")                               \n" +
				"                                                                              \n" +
				"   (assert (= 3     (evaluate \"(1 + 2)\")))                                  \n" +
				"   (assert (= -1    (evaluate \"(1 + -2)\")))                                 \n" +
				"   (assert (= -3    (evaluate \"(-1 + -2)\")))                                \n" +
				"   (assert (= 1     (evaluate \"-(1 + -2)\")))                                \n" +
				"                                                                              \n" +
				"   (assert (= 21    (evaluate \"(1 + 2) * (3 + 4)\")))                        \n" +
				"   (assert (= 11    (evaluate \"1 + 2 * 3 + 4\")))                            \n" +
				"   (assert (= 11    (evaluate \"1 + (2 * 3) + 4\")))                          \n" +
				"   (assert (= 3     (evaluate \"(3 + 6) / (2 + 1)\")))                        \n" +
				"   (assert (= 7     (evaluate \"3 + 6 / 2 + 1\")))                            \n" +
				"   (assert (= 7     (evaluate \"3 + (6 / 2) + 1\")))                          \n" +
				")";

		new Venice().eval(script);
	}

}
