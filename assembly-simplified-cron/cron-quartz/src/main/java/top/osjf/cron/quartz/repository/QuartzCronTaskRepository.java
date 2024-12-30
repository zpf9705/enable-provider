/*
 * Copyright 2024-? the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.osjf.cron.quartz.repository;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobFactory;
import top.osjf.cron.core.lang.NotNull;
import top.osjf.cron.core.listener.CronListener;
import top.osjf.cron.core.repository.CronTask;
import top.osjf.cron.core.repository.CronTaskRepository;
import top.osjf.cron.core.repository.RepositoryUtils;
import top.osjf.cron.core.repository.TaskBody;
import top.osjf.cron.quartz.IDJSONConversion;
import top.osjf.cron.quartz.MethodLevelJob;
import top.osjf.cron.quartz.listener.QuartzCronListener;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Properties;

/**
 * The Quartz cron task repository {@link CronTaskRepository}.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.0
 */
public class QuartzCronTaskRepository implements CronTaskRepository {

    /**
     * The scheduled task management class of Quartz.
     */
    private Scheduler scheduler;

    /**
     * The Quartz management interface for the listener.
     */
    private final ListenerManager listenerManager;

    /**
     * Create a construction method for {@link Scheduler} using a configuration properties.
     *
     * @param properties {@link StdSchedulerFactory} configuration properties.
     * @param jobFactory Quartz task production factory.
     */
    public QuartzCronTaskRepository(Properties properties, JobFactory jobFactory) {
        if (properties == null) properties = System.getProperties();
        try {
            getScheduler(new StdSchedulerFactory(), properties);
            if (jobFactory != null) {
                scheduler.setJobFactory(jobFactory);
            }
            listenerManager = scheduler.getListenerManager();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set up a scheduled scheduler, and if there is a retrieval failure,
     * adjust the scheduler factory parameters according to the abnormal
     * situation and continue to retrieve.
     *
     * @param schedulerFactory Scheduling factory.
     * @param properties       Configuration file object.
     * @see SimpleThreadPool#initialize()
     */
    void getScheduler(StdSchedulerFactory schedulerFactory, Properties properties) throws SchedulerException {
        SchedulerException getSchedulerIssue = null;
        schedulerFactory.initialize(properties);
        try {
            scheduler = schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            getSchedulerIssue = e;
        }
        if (getSchedulerIssue == null) {
            return;
        }
        String message = getSchedulerIssue.getMessage();
        if (message.contains("Thread count must be > 0")) {
            //If the number of threads is not configured, give a default value of number of available cores+1.
            properties.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_PREFIX + ".threadCount",
                    String.valueOf(Runtime.getRuntime().availableProcessors() + 1));
        }
        schedulerFactory.initialize(properties);
        scheduler = schedulerFactory.getScheduler();
    }

    /**
     * Return the scheduled task management class of Quartz.
     *
     * @return the scheduled task management.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    @NotNull
    public String register(@NotNull String expression, @NotNull TaskBody body) {
        return RepositoryUtils.doRegister(() -> {
            JobDetail jobDetail = body.unwrap(JobDetail.class);
            JobKey key = jobDetail.getKey();
            TriggerBuilder<CronTrigger> triggerBuilder = TriggerBuilder.newTrigger()
                    .withIdentity(key.getName())
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(expression));
            scheduler.scheduleJob(jobDetail, triggerBuilder.build());
            return IDJSONConversion.convertJobKeyAsJSONID(key);
        }, ParseException.class);
    }

    @Override
    @NotNull
    public String register(@NotNull CronTask task) {
        Method method = task.getRunnable().getMethod();
        return register(task.getExpression(), new JobDetailTaskBody(JobBuilder.newJob(MethodLevelJob.class)
                .withIdentity(method.getName(), method.getDeclaringClass().getName()).build()));
    }

    @Override
    public void update(@NotNull String id, @NotNull String newExpression) {
        JobKey jobKey = IDJSONConversion.convertJSONIDAsJobKey(id);
        String name = jobKey.getName();
        RepositoryUtils.doVoidInvoke(() -> scheduler.rescheduleJob(new TriggerKey(name), TriggerBuilder.newTrigger()
                .withIdentity(name)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(newExpression))
                .build()), ParseException.class);
    }

    @Override
    public void remove(@NotNull String id) {
        RepositoryUtils.doVoidInvoke(() ->
                scheduler.deleteJob(IDJSONConversion.convertJSONIDAsJobKey(id)), null);
    }

    @Override
    public void addListener(@NotNull CronListener listener) {
        listenerManager.addJobListener(listener.unwrap(QuartzCronListener.class));
    }

    @Override
    public void removeListener(@NotNull CronListener listener) {
        listenerManager.removeJobListener(listener.unwrap(QuartzCronListener.class).getName());
    }
}
