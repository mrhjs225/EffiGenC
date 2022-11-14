package edu.lu.uni.serval.tbar.direction;

import java.util.ArrayList;

import edu.lu.uni.serval.jdt.tree.ITree;

public class KeywordTree {
    // From ITree, I can't get the name of file and package
    // private String packageName;
    // private String fileName;
    // private String methodName;
    private String stmtContents;
    private String identifier;
    private ITree targetNode;
    private int level;
    public ArrayList<KeywordTree> childNodes;

    public KeywordTree(ITree node, String identifier, int level) {
        this.childNodes = new ArrayList<>();
        this.targetNode = node;
        this.identifier = identifier;
        this.level = level;
    }

    public void setStmtContents(ITree stmtNode) {
        if (stmtNode == null) {
            this.stmtContents = "";
        } else {
            this.stmtContents = stmtNode.toString();
        }
    }

    public int getLevel() {
        return this.level;
    }

    public String toString() {
        return identifier;
    }

    public ITree getNode() {
        return this.targetNode;
    }
}
