package org.checkerframework.checker.dividebyzero;

import org.checkerframework.checker.units.qual.N;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.framework.type.HashcodeAtmVisitor;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.dataflow.cfg.node.*;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;

import java.util.HashMap;
import java.util.Set;

import org.checkerframework.checker.dividebyzero.qual.*;

import static org.checkerframework.checker.dividebyzero.DivByZeroTransfer.Comparison.*;

public class DivByZeroTransfer extends CFTransfer {

    enum Comparison {
        /**
         * ==
         */
        EQ,
        /**
         * !=
         */
        NE,
        /**
         * <
         */
        LT,
        /**
         * <=
         */
        LE,
        /**
         * >
         */
        GT,
        /**
         * >=
         */
        GE
    }

    enum BinaryOperator {
        /**
         * +
         */
        PLUS,
        /**
         * -
         */
        MINUS,
        /**
         *
         */
        TIMES,
        /**
         * /
         */
        DIVIDE,
        /**
         * %
         */
        MOD
    }

    private final String EQ_ = EQ.toString();
    private final String NE_ = NE.toString();
    private final String LT_ = LT.toString();
    private final String LE_ = LE.toString();
    private final String GT_ = GT.toString();
    private final String GE_ = GE.toString();

    private final Class ZERO = Zero.class;
    private final Class POSITIVE = Positive.class;
    private final Class NEGATIVE = Negative.class;
    private final Class NONNEGATIVE = NonNegative.class;
    private final Class NONPOSITIVE = NonPositive.class;
    private final Class NONZERO = NonZero.class;
    private final Class TOP = Top.class;
    private final Class BOTTOM = Bottom.class;

    private final String ZERO_ = ZERO.getSimpleName();
    private final String POSITIVE_ = POSITIVE.getSimpleName();
    private final String NEGATIVE_ = NEGATIVE.getSimpleName();
    private final String NONNEGATIVE_ = NONNEGATIVE.getSimpleName();
    private final String NONPOSITIVE_ = NONPOSITIVE.getSimpleName();
    private final String NONZERO_ = NONZERO.getSimpleName();
    private final String TOP_ = TOP.getSimpleName();
    private final String BOTTOM_ = BOTTOM.getSimpleName();

    private final Class[] plusArray =
            {ZERO, POSITIVE, NEGATIVE, NONNEGATIVE, NONPOSITIVE, NONZERO, TOP, BOTTOM,
                    POSITIVE, POSITIVE, TOP, POSITIVE, TOP, TOP, TOP, BOTTOM,
                    NEGATIVE, TOP, NEGATIVE, TOP, NEGATIVE, TOP, TOP, BOTTOM,
                    NONNEGATIVE, POSITIVE, TOP, NONNEGATIVE, TOP, TOP, TOP, BOTTOM,
                    NONPOSITIVE, TOP, NEGATIVE, TOP, NONPOSITIVE, TOP, TOP, BOTTOM,
                    NONZERO, TOP, TOP, TOP, TOP, TOP, TOP, BOTTOM,
                    TOP, TOP, TOP, TOP, TOP, TOP, TOP, BOTTOM,
                    BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM,
            };

    private final HashMap<String, HashMap<String, Class>> PLUSMAP =
            generateMapping(plusArray);

    private final Class[] multiArray =
            {ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, TOP, BOTTOM,
                    ZERO, POSITIVE, NEGATIVE, NONNEGATIVE, NONPOSITIVE, NONZERO, TOP, BOTTOM,
                    ZERO, NEGATIVE, POSITIVE, NONPOSITIVE, NONNEGATIVE, NONZERO, TOP, BOTTOM,
                    ZERO, NONNEGATIVE, NONPOSITIVE, NONNEGATIVE, NONPOSITIVE, TOP, TOP, BOTTOM,
                    ZERO, NONPOSITIVE, NONNEGATIVE, NONPOSITIVE, NONNEGATIVE, TOP, TOP, BOTTOM,
                    ZERO, NONZERO, NONZERO, TOP, TOP, NONZERO, TOP, BOTTOM,
                    TOP, TOP, TOP, TOP, TOP, TOP, TOP, BOTTOM,
                    BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM,
            };

    private final HashMap<String, HashMap<String, Class>> MULTIMAP =
            generateMapping(multiArray);

