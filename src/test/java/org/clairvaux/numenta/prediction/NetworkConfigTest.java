package org.clairvaux.numenta.prediction;

import static org.numenta.nupic.algorithms.Anomaly.KEY_MODE;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.Anomaly.Mode;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.encoders.AdaptiveScalarEncoder;
import org.numenta.nupic.encoders.DateEncoder;
import org.numenta.nupic.encoders.MultiEncoder;
import org.numenta.nupic.encoders.SDRCategoryEncoder;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;

import com.opencsv.CSVReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import rx.Subscriber;

/**
 * Unit test for simple App.
 */
public class NetworkConfigTest extends TestCase {
			
	private final static Logger LOGGER = Logger.getLogger(NetworkConfigTest.class.getName());

	public NetworkConfigTest(String name) {
		super(name);
	}
	
	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(NetworkConfigTest.class);
	}	
	
	public void testPrediction() throws InterruptedException, IOException {
		Parameters parameters = getDefaultParameters();
		parameters = parameters.union(getSwarmParameters());
		
		Map<String, Object> params = new HashMap<String, Object>();
        params.put(KEY_MODE, Mode.PURE);
        
        Network network = Network.create("Network Prediction", parameters)
			    .add(Network.createRegion("r1")
			        .add(Network.createLayer("l1", parameters)
			            .alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
			            .add(Anomaly.create())
			            .add(new TemporalMemory())
			            .add(new SpatialPooler())
			            .add(initEncoder())));               
                        
        CSVReader reader = new CSVReader(new FileReader("ACLED All Africa File_20150101 to 20151010_csv.csv"));
        reader.readNext();        
        
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YYYY");        
        List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {     
        	Map<String, Object> multiInput = new HashMap<>();
        	DateTime eventDate = formatter.parseDateTime(nextLine[2]);
        	multiInput.put("GWNO", Integer.parseInt(nextLine[0]));	
        	multiInput.put("EVENT_DATE", eventDate);        	
        	multiInput.put("EVENT_TYPE", nextLine[5]);
        	multiInput.put("INTERACTION", Double.parseDouble(nextLine[12]));	        	        	
        	multiInput.put("ACTOR1", nextLine[6]);                	        	
        	multiInput.put("LOCATION", nextLine[17]);        	
        	data.add(multiInput);
        }
        reader.close();    
        
       DateTime lastEventDate = null;
        final int TRAINING_CYCLES = 3;
        for (int i=0; i<TRAINING_CYCLES; i++) {        	
        	for (Map<String,Object> multiInput : data) {
            	DateTime eventDate = (DateTime) multiInput.get("EVENT_DATE");
            	if (lastEventDate != null && eventDate.isBefore(lastEventDate)) {
            		network.reset();
            	}
            	network.compute(multiInput);
            	lastEventDate = eventDate;
            }
        	lastEventDate = null;
        }
        
        lastEventDate = null;
        network.observe().subscribe(getPredictionSubscriber());        
        for (Map<String,Object> multiInput : data) {
        	DateTime eventDate = (DateTime) multiInput.get("EVENT_DATE");
        	network.compute(multiInput);
        	lastEventDate = eventDate;
        }
        // This script is just for playing around with network configurations and parameters
		assertTrue(true);
	}

	MultiEncoder initEncoder() {
		MultiEncoder multiEncoder = MultiEncoder.builder()
				.name("")
				.build();
		
		DateEncoder dateEncoder = DateEncoder.builder()
				.timeOfDay(21, 1)
				.dayOfWeek(21, 1)
				.weekend(21)
				.forced(true)
				.build();
		multiEncoder.addEncoder("EVENT_DATE", dateEncoder);
		
		SDRCategoryEncoder sdrCategoryEncoder = SDRCategoryEncoder.builder()
                .n(121)
                .w(21)                                
                .forced(true)
                .build();
		multiEncoder.addEncoder("EVENT_TYPE", sdrCategoryEncoder);		
		
		AdaptiveScalarEncoder adaptiveScalarEncoder = AdaptiveScalarEncoder.adaptiveBuilder()
				.n(100)
				.w(21)				
				.maxVal(100)
				.build();
		multiEncoder.addEncoder("INTERACTION", adaptiveScalarEncoder);
				
		/*
		sdrCategoryEncoder = SDRCategoryEncoder.builder()
                .n(121)
                .w(21)                                
                .forced(true)
                .build();
		multiEncoder.addEncoder("ACTOR1", sdrCategoryEncoder);
					
		sdrCategoryEncoder = SDRCategoryEncoder.builder()
                .n(121)
                .w(21)                                
                .forced(true)
                .build();
		multiEncoder.addEncoder("LOCATION", sdrCategoryEncoder);
		*/
		return multiEncoder;
	}
		
	Parameters getSwarmParameters() {
		Parameters p = Parameters.getEncoderDefaultParameters();
		p.setParameterByKey(KEY.GLOBAL_INHIBITIONS, true);
		p.setParameterByKey(KEY.COLUMN_DIMENSIONS, new int[] { 2048 });
		p.setParameterByKey(KEY.CELLS_PER_COLUMN, 32);
		p.setParameterByKey(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 40.0d);
		p.setParameterByKey(KEY.SEED, 1956);
		p.setParameterByKey(KEY.POTENTIAL_PCT, 0.8d);
		p.setParameterByKey(KEY.SYN_PERM_CONNECTED, 0.1d);
		p.setParameterByKey(KEY.SYN_PERM_ACTIVE_INC, 0.05d);
		p.setParameterByKey(KEY.SYN_PERM_INACTIVE_DEC, 0.0005d);
		p.setParameterByKey(KEY.MAX_BOOST, 2.0d);

		p.setParameterByKey(KEY.INPUT_DIMENSIONS, new int[] { 2048 });
		p.setParameterByKey(KEY.MAX_NEW_SYNAPSE_COUNT, 20);
		p.setParameterByKey(KEY.INITIAL_PERMANENCE, 0.21d);
		p.setParameterByKey(KEY.PERMANENCE_INCREMENT, 0.1d);
		p.setParameterByKey(KEY.PERMANENCE_DECREMENT, 0.1d);
		p.setParameterByKey(KEY.MIN_THRESHOLD, 12);
		p.setParameterByKey(KEY.ACTIVATION_THRESHOLD, 16);

		p.setParameterByKey(KEY.CLIP_INPUT, true);
		return p;
	}

	Parameters getDefaultParameters() {
		Parameters parameters = Parameters.getAllDefaultParameters();
		parameters.setParameterByKey(KEY.CELLS_PER_COLUMN, 6);

		// SpatialPooler specific
		parameters.setParameterByKey(KEY.POTENTIAL_RADIUS, 12);// 3
		parameters.setParameterByKey(KEY.POTENTIAL_PCT, 0.5d);// 0.5
		parameters.setParameterByKey(KEY.GLOBAL_INHIBITIONS, false);
		parameters.setParameterByKey(KEY.LOCAL_AREA_DENSITY, -1.0d);
		parameters.setParameterByKey(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 5.0d);
		parameters.setParameterByKey(KEY.STIMULUS_THRESHOLD, 1.0d);
		parameters.setParameterByKey(KEY.SYN_PERM_INACTIVE_DEC, 0.0005d);
		parameters.setParameterByKey(KEY.SYN_PERM_ACTIVE_INC, 0.0015d);
		parameters.setParameterByKey(KEY.SYN_PERM_TRIM_THRESHOLD, 0.05d);
		parameters.setParameterByKey(KEY.SYN_PERM_CONNECTED, 0.1d);
		parameters.setParameterByKey(KEY.MIN_PCT_OVERLAP_DUTY_CYCLE, 0.1d);
		parameters.setParameterByKey(KEY.MIN_PCT_ACTIVE_DUTY_CYCLE, 0.1d);
		parameters.setParameterByKey(KEY.DUTY_CYCLE_PERIOD, 10);
		parameters.setParameterByKey(KEY.MAX_BOOST, 10.0d);
		parameters.setParameterByKey(KEY.SEED, 42);
		parameters.setParameterByKey(KEY.SP_VERBOSITY, 0);

		// Temporal Memory specific
		parameters.setParameterByKey(KEY.INITIAL_PERMANENCE, 0.2d);
		parameters.setParameterByKey(KEY.CONNECTED_PERMANENCE, 0.8d);
		parameters.setParameterByKey(KEY.MIN_THRESHOLD, 5);
		parameters.setParameterByKey(KEY.MAX_NEW_SYNAPSE_COUNT, 6);
		parameters.setParameterByKey(KEY.PERMANENCE_INCREMENT, 0.05d);
		parameters.setParameterByKey(KEY.PERMANENCE_DECREMENT, 0.05d);
		parameters.setParameterByKey(KEY.ACTIVATION_THRESHOLD, 4);				
		return parameters;
	}	
	
	Subscriber<Inference> getPredictionSubscriber() {
		return new Subscriber<Inference>() {
			public void onCompleted() {}

			public void onError(Throwable e) {
				e.printStackTrace();
			}

			public void onNext(Inference i) {	
				StringBuilder sb = new StringBuilder()
					.append(i.getRecordNum())
					.append(", ")
					.append("actual=")
					.append(i.getClassification("EVENT_TYPE").getActualValue(0))
					.append(", predicted=")
					.append(i.getClassification("EVENT_TYPE").getMostProbableValue(1));	
				LOGGER.log(Level.FINE, sb.toString());						
			}
		};
	}
}
