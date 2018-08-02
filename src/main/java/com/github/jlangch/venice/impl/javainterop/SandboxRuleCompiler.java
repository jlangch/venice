package com.github.jlangch.venice.impl.javainterop;

import java.util.regex.Pattern;


public class SandboxRuleCompiler {

	public static Pattern compile(final String rule) {
		String rule_ = rule;
				
		rule_ = rule_.replace("$", "[$]");
		rule_ = rule_.replace(".", "[.]");
		rule_ = rule_.replaceAll("[*][*]", "@@");
		rule_ = rule_.replaceAll("[*]", "[^.]*");
		rule_ = rule_.replaceAll("@@", ".*");

		return Pattern.compile(rule_);
	}
}