    private final Class[] divideArray =
            {BOTTOM, ZERO, ZERO, BOTTOM, BOTTOM, ZERO, BOTTOM, BOTTOM,
                    BOTTOM, NONNEGATIVE, NONPOSITIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, NONPOSITIVE, NONNEGATIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, NONNEGATIVE, NONPOSITIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, NONPOSITIVE, NONNEGATIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, TOP, TOP, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, TOP, TOP, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM
            };

    private final HashMap<String, HashMap<String, Class>> DIVIDEMAP =
            generateMapping(divideArray);

    private final Class[] modArray =
            {BOTTOM, ZERO, ZERO, BOTTOM, BOTTOM, ZERO, BOTTOM, BOTTOM,
                    BOTTOM, NONNEGATIVE, NONNEGATIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, NONPOSITIVE, NONPOSITIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, NONNEGATIVE, NONNEGATIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, NONPOSITIVE, NONPOSITIVE, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, TOP, TOP, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, TOP, TOP, BOTTOM, BOTTOM, TOP, BOTTOM, BOTTOM,
                    BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM,
            };

    private final HashMap<String, HashMap<String, Class>> MODMAP =
            generateMapping(modArray);

    private final Class[] operatorArray =
            {POSITIVE, NONNEGATIVE, ZERO, NONPOSITIVE, NEGATIVE, NONZERO, TOP, BOTTOM,
                    TOP, TOP, NONZERO, TOP, TOP, TOP, TOP, BOTTOM,
                    POSITIVE, POSITIVE, POSITIVE, TOP, TOP, TOP, TOP, BOTTOM,
                    POSITIVE, NONNEGATIVE, NONNEGATIVE, TOP, TOP, TOP, TOP, BOTTOM,
                    TOP, TOP, NEGATIVE, NEGATIVE, NEGATIVE, TOP, TOP, BOTTOM,
                    TOP, TOP, NONPOSITIVE, NONPOSITIVE, NEGATIVE, TOP, TOP, BOTTOM,
                    TOP, TOP, TOP, TOP, TOP, TOP, TOP, BOTTOM,
                    BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM
            };

    private final HashMap<String, HashMap<String, Class>> OPMAP =
            generateRefineLhsOfComparison(operatorArray);

    // ========================================================================
    // Transfer functions to implement

    /**
     * Assuming that a simple comparison (lhs `op` rhs) returns true, this
     * function should refine what we know about the left-hand side (lhs). (The
     * input value "lhs" is always a legal return value, but not a very useful
     * one.)
     *
     * <p>For example, given the code
     * <pre>
     * if (y != 0) { x = 1 / y; }
     * </pre>
     * the comparison "y != 0" causes us to learn the fact that "y is not zero"
     * inside the body of the if-statement. This function would be called with
     * "NE", "top", and "zero", and should return "not zero" (or the appropriate
     * result for your lattice).
     *
     * <p>Note that the returned value should always be lower in the lattice
     * than the given point for lhs. The "glb" helper function below will
     * probably be useful here.
     *
     * @param operator a comparison operator
     * @param lhs      the lattice point for the left-hand side of the comparison expression
     * @param rhs      the lattice point for the right-hand side of the comparison expression
     * @return a refined type for lhs
     */
    private AnnotationMirror refineLhsOfComparison(
            Comparison operator,
            AnnotationMirror lhs,
            AnnotationMirror rhs) {

        // Logic for bottom and top cases
        AnnotationMirror newRhs = top();
        if (isTop(rhs)) {
            newRhs = top();
        } else if (isBottom(rhs)) {
            newRhs = bottom();
        } else {

            switch (operator) {
                case EQ:
//                    System.out.println("EQ branch");

                    newRhs = rhs;
                    break;
                case NE:
//                    System.out.println("NE branch");
                    if (isZero(rhs)) {
                        newRhs = nonzero();
                    } else {
                        newRhs = top();
                    }
                    break;
                case LT:
//                    System.out.println("LT branch");
                    newRhs = reflect(OPMAP.get(LT_).get(rhs.getAnnotationType().asElement().getSimpleName().toString()));
                    break;
                case LE:
//                    System.out.println("LE branch");

                    newRhs = reflect(OPMAP.get(LE_).get(rhs.getAnnotationType().asElement().getSimpleName().toString()));
                    break;
                case GT:
//                    System.out.println("GT branch");

                    newRhs = reflect(OPMAP.get(GT_)
                            .get(rhs.getAnnotationType().asElement().getSimpleName().toString()));
                    break;
                case GE:
//                    System.out.println("GE branch");

                    newRhs = reflect(OPMAP.get(GE_).get(rhs.getAnnotationType().asElement().getSimpleName().toString()));
                    break;

            }
        }

        AnnotationMirror result = glb(lhs, newRhs);
        return result;

    }

