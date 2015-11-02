package org.clairvaux.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class NupicConverter {

	public static void outputNupicFormat(String acledFile, String nupicFile,
			boolean applyQuotesToAll) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(acledFile));
		CSVWriter writer = new CSVWriter(new FileWriter(nupicFile));
		
		try {
			String[] acledHeader = reader.readNext();
			String[] nupicHeader = new String[] { acledHeader[0], // GWNO
					acledHeader[1], // EVENT_ID
					acledHeader[2], // EVENT_DATE
					acledHeader[5], // EVENT_TYPE
					acledHeader[12], // INTERACTION
			};
			writer.writeNext(nupicHeader, applyQuotesToAll);

			String[] nupicFieldTypes = new String[] { "int", "string", "datetime",
					"string", "string" };
			writer.writeNext(nupicFieldTypes, applyQuotesToAll);

			String[] nupicFlags = new String[] { "S", // sequence
					"", 
					"T", // timestamp
					"C", // category
					"C" // category
			};
			writer.writeNext(nupicFlags, applyQuotesToAll);

			DateTimeFormatter acledFormat = DateTimeFormat.forPattern("dd/MM/YY");
			DateTimeFormatter nupicFormat = DateTimeFormat.forPattern("YYYY-MM-dd");
			String[] acledLine;
			while ((acledLine = reader.readNext()) != null) {
				String[] nupicLine = new String[] { acledLine[0], acledLine[1],
						nupicFormat.print(acledFormat.parseDateTime(acledLine[2])),
						acledLine[5], acledLine[12] };
				writer.writeNext(nupicLine, applyQuotesToAll);
			}
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e) {}
		}
	}
}
