/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.misc;

public class StringBuilderExt {

	private StringBuilder _build = null;

	public StringBuilderExt() {
		_build = new StringBuilder();
	}

	public void append(String _source) {

		if (_source != null) {
			_build.append(_source);
		}
	}

	public int length() {
		return _build.length();
	}

	public String toString() {
		return _build.toString();
	}
}