    /**
     * For an arithmetic expression (lhs `op` rhs), compute the point in the
     * lattice for the result of evaluating the expression. ("Top" is always a
     * legal return value, but not a very useful one.)
     *
     * <p>For example,
     * <pre>x = 1 + 0</pre>
     * should cause us to conclude that "x is not zero".
     *
     * @param operator a binary operator
     * @param lhs      the lattice point for the left-hand side of the expression
     * @param rhs      the lattice point for the right-hand side of the expression
     * @return the lattice point for the result of the expression
     */
    private AnnotationMirror arithmeticTransfer(
            BinaryOperator operator,
            AnnotationMirror lhs,
            AnnotationMirror rhs) {

        String rhsString = rhs.getAnnotationType().asElement().getSimpleName().toString();
        String lhsString = lhs.getAnnotationType().asElement().getSimpleName().toString();

        switch (operator) {
            case PLUS:
//                System.out.println("Plus CASE");
                AnnotationMirror resultPlus = reflect(PLUSMAP.get(lhsString).get(rhsString));
//                System.out.println("result plus was "+ resultPlus);
                return resultPlus;

            case MINUS:
//                System.out.println("MINUS CASE");
                AnnotationMirror resultMinus = reflect(PLUSMAP.get(lhsString).get(negateAnnotation(rhs).getAnnotationType().asElement().getSimpleName().toString()));
//                System.out.println("result was "+ resultMinus);
                return resultMinus;

            case MOD:
                return reflect(MODMAP.get(lhsString).get(rhsString));

            case TIMES:
//                System.out.println("Times CASE");
                AnnotationMirror resultTimes = reflect(MULTIMAP.get(lhsString).get(rhsString));
//                System.out.println("result was "+ resultTimes);
                return resultTimes;

            case DIVIDE:
                HashMap<String, Class> temp = DIVIDEMAP.get(lhs.getAnnotationType().asElement().getSimpleName().toString());
                Class result = temp.get(rhs.getAnnotationType().asElement().getSimpleName().toString());
                return reflect(result);

        }

        return top();
    }


    private boolean isPositive(AnnotationMirror hs) {
        return equal(reflect(Positive.class), hs);
    }

    private boolean isTop(AnnotationMirror hs) {
        return equal(reflect(Top.class), hs);
    }

    private boolean isBottom(AnnotationMirror hs) {
        return equal(reflect(Bottom.class), hs);
    }

    private boolean isZero(AnnotationMirror hs) {
        return equal(reflect(Zero.class), hs);
    }

    private boolean isNegative(AnnotationMirror hs) {
        return equal(reflect(Negative.class), hs);
    }

    private boolean isNonPositive(AnnotationMirror hs) {
        return equal(reflect(NonPositive.class), hs);
    }

    private boolean isNonZero(AnnotationMirror hs) {
        return equal(reflect(NonZero.class), hs);
    }

    private boolean isNonNegative(AnnotationMirror hs) {
        return equal(reflect(NonNegative.class), hs);
    }


    private AnnotationMirror positive() {
        return reflect(Positive.class);
    }

    private AnnotationMirror negative() {
        return reflect(Negative.class);
    }

    private AnnotationMirror zero() {
        return reflect(Zero.class);
    }

    private AnnotationMirror nonzero() {
        return reflect(NonZero.class);
    }

    private AnnotationMirror negateAnnotation(AnnotationMirror original) {
        // Order is important
        // 1- handle zeros
        if (isZero(original)) {
            return zero();
        }
        // handle negative and positive
        else if (isNegative(original)) {
            return positive();
        } else if (isPositive(original)) {
            return negative();
        }

        // Handle higher order points
        else if (isNonPositive(original)) {
            return positive();
        } else if (isNonNegative(original)) {
            return negative();
        } else if (isNonZero(original)) {
            return nonzero();
        }

        return top();
    }

