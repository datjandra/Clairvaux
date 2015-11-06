package org.clairvaux.numenta.prediction;

import static org.numenta.nupic.algorithms.Anomaly.KEY_MODE;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.clairvaux.utils.NetworkUtils;
import org.joda.time.DateTime;
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

import com.opencsv.CSVReader;

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
	
	public void testSanity() {
		Parameters p = NetworkUtils.getNetworkHarnessParameters();
		Map<String, Map<String, Object>> fieldEncodings = NetworkUtils.setupMap(
                null,
                8, // n
                3, // w
                0.0, 8.0, 0, 1, Boolean.TRUE, null, Boolean.TRUE,
                "dayOfWeek", "number", "ScalarEncoder");
		Parameters encoderParams = Parameters.getEncoderDefaultParameters();
		encoderParams.setParameterByKey(KEY.FIELD_ENCODING_MAP, fieldEncodings);
        p = p.union(encoderParams);
        
        Map<String, Object> params = new HashMap<>();
        params.put(KEY_MODE, Anomaly.Mode.PURE);
        
        Network n = Network.create("test network", p)
                .add(Network.createRegion("r1")
                        .add(Network.createLayer("1", p)
                                .alterParameter(Parameters.KEY.AUTO_CLASSIFY, Boolean.TRUE))
                        .add(Network.createLayer("2", p)
                                .add(Anomaly.create(params)))
                        .add(Network.createLayer("3", p)
                                .add(new TemporalMemory()))
                        .add(Network.createLayer("4", p)
                                .add(new SpatialPooler())
                                .add(MultiEncoder.builder().name("").build()))
                        .connect("1", "2")
                        .connect("2", "3")
                        .connect("3", "4"));

        AggregateSubscriber subscriber = new AggregateSubscriber("dayOfWeek", new String[]{});
        n.observe().subscribe(subscriber);

        double[] primes = new double[]{2d,3d,5d,7d};
        final int NUM_CYCLES = 10;
        Map<String, Object> multiInput = new HashMap<>();
        for(int i = 0;i < NUM_CYCLES;i++) {
            for(int j = 0; j < primes.length; j++) {
                multiInput.put("dayOfWeek", primes[j]);
                n.compute(multiInput);
            }
            n.reset();
        }
        
        ConfusionMatrix cm = subscriber.getConfusionMatrix();
        assertTrue(cm.getAccuracy() > 0.4d);
	}
	
	public void testEventsPrediction() throws InterruptedException, IOException {
		Parameters parameters = NetworkUtils.getNetworkHarnessParameters();
		parameters = parameters.union(NetworkUtils.getSwarmParameters());
		
		Map<String, Object> params = new HashMap<String, Object>();
        params.put(KEY_MODE, Mode.PURE);
        
        Network network = Network.create("Network Prediction", parameters)
			    .add(Network.createRegion("r1")
			        .add(Network.createLayer("l1", parameters)
			            .alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
			            .add(Anomaly.create(params))
			            .add(new TemporalMemory())
			            .add(new SpatialPooler())
			            .add(initEncoder())));               
        
        // Train on a monthly file
        List<Map<String, Object>> trainingData = loadData("data/ACLED-All-Africa-File_20150701-to-20150731_csv.csv");
        long startTime = System.nanoTime();
        DateTime lastEventDate = null;
        final int TRAINING_CYCLES = 50;
        for (int i=0; i<TRAINING_CYCLES; i++) {        	
        	for (Map<String,Object> multiInput : trainingData) {
            	DateTime eventDate = (DateTime) multiInput.get("EVENT_DATE");
            	if (lastEventDate != null && eventDate.isBefore(lastEventDate)) {
            		network.reset();
            	}
            	network.computeImmediate(multiInput);
            	lastEventDate = eventDate;
            }
        	lastEventDate = null;
        }
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1000000000.0;
        System.out.println(String.format("%d training cycles completed in %f seconds", TRAINING_CYCLES, duration));
        
        // Test on a monthly file other than the one used for training
        startTime = System.nanoTime();
        List<Map<String, Object>> testingData = loadData("data/ACLED-All-Africa-File_20150801-to-20150831_csv.csv");
        AggregateSubscriber subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{});
        network.observe().subscribe(subscriber);        
        network.setLearn(false);
        for (Map<String,Object> multiInput : testingData) {
        	network.computeImmediate(multiInput);
        }
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000000.0;
        System.out.println(String.format("Testing completed in %f seconds", duration));
        System.out.println("Accuracy: " + subscriber.getConfusionMatrix().getAccuracy());
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
		
		sdrCategoryEncoder = SDRCategoryEncoder.builder()
				.n(121)
				.w(21)				
				.forced(true)
				.build();
		multiEncoder.addEncoder("INTERACTION", sdrCategoryEncoder);
				
		sdrCategoryEncoder = SDRCategoryEncoder.builder()
				.n(121)
				.w(21)				
				.forced(true)
				.build();
		multiEncoder.addEncoder("LOCATION", sdrCategoryEncoder);
		return multiEncoder;
	}
		
	static List<Map<String,Object>> loadData(String file) {
		CSVReader reader = null;
        List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		try {
			reader = new CSVReader(new FileReader(file));
			reader.readNext();        
			
			DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/YYYY");        
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {     
				Map<String, Object> multiInput = new HashMap<>();
				DateTime eventDate = formatter.parseDateTime(nextLine[2]);
				multiInput.put("EVENT_DATE", eventDate);        	
				multiInput.put("EVENT_TYPE", nextLine[5]);
				multiInput.put("INTERACTION", nextLine[12]);	        	        	
				multiInput.put("LOCATION", nextLine[17]);        	
				data.add(multiInput);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {}
		}
        return data;
	}
}
