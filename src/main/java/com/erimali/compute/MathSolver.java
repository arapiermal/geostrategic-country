package com.erimali.compute;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import com.erimali.cntrygame.ErrorLog;

public class MathSolver {
	public static double solve(String express) {
		return evaluateExpression(express, null);
	}

	public static double solve(String express, Map<String, Double> variables) {
		return evaluateExpression(express, variables);
	}

	private static double evaluateExpression(String express, Map<String, Double> variables) {
		express = express.replaceAll("\\s+", "");
		if (express.isEmpty())
			return 0.0;
		Stack<Double> stack = new Stack<>();
		Stack<Character> operatorStack = new Stack<>();
		Stack<Integer> f1 = new Stack<>();
		Stack<Integer> f2 = new Stack<>();
		for (int i = 0; i < express.length(); i++) {
			char ch = express.charAt(i);
			// how much does this improve speed?? NEEDS IMPLEMENTATION ELSEWHERE AS WELL
			// if(Character.isWhitespace(ch))
			// continue;
			if ((i == 0 && ch == '-') || (i > 0 && ch == '-' && express.charAt(i - 1) == '(')) {
				stack.push(0.0);
				operatorStack.push('-');
			} else if (Character.isDigit(ch)) {
				if (i < express.length() - 3 && Character.isLetter(express.charAt(i + 1))
						&& express.charAt(i + 1) != 'e' && express.charAt(i + 1) != 'E') {
					if (ch == '0') {
						// array!!!!!!!!!!!!!!!!!!!!!!
						int start = i + 1;

						while (start < express.length() && express.charAt(start) != '(') {
							start++;
						}
						start++;// skip '('
						int end = start;
						while (end < express.length() && express.charAt(end) != ')') {
							end++;
						}
						String fName = express.substring(i + 1, start - 1);
						String arrName = express.substring(start, end);
						double value = parseFuncStringArr(fName, arrName, variables);
						stack.push(value);
						i = end + 1;
					}
					continue;
				}
				int j = i + 1;

				// boolean scientificNotation = false;
				while (j < express.length() && (Character.isDigit(express.charAt(j)) || express.charAt(j) == '.'
						|| express.charAt(j) == 'E' || express.charAt(j) == 'e')) {
					j++;
				}
				double value = Double.parseDouble(express.substring(i, j));

				stack.push(value);
				i = j - 1;
			} else if (ch == '(') {
				operatorStack.push(ch);
			} else if (ch == ')') {
				while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
					stack.push(performOperation(operatorStack.pop(), stack.pop(), stack.pop()));
				}
				if (!f1.isEmpty() && !f2.isEmpty()) {
					double tempVal = stack.pop();
					String fName = express.substring(f1.pop(), f2.pop());
					double value = parseFunc(fName, tempVal);
					if (i > 0 && express.charAt(i - 1) == '-') {
						stack.push(-value);
					} else
						stack.push(value);
				}
				operatorStack.pop();
			} else if (isOperator(ch)) {
				while (!operatorStack.isEmpty() && hasPrecedence(operatorStack.peek(), ch)) {
					stack.push(performOperation(operatorStack.pop(), stack.pop(), stack.pop()));
				}
				operatorStack.push(ch);
			} else if (Character.isLetter(ch)) {
				int j = i + 1;
				while (j < express.length()
						&& (Character.isLetterOrDigit(express.charAt(j)) || express.charAt(j) == '_')) {
					j++;
				}
				String name = express.substring(i, j);
				if (j < express.length() - 2 && express.charAt(j) == '.') {
					int k = j + 1;
					while (k < express.length() && (Character.isLetter(express.charAt(k)))) {
						k++;
					}
					String funcName = express.substring(j + 1, k);
					double value = parseFuncStringArr(funcName, name, variables);
					stack.push(value);

					i = k - 1;// because i++!!!!!!!!!
				}
				// implement ',' for funcs with multiple vals?
				else if (j < express.length() - 2 && express.charAt(j) == '(') {
					// if charAt(j+1) == ')' , function with some def value, or different func
					// method
					f1.push(i);
					f2.push(j);
					i = j - 1;

				} else if (j < express.length() - 1 && express.charAt(j) == '[') {
					// INNEFICIENT, int for [[]]?
					int k = j + 1;
					while (k < express.length() && express.charAt(k) != ']') {
						k++;
					}
					int index = (int) MathSolver.solve(express.substring(j + 1, k), variables);
					if (index >= 0) {
						double value = parseVar(name + index, variables);
						stack.push(value);
					}

					i = k;
				} else {
					double value = parseVar(name, variables);

					stack.push(value);
					i = j - 1;

				}

			}
		}

