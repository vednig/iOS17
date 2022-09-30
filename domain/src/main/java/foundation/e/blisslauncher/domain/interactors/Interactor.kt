package foundation.e.blisslauncher.domain.interactors

import foundation.e.blisslauncher.domain.executors.PostExecutionThread
import foundation.e.blisslauncher.domain.executors.ThreadExecutor
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

interface Interactor<in P> {
    val threadExecutor: ThreadExecutor
    val postExecutionThread: PostExecutionThread
}

abstract class FlowableInteractor<in P, T> : Interactor<P>, Disposable {

    private val disposables: CompositeDisposable = CompositeDisposable()

    protected abstract fun buildObservable(params: P? = null): Flowable<T>

    operator fun invoke(params: P, onNext: (next: T) -> Unit = {}, onComplete: () -> Unit = {}) {
        disposables +=
            this.buildObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler)
                .subscribe(onNext, Timber::w, onComplete)
    }

    override fun dispose() = disposables.dispose()

    override fun isDisposed(): Boolean = disposables.isDisposed
}

abstract class CompletableInteractor<in P> : Interactor<P>, Disposable {

    private val disposables: CompositeDisposable = CompositeDisposable()

    protected abstract fun buildObservable(params: P? = null): Completable

    operator fun invoke(params: P, onComplete: () -> Unit = {}) {
        disposables +=
            this.buildObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler)
                .subscribe(onComplete, Timber::w)
    }

    override fun dispose() = disposables.dispose()

    override fun isDisposed(): Boolean = disposables.isDisposed
}
