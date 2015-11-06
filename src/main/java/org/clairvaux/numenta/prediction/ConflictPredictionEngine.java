package org.clairvaux.numenta.prediction;

import static org.numenta.nupic.algorithms.Anomaly.KEY_MODE;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clairvaux.utils.NetworkUtils;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.Anomaly.Mode;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.Region;
import org.numenta.nupic.network.sensor.FileSensor;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;

import rx.Subscriber;

public class ConflictPredictionEngine {

	private final String dataFile;
	private final static Logger LOGGER = Logger.getLogger(ConflictPredictionEngine.class.getName());
	
	public ConflictPredictionEngine(String dataFile) {
		this.dataFile = dataFile;
	}
	
	public void startNetwork(Subscriber<Inference> subscriber) throws InterruptedException {
		LOGGER.log(Level.INFO, "Initializing network");
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
			            .add(Sensor.create(FileSensor::create, SensorParams.create(
			            		Keys::path, "", dataFile)))));
        if (subscriber != null) {
        	network.observe().subscribe(subscriber);
        }
        network.start();
        Region r1 = network.lookup("r1");
        r1.lookup("l1").getLayerThread().join();
        network.halt();
        LOGGER.log(Level.INFO, "network halted");
	}
}
