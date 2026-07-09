package com.github.jlangch.venice.util.openai.bugs;

import com.github.jlangch.venice.util.openai.bug.UsageCostJsonParserBug;


public class UsageCostJsonParserBugTest {

    private UsageCostJsonParserBugTest() {
    }

    public static void main(String[] args) {
        final boolean workaround = false;

        UsageCostJsonParserBug.run(workaround);
    }

}
