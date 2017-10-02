package utilities;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.HashMap;
import java.util.stream.Collectors;

public class QuartzProcess {
    private HashMap<String,Runnable> jobs = new HashMap<>();
    private QuartzProcess(){}
    public static QuartzProcess getInstance(){
        if(instance == null)
            instance = new QuartzProcess();
        return instance;
    }
    public static HashMap<String,Runnable> getJobs(){
        return getInstance().jobs;
    }
    private static QuartzProcess instance;

    private static final String JOB_STRING = "jobString";
    private static Scheduler scheduler;
    public static Scheduler getScheduler() throws SchedulerException {
        if(scheduler == null){
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
        }
        return scheduler;
    }
    public static void scheduleJob(String cronSchedule, Runnable runnable, String jobName) throws SchedulerException {
        getJobs().put(jobName,runnable);
        if(getScheduler().getJobKeys(GroupMatcher.jobGroupEquals("group1")).stream()
                .filter(a->a.getName().equals(jobName)).collect(Collectors.toList()).size() != 0)
            return;
        JobDetail jobDetail = JobBuilder.newJob(PerformJob.class)
                .usingJobData(JOB_STRING,jobName)
                .withIdentity(jobName, "group1").build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity(jobName+"Trigger", "group1")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cronSchedule))
                .build();

        //schedule it
        getScheduler().scheduleJob(jobDetail, trigger);
    }

    public static void scheduleJob(String cronSchedule) throws SchedulerException {
        if(getScheduler().getJobKeys(GroupMatcher.jobGroupEquals("group1")).stream()
                .filter(a->a.getName().equals("dummyJobName")).collect(Collectors.toList()).size() != 0)
            return;
        JobDetail job = JobBuilder.newJob(PerformJob.class)
                .withIdentity("dummyJobName", "group1").build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("dummyTriggerName", "group1")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cronSchedule))
                .build();

        //schedule it
        getScheduler().scheduleJob(job, trigger);
    }
    public static class PerformJob implements Job{
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            getJobs().get(context.getJobDetail().getJobDataMap().getString(JOB_STRING)).run();
        }
    }

    public static void startQuarzExampleThreads() throws SchedulerException {
//        scheduleJob("30 2 * * * ?"); //run download job at 2:30 am every day
        scheduleJob("0 0 0/2 * * ?"); //run download job every 2 hours
    }

}