    private HashMap<String, HashMap<String, Class>>
    generateMapping(Class[] plusArray) {
        HashMap<String, HashMap<String, Class>> result = new HashMap<>();

        int i = 0;
        HashMap<String, Class> zeroCol = new HashMap<>();

        zeroCol.put(ZERO_, plusArray[i++]);
        zeroCol.put(POSITIVE_, plusArray[i++]);
        zeroCol.put(NEGATIVE_, plusArray[i++]);
        zeroCol.put(NONNEGATIVE_, plusArray[i++]);
        zeroCol.put(NONPOSITIVE_, plusArray[i++]);
        zeroCol.put(NONZERO_, plusArray[i++]);
        zeroCol.put(TOP_, plusArray[i++]);
        zeroCol.put(BOTTOM_, plusArray[i++]);
        result.put(ZERO_, zeroCol);

        HashMap<String, Class> positiveCol = new HashMap<>();
        positiveCol.put(ZERO_, plusArray[i++]);
        positiveCol.put(POSITIVE_, plusArray[i++]);
        positiveCol.put(NEGATIVE_, plusArray[i++]);
        positiveCol.put(NONNEGATIVE_, plusArray[i++]);
        positiveCol.put(NONPOSITIVE_, plusArray[i++]);
        positiveCol.put(NONZERO_, plusArray[i++]);
        positiveCol.put(TOP_, plusArray[i++]);
        positiveCol.put(BOTTOM_, plusArray[i++]);
        result.put(POSITIVE_, positiveCol);

        HashMap<String, Class> negativeCol = new HashMap<>();
        negativeCol.put(ZERO_, plusArray[i++]);
        negativeCol.put(POSITIVE_, plusArray[i++]);
        negativeCol.put(NEGATIVE_, plusArray[i++]);
        negativeCol.put(NONNEGATIVE_, plusArray[i++]);
        negativeCol.put(NONPOSITIVE_, plusArray[i++]);
        negativeCol.put(NONZERO_, plusArray[i++]);
        negativeCol.put(TOP_, plusArray[i++]);
        negativeCol.put(BOTTOM_, plusArray[i++]);
        result.put(NEGATIVE_, negativeCol);

        HashMap<String, Class> nonnegativeCol = new HashMap<>();
        nonnegativeCol.put(ZERO_, plusArray[i++]);
        nonnegativeCol.put(POSITIVE_, plusArray[i++]);
        nonnegativeCol.put(NEGATIVE_, plusArray[i++]);
        nonnegativeCol.put(NONNEGATIVE_, plusArray[i++]);
        nonnegativeCol.put(NONPOSITIVE_, plusArray[i++]);
        nonnegativeCol.put(NONZERO_, plusArray[i++]);
        nonnegativeCol.put(TOP_, plusArray[i++]);
        nonnegativeCol.put(BOTTOM_, plusArray[i++]);
        result.put(NONNEGATIVE_, nonnegativeCol);

        HashMap<String, Class> nonpositiveCol = new HashMap<>();
        nonpositiveCol.put(ZERO_, plusArray[i++]);
        nonpositiveCol.put(POSITIVE_, plusArray[i++]);
        nonpositiveCol.put(NEGATIVE_, plusArray[i++]);
        nonpositiveCol.put(NONNEGATIVE_, plusArray[i++]);
        nonpositiveCol.put(NONPOSITIVE_, plusArray[i++]);
        nonpositiveCol.put(NONZERO_, plusArray[i++]);
        nonpositiveCol.put(TOP_, plusArray[i++]);
        nonpositiveCol.put(BOTTOM_, plusArray[i++]);
        result.put(NONPOSITIVE_, nonpositiveCol);

        HashMap<String, Class> nonzeroCol = new HashMap<>();
        nonzeroCol.put(ZERO_, plusArray[i++]);
        nonzeroCol.put(POSITIVE_, plusArray[i++]);
        nonzeroCol.put(NEGATIVE_, plusArray[i++]);
        nonzeroCol.put(NONNEGATIVE_, plusArray[i++]);
        nonzeroCol.put(NONPOSITIVE_, plusArray[i++]);
        nonzeroCol.put(NONZERO_, plusArray[i++]);
        nonzeroCol.put(TOP_, plusArray[i++]);
        nonzeroCol.put(BOTTOM_, plusArray[i++]);
        result.put(NONZERO_, nonzeroCol);

        HashMap<String, Class> topCol = new HashMap<>();
        topCol.put(ZERO_, plusArray[i++]);
        topCol.put(POSITIVE_, plusArray[i++]);
        topCol.put(NEGATIVE_, plusArray[i++]);
        topCol.put(NONNEGATIVE_, plusArray[i++]);
        topCol.put(NONPOSITIVE_, plusArray[i++]);
        topCol.put(NONZERO_, plusArray[i++]);
        topCol.put(TOP_, plusArray[i++]);
        topCol.put(BOTTOM_, plusArray[i++]);
        result.put(TOP_, topCol);

        HashMap<String, Class> bottomCol = new HashMap<>();
        bottomCol.put(ZERO_, plusArray[i++]);
        bottomCol.put(POSITIVE_, plusArray[i++]);
        bottomCol.put(NEGATIVE_, plusArray[i++]);
        bottomCol.put(NONNEGATIVE_, plusArray[i++]);
        bottomCol.put(NONPOSITIVE_, plusArray[i++]);
        bottomCol.put(NONZERO_, plusArray[i++]);
        bottomCol.put(TOP_, plusArray[i++]);
        bottomCol.put(BOTTOM_, plusArray[i++]);
        result.put(BOTTOM_, bottomCol);

        return result;
    }

