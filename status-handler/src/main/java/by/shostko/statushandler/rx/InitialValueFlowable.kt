@file:Suppress("unused")

package by.shostko.statushandler.rx

import io.reactivex.Flowable
import io.reactivex.FlowableSubscriber
import io.reactivex.internal.subscriptions.SubscriptionHelper
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

abstract class InitialValueFlowable<T> : Flowable<T>() {

    protected abstract val initialValue: T?

    abstract fun skipInitialValue(): Flowable<T>

    internal abstract class WithListener<T> : InitialValueFlowable<T>() {

        final override fun subscribeActual(subscriber: Subscriber<in T>) {
            val listener = createListener { subscriber.onNext(it) }
            subscriber.onSubscribe(InternalSubscriptionWithInitial(subscriber, listener))
        }

        protected abstract fun createListener(onNextValue: (T) -> Unit): Listener

        override fun skipInitialValue(): Flowable<T> = Skipped()

        protected interface Listener {
            fun addListener()
            fun removeListener()
        }

        private inner class Skipped : Flowable<T>() {
            override fun subscribeActual(subscriber: Subscriber<in T>) {
                val listener = createListener { subscriber.onNext(it) }
                subscriber.onSubscribe(InternalSubscription(listener))
            }
        }

        private open class InternalSubscription(
            private val listener: Listener
        ) : Subscription {

            override fun request(n: Long) {
                listener.addListener()
            }

            override fun cancel() {
                listener.removeListener()
            }
        }

        private inner class InternalSubscriptionWithInitial(
            private val downstream: Subscriber<in T>,
            listener: Listener
        ) : InternalSubscription(listener) {

            override fun request(n: Long) {
                val value = initialValue
                if (value == null) {
                    super.request(n)
                } else {
                    downstream.onNext(value)
                    if (n > 1) {
                        super.request(n)
                    }
                }
            }
        }
    }

    internal abstract class WithSource<T>(
        private val source: Flowable<T>
    ) : InitialValueFlowable<T>() {

        override fun subscribeActual(subscriber: Subscriber<in T>) {
            source.subscribe(InternalSubscription(subscriber))
        }

        override fun skipInitialValue(): Flowable<T> = source

        private inner class InternalSubscription(
            private val downstream: Subscriber<in T>
        ) : Subscription, FlowableSubscriber<T> {

            private var upstream: Subscription? = null

            override fun cancel() {
                upstream?.cancel()
            }

            override fun request(n: Long) {
                val value = initialValue
                if (value == null) {
                    upstream?.request(n)
                } else {
                    downstream.onNext(value)
                    if (n > 1) {
                        upstream?.request(n - 1)
                    }
                }
            }

            override fun onComplete() {
                downstream.onComplete()
            }

            override fun onSubscribe(s: Subscription) {
                if (SubscriptionHelper.validate(upstream, s)) {
                    upstream = s
                    downstream.onSubscribe(this)
                }
            }

            override fun onNext(t: T) {
                downstream.onNext(t)
            }

            override fun onError(t: Throwable?) {
                downstream.onError(t)
            }
        }
    }
}