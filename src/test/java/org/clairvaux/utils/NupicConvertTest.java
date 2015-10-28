package org.clairvaux.utils;

import java.io.IOException;

import junit.framework.TestCase;

public class NupicConvertTest extends TestCase {

	public void testConvert() throws IOException {
		NupicConverter.outputNupicFormat("ACLED-All-Africa-File_20150801-to-20150831_csv.csv", 
				"ACLED-All-Africa-File_20150801-to-20150831_csv-nupic.csv", false);
		assertTrue(true);
	}
	
	public void testConvertAll() throws IOException {
		NupicConverter.outputNupicFormat("ACLED All Africa File_20150101 to 20151010_csv.csv", 
				"ACLED-All-Africa-File_20150101-to-20151010_csv-nupic.csv", false);
		assertTrue(true);
	}
}