    private HashMap<String, HashMap<String, Class>> generateRefineLhsOfComparison(Class[] plusArray) {
        HashMap<String, HashMap<String, Class>> result = new HashMap<>();
        int i = 0;

        HashMap<String, Class> zeroCol = new HashMap<>();
        zeroCol.put(POSITIVE_, plusArray[i++]);
        zeroCol.put(NONNEGATIVE_, plusArray[i++]);
        zeroCol.put(ZERO_, plusArray[i++]);
        zeroCol.put(NONPOSITIVE_, plusArray[i++]);
        zeroCol.put(NEGATIVE_, plusArray[i++]);
        zeroCol.put(NONZERO_, plusArray[i++]);
        zeroCol.put(TOP_, plusArray[i++]);
        zeroCol.put(BOTTOM_, plusArray[i++]);
        result.put(EQ_, zeroCol);


        HashMap<String, Class> positiveCol = new HashMap<>();
        positiveCol.put(POSITIVE_, plusArray[i++]);
        positiveCol.put(NONNEGATIVE_, plusArray[i++]);
        positiveCol.put(ZERO_, plusArray[i++]);
        positiveCol.put(NONPOSITIVE_, plusArray[i++]);
        positiveCol.put(NEGATIVE_, plusArray[i++]);
        positiveCol.put(NONZERO_, plusArray[i++]);
        positiveCol.put(TOP_, plusArray[i++]);
        positiveCol.put(BOTTOM_, plusArray[i++]);
        result.put(NE_, positiveCol);


        HashMap<String, Class> negativeCol = new HashMap<>();

        negativeCol.put(POSITIVE_, plusArray[i++]);
        negativeCol.put(NONNEGATIVE_, plusArray[i++]);
        negativeCol.put(ZERO_, plusArray[i++]);
        negativeCol.put(NONPOSITIVE_, plusArray[i++]);
        negativeCol.put(NEGATIVE_, plusArray[i++]);
        negativeCol.put(NONZERO_, plusArray[i++]);
        negativeCol.put(TOP_, plusArray[i++]);
        negativeCol.put(BOTTOM_, plusArray[i++]);
        result.put(GT_, negativeCol);

        HashMap<String, Class> nonnegativeCol = new HashMap<>();
        nonnegativeCol.put(POSITIVE_, plusArray[i++]);
        nonnegativeCol.put(NONNEGATIVE_, plusArray[i++]);
        nonnegativeCol.put(ZERO_, plusArray[i++]);
        nonnegativeCol.put(NONPOSITIVE_, plusArray[i++]);
        nonnegativeCol.put(NEGATIVE_, plusArray[i++]);
        nonnegativeCol.put(NONZERO_, plusArray[i++]);
        nonnegativeCol.put(TOP_, plusArray[i++]);
        nonnegativeCol.put(BOTTOM_, plusArray[i++]);
        result.put(GE_, nonnegativeCol);

        HashMap<String, Class> nonpositiveCol = new HashMap<>();
        nonpositiveCol.put(POSITIVE_, plusArray[i++]);
        nonpositiveCol.put(NONNEGATIVE_, plusArray[i++]);
        nonpositiveCol.put(ZERO_, plusArray[i++]);
        nonpositiveCol.put(NONPOSITIVE_, plusArray[i++]);
        nonpositiveCol.put(NEGATIVE_, plusArray[i++]);
        nonpositiveCol.put(NONZERO_, plusArray[i++]);
        nonpositiveCol.put(TOP_, plusArray[i++]);
        nonpositiveCol.put(BOTTOM_, plusArray[i++]);
        result.put(LT_, nonpositiveCol);

        HashMap<String, Class> nonzeroCol = new HashMap<>();
        nonzeroCol.put(POSITIVE_, plusArray[i++]);
        nonzeroCol.put(NONNEGATIVE_, plusArray[i++]);
        nonzeroCol.put(ZERO_, plusArray[i++]);
        nonzeroCol.put(NONPOSITIVE_, plusArray[i++]);
        nonzeroCol.put(NEGATIVE_, plusArray[i++]);
        nonzeroCol.put(NONZERO_, plusArray[i++]);
        nonzeroCol.put(TOP_, plusArray[i++]);
        nonzeroCol.put(BOTTOM_, plusArray[i++]);
        result.put(LE_, nonzeroCol);

        return result;
    }


