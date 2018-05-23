/*
 * Copyright 2012-2018 Udo Klimaschewski
 * 
 * http://UdoJava.com/
 * http://about.me/udo.klimaschewski
 *
 * Derivative work: ExBuilder (https://github.com/ethancommitpush)
 * Modifications Copyright 2018 Yisin Lin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package com.udojava.evalex;

import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import idv.ethancommitpush.exbuilder.ExpNode;
import idv.ethancommitpush.exbuilder.ExpNode.ExpNodeType;

public class Expression {

	/** The {@link MathContext} to use for calculations. **/
	private MathContext mc = null;

	/** The characters (other than letters and digits) allowed as the first character in a variable. */
	private String firstVarChars = "_";

	/** The characters (other than letters and digits) allowed as the second or subsequent characters in a variable. */
	private String varChars = "_";

	/** The original infix expression. */
	private final String originalExpression;

	/** The current infix expression, with optional variable substitutions. */
	private String expression = null;

	/** The cached RPN (Reverse Polish Notation) of the expression. */
	private List<Token> rpn = null;

	/** All defined variables with name and value. */
	private Map<String, ExpNode> variableMap;

	/** All defined operators with name and implementation. */
	private Map<String, Operator> operatorMap;

	/** All defined functions with name and implementation. */
	private Map<String, LazyFunction> functionMap;

	/** What character to use for decimal separators. */
	private static final char decimalSeparator = '.';

	/** What character to use for minus sign (negative values). */
	private static final char minusSign = '-';

	/** The BigDecimal representation of the left parenthesis, used for parsing varying numbers of function parameters. */
	private static final LazyNumber PARAMS_START = new LazyNumber() {
		public ExpNode eval() {
			return null;
		}

		public String getString() {
			return null;
		}
	};

	enum TokenType {
		VARIABLE, FUNCTION, LITERAL, OPERATOR, UNARY_OPERATOR, OPEN_PAREN, COMMA, CLOSE_PAREN, HEX_LITERAL, STR_LITERAL
	}

	class Token {
		public String surface = "";
		public TokenType type;
		public int pos;

		public void append(char c) {
			surface += c;
		}

		public void append(String s) {
			surface += s;
		}

		public char charAt(int pos) {
			return surface.charAt(pos);
		}

		public int length() {
			return surface.length();
		}

		@Override
		public String toString() {
			return surface;
		}
	}

	/**
	 * Expression tokenizer that allows to iterate over a {@link String}
	 * expression token by token. Blank characters will be skipped.
	 */
	private class Tokenizer implements Iterator<Token> {

		/** Actual position in expression string. */
		private int pos = 0;

		/** The original input expression. */
		private String input;
		/** The previous token or <code>null</code> if none. */
		private Token previousToken;

		/**
		 * Creates a new tokenizer for an expression.
		 * @param input The expression string.
		 */
		public Tokenizer(String input) {
			this.input = input.trim();
		}

		@Override
		public boolean hasNext() {
			return (pos < input.length());
		}

		/**
		 * Peek at the next character, without advancing the iterator.
		 * @return The next character or character 0, if at end of string.
		 */
		private char peekNextChar() {
			if (pos < (input.length() - 1)) {
				return input.charAt(pos + 1);
			} else {
				return 0;
			}
		}

		private boolean isHexDigit(char ch) {
			return ch == 'x' || ch == 'X' || (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
		}

		@Override
		public Token next() {
			Token token = new Token();

			if (pos >= input.length()) {
				return previousToken = null;
			}
			char ch = input.charAt(pos);
			while (Character.isWhitespace(ch) && pos < input.length()) {
				ch = input.charAt(++pos);
			}
			token.pos = pos;

			boolean isHex = false;
			boolean isStr = false;
			

			if (ch == '\'' ) {
				isStr = true;
				for (int i = 0; isStr; i += 1) {
					if (i == 0) {
						pos++;
						ch = pos == input.length() ? 0 : input.charAt(pos);
					}
					if (ch != '\'') {
						token.append(input.charAt(pos++));
						ch = pos == input.length() ? 0 : input.charAt(pos);
					} else {
						pos++;
						ch = pos == input.length() ? 0 : input.charAt(pos);
						break;
					}
				}
				token.type = TokenType.STR_LITERAL;
			} else if (Character.isDigit(ch)) {
				if(ch == '0' && (peekNextChar() == 'x' || peekNextChar() == 'X')) isHex = true;
				while ((isHex && isHexDigit(ch)) || (Character.isDigit(ch) || ch == decimalSeparator
                                                || ch == 'e' || ch == 'E'
                                                || (ch == minusSign && token.length() > 0 
                                                    && ('e'==token.charAt(token.length()-1) || 'E'==token.charAt(token.length()-1)))
                                                || (ch == '+' && token.length() > 0 
                                                    && ('e'==token.charAt(token.length()-1) || 'E'==token.charAt(token.length()-1)))
                                                ) && (pos < input.length())) {
					token.append(input.charAt(pos++));
					ch = pos == input.length() ? 0 : input.charAt(pos);
				}
				token.type = isHex ? TokenType.HEX_LITERAL : TokenType.LITERAL;
			} else if (Character.isLetter(ch) || firstVarChars.indexOf(ch) >= 0) {
				while ((Character.isLetter(ch) || Character.isDigit(ch)
						|| varChars.indexOf(ch) >= 0 || token.length() == 0 && firstVarChars.indexOf(ch) >= 0)
						&& (pos < input.length())) {
					token.append(input.charAt(pos++));
					ch = pos == input.length() ? 0 : input.charAt(pos);
				}
				//Remove optional white spaces after function or variable name
				if (ch == ' ') {
					while (ch == ' ' && pos < input.length()) {
						ch = input.charAt(pos++);
					}
					pos--;
				}
				token.type = ch == '(' ? TokenType.FUNCTION : TokenType.VARIABLE;
			} else if (ch == '(' || ch == ')' || ch == ',') {
				if(ch == '(') {
					token.type = TokenType.OPEN_PAREN;
				} else if (ch == ')') {
					token.type = TokenType.CLOSE_PAREN;
				} else {
					token.type = TokenType.COMMA;
				}
				token.append(ch);
				pos++;
			} else {
				String greedyMatch = "";
				int initialPos = pos;
				ch = input.charAt(pos);
				int validOperatorSeenUntil = -1;
				while (!Character.isLetter(ch) && !Character.isDigit(ch)
						&& firstVarChars.indexOf(ch) < 0 && !Character.isWhitespace(ch)
						&& ch != '(' && ch != ')' && ch != ','
						&& (pos < input.length())) {
							greedyMatch += ch;
							pos++;
							if(operatorMap.containsKey(greedyMatch)) {
								validOperatorSeenUntil = pos;
							}
							ch = pos == input.length() ? 0 : input.charAt(pos);
				}
				if(validOperatorSeenUntil != -1) {
					token.append(input.substring(initialPos, validOperatorSeenUntil));
					pos = validOperatorSeenUntil;
				} else {
					token.append(greedyMatch);
				}

				if(previousToken == null || previousToken.type == TokenType.OPERATOR || previousToken.type == TokenType.OPEN_PAREN 
						|| previousToken.type == TokenType.COMMA) {
					token.surface += "u";
					token.type = TokenType.UNARY_OPERATOR;
				} else {
					token.type = TokenType.OPERATOR;
				}
			}
			return previousToken = token;
		}

		@Override
		public void remove() {
			throw new ExpressionException("remove() not supported");
		}

	}

	/**
	 * Creates a new expression instance from an expression string with a given
	 * default match context of {@link MathContext#DECIMAL32}.
	 * @param expression The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or <code>"sin(y)>0 & max(z, 3)>3"</code>
	 */
//	public Expression(String expression) {
////		this(expression, MathContext.DECIMAL32);
//		this(expression, new MathContext(10, RoundingMode.HALF_EVEN));
//	}

	/**
	 * Creates a new expression instance from an expression string with a given default match context.
	 * @param expression The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or <code>"sin(y)>0 & max(z, 3)>3"</code>
	 * @param defaultMathContext The {@link MathContext} to use by default.
	 */
	public Expression(String expression, MathContext defaultMathContext
			, Map<String, ExpNode> variableMap, Map<String, Operator> operatorMap, Map<String, LazyFunction> functionMap) {
		this.mc = defaultMathContext;
		this.expression = expression;
		this.originalExpression = expression;
		this.variableMap = variableMap;
		this.operatorMap = operatorMap;
		this.functionMap = functionMap;

	}

	/**
	 * Implementation of the <i>Shunting Yard</i> algorithm to transform an infix expression to a RPN expression.
	 * @param expression The input expression in infx.
	 * @return A RPN representation of the expression, with each token as a list member.
	 */
	private List<Token> shuntingYard(String expression) {
		List<Token> outputQueue = new ArrayList<Token>();
		Stack<Token> stack = new Stack<Token>();

		Tokenizer tokenizer = new Tokenizer(expression);

		Token lastFunction = null;
		Token previousToken = null;
		while (tokenizer.hasNext()) {
			Token token = tokenizer.next();
			switch(token.type) {
				case LITERAL:
				case HEX_LITERAL:
					outputQueue.add(token);
					break;
				case STR_LITERAL:
					outputQueue.add(token);
					break;
				case VARIABLE:
					outputQueue.add(token);
					break;
				case FUNCTION:
					stack.push(token);
					lastFunction = token;
					break;
				case COMMA:
					if (previousToken != null && previousToken.type == TokenType.OPERATOR) {
						throw new ExpressionException("Missing parameter(s) for operator " + previousToken +
								" at character position " + previousToken.pos);
					}
					while (!stack.isEmpty() && stack.peek().type != TokenType.OPEN_PAREN) {
						outputQueue.add(stack.pop());
					}
					if (stack.isEmpty()) {
						throw new ExpressionException("Parse error for function '"
								+ lastFunction + "'");
					}
					break;
				case OPERATOR: {
					if (previousToken != null && (previousToken.type == TokenType.COMMA 
							|| previousToken.type == TokenType.OPEN_PAREN)) {
						throw new ExpressionException("Missing parameter(s) for operator " + token +
								" at character position " + token.pos);
					}
					Operator o1 = operatorMap.get(token.surface);
					if (o1 == null) {
						throw new ExpressionException("Unknown operator '" + token
								+ "' at position " + (token.pos + 1));
					}

					shuntOperators(outputQueue, stack, o1);
					stack.push(token);
					break;
				}
				case UNARY_OPERATOR: {
					if (previousToken != null && previousToken.type != TokenType.OPERATOR
							&& previousToken.type != TokenType.COMMA && previousToken.type != TokenType.OPEN_PAREN) {
						throw new ExpressionException("Invalid position for unary operator " + token +
								" at character position " + token.pos);
					}
					Operator o1 = operatorMap.get(token.surface);
					if (o1 == null) {
						throw new ExpressionException("Unknown unary operator '"
								+ token.surface.substring(0,token.surface.length() - 1)
								+ "' at position " + (token.pos + 1));
					}

					shuntOperators(outputQueue, stack, o1);
					stack.push(token);
					break;
				}
				case OPEN_PAREN:
					if (previousToken != null) {
						if (previousToken.type == TokenType.LITERAL || previousToken.type == TokenType.CLOSE_PAREN
								|| previousToken.type == TokenType.VARIABLE
								|| previousToken.type == TokenType.HEX_LITERAL
								|| previousToken.type == TokenType.STR_LITERAL) {
							// Implicit multiplication, e.g. 23(a+b) or (a+b)(a-b)
							Token multiplication = new Token();
							multiplication.append("*");
							multiplication.type = TokenType.OPERATOR;
							stack.push(multiplication);
						}
						// if the ( is preceded by a valid function, then it
						// denotes the start of a parameter list
						if (previousToken.type == TokenType.FUNCTION) {
							outputQueue.add(token);
						}
					}
					stack.push(token);
					break;
				case CLOSE_PAREN:
					if (previousToken != null && previousToken.type == TokenType.OPERATOR) {
						throw new ExpressionException("Missing parameter(s) for operator " + previousToken +
								" at character position " + previousToken.pos);
					}
					while (!stack.isEmpty() && stack.peek().type != TokenType.OPEN_PAREN) {
						outputQueue.add(stack.pop());
					}
					if (stack.isEmpty()) {
						throw new ExpressionException("Mismatched parentheses");
					}
					stack.pop();
					if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION) {
						outputQueue.add(stack.pop());
					}
			}
			previousToken = token;
		}

		while (!stack.isEmpty()) {
			Token element = stack.pop();
			if (element.type == TokenType.OPEN_PAREN || element.type == TokenType.CLOSE_PAREN) {
				throw new ExpressionException("Mismatched parentheses");
			}
			outputQueue.add(element);
		}
		return outputQueue;
	}

	private void shuntOperators(List<Token> outputQueue, Stack<Token> stack, Operator o1) {
		Expression.Token nextToken = stack.isEmpty() ? null : stack.peek();
		while (nextToken != null &&
                (nextToken.type == Expression.TokenType.OPERATOR || nextToken.type == Expression.TokenType.UNARY_OPERATOR)
                && ((o1.isLeftAssoc()
                    && o1.getPrecedence() <= operatorMap.get(nextToken.surface).getPrecedence())
                    || (o1.getPrecedence() < operatorMap.get(nextToken.surface).getPrecedence()))) {
            outputQueue.add(stack.pop());
            nextToken = stack.isEmpty() ? null : stack.peek();
        }
	}
	
	/**
	 * Evaluates the expression.
	 * @param stripTrailingZeros If set to <code>true</code> trailing zeros in the result are stripped.
	 * @return The result of the expression.
	 * @throws Exception 
	 */
	public ExpNode eval() throws Exception {

		Stack<LazyNumber> stack = new Stack<LazyNumber>();
		List<Token> rpn = getRPN();
		for (final Token token : rpn) {
//			String surface = token.surface;
			switch(token.type) {
				case UNARY_OPERATOR: {
					final LazyNumber value = stack.pop();
					LazyNumber result = new LazyNumber() {
						public ExpNode eval() throws Exception {
							return operatorMap.get(token.surface).eval(value.eval(), null);
						}

						@Override
						public String getString() throws Exception {
							return String.valueOf(operatorMap.get(token.surface).eval(value.eval(), null));
						}
					};
					stack.push(result);
					break;
				}
				case OPERATOR:
					final LazyNumber v1 = stack.pop();
					final LazyNumber v2 = stack.pop();
					LazyNumber result = new LazyNumber() {
						public ExpNode eval() throws Exception {
							return operatorMap.get(token.surface).eval(v2.eval(), v1.eval());
						}
						
						public String getString() throws Exception {
						    return String.valueOf(operatorMap.get(token.surface).eval(v2.eval(), v1.eval()));
						}
					};
					stack.push(result);
					break;
				case VARIABLE:
					if (!variableMap.containsKey(token.surface)) {
						throw new ExpressionException("Unknown operator or function: " + token);
					}

					stack.push(new LazyNumber() {
						public ExpNode eval() {
							ExpNode value = variableMap.get(token.surface);
							if (value == null) {
								value = null;
							} else if (value.getType() == ExpNodeType.NUMERIC) {
									value = value.round(mc);
							}
							return value;
						}

						public String getString() {
							return token.surface;
						}
					});
					break;
				case FUNCTION:
					LazyFunction f = functionMap.get(token.surface.toUpperCase(Locale.ROOT));
					ArrayList<LazyNumber> p = new ArrayList<LazyNumber>(
							!f.numParamsVaries() ? f.getNumParams() : 0);
					// pop parameters off the stack until we hit the start of
					// this function's parameter list
					while (!stack.isEmpty() && stack.peek() != PARAMS_START) {
						p.add(0, stack.pop());
					}
					
					if (stack.peek() == PARAMS_START) {
						stack.pop();
					}
					
					LazyNumber fResult = f.lazyEval(p);
					stack.push(fResult);
					break;
				case OPEN_PAREN:
					stack.push(PARAMS_START);
					break;
				case LITERAL:
					stack.push(new LazyNumber() {
						public ExpNode eval() {
							if (token.surface.equalsIgnoreCase("NULL")) {
								return null;
							}

							ExpNode value = new ExpNode(token.surface, mc);
							return value;
						}

						public String getString() {
							return String.valueOf(new ExpNode(token.surface, mc));
						}
					});
					break;
				case HEX_LITERAL:
					stack.push(new LazyNumber() {
						public ExpNode eval() {
							return new ExpNode(
									new BigInteger(token.surface.substring(2), 16)
									, mc);
						}
						public String getString() {
							return new BigInteger(token.surface.substring(2), 16).toString();
						} 
					});
					break;
				case STR_LITERAL:
					stack.push(new LazyNumber() {
						public ExpNode eval() {
							ExpNode ret = new ExpNode();
							ret.setText(token.surface);
							return ret;
						}
						public String getString() {
							return token.surface;
						} 
					});
					break;
				default:
					break;
			}
		}
		ExpNode result = stack.pop().eval();
		ExpNode ret = null;
		if (result == null) {
			ret = null;
		} else {
			ret = result;
		}
		return ret;
	}

	/**
	 * Cached access to the RPN notation of this expression, ensures only one
	 * calculation of the RPN per expression instance. If no cached instance
	 * exists, a new one will be created and put to the cache.
	 * @return The cached RPN instance.
	 */
	private List<Token> getRPN() {
		if (rpn == null) {
			rpn = shuntingYard(this.expression);
			validate(rpn);
		}
		return rpn;
	}

	/**
	 * Check that the expression has enough numbers and variables to fit the
	 * requirements of the operators and functions, also check 
	 * for only 1 result stored at the end of the evaluation.
	 */
	private void validate(List<Token> rpn) {
		/*-
		* Thanks to Norman Ramsey:
		* http://http://stackoverflow.com/questions/789847/postfix-notation-validation
		*/
		// each push on to this stack is a new function scope, with the value of each
		// layer on the stack being the count of the number of parameters in that scope
		Stack<Integer> stack = new Stack<Integer>();

		// push the 'global' scope
		stack.push(0);

		for (final Token token : rpn) {
			switch(token.type) {
				case UNARY_OPERATOR:
					if(stack.peek() < 1) {
						throw new ExpressionException("Missing parameter(s) for operator " + token);
					}
					break;
				case OPERATOR:
					if (stack.peek() < 2) {
						throw new ExpressionException("Missing parameter(s) for operator " + token);
					}
					// pop the operator's 2 parameters and add the result
					stack.set(stack.size() - 1, stack.peek() - 2 + 1);
					break;
				case FUNCTION:
					LazyFunction f = functionMap.get(token.surface.toUpperCase(Locale.ROOT));
					if(f == null) {
						throw new ExpressionException("Unknown function '" + token
								+ "' at position " + (token.pos + 1));
					}

					int numParams = stack.pop();
					if (!f.numParamsVaries() && numParams != f.getNumParams()) {
						throw new ExpressionException("Function " + token + " expected " 
								+ f.getNumParams() + " parameters, got " + numParams);
					}
					if (stack.size() <= 0) {
						throw new ExpressionException("Too many function calls, maximum scope exceeded");
					}
					// push the result of the function
					stack.set(stack.size() - 1, stack.peek() + 1);
					break;
				case OPEN_PAREN:
					stack.push(0);
					break;
				default:
					stack.set(stack.size() - 1, stack.peek() + 1);
			}
		}

		if (stack.size() > 1) {
			throw new ExpressionException("Too many unhandled function parameter lists");
		} else if (stack.peek() > 1) {
			throw new ExpressionException("Too many numbers or variables");
		} else if (stack.peek() < 1) {
			throw new ExpressionException("Empty expression");
		}
	}

	/** The original expression used to construct this expression, without variables substituted. */
	public String getOriginalExpression() {
		return this.originalExpression;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Expression that = (Expression) o;
		if (this.expression == null) {
			return that.expression == null;
		} else {
			return this.expression.equals(that.expression);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.expression == null ? 0 : this.expression.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.expression;
	}
	
}