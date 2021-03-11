@file:Suppress("unused")

package by.shostko.statushandler.worker

/*
private fun <E> StatusHandler<E>.startWorkAndObserveStatus(lifecycleOwner: LifecycleOwner, callable: () -> UUID) {
    val uuid = callable.invoke()
    Handler(Looper.getMainLooper()).post {
        WorkManager.getInstance().getWorkInfoByIdLiveData(uuid)
            .observe(lifecycleOwner, Observer { info ->
                info?.state?.let {
                    when {
                        !it.isFinished -> updateWorking()
                        it == WorkInfo.State.FAILED -> updateFailed(info.outputData.keyValueMap)
                        else -> updateSuccess()
                    }
                }
            })
    }
}

fun <E, T> StatusHandler<E>.combineWithWorker(lifecycleOwner: LifecycleOwner, callable: () -> UUID): FlowableTransformer<T, T> =
    FlowableTransformer { upstream ->
        Flowable.combineLatest(
            action.startWith(Action.REFRESH)
                .doOnNext { startWorkAndObserveStatus(lifecycleOwner, callable) }
                .ignoreElements()
                .toFlowable<Unit>()
                .startWith(Unit)
                .distinctUntilChanged(),
            upstream, BiFunction { _, t -> t })
    }

fun <E, T> StatusHandler<E>.prepareWithWorker(lifecycleOwner: LifecycleOwner, callable: () -> UUID): FlowableTransformer<T, T> =
    FlowableTransformer { upstream ->
        Flowable.combineLatest(
            action.doOnNext { startWorkAndObserveStatus(lifecycleOwner, callable) }
                .ignoreElements()
                .toFlowable<Unit>()
                .startWith(Unit)
                .distinctUntilChanged(),
            upstream, BiFunction { _, t -> t })
    }*/
// TODO replace with new implementation for workers
// TODO create mb another artifact/module for worker+paging combination