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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.List;

public class TreeMapBuilder<K, V> {

	private Comparator<K> c;
	private List<K> ks = new ArrayList<K>();
	private List<V> vs = new ArrayList<V>();
	
	public TreeMapBuilder(Comparator<K> comparator) {
		c = comparator;
	}
	
	public TreeMapBuilder<K, V> add(K k, V v) {
		ks.add(k);
		vs.add(v);
		return this;
	}
	
	public TreeMap<K, V> build() {
		TreeMap<K, V> map = new TreeMap<K, V>(c);
		for (int i = 0, l = ks.size(); i < l; i += 1) {
			K k = ks.get(i);
			V v = vs.get(i);
			map.put(k, v);
		}
		return map;
	}

}
