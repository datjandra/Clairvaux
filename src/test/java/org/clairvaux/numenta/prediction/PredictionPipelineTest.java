package org.clairvaux.numenta.prediction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.clairvaux.utils.FileUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class PredictionPipelineTest extends TestCase {

	public void testOutputGraph() throws JsonGenerationException, JsonMappingException, IOException {
		AggregateSubscriber subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{
		    	"EVENT_DATE",	
		    	"INTERACTION",
		    	"LOCATION"
		    });	
		String dataFile = "ACLED-All-Africa-File_20150801-to-20150831_csv.csv";
		File workingFile = new File("data", dataFile);
		ConflictPredictionEngine.NETWORK.startCompute(workingFile.getAbsolutePath(), null, subscriber, 1, true);
		
		String jsonFileName = String.format("%s.json", FileUtils.extractBaseName(dataFile));
		subscriber.dumpJsonGraph(new FileWriter(jsonFileName));	
		ConfusionMatrix cm = subscriber.getConfusionMatrix();
		System.out.println("Prediction accuracy: " + cm.getAccuracy());
		assertTrue(true);
	}
}
