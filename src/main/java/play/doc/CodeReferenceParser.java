/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.doc;

import org.parboiled.Rule;
import org.parboiled.support.StringVar;
import org.pegdown.Parser;
import org.pegdown.plugins.BlockPluginParser;

/**
 * Parboiled parser for code references in markdown.
 *
 * Implemented in Java because this is necessary for parboiled enhancement to work.
 */
public class CodeReferenceParser extends Parser implements BlockPluginParser {

    public CodeReferenceParser() {
        super(ALL, 1000l, DefaultParseRunnerProvider);
    }

    public Rule CodeReference() {
        StringVar label = new StringVar();
        StringVar source = new StringVar();
        return NodeSequence(
                '@',
                '[',
                Sequence(ZeroOrMore(TestNot(']'), ANY), label.set(match())),
                ']',
                '(',
                Sequence(OneOrMore(TestNot(')'), ANY), source.set(match())),
                ')',
                push(new CodeReferenceNode(label.get(), source.get())),
                Newline()
        );
    }

    @Override
    public Rule[] blockPluginRules() {
        return new Rule[] {CodeReference()};
    }
}

