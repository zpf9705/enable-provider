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

package top.osjf.cron.spring.scheduler;

import top.osjf.cron.core.listener.CronListener;

/**
 * The spring scheduling cron task listener {@link CronListener}.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.3
 */
public interface SchedulingListener extends CronListener {

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Note:</strong>
     * If the method {@link #start} is rewritten, the rewriting of this method will
     * become invalid. Please be aware!
     *
     * @param id {@inheritDoc}
     */
    @Override
    default void startWithId(String id) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Note:</strong>
     * If the method {@link #success} is rewritten, the rewriting of this method will
     * become invalid. Please be aware!
     *
     * @param id {@inheritDoc}
     */
    @Override
    default void successWithId(String id) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Note:</strong>
     * If the method {@link #failed} is rewritten, the rewriting of this method will
     * become invalid. Please be aware!
     *
     * @param id {@inheritDoc}
     */
    @Override
    default void failedWithId(String id, Throwable exception) {
    }
}
