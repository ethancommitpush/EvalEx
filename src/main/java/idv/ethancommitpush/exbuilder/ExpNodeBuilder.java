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

import idv.ethancommitpush.exbuilder.ExpNode.ExpNodeType;

public class ExpNodeBuilder {
	
	private String text;
	private BigDecimal numeric;
	
	public ExpNodeBuilder setText(String text) {
		this.text = text;
		return this;
	}
	
	public ExpNodeBuilder setNumeric(BigDecimal numeric) {
		this.numeric = numeric;
		return this;
	}
	
	public ExpNode buildText() {
		ExpNode var = new ExpNode();
		var.setText(text);
		return var;
	}
	
	public ExpNode buildNumeric() {
		ExpNode var = new ExpNode();
		var.setNumeric(numeric);
		var.setText(String.valueOf(numeric));
		var.setType(ExpNodeType.NUMERIC);
		return var;
	}

}