    // ========================================================================
    // Useful helpers

    /**
     * Get the top of the lattice
     */
    private AnnotationMirror top() {
        return analysis.getTypeFactory().getQualifierHierarchy().getTopAnnotations().iterator().next();
    }

    /**
     * Get the bottom of the lattice
     */
    private AnnotationMirror bottom() {
        return analysis.getTypeFactory().getQualifierHierarchy().getBottomAnnotations().iterator().next();
    }

    /**
     * Compute the least-upper-bound of two points in the lattice
     */
    private AnnotationMirror lub(AnnotationMirror x, AnnotationMirror y) {
        return analysis.getTypeFactory().getQualifierHierarchy().leastUpperBound(x, y);
    }

    /**
     * Compute the greatest-lower-bound of two points in the lattice
     */
    private AnnotationMirror glb(AnnotationMirror x, AnnotationMirror y) {
        return analysis.getTypeFactory().getQualifierHierarchy().greatestLowerBound(x, y);
    }

    /**
     * Convert a "Class" object (e.g. "Top.class") to a point in the lattice
     */
    private AnnotationMirror reflect(Class<? extends Annotation> qualifier) {
        return AnnotationBuilder.fromClass(
                analysis.getTypeFactory().getProcessingEnv().getElementUtils(),
                qualifier);
    }

    /**
     * Determine whether two AnnotationMirrors are the same point in the lattice
     */
    private boolean equal(AnnotationMirror x, AnnotationMirror y) {
        return AnnotationUtils.areSame(x, y);
    }

    /**
     * `x op y` == `y flip(op) x`
     */
    private Comparison flip(Comparison op) {
        switch (op) {
            case EQ:
                return Comparison.EQ;
            case NE:
                return NE;
            case LT:
                return Comparison.GT;
            case LE:
                return Comparison.GE;
            case GT:
                return Comparison.LT;
            case GE:
                return LE;
            default:
                throw new IllegalArgumentException(op.toString());
        }
    }

    /**
     * `x op y` == `!(x negate(op) y)`
     */
    private Comparison negate(Comparison op) {
        switch (op) {
            case EQ:
                return NE;
            case NE:
                return Comparison.EQ;
            case LT:
                return Comparison.GE;
            case LE:
                return Comparison.GT;
            case GT:
                return LE;
            case GE:
                return Comparison.LT;
            default:
                throw new IllegalArgumentException(op.toString());
        }
    }

    // ========================================================================
    // Checker Framework plumbing

    public DivByZeroTransfer(CFAnalysis analysis) {
        super(analysis);
    }

