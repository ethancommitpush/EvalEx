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

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

import com.udojava.evalex.Expression;
import com.udojava.evalex.LazyFunction;
import com.udojava.evalex.Operator;

/** The builder to keep definition of all variables, operators, and functions, 
 *  for create customized Expression without adding the definition after it was created */
public class ExpressionBuilder {
	
	private String strExp;
	private MathContext mc;
	private Map<String, ExpNode> variableMap;
	private Map<String, Operator> operatorMap;
	private Map<String, LazyFunction> functionMap;
	
	public ExpressionBuilder setExpression(String strExp) {
		this.strExp = strExp;
		return this;
	}
	
	public ExpressionBuilder setMathContext(MathContext mc) {
		this.mc = mc;
		return this;
	}
	
	public ExpressionBuilder setVariableMap(Map<String, ExpNode> variableMap) {
		this.variableMap = variableMap;
		return this;
	}
	
	public ExpressionBuilder setOperatorMap(Map<String, Operator> operatorMap) {
		this.operatorMap = operatorMap;
		return this;
	}
	
	public ExpressionBuilder setFunctionMap(Map<String,LazyFunction> functionMap) {
		this.functionMap = functionMap;
		return this;
	}
	
	public Expression build() {
		if (mc == null) {
			mc = new MathContext(10, RoundingMode.HALF_EVEN);
		}
		Expression exp = new Expression(strExp, mc, variableMap, operatorMap, functionMap);
		return exp;
	}

}