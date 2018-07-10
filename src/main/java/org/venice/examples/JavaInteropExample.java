package org.venice.examples;

import java.time.ZonedDateTime;

import org.venice.Venice;


public class JavaInteropExample {
	
	public static void main(final String[] args) {
		Venice venice = new Venice();
		
		// qualified classes
		Long val = (Long)venice.eval("(. :java.lang.Math :min 20 30)");
		
		// class import
		ZonedDateTime ts = (ZonedDateTime)venice.eval(
								"(do " +
								"   (import :java.time.ZonedDateTime) " +
								"   (. (. :ZonedDateTime :now) :plusDays 5))");
		
		System.out.println(val);
		System.out.println(ts);
	}
	
}