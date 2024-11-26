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

package top.osjf.sdk.http;

import top.osjf.sdk.core.client.Client;
import top.osjf.sdk.core.process.AbstractRequestParams;
import top.osjf.sdk.core.util.JSONUtil;

import java.util.Collections;
import java.util.Map;

/**
 * The abstract class {@code AbstractHttpRequestParams} extends {@code AbstractRequestParams}
 * and implements the HttpRequest interface.
 * This class defines common behaviors for HTTP request parameters and allows subclasses to
 * implement specific request parameter handling logic by extending it.
 *
 * <p>You can check the example code:
 * <pre>
 * {@code
 * public class ExampleHttpRequestParams extends AbstractHttpRequestParams<Example> {
 *
 *     public HttpSdkEnum matchSdk() {
 *         return new HttpSdkEnum() {
 *
 *             public String getUlr(String host) {
 *                 return "https:// + host +"/example/query.json";
 *             }
 *
 *             public HttpRequestMethod getHttpRequestMethod() {
 *                 return HttpRequestMethod.POST;
 *             }
 *
 *             public String name() {
 *                 return "EXAMPLE";
 *             }
 *         };
 *     }
 *
 *     public Object getRequestParam() {
 *         return "{"address":"example.com"}";
 *     }
 *
 *     public Class<Example> getResponseCls() {
 *         return Example.class;
 *     }
 * }}
 * </pre>
 *
 * @param <R> Implement a unified response class data type.
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractHttpRequestParams<R extends AbstractHttpResponse> extends AbstractRequestParams<R>
        implements HttpRequest<R> {

    private static final long serialVersionUID = 7487068349280012103L;

    @Override
    public Object getRequestParam() {
        Object param = getParam();
        if (param == null) {
            return null;
        }
        if (defaultToJson()) {
            param = JSONUtil.toJSONString(param);
        }
        return param;
    }

    /**
     * {@inheritDoc}
     * <p>
     * When the request parameter exists and JSON serialization is used,
     * this method defaults to returning the context type of {@code application/json}.
     *
     * @return {@inheritDoc}
     * @since 1.0.2
     */
    @Override
    public Map<String, Object> getHeadMap() {
        if (getRequestParam() != null && defaultToJson()) {
            return Collections.singletonMap("Content-Type", "application/json");
        }
        return super.getHeadMap();
    }

    @Override
    public boolean montage() {
        return false;
    }

    @Override
    public Class<? extends Client> getClientCls() {
        return DefaultHttpClient.class;
    }

    /**
     * The abstract method for obtaining the actual request body.
     * <p>If {@link #defaultToJson()} requires JSON serialization,
     * it will be done in {@link #getRequestParam()}.
     * <p>If it is not a JSON request parameter, this method needs
     * to be rewritten to directly convert the input parameter format
     * and set {@link #defaultToJson()} to {@literal true}.
     *
     * @return Returns an input parameter object, which may have
     * multiple forms of existence or may be {@literal null}.
     */
    public Object getParam() {
        return null;
    }

    /**
     * Mark whether to use input parameters as JSON serial numbers.
     *
     * @return True is required, while false is not required.
     */
    public boolean defaultToJson() {
        return true;
    }
}