		while (!operatorStack.isEmpty()) {
			stack.push(performOperation(operatorStack.pop(), stack.pop(), stack.pop()));
		}

		return stack.pop();
	}

	public static double parseVar(String varName, Map<String, Double> variables) {
		if (variables == null) {
			ErrorLog.logError("NO VARIABLE MAP INPUT");
			return 0.0;
		} else if (variables.containsKey(varName)) {
			return variables.get(varName);
		} else
			throw new IllegalArgumentException("INVALID VARIABLE");
	}

// 0len(a)
	public static double parseFuncStringArr(String substring, String arr, Map<String, Double> vars)
			throws IllegalArgumentException {
        return switch (substring.toUpperCase()) {
            case "LEN" -> fakeArrLen(arr, vars);
            case "MAX" -> fakeArrMax(arr, vars);
            case "MIN" -> fakeArrMin(arr, vars);
            case "SUM" -> fakeArrSum(arr, vars);
            case "AVG" -> fakeArrAverage(arr, vars);
            default -> throw new IllegalArgumentException("INVALID FUNCTION");
        };
	}




	public static int fakeArrLen(String name, Map<String, Double> vars) {
		int len = 0;
		while (vars.containsKey(name + len)) {
			len++;
		}
		return len;
	}
	private static double fakeArrSum(String arrName, Map<String, Double> vars) {
		double sum = vars.getOrDefault(arrName + "0", 0.0);
		int i = 1;
		String currElem;
		while (vars.containsKey((currElem = arrName + i))) {
			sum += vars.get(currElem);
			i++;
		}
		return sum;
	}
	private static double fakeArrAverage(String arrName, Map<String, Double> vars) {
		double sum = vars.getOrDefault(arrName + "0", 0.0);
		int i = 1;
		String currElem;
		while (vars.containsKey((currElem = arrName + i))) {
			sum += vars.get(currElem);
			i++;
		}
		return sum/i;
	}
	public static double parseFuncVals(String substring, double... vals) throws IllegalArgumentException {
		switch (substring.toUpperCase()) {
		case "MAX":
			return arrMax(vals);
		case "MIN":
			return arrMin(vals);

		default:
			throw new IllegalArgumentException("INVALID FUNCTION");
		}
	}

	public static double arrMax(double... vals) {
		double max = vals[0];
		for (int i = 1; i < vals.length; i++) {
			if (max < vals[i]) {
				max = vals[i];
			}
		}
		return max;
	}

	public static double fakeArrMax(String arrName, Map<String, Double> vars) {
		double max = vars.getOrDefault(arrName + "0", 0.0);
		int i = 1;
		String currElem;
		while (vars.containsKey((currElem = arrName + i))) {
			if (max < vars.get(currElem)) {
				max = vars.get(currElem);
			}
			i++;
		}
		return max;
	}

	public static double fakeArrMin(String arrName, Map<String, Double> vars) {
		double min = vars.getOrDefault(arrName + "0", 0.0);
		int i = 1;
		String currElem;
		while (vars.containsKey((currElem = arrName + i))) {
			if (min > vars.get(currElem)) {
				min = vars.get(currElem);
			}
			i++;
		}
		return min;
	}

	public static double arrMin(double... vals) {
		double min = vals[0];
		for (int i = 1; i < vals.length; i++) {
			if (min > vals[i]) {
				min = vals[i];
			}
		}
		return min;
	}

	public static double parseFunc(String substring, double val) throws IllegalArgumentException {
		switch (substring.toUpperCase()) {
		case "TIME":
			int type = (int) val;
			if (type == 1) {
				return System.nanoTime();
			}
			return System.currentTimeMillis();
		case "DATE":
			int a = (int) val;
			LocalDateTime now = LocalDateTime.now();
			// ENHANCED!!!
            return switch (a) {
                case 0 -> now.getYear();
                case 1 -> now.getMonthValue();
                case 2 -> now.getDayOfMonth();
                case 3 -> now.getHour();
                case 4 -> now.getMinute();
                case 5 -> now.getSecond();
                case 6 -> now.getNano();
                case 7 -> now.getDayOfWeek().getValue();
                case 8 -> now.getDayOfYear();
                default -> 0;
            };
		case "INT":
			return (int) val;
		case "LONG":
			return (long) val;
		case "ABS":
			return Math.abs(val);
		case "ROUND":
			return Math.round(val);
		case "FLOOR":
			return Math.floor(val);
		case "CEIL":
			return Math.ceil(val);
		case "SQRT":
			return Math.sqrt(val);
		case "SIN":
			return Math.sin(val);
		case "COS":
			return Math.cos(val);
		case "RAD":
			return Math.toRadians(val);
		case "DEG":
			return Math.toDegrees(val);
		case "TAN":
			return Math.tan(val);
		case "LOG":
			return Math.log(val);
		case "LOG10":
			return Math.log10(val);
		case "EXP":
			return Math.exp(val);
		case "PI":
			return Math.pow(Math.PI, val);
		case "RAND":
			return (new Random()).nextDouble(val);
		case "RANDOM":
			return Math.random() * val;
		case "FACT":
			return factorial((long) val);
		case "FIB":
			return fibonacci((long) val);
		case "SUMUPTO":
			return sumUpTo((long) val);
		case "INFINITY":
			return ((int) val) >= 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		default:
			throw new IllegalArgumentException("INVALID FUNCTION");
		}
	}

	private static long sumUpTo(long val) {
		long s = 0;
		for (long i = 1; i <= val; i++) {
			s += i;
		}
		return s;
	}

	public static boolean isOperator(char ch) {
		return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%' || ch == '^';
	}

	public static boolean hasPrecedence(char op1, char op2) {
		if ((op1 == '*' || op1 == '/' || op1 == '%') && (op2 == '+' || op2 == '-')) {
			return true;
		} else return (op1 == '^') && (op2 == '+' || op2 == '-' || op2 == '*' || op2 == '/' || op2 == '%');
    }

	public static double performOperation(char operator, double rightOperand, double leftOperand) {
		switch (operator) {
		case '+':
			return leftOperand + rightOperand;
		case '-':
			return leftOperand - rightOperand;
		case '*':
			return leftOperand * rightOperand;
		case '/':
			//if (rightOperand == 0) {throw new ArithmeticException("Division by zero is not allowed.");}
			return leftOperand / rightOperand;
		case '%':
			//if (rightOperand == 0) {throw new ArithmeticException("Modulo by zero is not allowed.");}
			return leftOperand % rightOperand;
		case '^':

			return Math.pow(leftOperand, rightOperand);
		default:
			throw new IllegalArgumentException("Invalid operator");
		}
	}

	public static int factorial(int n) {
		int f = 1;
		for (int i = 2; i <= n; i++) {
			f *= i;
		}
		return f;
	}

	public static long factorial(long n) {
		long f = 1;
		for (long i = 2; i <= n; i++) {
			f *= i;
		}
		return f;
	}

	private static final String ARRSOLVESEPARATOR = "~";

	public static double[] solveArr(String in) {
		String a[] = in.split(ARRSOLVESEPARATOR);
		double[] vals = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			vals[i] = solve(in);
		}
		return vals;
	}

	public static boolean isPrime(double val) {
		if (val % 1 != 0)
			return false;
		if (val <= 1 || (val != 2 && val % 2 == 0))
			return false;
		long v = (long) val;
		for (long i = 3; i < (long) Math.sqrt(v); i += 2) {
			if (v % i == 0)
				return false;
		}
		return true;
	}

	public static int fibonacci(int n) {
		if (n <= 0) {
			return 0;
		} else if (n == 1) {
			return 1;
		} else {
			int a = 0, b = 1, temp;
			for (int i = 2; i <= n; i++) {
				temp = a + b;
				a = b;
				b = temp;
			}
			return b;
		}
	}

	public static long fibonacci(long n) {
		if (n <= 0) {
			return 0;
		} else if (n == 1) {
			return 1;
		} else {
			long a = 0, b = 1, temp;
			for (long i = 2; i <= n; i++) {
				temp = a + b;
				a = b;
				b = temp;
			}
			return b;
		}
	}
}
