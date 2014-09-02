package play.doc;

import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

import java.util.Collections;
import java.util.List;

/**
 * A table of contents node
 */
public class TocNode extends AbstractNode {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }
}