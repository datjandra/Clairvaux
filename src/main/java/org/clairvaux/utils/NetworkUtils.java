package org.clairvaux.utils;

import static org.numenta.nupic.algorithms.Anomaly.KEY_MODE;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.Anomaly.Mode;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.encoders.DateEncoder;
import org.numenta.nupic.encoders.MultiEncoder;
import org.numenta.nupic.encoders.SDRCategoryEncoder;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.util.Tuple;

import com.opencsv.CSVReader;

public class NetworkUtils {
	
	private final static Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
	
	public static Network createSimpleNetwork() {
		Parameters parameters = getNetworkHarnessParameters();
		parameters = parameters.union(getSwarmParameters());
		
		Map<String, Object> params = new HashMap<String, Object>();
        params.put(KEY_MODE, Mode.PURE);
        
        Network network = Network.create("Network Prediction", parameters)
			    .add(Network.createRegion("r1")
			        .add(Network.createLayer("l1", parameters)
			            .alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
			            .add(Anomaly.create(params))
			            .add(new TemporalMemory())
			            .add(new SpatialPooler())
			            .add(initAcledEncoder()))); 
        return network;
	}
		
	public static Map<String, Map<String, Object>> setupMap(
			Map<String, Map<String, Object>> map, int n, int w, double min,
			double max, double radius, double resolution, Boolean periodic,
			Boolean clip, Boolean forced, String fieldName, String fieldType,
			String encoderType) {

		if (map == null) {
			map = new HashMap<String, Map<String, Object>>();
		}
		Map<String, Object> inner = null;
		if ((inner = map.get(fieldName)) == null) {
			map.put(fieldName, inner = new HashMap<String, Object>());
		}

		inner.put("n", n);
		inner.put("w", w);
		inner.put("minVal", min);
		inner.put("maxVal", max);
		inner.put("radius", radius);
		inner.put("resolution", resolution);

		if (periodic != null)
			inner.put("periodic", periodic);
		if (clip != null)
			inner.put("clipInput", clip);
		if (forced != null)
			inner.put("forced", forced);
		if (fieldName != null)
			inner.put("fieldName", fieldName);
		if (fieldType != null)
			inner.put("fieldType", fieldType);
		if (encoderType != null)
			inner.put("encoderType", encoderType);
		return map;
	}
	
	// Copied from NetworkTestHarness.getParameters()
	public static Parameters getNetworkHarnessParameters() {
		Parameters parameters = Parameters.getAllDefaultParameters();
        parameters.setParameterByKey(KEY.INPUT_DIMENSIONS, new int[] { 8 });
        parameters.setParameterByKey(KEY.COLUMN_DIMENSIONS, new int[] { 20 });
        parameters.setParameterByKey(KEY.CELLS_PER_COLUMN, 6);
        
        //SpatialPooler specific
        parameters.setParameterByKey(KEY.POTENTIAL_RADIUS, 12);//3
        parameters.setParameterByKey(KEY.POTENTIAL_PCT, 0.5);//0.5
        parameters.setParameterByKey(KEY.GLOBAL_INHIBITIONS, false);
        parameters.setParameterByKey(KEY.LOCAL_AREA_DENSITY, -1.0);
        parameters.setParameterByKey(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 5.0);
        parameters.setParameterByKey(KEY.STIMULUS_THRESHOLD, 1.0);
        parameters.setParameterByKey(KEY.SYN_PERM_INACTIVE_DEC, 0.01);
        parameters.setParameterByKey(KEY.SYN_PERM_ACTIVE_INC, 0.1);
        parameters.setParameterByKey(KEY.SYN_PERM_TRIM_THRESHOLD, 0.05);
        parameters.setParameterByKey(KEY.SYN_PERM_CONNECTED, 0.1);
        parameters.setParameterByKey(KEY.MIN_PCT_OVERLAP_DUTY_CYCLE, 0.1);
        parameters.setParameterByKey(KEY.MIN_PCT_ACTIVE_DUTY_CYCLE, 0.1);
        parameters.setParameterByKey(KEY.DUTY_CYCLE_PERIOD, 10);
        parameters.setParameterByKey(KEY.MAX_BOOST, 10.0);
        parameters.setParameterByKey(KEY.SEED, 42);
        parameters.setParameterByKey(KEY.SP_VERBOSITY, 0);
        
        //Temporal Memory specific
        parameters.setParameterByKey(KEY.INITIAL_PERMANENCE, 0.2);
        parameters.setParameterByKey(KEY.CONNECTED_PERMANENCE, 0.8);
        parameters.setParameterByKey(KEY.MIN_THRESHOLD, 5);
        parameters.setParameterByKey(KEY.MAX_NEW_SYNAPSE_COUNT, 6);
        parameters.setParameterByKey(KEY.PERMANENCE_INCREMENT, 0.05);
        parameters.setParameterByKey(KEY.PERMANENCE_DECREMENT, 0.05);
        parameters.setParameterByKey(KEY.ACTIVATION_THRESHOLD, 4);
        return parameters;
	}
	
