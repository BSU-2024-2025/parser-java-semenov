package com.alexey.parser;

import org.junit.jupiter.api.Test;

import com.alexey.parser.solver.Errors;
import com.alexey.parser.solver.ExpressionSolver;
import com.alexey.parser.solver.exception.InvalidExpressionException;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionSolverTest {

    private final ExpressionSolver solver = new ExpressionSolver();

    @Test
    void testValidExpressions() throws InvalidExpressionException {
        assertEquals(35.0, solver.solveExpression("30 + 5"));
        assertEquals(6.0, solver.solveExpression("2 * 3"));
        
        assertEquals(14.0, solver.solveExpression("2 * (3 + 4)"));
        assertEquals(9.0, solver.solveExpression("(1 + 2) * 3"));
        
        assertEquals(5.0, solver.solveExpression("10 - 5"));
        assertEquals(1.0, solver.solveExpression("6 / (4 + 2)"));
    }

    @Test
    void testValidExpressionsWithComments() throws InvalidExpressionException {
        assertEquals(10.0, solver.solveExpression("5 + 5 // addition //"));
        assertEquals(4.0, solver.solveExpression("8// division // / 2"));
    }

    @Test
    void testInvalidExpressions() {
        Exception exception = assertThrows(InvalidExpressionException.class, 
            () -> solver.solveExpression(""));
        assertEquals(Errors.EMPTY_AFTER_COMMENTS, exception.getMessage());

        exception = assertThrows(InvalidExpressionException.class, 
            () -> solver.solveExpression("10 / 0"));
        assertEquals(Errors.DIVISION_BY_ZERO, exception.getMessage());

        exception = assertThrows(InvalidExpressionException.class, 
            () -> solver.solveExpression("5 ^ 2"));
        assertEquals(Errors.UNSUPPORTED_OPERATOR, exception.getMessage());
        
        exception = assertThrows(InvalidExpressionException.class, 
            () -> solver.solveExpression("(3 + 5"));
        assertEquals(Errors.MISMATCHED_PARENTHESES, exception.getMessage());

        exception = assertThrows(InvalidExpressionException.class, 
            () -> solver.solveExpression("3 + * 5"));
        assertEquals(Errors.INVALID_EXPRESSION, exception.getMessage());
    }

    @Test
    void testEdgeCases() throws InvalidExpressionException {
        assertEquals(20.0, solver.solveExpression("((2 + 3) * 2) + ((4 / 2) * 5)"));

        assertEquals(15.0, solver.solveExpression("  10  + 5  "));
    }

    @Test
    void testVariableAssignments() throws InvalidExpressionException {
        assertEquals(10.0, solver.solveExpression("a = 5; b = 5; a + b"));
        assertEquals(14.0, solver.solveExpression("x = 7; y = 2 * x; y"));
        assertEquals(12.0, solver.solveExpression("var1 = 6; var2 = var1 * 2; var2"));
        assertEquals(15.0, solver.solveExpression("x = 10; y = 5; x + y"));

        Exception exception = assertThrows(InvalidExpressionException.class,
            () -> solver.solveExpression("a = 5; b = c + 3; a + b"));
        assertTrue(exception.getMessage().contains("Неизвестная переменная"));
    }

    @Test
    void testLeadingMinusHandling() throws InvalidExpressionException {
        assertEquals(-5.0, solver.solveExpression("-5"));
        assertEquals(-10.0, solver.solveExpression("-5 - 5"));
        assertEquals(-15.0, solver.solveExpression("x = -5; y = -10; x + y"));
        assertEquals(-6.0, solver.solveExpression("x = -3; x * 2"));
    }
}
