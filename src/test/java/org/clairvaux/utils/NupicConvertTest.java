package org.clairvaux.utils;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class NupicConvertTest extends TestCase {

	public void testConvertMonthlyData() throws IOException {
		String link = "http://www.acleddata.com/wp-content/uploads/2015/09/ACLED-All-Africa-File_20150801-to-20150831_csv.zip";
		String acledFile = FileUtils.extractFromUrl(link, ".csv");
		String nupicFile = acledFile.replace(".csv", "-nupic.csv");
		NupicConverter.outputNupicFormat(acledFile, nupicFile, false);
		assertTrue(new File(nupicFile).exists());
	}
	
	public void testConvertCompleteData() throws IOException {
		// Don't run this on slow connection
		/*
		String link = "http://www.acleddata.com/wp-content/uploads/2015/10/ACLED-All-Africa-File_20150101-to-20151024_csv.zip";
		String acledFile = FileUtils.extractFromUrl(link, ".csv");
		String nupicFile = acledFile.replace(".csv", "nupic.csv");
		NupicConverter.outputNupicFormat(acledFile, nupicFile, false);
		*/
		assertTrue(true);
	}
}
