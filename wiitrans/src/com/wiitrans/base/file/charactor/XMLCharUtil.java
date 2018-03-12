package com.wiitrans.base.file.charactor;

public class XMLCharUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		char c = 31;
		String text = c + "asdf";
		// String encode = new XMLCharUtil().Encode(text);
		// String decode = new XMLCharUtil().Decode(encode);
		//
		// String t = new XMLCharUtil().Decode("asdf{uni:11}gas");
	}

	public String Filtrate(String text) {
		if (text == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		char c;
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);

			sb.append(Filtrate(c));

		}
		return sb.toString();
	}

	public String Encode(String text) {
		if (text == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		char c;
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);
			if (Invalid(c)) {
				// 调整成paragraph时，不可见字符全部忽略 //
				// sb.append("{___:").append((int)c).append("}");
				sb.append(" ");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public String Decode(String text) {
		if (text == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		char c, c1, c2, c3, c4, c5, c6, c7, c0;
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);
			if (c == '{') {
				c1 = c2 = c3 = c4 = c5 = c6 = c7 = 0;

				if (text.length() > i + 6) {
					c1 = text.charAt(i + 1);
					c2 = text.charAt(i + 2);
					c3 = text.charAt(i + 3);
					c4 = text.charAt(i + 4);
					c5 = text.charAt(i + 5);
					if (c1 == '_' && c2 == '_' && c3 == '_' && c4 == ':') {
						if (c5 <= '9' && c5 >= '0') {
							c6 = text.charAt(i + 6);
							if (c6 == '}') {
								c0 = (char) (c5 - '0');
								if (Invalid(c0)) {
									sb.append(c0);
									i += 6;
									continue;
								}

							} else if (c6 <= '9' && c6 >= '0'
									&& text.length() > i + 7) {
								c7 = text.charAt(i + 7);
								if (c7 == '}') {
									c0 = (char) ((c5 - '0') * 10 + c6 - '0');
									if (Invalid(c0)) {
										sb.append(c0);
										i += 7;
										continue;
									}
								}
							}
						}
					}
				}
			}

			sb.append(c);

		}
		return sb.toString();
	}

	private String Filtrate(char c) {
		// if (c >= 0 && c <= 8 || c == 11 || c == 12 || c >= 14 && c <= 31) {
		if (c == 10 || c == 13 || c == 14) {
			return String.valueOf(c);
		} else if (c == 9) {
			return " ";
		} else if (c >= 0 && c <= 31) {
			return "";
		} else {
			return String.valueOf(c);
		}
	}

	private boolean Invalid(char c) {
		// if (c >= 0 && c <= 8 || c == 11 || c == 12 || c >= 14 && c <= 31) {
		// if (c == 10 || c == 13) {
		// return false;
		// } else
		if (c >= 0 && c <= 31) {
			return true;
		} else {
			return false;
		}
	}
}
