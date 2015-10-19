package org.clairvaux.data;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.clairvaux.data.RealtimeDataFetcher;

import junit.framework.TestCase;

public class DataFetcherTest extends TestCase {

	public void testDataFetch() throws IOException {
		RealtimeDataFetcher.fetch(".", null, "MACOSX");
		File dir = new File(".");
		String[] csvFiles = dir.list(new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
		        return fileName.toLowerCase().endsWith(".csv");
		    }
		});
		assertTrue(csvFiles.length > 0);
	}
}
