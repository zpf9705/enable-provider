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

package top.osjf.cron.hutool.lifestyle;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The Hutool cron task Startup Args.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.0
 */
public class HutoolCronStartupArgs {

    /**
     * Set whether to support second matching.
     * <p>This method is used to define whether to use the second matching mode.
     * If it is true, the first digit in the timed task expression is seconds,
     * otherwise it is minutes, and the default is minutes.
     */
    private boolean isMatchSecond = true;

    /**
     * Whether to start as a daemon thread.
     * <p>If true, the scheduled task executed immediately after calling the
     * {@link HutoolCronLifeStyle#stop()} method will end.
     * Otherwise, it will wait for the execution to complete before ending.
     */
    private boolean isDaemon = false;

    /**
     * Analyze the array parameters in the order of {@link #isMatchSecond} first
     * bit and {@link #isDaemon} second bit.
     *
     * @param args the array parameters.
     * @return Analyze the results of parameter objects.
     */
    public static HutoolCronStartupArgs of(Object[] args) {
        HutoolCronStartupArgs startupArgs = new HutoolCronStartupArgs();
        if (ArrayUtils.isEmpty(args)) return startupArgs;
        startupArgs.isMatchSecond = (boolean) args[0];
        if (args.length > 1) startupArgs.isDaemon = (boolean) args[1];
        return startupArgs;
    }

    public boolean isMatchSecond() {
        return isMatchSecond;
    }

    public boolean isDaemon() {
        return isDaemon;
    }
}
