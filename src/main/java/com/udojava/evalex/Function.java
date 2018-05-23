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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import idv.ethancommitpush.exbuilder.ExpNode;


public abstract class Function extends LazyFunction {
	
	public Function(String name, int numParams) {
		super(name, numParams);
	}

	public LazyNumber lazyEval(final List<LazyNumber> lazyParams) {
		return new LazyNumber() {
		    
		    private List<ExpNode> params;
		    
			public ExpNode eval() throws Exception {
				return Function.this.eval(getParams());
			}

			public String getString() throws Exception {
				return String.valueOf(Function.this.eval(getParams()));
			}
			
			private List<ExpNode> getParams() throws Exception {
                if (params == null) {
                    params = new ArrayList<ExpNode>();
                    for (LazyNumber lazyParam : lazyParams) {
                        params.add(lazyParam.eval());
                    }
                }
	            return params;
			}
		};
	}

	/**
	 * Implementation for this function.
	 * @param parameters Parameters will be passed by the expression evaluator 
	 * as a {@link List} of {@link BigDecimal} values.
	 * @return The function must return a new {@link BigDecimal} value as a computing result.
	 * @throws Exception 
	 */
	public abstract ExpNode eval(List<ExpNode> parameters) throws Exception;
}