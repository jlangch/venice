package com.github.jlangch.venice.support;

import java.util.function.Predicate;


public class Functions {

    public Functions() {

    }


    public boolean evalPredicate(final Predicate<String> predicate, final String value) {
        return predicate.test(value);
    }
}
