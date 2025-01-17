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

package top.osjf.sdk.core.caller;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import top.osjf.sdk.core.Response;
import top.osjf.sdk.core.support.NotNull;
import top.osjf.sdk.core.support.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The {@code FlowableCaller} class is a utility class used to perform asynchronous operations
 * (see class {@link AsyncFlowableCaller}) and process response results.
 * It implements the Runnable interface, supports retry mechanism, and provides flexible error
 * handling and response handling strategies.
 *
 * <p>This class encapsulates the creation and subscription logic of {@code Flowable} objects
 * based on ReactiveX RxJava3.
 * Allow users to define the operator, retry count, retry strategy in case of unsuccessful
 * response, and custom exception and response handling logic.
 *
 * @param <R> Generic R represents the type returned by an operation, which must
 *            inherit from the {@link Response} class.
 * @author <a href="mailto:929160069@qq.com">zhangpengfei</a>
 * @since 1.0.2
 */
public class FlowableCaller<R extends Response>
        extends AbstractFlowableCaller<R> implements FlowableProcessElement<R>, DisposableRunnable {

    /*** Customized subscription for regular consumers, used to handle normal response results. */
    @Nullable
    private final Consumer<R> customSubscriptionRegularConsumer;

    /*** Custom subscription exception consumers, used to handle exceptions that occur during the
     * subscription process. */
    @Nullable
    private final Consumer<Throwable> customSubscriptionExceptionConsumer;

    /*** The Disposable object represents the 'handle' of the subscription.
     * <p>It only exists after a subscription relationship has occurred.</p>
     * */
    private Disposable disposable;

    /**
     * Creates a new {@code FlowableCaller} with the given {@code AbstractFlowableCaller}
     * and initial parameters and custom subscription regular consumer and custom subscription
     * exception consumer.
     *
     * @param runBody                             {@link AbstractFlowableCaller#getRunBody()}.
     * @param retryTimes                          {@link AbstractFlowableCaller#getRetryTimes()}.
     * @param retryIntervalMilliseconds           {@link AbstractFlowableCaller#getRetryIntervalMilliseconds()}.
     * @param whenResponseNonSuccessRetry         {@link AbstractFlowableCaller#isWhenResponseNonSuccessRetry()}.
     * @param whenResponseNonSuccessFinalThrow    {@link AbstractFlowableCaller#isWhenResponseNonSuccessFinalThrow()}.
     * @param customRetryExceptionPredicate       {@link AbstractFlowableCaller#getCustomRetryExceptionPredicate()}.
     * @param customSubscriptionRegularConsumer   A custom consumer invoked upon successful subscription completion,
     *                                            used to handle successful results.
     * @param customSubscriptionExceptionConsumer A custom consumer invoked upon exception during subscription,
     *                                            used to handle errors.
     */
    public FlowableCaller(@NotNull Supplier<R> runBody,
                          int retryTimes,
                          long retryIntervalMilliseconds,
                          boolean whenResponseNonSuccessRetry,
                          boolean whenResponseNonSuccessFinalThrow,
                          @Nullable Predicate<? super Throwable> customRetryExceptionPredicate,
                          @Nullable Consumer<R> customSubscriptionRegularConsumer,
                          @Nullable Consumer<Throwable> customSubscriptionExceptionConsumer) {
        super(runBody, retryTimes, retryIntervalMilliseconds, whenResponseNonSuccessRetry,
                whenResponseNonSuccessFinalThrow, customRetryExceptionPredicate);
        this.customSubscriptionRegularConsumer = customSubscriptionRegularConsumer;
        this.customSubscriptionExceptionConsumer = customSubscriptionExceptionConsumer;
    }


    @Override
    @Nullable
    public Consumer<R> getCustomSubscriptionRegularConsumer() {
        return customSubscriptionRegularConsumer;
    }

    @Override
    @Nullable
    public Consumer<Throwable> getCustomSubscriptionExceptionConsumer() {
        return customSubscriptionExceptionConsumer;
    }

    @Override
    public void run() {
        this.disposable = getFlowable().subscribe(getOnNext(), getOnError());
        if (disposeSync()) dispose();
    }

    /**
     * Check if the disposable object has not been disposed of yet, and if so,
     * proceed with the disposal operation.
     * This is to ensure that resources can be released correctly even in the event of
     * an exception, avoiding resource leakage.
     */
    @Override
    public void dispose() {

        if (!isDisposed()) {
            disposable.dispose();
            LOGGER.info("Resource release completed");
        } else {
            LOGGER.info("The resource has been automatically released");
        }
    }

    @Override
    public boolean isDisposed() {
        return disposable.isDisposed();
    }

    /**
     * Return Boolean markers indicating whether to synchronize resource
     * release related operations.
     *
     * @return Boolean markers indicating.
     */
    protected boolean disposeSync() {
        return true;
    }

    /**
     * @return {@link OnNext}.
     * @see FlowableCaller.OnNext
     */
    protected OnNext getOnNext() {
        return new OnNext();
    }

    /**
     * @return {@link OnError}.
     * @see FlowableCaller.OnError
     */
    protected OnError getOnError() {
        return new OnError();
    }

    /* FlowableCaller void static method */

    /**
     * A static method for SDK calls using the API of {@code FlowableCaller}.
     *
     * @param runBody    {@link #getRunBody()}.
     * @param retryTimes {@link #getRetryTimes()}.
     * @param <R>        Generic R represents the type returned by an operation, which must
     *                   inherit from the {@link Response} class.
     * @throws NullPointerException if input runBody is {@literal null}.
     */
    public static <R extends Response> void call(@NotNull Supplier<R> runBody,
                                                 int retryTimes) {
        call(runBody, retryTimes, 0);
    }

    /**
     * A static method for SDK calls using the API of {@code FlowableCaller}.
     *
     * @param runBody                   {@link #getRunBody()}.
     * @param retryTimes                {@link #getRetryTimes()}.
     * @param retryIntervalMilliseconds {@link #getRetryIntervalMilliseconds()}.
     * @param <R>                       Generic R represents the type returned by an operation, which must
     *                                  inherit from the {@link Response} class.
     * @throws NullPointerException if input runBody is {@literal null}.
     */
    public static <R extends Response> void call(@NotNull Supplier<R> runBody,
                                                 int retryTimes,
                                                 long retryIntervalMilliseconds) {
        call(runBody, retryTimes, retryIntervalMilliseconds, false);
    }

    /**
     * A static method for SDK calls using the API of {@code FlowableCaller}.
     *
     * @param runBody                     {@link #getRunBody()}.
     * @param retryTimes                  {@link #getRetryTimes()}.
     * @param retryIntervalMilliseconds   {@link #getRetryIntervalMilliseconds()}.
     * @param whenResponseNonSuccessRetry {@link #isWhenResponseNonSuccessRetry()}.
     * @param <R>                         Generic R represents the type returned by an operation, which must
     *                                    inherit from the {@link Response} class.
     * @throws NullPointerException if input runBody is {@literal null}.
     */
    public static <R extends Response> void call(@NotNull Supplier<R> runBody,
                                                 int retryTimes,
                                                 long retryIntervalMilliseconds,
                                                 boolean whenResponseNonSuccessRetry) {
        call(runBody, retryTimes, retryIntervalMilliseconds,
                whenResponseNonSuccessRetry, false);
    }

    /**
     * A static method for SDK calls using the API of {@code FlowableCaller}.
     *
     * @param runBody                          {@link #getRunBody()}.
     * @param retryTimes                       {@link #getRetryTimes()}.
     * @param retryIntervalMilliseconds        {@link #getRetryIntervalMilliseconds()}.
     * @param whenResponseNonSuccessRetry      {@link #isWhenResponseNonSuccessRetry()}.
     * @param whenResponseNonSuccessFinalThrow {@link #isWhenResponseNonSuccessFinalThrow()}.
     * @param <R>                              Generic R represents the type returned by an operation, which must
     *                                         inherit from the {@link Response} class.
     * @throws NullPointerException if input runBody is {@literal null}.
     */
    public static <R extends Response> void call(@NotNull Supplier<R> runBody,
                                                 int retryTimes,
                                                 long retryIntervalMilliseconds,
                                                 boolean whenResponseNonSuccessRetry,
                                                 boolean whenResponseNonSuccessFinalThrow) {
        call(runBody, retryTimes, retryIntervalMilliseconds,
                whenResponseNonSuccessRetry, whenResponseNonSuccessFinalThrow, null);
    }

    /**
     * A static method for SDK calls using the API of {@code FlowableCaller}.
     *
     * @param runBody                          {@link #getRunBody()}.
     * @param retryTimes                       {@link #getRetryTimes()}.
     * @param retryIntervalMilliseconds        {@link #getRetryIntervalMilliseconds()}.
     * @param whenResponseNonSuccessRetry      {@link #isWhenResponseNonSuccessRetry()}.
     * @param whenResponseNonSuccessFinalThrow {@link #isWhenResponseNonSuccessFinalThrow()}.
     * @param customRetryExceptionPredicate    {@link #getCustomRetryExceptionPredicate()}.
     * @param <R>                              Generic R represents the type returned by an operation, which must
     *                                         inherit from the {@link Response} class.
     * @throws NullPointerException if input runBody is {@literal null}.
     */
    public static <R extends Response> void call(@NotNull Supplier<R> runBody,
                                                 int retryTimes,
                                                 long retryIntervalMilliseconds,
                                                 boolean whenResponseNonSuccessRetry,
                                                 boolean whenResponseNonSuccessFinalThrow,
                                                 @Nullable Predicate<? super Throwable> customRetryExceptionPredicate) {
        call(runBody, retryTimes, retryIntervalMilliseconds,
                whenResponseNonSuccessRetry, whenResponseNonSuccessFinalThrow,
                customRetryExceptionPredicate, null, null);
    }

    /**
     * A static method for SDK calls using the API of {@code FlowableCaller}.
     *
     * @param runBody                             {@link #getRunBody()}.
     * @param retryTimes                          {@link #getRetryTimes()}.
     * @param retryIntervalMilliseconds           {@link #getRetryIntervalMilliseconds()}.
     * @param whenResponseNonSuccessRetry         {@link #isWhenResponseNonSuccessRetry()}.
     * @param whenResponseNonSuccessFinalThrow    {@link #isWhenResponseNonSuccessFinalThrow()}.
     * @param customRetryExceptionPredicate       {@link #getCustomRetryExceptionPredicate()}.
     * @param customSubscriptionRegularConsumer   {@link #customSubscriptionRegularConsumer}.
     * @param customSubscriptionExceptionConsumer {@link #customSubscriptionExceptionConsumer}.
     * @param <R>                                 Generic R represents the type returned by an operation, which must
     *                                            inherit from the {@link Response} class.
     * @throws NullPointerException if input runBody is {@literal null}.
     */
    public static <R extends Response> void call(@NotNull Supplier<R> runBody,
                                                 int retryTimes,
                                                 long retryIntervalMilliseconds,
                                                 boolean whenResponseNonSuccessRetry,
                                                 boolean whenResponseNonSuccessFinalThrow,
                                                 @Nullable Predicate<? super Throwable> customRetryExceptionPredicate,
                                                 @Nullable Consumer<R> customSubscriptionRegularConsumer,
                                                 @Nullable Consumer<Throwable> customSubscriptionExceptionConsumer) {
        new FlowableCaller<>(runBody, retryTimes, retryIntervalMilliseconds,
                whenResponseNonSuccessRetry, whenResponseNonSuccessFinalThrow, customRetryExceptionPredicate,
                customSubscriptionRegularConsumer, customSubscriptionExceptionConsumer).run();
    }

    /* FlowableCaller help class */

    /*** It happened after {@link Flowable#subscribe(io.reactivex.rxjava3.functions.Consumer,
     *  io.reactivex.rxjava3.functions.Consumer)})} onNext.*/
    protected class OnNext implements io.reactivex.rxjava3.functions.Consumer<R> {

        @Override
        public void accept(R r) {
            if (customSubscriptionRegularConsumer != null) {
                customSubscriptionRegularConsumer.accept(r);
            }
        }
    }

    /*** It happened after {@link Flowable#subscribe(io.reactivex.rxjava3.functions.Consumer,
     *  io.reactivex.rxjava3.functions.Consumer)})} onError.*/
    protected class OnError implements io.reactivex.rxjava3.functions.Consumer<Throwable> {

        @Override
        public void accept(Throwable e) {
            if (customSubscriptionExceptionConsumer != null) {
                customSubscriptionExceptionConsumer.accept(e);
            }
        }
    }
}
