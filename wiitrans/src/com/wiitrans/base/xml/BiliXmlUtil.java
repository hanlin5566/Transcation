package com.wiitrans.base.xml;

public class BiliXmlUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String Encode(String s) {
		if (s == null) {
			return null;
		} else {
			return s.replace("&#", "☯");
		}
	}

	public String Decode(String s) {
		if (s == null) {
			return null;
		} else {
			return s.replace("☯", "&#");
		}
	}
}
