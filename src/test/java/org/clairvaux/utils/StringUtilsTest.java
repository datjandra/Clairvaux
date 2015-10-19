package org.clairvaux.utils;

import org.clairvaux.utils.StringUtils;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

	public void testPadString() {
		String padded = StringUtils.padString("9", '0', 2);
		assertEquals(padded, "09");
	}
}
