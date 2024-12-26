package com.alexey.parser.solver;

import com.alexey.parser.solver.exception.InvalidExpressionException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Service
public class ExpressionSolver {

    public double solveExpression(String input) throws InvalidExpressionException {
        input = input.replaceAll("//.*?//", "").trim();

        if (input.isEmpty()) {
            throw new InvalidExpressionException(Errors.EMPTY_AFTER_COMMENTS);
        }

        String[] parts = input.split(";");
        Map<String, Double> variables = new HashMap<>();

        for (int i = 0; i < parts.length - 1; i++) {
            String assignment = parts[i].trim();
            if (!assignment.isEmpty()) {
                String[] assignmentParts = assignment.split("=");
                if (assignmentParts.length != 2) {
                    throw new InvalidExpressionException("Неверное определение переменной: " + assignment);
                }
                String varName = assignmentParts[0].trim();
                String varValue = assignmentParts[1].trim();

                if (!varName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    throw new InvalidExpressionException("Недопустимое имя переменной: " + varName);
                }

                varValue = preprocessExpression(varValue);
                double value = eval(varValue, variables);
                variables.put(varName, value);
            }
        }

        String expression = parts[parts.length - 1].trim();
        if (expression.isEmpty()) {
            throw new InvalidExpressionException(Errors.INVALID_EXPRESSION);
        }

        expression = preprocessExpression(expression);
        return eval(expression, variables);
    }

    private String preprocessExpression(String expression) {
        expression = expression.trim();
        if (expression.startsWith("-")) {
            expression = "0" + expression;
        }

        expression = expression.replaceAll("\\(\\s*-", "(0-");
        return expression;
    }

    private double eval(String expression, Map<String, Double> variables) throws InvalidExpressionException {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int length = expression.length();
        boolean expectOperand = true; 
    
        for (int i = 0; i < length; i++) {
            char current = expression.charAt(i);
    
            if (Character.isWhitespace(current)) continue;
    
            if (Character.isDigit(current)) {
                StringBuilder buffer = new StringBuilder();
                while (i < length && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    buffer.append(expression.charAt(i++));
                }
                i--;
                values.push(Double.parseDouble(buffer.toString()));
                expectOperand = false; 
            } else if (Character.isLetter(current)) {
                StringBuilder buffer = new StringBuilder();
                while (i < length && (Character.isLetterOrDigit(expression.charAt(i)) || expression.charAt(i) == '_')) {
                    buffer.append(expression.charAt(i++));
                }
                i--;
                String varName = buffer.toString();
                if (!variables.containsKey(varName)) {
                    throw new InvalidExpressionException("Неизвестная переменная: " + varName);
                }
                values.push(variables.get(varName));
                expectOperand = false;
            } else if (current == '(') {
                operators.push(current);
                expectOperand = true; 
            } else if (current == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(apply(operators.pop(), values.pop(), values.pop()));
                }
                if (operators.isEmpty() || operators.pop() != '(') {
                    throw new InvalidExpressionException(Errors.MISMATCHED_PARENTHESES);
                }
                expectOperand = false; 
            } else if (isOperator(current)) {
                if (expectOperand) {
                    throw new InvalidExpressionException(Errors.INVALID_EXPRESSION);
                }
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(current)) {
                    values.push(apply(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(current);
                expectOperand = true; 
            } else {
                throw new InvalidExpressionException(String.format(Errors.UNSUPPORTED_OPERATOR, current));
            }
        }
    
        while (!operators.isEmpty()) {
            if (operators.peek() == '(') {
                throw new InvalidExpressionException(Errors.MISMATCHED_PARENTHESES);
            }
            values.push(apply(operators.pop(), values.pop(), values.pop()));
        }
    
        if (values.size() != 1) {
            throw new InvalidExpressionException(Errors.INVALID_EXPRESSION);
        }
    
        return values.pop();
    }
    
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    private double apply(char operator, double b, double a) throws InvalidExpressionException {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new InvalidExpressionException(Errors.DIVISION_BY_ZERO);
                }
                return a / b;
            default:
                throw new InvalidExpressionException(String.format(Errors.UNSUPPORTED_OPERATOR, operator));
        }
    }
}
