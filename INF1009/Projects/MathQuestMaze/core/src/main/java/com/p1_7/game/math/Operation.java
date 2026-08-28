package com.p1_7.game.math;

/**
 * arithmetic operations available for use in math questions.
 *
 * each constant represents one of the four basic operations. the question generator
 * uses the set of allowed operations defined by the active difficulty level to decide
 * which operation to apply when building a new question.
 */
public enum Operation {

    /** addition of two operands */
    ADDITION,

    /** subtraction of two operands; the generator ensures the result is non-negative */
    SUBTRACTION,

    /** multiplication of two operands */
    MULTIPLICATION,

    /** integer division producing a whole-number quotient */
    DIVISION
}
