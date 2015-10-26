package org.clairvaux.numenta.prediction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import junit.framework.TestCase;

public class PredictionPipelineTest extends TestCase {

	private final static Logger LOGGER = Logger.getLogger(PredictionPipelineTest.class.getName());
	
	public void testGraphOutput() throws InterruptedException, JsonGenerationException, JsonMappingException, IOException {
		String dataFile = "ACLED-All-Africa-File_20150801-to-20150831_csv-nupic.csv";
		ConflictPredictionEngine predictionEngine = 
				new ConflictPredictionEngine(dataFile);
		
		AggregateSubscriber subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{
		    	"EVENT_DATE",	
		    	"INTERACTION"
		    });
		predictionEngine.startNetwork(subscriber);
		subscriber.dumpJsonGraph(new FileWriter("graph.json"));
		assertTrue(true);
	}
}
