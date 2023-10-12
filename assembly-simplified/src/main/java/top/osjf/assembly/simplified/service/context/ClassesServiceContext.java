package top.osjf.assembly.simplified.service.context;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import top.osjf.assembly.simplified.service.annotation.EnableServiceCollection;
import top.osjf.assembly.simplified.service.annotation.EnableServiceCollection2;
import top.osjf.assembly.simplified.service.annotation.ServiceCollection;
import top.osjf.assembly.util.data.ClassMap;
import top.osjf.assembly.util.data.ThreadSafeClassMap;
import top.osjf.assembly.util.io.ScanUtils;
import top.osjf.assembly.util.lang.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Support implementation class for service context {@link ServiceContext}.
 *
 * <p>The implementation process is as follows.
 *
 * <p>Due to scanning the package path where the startup class is located,
 * {@link SpringApplicationRunListener} listening is implemented.
 * In the first step of obtaining the package path of the startup class,
 * it should be noted that this is not an instantiation of the injection container,
 * so it is not {@link top.osjf.assembly.simplified.service.ServiceContextConfiguration}
 * configuring the injected object{@link ClassesServiceContext}.
 *
 * <p>After the first step of obtaining the startup class package, we manually configure
 * and inject a {@link ClassesServiceContext} as the preparation object.
 *
 * <p>When {@link SpringApplicationRunListener} enters the start phase, we call
 * back the {@link #started(ConfigurableApplicationContext)} method.
 *
 * <p>The object calling the {@link #started(ConfigurableApplicationContext)} method here
 * is created through Spring's SPI mechanism, not a container singleton.
 *
 * <p>At this point, we collect the annotated class of {@link ServiceCollection},
 * with the package as the main class package path.
 *
 * <p>By using the spring context method {@link org.springframework.context.ApplicationContext#getBeansOfType(Class)},
 * we can query all container subclasses and place them in the object we just injected into the container.
 *
 * <p>At this point, the service collection is complete.
 *
 * <p>It should be noted that the objects created by Spring's spi mechanism are not container objects.
 * These objects are configured {@link SpringApplicationRunListener} in [/META INF/spring. factories],
 * and for extension notifications, it is important to pay attention.
 *
 * <p>The configuration to trigger this type can be selected as {@link EnableServiceCollection}
 * or {@link EnableServiceCollection2}.<pre>&#064;EnableServiceCollection2@type=CLASSES</pre>
 *
 * @author zpf
 * @since 2.0.4
 */
public class ClassesServiceContext extends AbstractServiceContext implements SpringApplicationRunListener {

    private final ClassMap<String, Object> contextMap = new ThreadSafeClassMap<>(4);

    public static final String CLASSES_SERVICE_CONTENT_BEAN = "CLASSES_SERVICE_CONTENT_BEAN";

    private String[] scanPackages;

    public static final String COMMA = " : ";

    /**
     * The empty structure here is mainly used for configuration purposes.
     */
    public ClassesServiceContext() {
    }

    /**
     * The necessary constructor for using {@link SpringApplicationRunListener}.
     * <p>Is the same object as calling {@link #started(ConfigurableApplicationContext)}.
     *
     * @param application Spring's application startup class.
     * @param args        The startup parameters for the application startup class of Spring.
     */
    public ClassesServiceContext(SpringApplication application, String[] args) {
        scanPackages = new String[]{
                application.getMainApplicationClass().getPackage().getName()
        };
    }

    /**
     * Set the scan package path for the service.
     *
     * @param packages Scanning packages , must not be {@literal null}.
     */
    private void setScanPackages(String... packages) {
        Objects.requireNonNull(packages, "Setting scanPackages no be null");
        this.scanPackages = packages;
    }

    /**
     * Add a non null service name map.
     *
     * @param contextMap Service name map , must not be {@literal null}.
     */
    @SuppressWarnings("unchecked")
    public void addContextMap(Map<String, Object> contextMap) {
        Objects.requireNonNull(contextMap, "Setting contextMap no be null");
        this.contextMap.mergeMaps(contextMap);
    }

    @Override
    public <S> S getService(String serviceName, Class<S> requiredType) throws NoSuchServiceException {
        //Prevent potential duplicate service names.
        //org.example.Text : ServiceName
        serviceName = requiredType.getName() + COMMA + serviceName;
        S service = contextMap.getValueOnClass(serviceName, requiredType);
        if (service == null) {
            service = super.getService(serviceName, requiredType);
        }
        return service;
    }

    //This method is called by an object created by the SPI mechanism, not a container object.
    //Same object for call constructor {#ClassesServiceContext(SpringApplication application, String[] args)}
    @Override
    public void started(ConfigurableApplicationContext context) {
        //Find the previously prepared implementation class object and assign a value to the forwarding service map.
        ClassesServiceContext contextBean;
        try {
            contextBean = context.getBean(CLASSES_SERVICE_CONTENT_BEAN, ClassesServiceContext.class);
        } catch (BeansException ignored) {
            return;
        }
        //Place the scan path without overloading.
        contextBean.setScanPackages(scanPackages);
        //Load the service context map.
        load(contextBean, context);
    }


    @Override
    public void reloadWithScanPackages(String... packages) {
        close();
        setScanPackages(packages);
        load(this, getApplicationContext());
    }

    @Override
    public void close() {
        contextMap.clear();
        scanPackages = null;
    }

    private void load(ClassesServiceContext contextBean, ApplicationContext context) {
        Set<Class<Object>> serviceClasses = ScanUtils.getTypesAnnotatedWith(ServiceCollection.class,
                scanPackages);
        if (CollectionUtils.isEmpty(serviceClasses)) {
            return;
        }
        for (Class<Object> serviceClass : serviceClasses) {
            Map<String, Object> beansMap;
            try {
                beansMap = context.getBeansOfType(serviceClass);
            } catch (BeansException ignored) {
                continue;
            }
            contextBean.addContextMap(beansMap);
        }
    }
}
