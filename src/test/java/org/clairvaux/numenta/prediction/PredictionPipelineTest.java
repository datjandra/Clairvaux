package org.clairvaux.numenta.prediction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.clairvaux.utils.FileUtils;
import org.clairvaux.utils.NupicConverter;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class PredictionPipelineTest extends TestCase {

	public void testGraphOutput() throws InterruptedException, JsonGenerationException, JsonMappingException, IOException {
		String dataFile = "ACLED-All-Africa-File_20150801-to-20150831_csv-nupic.csv";
		File file = new File(dataFile);
		if (!file.exists()) {
			String link = "http://www.acleddata.com/wp-content/uploads/2015/09/ACLED-All-Africa-File_20150801-to-20150831_csv.zip";
			String acledFile = FileUtils.extractFromUrl(link, ".csv");
			String nupicFile = acledFile.replace(".csv", "-nupic.csv");
			NupicConverter.outputNupicFormat(acledFile, nupicFile, false);
		}
		
		ConflictPredictionEngine predictionEngine = 
				new ConflictPredictionEngine(dataFile);
		
		AggregateSubscriber subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{
		    	"EVENT_DATE",	
		    	"INTERACTION"
		    });
		predictionEngine.startNetwork(subscriber);
		subscriber.dumpJsonGraph(new FileWriter("ACLED-All-Africa-File_20150801-to-20150831_csv.json"));
		assertTrue(true);
	}
}
