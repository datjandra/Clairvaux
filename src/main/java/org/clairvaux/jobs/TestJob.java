package org.clairvaux.jobs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.opencsv.CSVWriter;

public class TestJob implements Job {

	private final static Logger LOGGER = Logger.getLogger(TestJob.class.getName()); 
	
	public void execute(final JobExecutionContext ctx) 
    		throws JobExecutionException {
		JobDataMap map = ctx.getJobDetail().getJobDataMap();
		String rootPath = String.format("%s%sdata%s", map.getString("path"), File.separator , File.separator);
		File parent = new File(rootPath);
		if (!parent.exists()) {
			parent.mkdirs();
		}
		
		File testFile = new File(rootPath + "test.csv");
		try {
			if (!testFile.exists()) {
				testFile.createNewFile();
			} else {
				return;
			}
			
			CSVWriter writer = new CSVWriter(new FileWriter(testFile));
			String[] entries = "first#second#third".split("#");
		    writer.writeNext(entries);
			writer.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    }
}