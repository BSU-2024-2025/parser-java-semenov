package com.alexey.parser.controller;

import com.alexey.parser.solver.Errors;
import com.alexey.parser.solver.exception.InvalidExpressionException;
import com.alexey.parser.solver.ExpressionSolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ExpressionController {

    private final List<String> expressions = new ArrayList<>();
    private String solvedExpression;
    private String result;

    @Autowired
    private ExpressionSolver expressionSolver;

    @GetMapping("/")
    public String getHomePage(Model model) {
        model.addAttribute("expressions", expressions);
        model.addAttribute("solvedExpression", solvedExpression);
        model.addAttribute("result", result);
        return "index";
    }

    @PostMapping("/")
    public String handleExpression(
            @RequestParam String action,
            @RequestParam(required = false) String expression,
            @RequestParam(required = false) Integer index,
            Model model) {
        try {
            if ("add".equals(action)) {
                if (expression == null || expression.trim().isEmpty()) {
                    throw new InvalidExpressionException(Errors.EMPTY_AFTER_COMMENTS);
                }
                expressions.add(expression);
            } else if ("delete".equals(action) && index != null) {
                expressions.remove((int) index);
            } else if ("solve".equals(action) && index != null) {
                solvedExpression = expressions.get(index);
                result = String.valueOf(expressionSolver.solveExpression(solvedExpression));
            }
        } catch (InvalidExpressionException e) {
            result = e.getMessage();
        }
        return "redirect:/";
    }
}
