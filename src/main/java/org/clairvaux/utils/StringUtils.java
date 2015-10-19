package org.clairvaux.utils;

public class StringUtils {

	public static String padString(String str, char ch, int length) {
		StringBuilder sb = new StringBuilder();
		int padLength = length - str.length();
		if (padLength > 0) {
			for (int i=0; i<padLength; i++) {
				sb.append(ch);
			}
		}
		sb.append(str);
		return sb.toString();
	}
}
