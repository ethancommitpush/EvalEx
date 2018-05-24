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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/** The object to keep both numeric and string type operands */
public class ExpNode extends Object {
	
	public enum ExpNodeType {NUMERIC, TEXT}
	private BigDecimal numeric;
	private String text;
	private ExpNodeType type = ExpNodeType.TEXT;
	
	public static ExpNode genTextExpNode(String text) {
		ExpNode tmp = new ExpNode();
		tmp.text = text;
		return tmp;
	}

	public ExpNode() {
	}

	public ExpNode(int val) {
		this(val, null);
	}

	public ExpNode(int val, MathContext mc) {
		this.numeric = mc != null ? new BigDecimal(val, mc) : new BigDecimal(val);
		this.text = numeric.toPlainString();
		this.type = ExpNodeType.NUMERIC;
	}

	public ExpNode(double val) {
		this(val, null);
	}

	public ExpNode(double val, MathContext mc) {
		this.numeric = mc != null ? new BigDecimal(val, mc) : new BigDecimal(val);
		this.text = numeric.toPlainString();
		this.type = ExpNodeType.NUMERIC;
	}

	public ExpNode(String val) {
		this(val, null);
	}

	public ExpNode(String val, MathContext mc) {
		this.numeric = mc != null ? new BigDecimal(val, mc) : new BigDecimal(val);
		this.text = numeric.toPlainString();
		this.type = ExpNodeType.NUMERIC;
	}

	public ExpNode(BigInteger val) {
		this(val, null);
	}

	public ExpNode(BigInteger val, MathContext mc) {
		this.numeric = mc != null ? new BigDecimal(val, mc) : new BigDecimal(val);
		this.numeric = new BigDecimal(val, mc);
		this.text = numeric.toPlainString();
		this.type = ExpNodeType.NUMERIC;
	}

	public ExpNode(BigDecimal numeric) {
		this.numeric = numeric;
		this.text = numeric.toPlainString();
		this.type = ExpNodeType.NUMERIC;
	}

	public BigDecimal getNumeric() {
		return numeric;
	}

	public void setNumeric(BigDecimal numeric) {
		this.numeric = numeric;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public ExpNodeType getType() {
		return type;
	}

	public void setType(ExpNodeType type) {
		this.type = type;
	}

	public ExpNode add(ExpNode augend) {
		return add(augend, null);
	}

	public ExpNode add(ExpNode augend, MathContext mc) {
		BigDecimal ret = mc != null ? 
				this.numeric.add(augend.getNumeric(), mc) : this.numeric.add(augend.getNumeric());
		if (ret.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
			ret = new BigDecimal(ret.intValue());
		}
		
		ExpNode tmp = new ExpNode(ret);
		return tmp;
	}

	public ExpNode subtract(ExpNode subtrahend) {
		return subtract(subtrahend, null);
	}

	public ExpNode subtract(ExpNode subtrahend, MathContext mc) {
		BigDecimal ret = mc != null ? 
				this.numeric.subtract(subtrahend.getNumeric(), mc) : this.numeric.subtract(subtrahend.getNumeric());
		if (ret.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
			ret = new BigDecimal(ret.intValue());
		}
				
		ExpNode tmp = new ExpNode(ret);
		return tmp;
	}

	public ExpNode multiply(ExpNode multiplicand) {
		return multiply(multiplicand, null);
	}

	public ExpNode multiply(ExpNode multiplicand, MathContext mc) {
		BigDecimal ret = mc != null ? 
				this.numeric.multiply(multiplicand.getNumeric(), mc) : this.numeric.multiply(multiplicand.getNumeric());
		if (ret.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
			ret = new BigDecimal(ret.intValue());
		}
		
		ExpNode tmp = new ExpNode(ret);
		return tmp;
	}

	public ExpNode divide(ExpNode divisor) {
		return divide(divisor, null);
	}

	public ExpNode divide(ExpNode divisor, MathContext mc) {
		BigDecimal ret = mc != null ? 
				this.numeric.divide(divisor.getNumeric(), mc) : this.numeric.divide(divisor.getNumeric());
		if (ret.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
			ret = new BigDecimal(ret.intValue());
		}
		
		ExpNode tmp = new ExpNode(ret);
		return tmp;
	}

	public ExpNode leftShift(int n) {
		BigDecimal ret = this.numeric;
		if (this.numeric.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
			ret = new BigDecimal(this.numeric.intValue() << n);
		}
		
		ExpNode tmp = new ExpNode(ret);
		return tmp;
	}

	public ExpNode rightShift(int n) {
		BigDecimal ret = this.numeric;
		if (this.numeric.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
			ret = new BigDecimal(this.numeric.intValue() >> n);
		}
		
		ExpNode tmp = new ExpNode(ret);
		return tmp;
	}

	public ExpNode round(MathContext mc) {
		BigDecimal ret = this.numeric.round(mc);
		
		ExpNode tmp = new ExpNode(ret);
		return tmp;
	}

	public ExpNode append(ExpNode augend) {
		
		ExpNode tmp = genTextExpNode(this.text + augend.getText());
		return tmp;
	}
	
	public static ExpNode random(MathContext mc) {
		double d = Math.random();
		ExpNode tmp = new ExpNode(d, mc);
		return tmp;
	}

}
