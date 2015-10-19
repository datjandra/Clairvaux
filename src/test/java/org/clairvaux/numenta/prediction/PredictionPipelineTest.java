package org.clairvaux.numenta.prediction;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clairvaux.jobs.PredictionPipelineListener;
import org.clairvaux.numenta.prediction.AggregateSubscriber;
import org.clairvaux.numenta.prediction.ConflictPredictionEngine;

import junit.framework.TestCase;

public class PredictionPipelineTest extends TestCase implements PredictionPipelineListener {

	private ConflictPredictionEngine predictionEngine;
	private AggregateSubscriber subscriber;
	
	private final static Logger LOGGER = Logger.getLogger(PredictionPipelineTest.class.getName());
	
	public void testPredictionPipeline() {
		// Get CSV data from http://www.acleddata.com/data/ and save it in project directory
		String dataFile = "ACLED-All-Africa-File_20150801-to-20150831_csv.csv";
		predictionEngine = new ConflictPredictionEngine(dataFile, 1);
		predictionEngine.setListener(this);
		try {
			predictionEngine.startTraining();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public void onDataRefreshed(String archiveUrl) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDataCached(List<String> fileNames) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStartTraining() {
		LOGGER.log(Level.INFO, "onStartTraining");
	}

	@Override
	public void onContinueTraining(int iteration) {
		LOGGER.log(Level.INFO, "onContinueTraining");
	}

	@Override
	public void onStopTraining() {
		subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{
		    	"EVENT_DATE",	
		    	"INTERACTION"
		    });
		predictionEngine.startPrediction(subscriber);
	}

	@Override
	public void onStartPrediction() {
		LOGGER.log(Level.INFO, "onStartPrediction");
	}

	@Override
	public void onStopPrediction() {
		LOGGER.log(Level.INFO, "onStopPrediction");
		String outputFile = "src/main/webapp/graph.json";
		try {
			subscriber.dumpJson(outputFile);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
