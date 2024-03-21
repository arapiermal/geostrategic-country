package com.erimali.compute;

import java.util.Map;
import java.util.Stack;

import com.erimali.cntrygame.CommandLine;

public class BooleanSolver {

	public static boolean solve(String in) {
		return solve(in, null);
	}

	public static boolean solve(String in, Map<String, Double> variables) {
		in = in.replaceAll("\\s+", "");
		Stack<Boolean> stack = new Stack<>();
		Stack<Character> operatorStack = new Stack<>();
		boolean opposite = false;
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i) == '!') {
				opposite = true;// next pushed value -> !value
			} else if (in.charAt(i) == '&' || in.charAt(i) == '|') {
				operatorStack.push(in.charAt(i));
			} else if (in.charAt(i) == '_') {
				if (in.charAt(i + 1) == '(') {
					int k = i + 2;
					while (k < in.length() && in.charAt(k) != ')') {
						k++;
					}
					String var = in.substring(i+2,k);
					if (opposite) {
						stack.push(!parseCheck(var, variables));
						opposite = false;
					} else {
						stack.push(parseCheck(var, variables));
					}
					i = k;
				} else {
					int j = i + 1;
					while (j < in.length() && in.charAt(j) != '(') {
						j++;
					}
					String fName = in.substring(i + 1, j);
					int k = j + 1;
					while (k < in.length() && in.charAt(k) != ')') {
						k++;
					}
					String var = in.substring(j + 1, k);
					if (opposite) {
						stack.push(!parseFunc(fName, var, variables));
						opposite = false;
					} else {
						stack.push(parseFunc(fName, var, variables));
					}
					i = k;
				}
			} else {
				int j = i + 1;
				while (j < in.length() && !isORAND(in.charAt(j))) {
					j++;
				}
				if (opposite) {
					stack.push(!compare(in.substring(i, j), variables));
					opposite = false;
				} else {
					stack.push(compare(in.substring(i, j), variables));
				}
				i = j - 1;
			}
		}
		while (!operatorStack.isEmpty()) {
			stack.push(applyOperator(stack.pop(), stack.pop(), operatorStack.pop()));
		}
		return stack.pop();
	}

	private static boolean parseCheck(String var, Map<String, Double> variables) {
		if(variables.containsKey(var))
			return true;
		return false;
	}

	public static boolean parseFunc(String name, String var, Map<String, Double> variables) {
		var = var.trim();
		switch (name.toUpperCase()) {
		case "BOOL":
			if (variables.get(var) < 1.0) {
				return false;
			} else {
				return true;
			}
		case "CONTAINS":
			return variables.containsKey(var);
		case "POSITIVE":
			return MathSolver.solve(var, variables) >= 0.0;
		case "NEGATIVE":
			return MathSolver.solve(var, variables) <= -0.0;
		case "PRIME":
			return MathSolver.isPrime(MathSolver.solve(var, variables));
		//
		case "EXEC":
			return CommandLine.checkStatement(var);
		default:
			throw new IllegalArgumentException("INVALID BOOLEAN FUNCTION");
		}
	}

	public static boolean applyOperator(boolean b2, boolean b1, char c) {
		switch (c) {
		case '|':
			return b1 || b2;
		case '&':
			return b1 && b2;
		default:
			throw new IllegalArgumentException("INVALID BOOLEAN OPERATOR");
		}
	}

	public static boolean isORAND(char c) {
		return c == '|' || c == '&';
	}

	public static boolean hasPrecendenceORAND(char c1, char c2) {
		if (c1 == '|' && c2 == '&')
			return true;
		return false;
	}

	public static boolean compare(String in, Map<String, Double> variables) {
		boolean result = false;
		for (int i = 1; i < in.length() - 1; i++) {
			if (isComparator(in.charAt(i))) {
				if (isComparator(in.charAt(i), in.charAt(i + 1))) {
					double l = MathSolver.solve(in.substring(0, i), variables);
					double r = MathSolver.solve(in.substring(i + 2), variables);
					result = compare(l, r, in.charAt(i), in.charAt(i + 1));
				} else {
					double l = MathSolver.solve(in.substring(0, i), variables);
					double r = MathSolver.solve(in.substring(i + 1), variables);
					result = compare(l, r, in.charAt(i));
				}
				break;
			}
		}
		return result;
	}

	public static boolean isComparator(char ch) {
		return ch == '<' || ch == '>';
	}

	public static boolean isComparator(char c1, char c2) {
		return isComparator(c1) && c2 == '=' || c1 == '<' && c2 == '>' || c1 == '>' && c2 == '<';
	}

	public static boolean compare(double l, double r, char ch) {
		switch (ch) {
		case '<':
			return l < r;
		case '>':
			return l > r;
		default:
			throw new IllegalArgumentException("INVALID COMPARATOR");
		}
	}

	public static boolean compare(double l, double r, char c1, char c2) {
		String temp = "" + c1 + c2;
		switch (temp) {
		case "<=":
			return l <= r;
		case ">=":
			return l >= r;
		case "<>":
			return l != r;
		case "><":
			return l == r;
		default:
			throw new IllegalArgumentException("INVALID COMPARATOR");
		}
	}
}
