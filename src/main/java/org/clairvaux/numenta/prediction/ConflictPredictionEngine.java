package org.clairvaux.numenta.prediction;

import static org.numenta.nupic.algorithms.Anomaly.KEY_MODE;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.clairvaux.jobs.PredictionPipelineListener;
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

import rx.Subscriber;

public class ConflictPredictionEngine {

	private final static int TRAINING_CYCLES = 100;
	private final static int NOTIFY_INTERVAL = 10;
	private final String dataFile;
	private final static Logger LOGGER = Logger.getLogger(ConflictPredictionEngine.class.getName());
	
	private PredictionPipelineListener listener;
	private Network network;
	private List<Map<String,Object>> inputList;
	private Integer trainingCycles = TRAINING_CYCLES;
	
	public ConflictPredictionEngine(String dataFile, Integer cycles) {
		this.dataFile = dataFile;
		if (cycles != null) {
			// Increase training cycle for better prediction results
			// Lower training cycle to speed up pipeline for development or testing
			trainingCycles = cycles;
		}
	}
	
	public void startTraining() throws IOException {
		if (listener != null) {
			listener.onStartTraining();
		}
		
		network = createNetwork();
		DateTime lastEventDate = null;
		inputList = readInput();
		for (int i=0; i<trainingCycles; i++) {
			for (Map<String,Object> multiInput : inputList) {
				DateTime eventDate = (DateTime) multiInput.get("EVENT_DATE");
            	if (lastEventDate != null && eventDate.isBefore(lastEventDate)) {
            		network.reset();
            	}
            	network.computeImmediate(multiInput);
            	lastEventDate = eventDate;
			}
			
			if (i % NOTIFY_INTERVAL == 0 && listener != null) {
				listener.onContinueTraining(i);
			}
		}
		
		if (listener != null) {
			listener.onStopTraining();
		}
	}
	
	public void startPrediction(Subscriber<Inference> subscriber) {
		if (listener != null) {
			listener.onStartPrediction();
		}
		
		network.observe().subscribe(subscriber); 
		for (Map<String,Object> multiInput : inputList) {
        	network.computeImmediate(multiInput);
        }
		
		if (listener != null) {
			listener.onStopPrediction();
		}
	}
	
	public PredictionPipelineListener getListener() {
		return listener;
	}

	public void setListener(PredictionPipelineListener listener) {
		this.listener = listener;
	}

	private Network createNetwork() {
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
        return network;
	}
	
	private List<Map<String,Object>> readInput() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(dataFile));
        reader.readNext(); 
        
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YYYY");        
        List<Map<String,Object>> inputList = new ArrayList<Map<String,Object>>();
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
        	inputList.add(multiInput);
        }
        reader.close();
       return inputList;
	}
	
	private MultiEncoder initEncoder() {
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
		
	private Parameters getSwarmParameters() {
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

	private Parameters getDefaultParameters() {
		Parameters parameters = Parameters.getAllDefaultParameters();
		parameters.setParameterByKey(KEY.INPUT_DIMENSIONS, new int[] { 8 });
		parameters.setParameterByKey(KEY.COLUMN_DIMENSIONS, new int[] { 20 });
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
}
