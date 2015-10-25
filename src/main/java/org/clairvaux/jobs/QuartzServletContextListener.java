package org.clairvaux.jobs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzServletContextListener implements ServletContextListener {

	private final static Logger LOGGER = Logger.getLogger(QuartzServletContextListener.class.getName());

	public void contextDestroyed(ServletContextEvent event) {
		LOGGER.info("ServletContextListener destroyed");
	}

	public void contextInitialized(ServletContextEvent event) {
		LOGGER.info("ServletContextListener started");
		String path = event.getServletContext().getRealPath("/");
		JobDataMap map = new JobDataMap();
		map.put("path", path);
		
		JobDetail job = JobBuilder.newJob(PredictionPipelineJob.class).withIdentity("predictionJob").setJobData(map).build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("predictionTrigger", "predictionGroup")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?")).build();

		try {
			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
