package edu.lu.uni.serval.tbar.utils;

import java.util.ArrayList;
import edu.lu.uni.serval.tbar.utils.JsUtils;

import edu.lu.uni.serval.jdt.tree.ITree;
import edu.lu.uni.serval.tbar.utils.Checker;


public class JsStaticSlicer {
    public static void staticBackwardSlicer(ITree targetNode) {
        ArrayList<ITree> slicingElement = new ArrayList<>();
        ArrayList<ITree> slicedStatement = new ArrayList<>();
        JsUtils.extractNode(targetNode, slicingElement);
        invocationSearcher(targetNode, slicingElement, slicedStatement);
        
    }

    private static void invocationSearcher(ITree targetNode, ArrayList<ITree> slicingElement, ArrayList<ITree> slicedStatement) {
        backwardSlicer(targetNode, slicingElement, slicedStatement);
        // 전체 프로젝트 파일 읽으면서 호출되는 거 있는지 확인
        // 있을 경우 해당 메소드에 대한 invocationSearcher 호출
        // 끝 

    }

    private static void backwardSlicer(ITree targetNode, ArrayList<ITree> slicingElement, ArrayList<ITree> slicedStatement) {      
        int nodeIndex = findNodeIndex(targetNode);
        int i = nodeIndex - 1;
        while(i >= 0) {
            ITree siblingNode = targetNode.getParent().getChild(i);
            if (isSlice(siblingNode, slicingElement)) {
                slicedStatement.add(siblingNode);
            }
            i--;
        }
        if (!Checker.isMethodDeclaration(targetNode.getParent().getType())) {
            backwardSlicer(targetNode.getParent(), slicingElement, slicedStatement);
        }
    }

    private static boolean isSlice(ITree targetNode, ArrayList<ITree> slicingElement) {
        ArrayList<ITree> identifiers = new ArrayList<>();
        JsUtils.extractNode(targetNode, identifiers);

        for (ITree identifier : identifiers) {
            for (ITree element : slicingElement) {
                if (identifier.getLabel().equals(element.getLabel())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int findNodeIndex(ITree targetnode) {
        ITree parentNode = targetnode.getParent();
        int i = 0;
        for (ITree childNode : parentNode.getChildren()) {
            if (targetnode.equals(childNode)) {
                break;
            }
            i++;
        }
        return i;
    }

    public static void testPrint(ITree targetNode) {
        System.out.println("---------");
        System.out.println(targetNode.toShortString());
        System.out.println(targetNode.getLabel());

        for (ITree child : targetNode.getChildren()) {
            testPrint(child);
        }
    }
   
}
