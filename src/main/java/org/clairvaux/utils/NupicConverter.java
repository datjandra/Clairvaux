package org.clairvaux.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class NupicConverter {

	public static void outputNupicFormat(String acledFile, String nupicFile) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(acledFile));
        String[] acledHeader = reader.readNext(); 
        
        CSVWriter writer = new CSVWriter(new FileWriter(nupicFile));
        String[] nupicHeader = new String[] {
        	acledHeader[0],		// GWNO	
        	acledHeader[1],		// EVENT_ID
        	acledHeader[2],		// EVENT_DATE	
        	acledHeader[5],		// EVENT_TYPE
        	acledHeader[12],	// INTERACTION
        };
        writer.writeNext(nupicHeader, false);
        
        String[] nupicFieldTypes = new String[] {
        	"int",
        	"string",
        	"datetime",
        	"string",
        	"int"
        };
        writer.writeNext(nupicFieldTypes, false);
        
        String[] nupicFlags = new String[] {
        	"S",	// sequence
        	"",
        	"T",	// timestamp
        	"C",	// category
        	"C"		// category
        };
        writer.writeNext(nupicFlags, false);
        
        DateTimeFormatter acledFormat = DateTimeFormat.forPattern("dd/MM/YY");        
        DateTimeFormatter nupicFormat = DateTimeFormat.forPattern("MM/dd/YY");
        String [] acledLine;
        while ((acledLine = reader.readNext()) != null) {     
        	String[] nupicLine = new String[] {
        		acledLine[0],
        		acledLine[1],
        		nupicFormat.print(acledFormat.parseDateTime(acledLine[2])),
        		acledLine[5],
        		acledLine[12]
        	};
        	writer.writeNext(nupicLine, false);
        }
        reader.close();
        writer.close();
	}
}
