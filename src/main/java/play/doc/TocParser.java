/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.doc;

import org.parboiled.Rule;
import org.pegdown.Parser;
import org.pegdown.plugins.BlockPluginParser;

/**
 * Parser for parsing variables that are substituted with arbitrary text.
 */
public class TocParser extends Parser implements BlockPluginParser {

    public TocParser() {
        super(ALL, 1000l, DefaultParseRunnerProvider);
    }

    public Rule TocRule() {
        return NodeSequence(
                "@toc@",
                push(new TocNode())
        );
    }

    @Override
    public Rule[] blockPluginRules() {
        return new Rule[] {TocRule()};
    }
}