	// Obtained by running large swarm with 1-step prediction for EVENT_TYPE
	public static Parameters getSwarmParameters() {
		Parameters p = Parameters.getEncoderDefaultParameters();
		
		// Universal params
		p.setParameterByKey(KEY.COLUMN_DIMENSIONS, new int[] { 256 });
		p.setParameterByKey(KEY.INPUT_DIMENSIONS, new int[] { 2048 });
		p.setParameterByKey(KEY.CELLS_PER_COLUMN, 32);
		p.setParameterByKey(KEY.SEED, 1960);
		
		// SpatialPooler specific
		p.setParameterByKey(KEY.GLOBAL_INHIBITIONS, true);
		p.setParameterByKey(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 40.0d);
		p.setParameterByKey(KEY.SEED, 1956);
		p.setParameterByKey(KEY.POTENTIAL_PCT, 0.8d);
		p.setParameterByKey(KEY.SYN_PERM_CONNECTED, 0.1d);
		p.setParameterByKey(KEY.SYN_PERM_ACTIVE_INC, 0.05d);
		p.setParameterByKey(KEY.SYN_PERM_INACTIVE_DEC, 0.0005d);
		p.setParameterByKey(KEY.MAX_BOOST, 2.0d);

		// Temporal Memory specific
		p.setParameterByKey(KEY.MAX_NEW_SYNAPSE_COUNT, 20);
		p.setParameterByKey(KEY.INITIAL_PERMANENCE, 0.21d);
		p.setParameterByKey(KEY.PERMANENCE_INCREMENT, 0.1d);
		p.setParameterByKey(KEY.PERMANENCE_DECREMENT, 0.1d);
		p.setParameterByKey(KEY.MIN_THRESHOLD, 12);
		p.setParameterByKey(KEY.ACTIVATION_THRESHOLD, 16);

		p.setParameterByKey(KEY.CLIP_INPUT, true);
		return p;
	}	
	
	private static MultiEncoder initAcledEncoder() {
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

		sdrCategoryEncoder = SDRCategoryEncoder.builder()
				.n(121)
				.w(21)				
				.forced(true)
				.build();
		multiEncoder.addEncoder("INTERACTION", sdrCategoryEncoder);
				
		/*
		sdrCategoryEncoder = SDRCategoryEncoder.builder()
				.n(121)
				.w(21)				
				.forced(true)
				.build();
		multiEncoder.addEncoder("LOCATION", sdrCategoryEncoder);
		*/
		return multiEncoder;
	}
	
	public static List<Map<String,Object>> loadAcledData(String file, String dateFormat) {
		if (dateFormat == null) {
			dateFormat = "dd/MM/YY";
		}
		CSVReader reader = null;
        List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		try {
			reader = new CSVReader(new FileReader(file));
			reader.readNext();        
			
			DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);        
			String [] nextLine;
			
			/*
			 * Data files are sorted by GWNO (numeric code for each country), then by ascending date.
			 * The system will then learn sequence of events per country.
			 * Other context fields may be used like events per actor, events per city, etc.
			 * Data file will have to be re-sorted by context field (actor, city, etc.), then by ascending date.
			 * Change to ConflictPredictionEngine.java is also required to call network reset when field's value change.
			 */
			while ((nextLine = reader.readNext()) != null) {     
				Map<String, Object> multiInput = new HashMap<>();
				try {
					DateTime eventDate = formatter.parseDateTime(nextLine[2]);
					multiInput.put("GWNO", nextLine[0]);	// Used only for sequence reset    
					multiInput.put("EVENT_DATE", eventDate);        	
					multiInput.put("EVENT_TYPE", nextLine[5].trim().toUpperCase());
					multiInput.put("INTERACTION", nextLine[12].trim());	   
					
					/*     	        	
					multiInput.put("LOCATION", nextLine[17].trim().toUpperCase());
					*/
					data.add(multiInput);
				} catch (IllegalFieldValueException e) {
					LOGGER.log(Level.WARNING, String.format("Skipped line with bad date format %s", Arrays.toString(nextLine)));
				}        					
			}
		} catch (IOException e) {
			LOGGER.log(Level.INFO, e.getMessage(), e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {}
		}
        return data;
	}
			
	// Not needed if sensor is not used
	@SuppressWarnings("unused")
	private static Map<String, Map<String, Object>> getAcledEncodingMap() {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                100, // n
                21, // w
                0, 1000d, 0, 0, null, Boolean.TRUE, null,
                "GWNO", "int", "ScalarEncoder");
        
        fieldEncodings = setupMap(
        		fieldEncodings,
                0, // n
                0, // w
                0, 0, 0, 0, null, null, null,
                "EVENT_DATE", "datetime", "DateEncoder");

        fieldEncodings = setupMap(
                fieldEncodings, 
                121, 
                21, 
                0, 0, 0, 0, null, null, Boolean.FALSE, 
                "EVENT_TYPE", "string", "SDRCategoryEncoder");
        
        fieldEncodings = setupMap(
                fieldEncodings, 
                121, 
                21, 
                0, 0, 0, 0, null, null, Boolean.FALSE, 
                "INTERACTION", "string", "SDRCategoryEncoder");
        
        fieldEncodings.get("EVENT_DATE").put(KEY.DATEFIELD_TOFD.getFieldName(), new Tuple(21, 1d)); // Time of day
        fieldEncodings.get("EVENT_DATE").put(KEY.DATEFIELD_DOFW.getFieldName(), new Tuple(21, 1d)); // Date of week
        fieldEncodings.get("EVENT_DATE").put(KEY.DATEFIELD_PATTERN.getFieldName(), "YYYY-MM-dd");
        return fieldEncodings;
    }		
}