    private TransferResult<CFValue, CFStore> implementComparison(Comparison op, BinaryOperationNode n, TransferResult<CFValue, CFStore> out) {
        QualifierHierarchy hierarchy = analysis.getTypeFactory().getQualifierHierarchy();
        AnnotationMirror l = findAnnotation(analysis.getValue(n.getLeftOperand()).getAnnotations(), hierarchy);
        AnnotationMirror r = findAnnotation(analysis.getValue(n.getRightOperand()).getAnnotations(), hierarchy);

        if (l == null || r == null) {
            // this can happen for generic types
            return out;
        }

        CFStore thenStore = out.getThenStore().copy();
        CFStore elseStore = out.getElseStore().copy();

        thenStore.insertValue(
                JavaExpression.fromNode(n.getLeftOperand()),
                refineLhsOfComparison(op, l, r));

        thenStore.insertValue(
                JavaExpression.fromNode(n.getRightOperand()),
                refineLhsOfComparison(flip(op), r, l));

        elseStore.insertValue(
                JavaExpression.fromNode(n.getLeftOperand()),
                refineLhsOfComparison(negate(op), l, r));

        elseStore.insertValue(
                JavaExpression.fromNode(n.getRightOperand()),
                refineLhsOfComparison(flip(negate(op)), r, l));

        return new ConditionalTransferResult<>(out.getResultValue(), thenStore, elseStore);
    }

    private TransferResult<CFValue, CFStore> implementOperator(BinaryOperator op, BinaryOperationNode n, TransferResult<CFValue, CFStore> out) {
        QualifierHierarchy hierarchy = analysis.getTypeFactory().getQualifierHierarchy();
        AnnotationMirror l = findAnnotation(analysis.getValue(n.getLeftOperand()).getAnnotations(), hierarchy);
        AnnotationMirror r = findAnnotation(analysis.getValue(n.getRightOperand()).getAnnotations(), hierarchy);

        if (l == null || r == null) {
            // this can happen for generic types
            return out;
        }

        AnnotationMirror res = arithmeticTransfer(op, l, r);
        CFValue newResultValue = analysis.createSingleAnnotationValue(res, out.getResultValue().getUnderlyingType());
        return new RegularTransferResult<>(newResultValue, out.getRegularStore());
    }

    @Override
    public TransferResult<CFValue, CFStore> visitEqualTo(EqualToNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.EQ, n, super.visitEqualTo(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNotEqual(NotEqualNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(NE, n, super.visitNotEqual(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThan(GreaterThanNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.GT, n, super.visitGreaterThan(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThanOrEqual(GreaterThanOrEqualNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.GE, n, super.visitGreaterThanOrEqual(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitLessThan(LessThanNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(Comparison.LT, n, super.visitLessThan(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitLessThanOrEqual(LessThanOrEqualNode n, TransferInput<CFValue, CFStore> p) {
        return implementComparison(LE, n, super.visitLessThanOrEqual(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitIntegerDivision(IntegerDivisionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.DIVIDE, n, super.visitIntegerDivision(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitIntegerRemainder(IntegerRemainderNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.MOD, n, super.visitIntegerRemainder(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitFloatingDivision(FloatingDivisionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.DIVIDE, n, super.visitFloatingDivision(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitFloatingRemainder(FloatingRemainderNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.MOD, n, super.visitFloatingRemainder(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalMultiplication(NumericalMultiplicationNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.TIMES, n, super.visitNumericalMultiplication(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalAddition(NumericalAdditionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.PLUS, n, super.visitNumericalAddition(n, p));
    }

    @Override
    public TransferResult<CFValue, CFStore> visitNumericalSubtraction(NumericalSubtractionNode n, TransferInput<CFValue, CFStore> p) {
        return implementOperator(BinaryOperator.MINUS, n, super.visitNumericalSubtraction(n, p));
    }

    private static AnnotationMirror findAnnotation(
            Set<AnnotationMirror> set, QualifierHierarchy hierarchy) {
        if (set.size() == 0) {
            return null;
        }
        Set<? extends AnnotationMirror> tops = hierarchy.getTopAnnotations();
        return hierarchy.findAnnotationInSameHierarchy(set, tops.iterator().next());
    }

}
