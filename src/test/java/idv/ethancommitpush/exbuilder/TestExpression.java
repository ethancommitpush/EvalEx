/*
 * Copyright 2018 Yisin Lin
 * 
 * https://github.com/ethancommitpush
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
package idv.ethancommitpush.exbuilder;

import org.junit.Test;

import com.udojava.evalex.Expression;
import com.udojava.evalex.Function;
import com.udojava.evalex.LazyFunction;
import com.udojava.evalex.Operator;
import com.udojava.evalex.UnaryOperator;

import idv.ethancommitpush.exbuilder.ExpNode;
import idv.ethancommitpush.exbuilder.ExpNode.ExpNodeType;
import idv.ethancommitpush.exbuilder.ExpNodeBuilder;
import idv.ethancommitpush.exbuilder.ExpressionBuilder;
import idv.ethancommitpush.exbuilder.TreeMapBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class TestExpression {

	@Test
	public void testAndTokenizer() throws Exception {
		ExpressionBuilder builder = customExpressionBuilder();

		assertEquals("b80126d0", eval(builder, "hex(1523505506230)"));
		assertEquals("192.168.633.1", eval(builder, "replace('192.168.0.1','0','633')"));
		assertEquals("3,12,-30,0.875,3.141592654", eval(builder, "(1+2)+','+(+3*+4)+','+(-5*6)+','+(7/8)+','+PI"));
		assertEquals("reachable", eval(builder, "ping('127.0.0.1',1000)"));
		System.out.println("finished");
	}

	private String eval(ExpressionBuilder builder, String exp) throws Exception {
		long ts1 = System.currentTimeMillis();
		Expression expression = builder.setExpression(exp).build();
		ExpNode result = expression.eval();
		System.out.println((System.currentTimeMillis() - ts1) + "ms/call");
		return result.getText();
	}
	
	/** Define customized variables, operators, and functions */
	private ExpressionBuilder customExpressionBuilder() {
		final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
		ExpNode[] variables = new ExpNode[]{
				(new ExpNodeBuilder()).setNumeric(new BigDecimal(
						"3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679")).buildNumeric()
		};
		
		Map<String, ExpNode> variableMap = 
				(new TreeMapBuilder<String, ExpNode>(String.CASE_INSENSITIVE_ORDER))
				.add("PI", variables[0])
				.build();
		
		Operator[] operators = new Operator[]{
				new Operator("+", 20, true) {
					@Override
					public ExpNode eval(ExpNode v1, ExpNode v2) {
						assertNotNull(v1, v2);
						if (v1.getType() == ExpNodeType.NUMERIC && v2.getType() == ExpNodeType.NUMERIC) {
							return v1.add(v2);
						} else {
							return v1.append(v2);
						}
					}
				}
				, new Operator("-", 20, true) {
					@Override
					public ExpNode eval(ExpNode v1, ExpNode v2) {
						assertNotNull(v1, v2);
						return v1.subtract(v2);
					}
				}
				, new Operator("*", 30, true) {
					@Override
					public ExpNode eval(ExpNode v1, ExpNode v2) {
						assertNotNull(v1, v2);
						return v1.multiply(v2);
					}
				}
				, new Operator("/", 30, true) {
					@Override
					public ExpNode eval(ExpNode v1, ExpNode v2) {
						assertNotNull(v1, v2);
						return v1.divide(v2);
					}
				}
				, new Operator("<<", 30, true) {
					@Override
					public ExpNode eval(ExpNode v1, ExpNode v2) {
						return v1.leftShift(v2.getNumeric().intValue());
					}
				}
				, new Operator(">>", 30, true) {
					@Override
					public ExpNode eval(ExpNode v1, ExpNode v2) {
						return v1.rightShift(v2.getNumeric().intValue());
					}
				}
				, new UnaryOperator("-u", 60, false) {
					@Override
					public ExpNode evalUnary(ExpNode v1) {
						return v1.multiply(new ExpNode(-1));
					}
				}
				, new UnaryOperator("+u", 60, false) {
					@Override
					public ExpNode evalUnary(ExpNode v1) {
						return v1.multiply(new ExpNode(BigDecimal.ONE));
					}
				}
		};
		
		Map<String, Operator> operatorMap = 
				(new TreeMapBuilder<String, Operator>(String.CASE_INSENSITIVE_ORDER))
				.add("+", operators[0])
				.add("-", operators[1])
				.add("*", operators[2])
				.add("/", operators[3])
				.add("<<", operators[4])
				.add(">>", operators[5])
				.add("-u", operators[6])
				.add("+u", operators[7])
				.build();
		
		LazyFunction[] functions = new LazyFunction[]{
				new Function("ROUND", 1) {
					@Override
					public ExpNode eval(List<ExpNode> parameters) {
						assertNotNull(parameters.get(0));
						return parameters.get(0).round(mc);
					}
				}
				, new Function("RAND", 0) {
					@Override
					public ExpNode eval(List<ExpNode> parameters) {
						return ExpNode.random(mc);
					}
				}
				, new Function("TIME", 1) {
					@Override
					public ExpNode eval(List<ExpNode> parameters) {
						assertNotNull(parameters.get(0));
						if (parameters.size() < 1) {
							return null;
						}
						String arg = (String) parameters.get(0).getText();
						int ts = (int) (System.currentTimeMillis() / 1000);
						int offset = Integer.parseInt(arg);
						
						ExpNode tmp = new ExpNode(ts + offset);
						return tmp;
					}
				}
				, new Function("HEX", 1) {
					@Override
					public ExpNode eval(List<ExpNode> parameters) {
						assertNotNull(parameters.get(0));
						if (parameters.size() < 1) {
							return null;
						}
						BigDecimal arg = parameters.get(0).getNumeric();
						String ret = Integer.toHexString(arg.intValue());
						ExpNode tmp = ExpNode.genTextExpNode(ret);
						return tmp;
					}
				}
				, new Function("REPLACE", 3) {
					@Override
					public ExpNode eval(List<ExpNode> parameters) {
						assertNotNull(parameters.get(0));
						if (parameters.size() < 3) {
							return null;
						}
						String arg = parameters.get(0).getText();
						String regex = parameters.get(1).getText();
						String replacement = parameters.get(2).getText();
						ExpNode tmp = ExpNode.genTextExpNode(arg.replaceAll(regex, replacement));
						return tmp;
					}
				}
				, new Function("PING", 2) {
					@Override
					public ExpNode eval(List<ExpNode> parameters) throws IOException, InterruptedException {
						assertNotNull(parameters.get(0));
						if (parameters.size() < 2) {
							return null;
						}
						String arg = parameters.get(0).getText();
						long millis = parameters.get(1).getNumeric().longValue();
						String ret = ping(arg, millis);
						ExpNode tmp = ExpNode.genTextExpNode(ret);
						return tmp;
					}
				}
		};
		
		Map<String,LazyFunction> functionMap = (new TreeMapBuilder<String, LazyFunction>(String.CASE_INSENSITIVE_ORDER))
				.add("ROUND", functions[0])
				.add("RAND", functions[1])
				.add("TIME", functions[2])
				.add("HEX", functions[3])
				.add("REPLACE", functions[4])
				.add("PING", functions[5])
				.build();
		ExpressionBuilder builder = 
				(new ExpressionBuilder())
				.setMathContext(mc)
				.setVariableMap(variableMap)
				.setOperatorMap(operatorMap)
				.setFunctionMap(functionMap);
		return builder;
	}
	
	private String ping(String ipAddr, long millis) throws IOException, InterruptedException {
		String[] splits = ipAddr.split("\\.");
	    InetAddress inet;
	    inet = InetAddress.getByAddress(
	    		new byte[] { (byte) Integer.parseInt(splits[0]), (byte) Integer.parseInt(splits[1])
	    				, (byte) Integer.parseInt(splits[2]), (byte) Integer.parseInt(splits[3]) });
	    //try to wait
		Thread.sleep(millis);
	    boolean reachable = inet.isReachable(5000);
	    return reachable ? "reachable" : "unreachable";
	}
	
	private void assertNotNull(ExpNode v1) {
		if (v1 == null) {
			throw new ArithmeticException("Operand may not be null");
		}
	}
	
	private void assertNotNull(ExpNode v1, ExpNode v2) {
		if (v1 == null) {
			throw new ArithmeticException("First operand may not be null");
		}
		if (v2 == null) {
			throw new ArithmeticException("Second operand may not be null");
		}		
	}
}
