package org.clairvaux.numenta.prediction;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clairvaux.utils.NetworkUtils;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;

import rx.Subscriber;

public enum ConflictPredictionEngine {

	NETWORK;

	private final Network network;
	private final Object lock = new Object();

	private final static Logger LOGGER = Logger
			.getLogger(ConflictPredictionEngine.class.getName());

	private ConflictPredictionEngine() {
		network = NetworkUtils.createSimpleNetwork();
	}

	public void startCompute(String dataFile, String dateFormat,
			Subscriber<Inference> subscriber, int trainingCycles, boolean learn) {
		LOGGER.log(
				Level.INFO,
				String.format(
						"begin network computation with %d cycles and learning mode %b",
						trainingCycles, learn));
		long startTime = System.nanoTime();
		List<Map<String, Object>> data = NetworkUtils.loadAcledData(dataFile,
				dateFormat);
		synchronized (lock) {
			network.setLearn(learn);
			if (subscriber != null) {
				network.observe().subscribe(subscriber);
			}

			String lastGwno = null;
			for (int i = 0; i < trainingCycles; i++) {
				for (Map<String, Object> multiInput : data) {
					/*
					 * HTM is currently learning sequence of events per GWNO (country code).
					 * To learn sequences for a different field, input the data and call reset in similar manner.
					 */
					String gwno = multiInput.get("GWNO").toString();
					if (lastGwno != null
							&& !lastGwno.equals(gwno)) {
						network.reset();
					}
					network.computeImmediate(multiInput);
					lastGwno = gwno;
				}
				lastGwno = null;
			}
		}
		data = null;
		long endTime = System.nanoTime();
		double duration = (endTime - startTime) / 1000000000.0;
		LOGGER.log(Level.INFO, String.format(
				"%d cycles completed in %f seconds", trainingCycles, duration));
	}
}
