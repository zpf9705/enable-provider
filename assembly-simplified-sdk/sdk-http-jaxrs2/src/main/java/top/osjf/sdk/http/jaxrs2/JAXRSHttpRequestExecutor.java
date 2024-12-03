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

package top.osjf.sdk.http.jaxrs2;

import top.osjf.sdk.core.support.LoadOrder;
import top.osjf.sdk.core.support.Nullable;
import top.osjf.sdk.http.executor.AbstractMultiHttpMethodExecutor;
import top.osjf.sdk.http.executor.HttpRequestExecutor;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * One of the implementation classes of {@link HttpRequestExecutor}, please
 * refer to{@link JAXRSHttpSimpleRequestUtils} for implementation.
 *
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.2
 */
@LoadOrder(Integer.MIN_VALUE + 20)
public class JAXRSHttpRequestExecutor extends AbstractMultiHttpMethodExecutor {

    @Override
    public String get(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.get(url, headers, body, charset);
    }

    @Override
    public String post(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.post(url, headers, body, charset);
    }

    @Override
    public String put(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.put(url, headers, body, charset);
    }

    @Override
    public String delete(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.delete(url, headers, body, charset);
    }

    @Override
    public String trace(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.trace(url, headers, body, charset);
    }

    @Override
    public String options(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.options(url, headers, body, charset);
    }

    @Override
    public String head(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.head(url, headers, body, charset);
    }

    @Override
    public String patch(String url, @Nullable Map<String, String> headers, @Nullable Object body, @Nullable Charset charset) {
        return JAXRSHttpSimpleRequestUtils.patch(url, headers, body, charset);
    }
}
