package top.osjf.assembly.simplified.support;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Manually register objects into the support classes of the Spring container.
 *
 * <p>With annotation usage as the core, the general process is to query and annotate
 * an interface class for a certain annotation, construct a {@link BeanDefinition} based
 * on the information, and inject it into the spring container.
 *
 * <p>This process can be enabled using an annotation switch.
 *
 * <p>For example, when the springboot program starts, if it detects that the container bean is
 * wearing {@link top.osjf.assembly.simplified.sdk.annotation.EnableSdkProxyRegister} annotations,
 * it will automatically search for the wearing annotation {@link top.osjf.assembly.simplified.sdk.annotation.Sdk}
 * interface in this class and create an implementation class (refer to {@link AbstractJdkProxySupport}).
 *
 * <p>The implementation process depends on the bean's registrar interface {@link ImportBeanDefinitionRegistrar}.
 *
 * <p>The lookup of annotated interfaces depends on the interface {@link ResourceLoader} and
 * {@link ClassPathScanningCandidateComponentProvider}.
 *
 * @author zpf
 * @since 1.1.0
 */
public abstract class AbstractProxyBeanInjectSupport<O extends Annotation, F extends Annotation>
        implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    private Environment environment;

    private ResourceLoader resourceLoader;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry,
                                        @NonNull BeanNameGenerator importBeanNameGenerator) {
        this.registerBeanDefinitions(metadata, registry);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry) {
        //Obtain the annotation value for opening
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(getOpenClazz()
                .getName()));
        //AssertUtils to Analysis
        Assert.notNull(attributes, "Analysis " + getOpenClazz().getName() + " attribute encapsulation to " +
                "org.springframework.core.annotation.AnnotationAttributes failed");
        //Obtain Scan Path
        String[] basePackages = attributes.getStringArray(getPackagesSign());
        if (ArrayUtils.isEmpty(basePackages)) {
            basePackages = SourceEnvironmentPostProcessor.findSpringbootPrimarySourcesPackages();
        }
        //Obtain Path Scan Provider
        ClassPathScanningCandidateComponentProvider classPathScan = this.getClassPathScanUseAnnotationClass();
        //The class annotated by the subcontracting scanning target
        for (String basePackage : basePackages) {
            Set<BeanDefinition> beanDefinitions = classPathScan.findCandidateComponents(basePackage);
            if (CollectionUtils.isEmpty(beanDefinitions)) {
                continue;
            }
            //Perform proxy registration for each bean
            for (BeanDefinition beanDefinition : beanDefinitions) {
                if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
                    continue;
                }
                AnnotatedBeanDefinition adi = (AnnotatedBeanDefinition) beanDefinition;
                //Obtain annotation class identification interface
                AnnotationMetadata meta = adi.getMetadata();
                if (!meta.isInterface()) {
                    continue;
                }
                //Obtain annotation class identification interface annotation value domain
                AnnotationAttributes attributesFu = AnnotationAttributes
                        .fromMap(meta.getAnnotationAttributes(getFindClazz().getCanonicalName()));
                if (attributesFu == null) {
                    continue;
                }
                //Bean registration
                this.beanRegister(attributesFu, registry, meta);
            }
        }
    }

    /**
     * Register annotation association scanning class.
     *
     * @param attributes Annotation value Range.
     * @param registry   Bean Keygen.
     * @param meta       Associated annotation interface.
     */
    public void beanRegister(AnnotationAttributes attributes, BeanDefinitionRegistry registry, AnnotationMetadata meta) {
    }

    /**
     * Provide a conditional class scanner based on the provided class object.
     *
     * @return {@link ClassPathScanningCandidateComponentProvider}.
     */
    @NonNull
    public ClassPathScanningCandidateComponentProvider getClassPathScanUseAnnotationClass() {
        ClassPathScanningCandidateComponentProvider classPathScan =
                new ClassPathScanningCandidateComponentProvider(false, this.environment) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation();
                    }
                };
        classPathScan.setResourceLoader(this.resourceLoader);
        //scan all class type
        classPathScan.setResourcePattern("**/*.class");
        classPathScan.addIncludeFilter(new AnnotationTypeFilter(getFindClazz()));
        return classPathScan;
    }

    /**
     * Provide functionality to enable annotation class objects.
     *
     * @return must not be {@literal null}.
     */
    @NonNull
    public abstract Class<O> getOpenClazz();

    /**
     * Provide class objects for annotation of annotation classes.
     *
     * @return must not be {@literal null}.
     */
    @NonNull
    public abstract Class<F> getFindClazz();

    /**
     * Provide path scan variable identification, default to {@code value}.
     *
     * @return must not be {@literal null}.
     */
    @NonNull
    public String getPackagesSign() {
        return "value";
    }

    /**
     * Get Spring startup environment variables.
     *
     * @return {@link Environment}.
     */
    @NonNull
    public Environment getEnvironment() {
        return this.environment;
    }
}
