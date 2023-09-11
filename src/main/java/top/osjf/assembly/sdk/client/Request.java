package top.osjf.assembly.sdk.client;

import copy.cn.hutool.v_5819.core.bean.BeanUtil;
import copy.cn.hutool.v_5819.core.bean.copier.CopyOptions;
import copy.cn.hutool.v_5819.core.util.ArrayUtil;
import copy.cn.hutool.v_5819.core.util.ReflectUtil;
import top.osjf.assembly.sdk.SdkEnum;
import top.osjf.assembly.sdk.SdkException;
import top.osjf.assembly.sdk.annotation.MapFiled;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Define the value processing of the request node, including methods such as
 * <p>
 * SDK attribute {@link SdkEnum},
 * <p>
 * URL attribute {@link #urlJoin()}{@link #formatUrl(String)},
 * <p>
 * request body attribute {@link #getBody()} ,
 * <p>
 * parameter validation{@link #validate()},
 * <p>
 * response body processing scheme , etc., aiming to standardize the entire request process.
 *
 * @author zpf
 * @since 1.1.0
 */
public interface Request<R extends Response> extends Serializable {

    /**
     * Affiliated API
     *
     * @return api enum for {@link SdkEnum}
     */
    SdkEnum matchApi();

    /**
     * The splicing after the URL requires
     *
     * @return Splicing item
     */
    default String urlJoin() {
        return "";
    }

    /**
     * Obtain format request url with host
     *
     * @param host Request host address
     * @return request url
     */
    default String formatUrl(String host) {
        return matchApi().getUlr(host) + urlJoin();
    }

    /**
     * Get Request Body Object
     *
     * @return Request Body Object
     */
    Object getBody();

    /**
     * Default parameter conversion to map method，be dependent on{@link #getBody()}
     * If you want to create an alias for the field, please add {@link MapFiled}
     *
     * @return The map form of the parameter body
     */
    default Map<String, Object> toParamsMap() {
        Object body = getBody();
        if (Objects.isNull(body)) {
            return Collections.emptyMap();
        }
        //get all fields include self and parent private and public
        Field[] allFields = ReflectUtil.getFields(body.getClass());
        if (ArrayUtil.isEmpty(allFields)) {
            return BeanUtil.beanToMap(body);
        } else {
            Map<String, String> fieldMapping = Arrays.stream(allFields)
                    //at MapFiled
                    .filter(f -> Objects.nonNull(f.getAnnotation(MapFiled.class)) &&
                            //no static
                            !Modifier.isStatic(f.getModifiers()))
                    .collect(Collectors.toMap(Field::getName,
                            field -> field.getAnnotation(MapFiled.class).name()));
            return BeanUtil.beanToMap(body, new LinkedHashMap<>(),
                    CopyOptions.create().setFieldMapping(fieldMapping));
        }
    }

    /**
     * Parameter verification
     */
    void validate() throws SdkException;

    /**
     * Return the response class object
     *
     * @return API - Response Object
     */
    Class<R> getResponseCls();

    /**
     * Get request header map
     *
     * @return {@link Map}
     */
    Map<String, String> getHeadMap();

    /**
     * Determine if the request response data type is {@code JSON}
     *
     * @return {@literal true} all right
     */
    default boolean isrResponseJsonType() {
        return true;
    }

    /**
     * Determine if the request response data type is {@code XML}
     *
     * @return {@literal true} all right
     */
    default boolean isrResponseXmlType() {
        return false;
    }

    /**
     * XML conversion function
     *
     * @return {@literal true} all right
     */
    Function<String, String> xmlConvert();

    /**
     * Special conversion requirements
     *
     * @return {@link Function}
     */
    Function<String, String> specialConvert();

    /**
     * Obtain request type (default is all HTTP)
     *
     * @return {@link ClientType}
     */
    ClientType getClientType();
}
