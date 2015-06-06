package play.doc;

import org.parboiled.Rule;
import org.pegdown.Parser;
import org.pegdown.plugins.InlinePluginParser;

/**
 * Parser for parsing variables that are substituted with arbitrary text.
 */
public class VariableParser extends Parser implements InlinePluginParser {

    final String name;

    public VariableParser(String name) {
        super(ALL, 1000l, DefaultParseRunnerProvider);
        this.name = name;
    }

    public Rule VariableRule() {
        return NodeSequence(
                "%",
                name,
                push(new VariableNode(name)),
                "%"
        );
    }

    @Override
    public Rule[] inlinePluginRules() {
        return new Rule[] {VariableRule()};
    }
}
