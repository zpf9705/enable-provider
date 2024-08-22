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

package top.osjf.spring.autoconfigure.cron;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.osjf.cron.core.lifestyle.StartupProperties;
import top.osjf.cron.cron4j.lifestyle.Cron4jCronLifeStyle;
import top.osjf.cron.cron4j.repository.Cron4jCronTaskRepository;
import top.osjf.cron.spring.cron4j.Cron4jCronTaskConfiguration;

/**
 * {@link Import Import Configuration}  for Cron4j Cron Task.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.1
 */
@Configuration(proxyBeanMethods = false)
@Import(Cron4jCronTaskConfiguration.class)
@ConditionalOnClass({Cron4jCronLifeStyle.class, Cron4jCronTaskRepository.class})
@ConditionalOnProperty(name = "spring.schedule.cron.client-type", havingValue = "cron4j", matchIfMissing = true)
public class Cron4jCronTaskImportConfiguration {

    @Bean
    public StartupProperties cron4jStartupProperties(CronProperties cronProperties) {
        return StartupProperties.of(cronProperties.getCron4j().toMetadata());
    }
}
