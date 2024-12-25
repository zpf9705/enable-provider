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

package top.osjf.optimize.service_bean.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import top.osjf.optimize.service_bean.annotation.ServiceCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code DefaultServiceContext} is the default service context implementation.
 *
 * <p>This class is a specific implementation of {@link AbstractServiceContext},
 * which provides functions for service registration, retrieval, existence
 * checking, and removal. It utilizes Spring's {@code ApplicationContext} to
 * manage and operate service beans, supporting dynamic addition and removal
 * of services.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.3
 */
public class DefaultServiceContext extends AbstractServiceContext {

    private static final Logger logger = LoggerFactory.getLogger(ServiceContext.class);

    @Override
    @SuppressWarnings("unchecked")
    public <S> S getService(String serviceName) throws NoAvailableServiceException {
        ApplicationContext applicationContext = getContext();
        if (!ServiceCore.isEnhancementServiceName(serviceName)
                || !applicationContext.containsBean(serviceName)) {
            throw new NoAvailableServiceException(serviceName);
        }
        return (S) applicationContext.getBean(serviceName);
    }

    @Override
    public <S> S getService(String serviceName, Class<S> requiredType) throws NoAvailableServiceException {
        ApplicationContext applicationContext = getContext();
        S service = null;
        for (String candidateName :
                ServiceCore.getEnhancementCandidateNames(serviceName, requiredType)) {
            if (applicationContext.containsBean(candidateName)) {
                service = applicationContext.getBean(candidateName, requiredType);
                break;
            }
        }
        if (service == null) {
            throw new NoAvailableServiceException(serviceName, requiredType);
        }
        return service;
    }

    @Override
    public <S> boolean addService(@Nullable String serviceName, Class<S> serviceType) {

        Objects.requireNonNull(serviceType, "ServiceType no be null");

        List<Class<?>> targetServiceTypes = ServiceCore.getTargetServiceTypes(serviceType);

        if (CollectionUtils.isEmpty(targetServiceTypes)) {
            if (logger.isWarnEnabled()) {
                logger.warn("No annotation {} was found on the related parent class or interface of type {}.",
                        ServiceCollection.class.getName(), serviceType.getName());
            }
            return false;
        }

        serviceName = ServiceCore.resolveNameByType(serviceType);

        String beanName = ServiceCore.enhancementBeanName(serviceType, serviceName);

        List<String> alisaNames = new ArrayList<>();
        for (Class<?> targetServiceType : targetServiceTypes) {
            alisaNames.add(ServiceCore.enhancementAlisaName(targetServiceType, beanName));
        }

        //Because beans that can be recognized by the Spring container will already be automatically
        // added to the collection column, dynamic service scope bean creation is required here.
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(serviceType)
                .setScope(ServiceContext.SUPPORT_SCOPE);
        BeanDefinitionReaderUtils.registerBeanDefinition
                (new BeanDefinitionHolder(builder.getBeanDefinition(), beanName,
                        alisaNames.toArray(new String[]{})), unwrapApplicationContext(BeanDefinitionRegistry.class));

        //After registration, activate the bean and initialize it.
        getContext().getBean(beanName, serviceType);

        if (logger.isInfoEnabled()) {
            logger.info("Created a dynamic bean for name {} and type {}.", beanName, serviceType.getName());
        }
        return true;
    }

    @Override
    public boolean containsService(String serviceName) {
        return ServiceCore.isEnhancementServiceName(serviceName) && getContext().containsBean(serviceName);
    }

    @Override
    public <S> boolean containsService(String serviceName, Class<S> requiredType) {
        ApplicationContext applicationContext = getContext();
        return ServiceCore.getEnhancementCandidateNames(serviceName, requiredType)
                .stream()
                .anyMatch(applicationContext::containsBean);
    }

    @Override
    public boolean removeService(String serviceName) {
        if (!containsService(serviceName)) {
            return true;
        }
        unwrapApplicationContext(ConfigurableApplicationContext.class)
                .getBeanFactory()
                .destroyScopedBean(serviceName);
        return true;
    }

    @Override
    public <S> boolean removeService(String serviceName, Class<S> requiredType) {
        ConfigurableApplicationContext applicationContext =
                unwrapApplicationContext(ConfigurableApplicationContext.class);
        boolean removeResult = false;
        for (String name
                : ServiceCore.getEnhancementCandidateNames(serviceName, requiredType)) {
            if (applicationContext.containsBean(name)) {
                applicationContext.getBeanFactory().destroyScopedBean(name);
                removeResult = true;
                break;
            }
        }
        return removeResult;
    }
}
