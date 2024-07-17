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

package top.osjf.cron.core.repository;

import top.osjf.cron.core.annotation.NotNull;
import top.osjf.cron.core.exception.CronExpressionInvalidException;
import top.osjf.cron.core.exception.CronTaskNoExistException;
import top.osjf.cron.core.listener.CronListener;

import java.util.Arrays;
import java.util.List;

/**
 * The executor of the dynamic registration of scheduled tasks, developers
 * can call the method of this interface to register or update or remove a
 * scheduled task to the current scheduled task center management at runtime.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public interface CronTaskRepository<ID, BODY> {

    /**
     * Build a scheduled task that can be registered for central
     * processing using Cron standardized expressions and an
     * executable runtime {@link Runnable}.
     *
     * @param cronExpression Expression in {@code Cron} format.
     * @param runsBody       Timed execution of the runtime.
     * @return Scheduled task ID.
     * @throws CronExpressionInvalidException The issue of infinite thrown
     *                                        exceptions by cron expressions.
     */
    ID register(String cronExpression, BODY runsBody) throws CronExpressionInvalidException;

    /**
     * Update a scheduled task based on the task ID and cron expression
     * generated by the implementer.
     *
     * @param id                Scheduled task ID.
     * @param newCronExpression New Expression in {@code Cron} format.
     * @throws CronExpressionInvalidException The issue of infinite thrown
     *                                        exceptions by cron expressions.
     * @throws CronTaskNoExistException       Exception thrown when the task ID cannot
     *                                        be found for the corresponding task.
     */
    void update(ID id, String newCronExpression) throws CronExpressionInvalidException,
            CronTaskNoExistException;

    /**
     * Delete a scheduled task based on the task ID generated
     * by the implementer.
     *
     * @param id Scheduled task ID.
     * @throws CronTaskNoExistException Exception thrown when the task ID cannot
     *                                  be found for the corresponding task.
     */
    void remove(ID id) throws CronTaskNoExistException;

    /**
     * Add a scheduled task listener.
     *
     * @param cronListener a scheduled task listener.
     */
    void addCronListener(@NotNull CronListener cronListener);

    /**
     * Add scheduled task listeners.
     *
     * @param cronListeners scheduled task listeners.
     */
    default void addCronListeners(List<CronListener> cronListeners) {
        for (CronListener cronListener : cronListeners) {
            addCronListener(cronListener);
        }
    }

    /**
     * Add scheduled task listeners.
     *
     * @param cronListeners scheduled task listeners.
     */
    default void addCronListeners(CronListener... cronListeners) {
        addCronListeners(Arrays.asList(cronListeners));
    }
}
