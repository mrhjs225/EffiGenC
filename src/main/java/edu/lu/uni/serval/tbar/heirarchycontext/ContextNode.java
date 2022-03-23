package edu.lu.uni.serval.tbar.heirarchycontext;

import java.util.List;
import java.util.ArrayList;

import edu.lu.uni.serval.jdt.tree.ITree;

public class ContextNode {
    private ITree node;
    private ContextNode parent;
    private ArrayList<ContextNode> children;
    private int level;
    // if node.toshortstring has "Name:" isColon = True
    private boolean isColon;

    public ContextNode(ITree node, int level) {
        this.node = node;
        this.parent = null;
        this.children = new ArrayList<ContextNode>();
        this.level = level;
        this.isColon = false;
    }

    public ContextNode getParent() {
        return this.parent;
    }

    public ContextNode setParent(ContextNode parentNode) {
        return this.parent = parentNode;
    }

    public List<ContextNode> getChildren() {
        return this.children;
    }

    public void addChild(ContextNode node) {
        this.children.add(node);
    }

    public String toString() {
        return this.node.toShortString();
    }

    public int getType() {
        return this.node.getType();
    }

    public int getLevel() {
        return this.level;
    }

    public void setIsColon(boolean iscolon) {
        this.isColon = iscolon;
    }

    public boolean getIsColon() {
        return this.isColon;
    }

    public ITree getNode() {
        return this.node;
    }
}
