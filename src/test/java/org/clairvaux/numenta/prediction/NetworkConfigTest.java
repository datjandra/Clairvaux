package org.clairvaux.numenta.prediction;

import static org.numenta.nupic.algorithms.Anomaly.KEY_MODE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.clairvaux.utils.NetworkUtils;
import org.joda.time.DateTime;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.encoders.MultiEncoder;
import org.numenta.nupic.network.Network;

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
		Network network = NetworkUtils.createSimpleNetwork();                      
        // Train on a monthly file
        List<Map<String, Object>> trainingData = NetworkUtils.loadAcledData("data/ACLED All Africa File_January 2015.csv", null);
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
        LOGGER.log(Level.INFO, String.format("%d training cycles completed in %f seconds", TRAINING_CYCLES, duration));
        
        // Test on a monthly file other than the one used for training
        startTime = System.nanoTime();
        List<Map<String, Object>> testingData = NetworkUtils.loadAcledData("data/ACLED-All-Africa-File_20150701-to-20150731_csv.csv", null);
        AggregateSubscriber subscriber = new AggregateSubscriber("EVENT_TYPE", new String[]{});
        network.observe().subscribe(subscriber);        
        network.setLearn(false);
        for (Map<String,Object> multiInput : testingData) {
        	network.computeImmediate(multiInput);
        }
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000000.0;
        LOGGER.log(Level.INFO, String.format("Testing completed in %f seconds", duration));
        
        ConfusionMatrix cm = subscriber.getConfusionMatrix();
        Double accuracy = cm.getAccuracy();
        LOGGER.log(Level.INFO, "Accuracy: " + accuracy);
        // 40% accuracy at least with this train/test set
        assertTrue(accuracy > 0.4d);
	}			
}
