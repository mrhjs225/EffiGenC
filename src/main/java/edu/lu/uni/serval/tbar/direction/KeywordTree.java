package edu.lu.uni.serval.tbar.direction;

public class KeywordTree {
    private String packageName;
    private String fileName;
    private String methodName;
    private String stmtContents;
    private String identifier;

    public KeywordTree(String identifier) {
        this.identifier = identifier;
    }

    public String toString() {
        return identifier;
    }
}
