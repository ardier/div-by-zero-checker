package org.checkerframework.checker.dividebyzero;

import com.sun.tools.javac.tree.JCTree;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

import javax.lang.model.type.TypeKind;
import java.lang.annotation.Annotation;
import com.sun.source.tree.*;

import java.util.Set;
import java.util.EnumSet;

import org.checkerframework.checker.dividebyzero.qual.*;

public class DivByZeroVisitor extends BaseTypeVisitor<DivByZeroAnnotatedTypeFactory> {

    /** Set of operators we care about */
    private static final Set<Tree.Kind> DIVISION_OPERATORS = EnumSet.of(
        /* x /  y */ Tree.Kind.DIVIDE,
        /* x /= y */ Tree.Kind.DIVIDE_ASSIGNMENT,
        /* x %  y */ Tree.Kind.REMAINDER,
        /* x %= y */ Tree.Kind.REMAINDER_ASSIGNMENT);

    /**
     * Determine whether to report an error at the given binary AST node.
     * The error text is defined in the messages.properties file.
     * @param node the AST node to inspect
     * @return true if an error should be reported, false otherwise
     */
    private boolean errorAt(BinaryTree node) {
//        printNodeInfo(node);

        // Step 1: if the node doesn't have annotations or is an integer, print it out and be done


            // Create rules
        /* x /  y  Tree.Kind.DIVIDE,
        x /= y  Tree.Kind.DIVIDE_ASSIGNMENT,
        x %  y  Tree.Kind.REMAINDER,
        x %= y  Tree.Kind.REMAINDER_ASSIGNMENT);*/

            // Step 1: Are we using one of the above operations:
        if (DIVISION_OPERATORS.contains(node.getKind())) {

                    if (isNonZero(node)) {
//                        System.out.println("returning false");
                        return false;
                    }
//                    System.out.println("returning true");

                    return true;
                }
        // all other cases should report false
        return false;
    }

    /**
     * Helper function to print node information for debugging purposes
     * @param node the AST node to inspect
     */
    private void printNodeInfo(BinaryTree node) {
        System.out.println();
        System.out.println("printNodeInfo");
        System.out.println("node was "+ node);
        System.out.println("is node int "+ isInt(node));
        System.out.println("does it have top annotation"+ hasAnnotation(node, Top.class));
        System.out.println("node.getRightOperand().getKind(): "+node.getRightOperand());
        System.out.println("node.getRightOperand().getKind().name(): "+node.getRightOperand().getKind().name());
        System.out.println("node.getRightOperand().getClass().getName(): "+node.getRightOperand().getClass().getName());
        System.out.println("node.getRightOperand().toString(): "+node.getRightOperand().toString());
        System.out.println("node.getRightOperand().getClass(): "+node.getRightOperand().getClass());
    }

    /**
     * Helper function to print node information for debugging purposes
     * @param node the AST node to inspect
     */
    private void printNodeInfo(CompoundAssignmentTree node) {
        System.out.println();
        System.out.println();
        System.out.println("node was "+ node);
        System.out.println("node kind was "+ node.getKind());
        System.out.println("node.getExpression(): "+node.getExpression());
        System.out.println("node.getExpression().getKind().name(): "+node.getExpression().getKind().name());
        System.out.println("node.getExpression().getClass().getName(): "+node.getExpression().getClass().getName());
        System.out.println("node.getVariable().getClass(): "+node.getVariable().getClass());
        System.out.println("node.getVariable().getKind().name(): "+node.getVariable().getKind().name());
        System.out.println("node.getVariable().toString(): "+node.getVariable().toString());

    }

    private boolean isNonZero (BinaryTree node) {
        return hasAnnotation(node.getRightOperand(), NonZero.class) ||
                hasAnnotation(node.getRightOperand(), Positive.class) ||
                hasAnnotation(node.getRightOperand(), Negative.class);
    }

    private boolean isNonZero (CompoundAssignmentTree node) {
        return hasAnnotation(node.getExpression(), NonZero.class) ||
                hasAnnotation(node.getExpression(), Positive.class) ||
                hasAnnotation(node.getExpression(), Negative.class);
    }

    /**
     * Determine whether to report an error at the given compound assignment
     * AST node. The error text is defined in the messages.properties file.
     * @param node the AST node to inspect
     * @return true if an error should be reported, false otherwise
     */
    private boolean errorAt(CompoundAssignmentTree node) {
        // A CompoundAssignmentTree represents any binary operator combined with an assignment,
        // such as "x += 10".
//        printNodeInfo(node);

            // Create rules
        /* x /  y  Tree.Kind.DIVIDE,
        x /= y  Tree.Kind.DIVIDE_ASSIGNMENT,
        x %  y  Tree.Kind.REMAINDER,
        x %= y  Tree.Kind.REMAINDER_ASSIGNMENT);*/

            // Step 1: Are we using one of the above operations:
            if (DIVISION_OPERATORS.contains(node.getKind())) {
                    if (isNonZero(node)) {
                        return false;
                    }
                    return true;
                }
        System.out.println("The end");
        return false;
    }



    // ========================================================================
    // Useful helpers

    private static final Set<TypeKind> INT_TYPES = EnumSet.of(
        TypeKind.INT,
        TypeKind.LONG);

    private boolean isInt(Tree node) {
        return INT_TYPES.contains(atypeFactory.getAnnotatedType(node).getKind());
    }

    private boolean hasAnnotation(Tree node, Class<? extends Annotation> c) {
        return atypeFactory.getAnnotatedType(node).hasAnnotation(c);
    }

    // ========================================================================
    // Checker Framework plumbing

    public DivByZeroVisitor(BaseTypeChecker c) {
        super(c);
    }

    @Override
    public Void visitBinary(BinaryTree node, Void p) {
        if (isInt(node)) {
            if (errorAt(node)) {
                checker.reportError(node, "divide.by.zero");
            }
        }
        return super.visitBinary(node, p);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        if (isInt(node.getExpression())) {
            if (errorAt(node)) {
                checker.reportError(node, "divide.by.zero");
            }
        }
        return super.visitCompoundAssignment(node, p);
    }

}
