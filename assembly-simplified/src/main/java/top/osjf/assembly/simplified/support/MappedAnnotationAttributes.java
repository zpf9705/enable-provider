package top.osjf.assembly.simplified.support;

import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Map the attribute binding class from {@link Annotation Annotation}
 * to {@link Map Map} and define the static parameter acquisition method
 * for annotation proxy instances.
 *
 * <p>Inherit {@link AnnotationAttributes} to support methods for obtaining
 * annotation properties, and use {@link AnnotationUtils#getAnnotationAttributes(Annotation)}
 * to parse annotations to add support for {@link AliasFor}.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 2.2.5
 */
public class MappedAnnotationAttributes extends AnnotationAttributes {

    private static final long serialVersionUID = 1326994275618873618L;

    /**
     * Create a {@link MappedAnnotationAttributes} encapsulated map structure using {@link Map map}.
     *
     * @param map original source of annotation attribute <em>key-value</em> pairs
     */
    public MappedAnnotationAttributes(Map<String, Object> map) {
        super(map);
    }

    /**
     * Static for Create a {@link MappedAnnotationAttributes} using {@link Method cronMethod}.
     *
     * @param element        program elements that can carry annotations.
     * @param annotationType the type of annotation represented by this.
     * @return an {@link MappedAnnotationAttributes} instance.
     */
    public static MappedAnnotationAttributes of(AnnotatedElement element, Class<? extends Annotation> annotationType) {
        return of(() -> element.getAnnotation(annotationType));
    }

    /**
     * Static for Create a {@link MappedAnnotationAttributes} using {@link Method cronMethod}.
     *
     * @param annotationSupplier The value provider for Annotation.
     * @return an {@link MappedAnnotationAttributes} instance.
     */
    public static MappedAnnotationAttributes of(Supplier<Annotation> annotationSupplier) {
        Annotation annotation = annotationSupplier.get();
        if (annotation == null) throw new IllegalArgumentException("Target annotation must not be null");
        return of(annotation);
    }

    /**
     * Static for Create a {@link MappedAnnotationAttributes} using {@link Annotation annotation}.
     *
     * @param annotation the annotation to retrieve the attributes for.
     * @return an {@link MappedAnnotationAttributes} instance.
     */
    public static MappedAnnotationAttributes of(Annotation annotation) {
        return of(AnnotationUtils.getAnnotationAttributes(annotation));
    }

    /**
     * Static for Create a {@link MappedAnnotationAttributes} using {@link Map map}.
     *
     * @param map original source of annotation attribute <em>key-value</em> pairs
     * @return an {@link MappedAnnotationAttributes} instance.
     */
    public static MappedAnnotationAttributes of(Map<String, Object> map) {
        return new MappedAnnotationAttributes(map);
    }
}
