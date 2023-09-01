package com.cloudx.ios17.core.mvp;

public class MvpPresenter<V extends MvpContract.View> implements MvpContract.Presenter<V> {

    private V mView;
    private boolean isPaused;

    @Override
    public void attachView(V view) {
        this.mView = view;
    }

    @Override
    public void resume() {
        isPaused = false;
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void detachView() {
        this.mView = null;
    }

    public V getView() {
        return mView;
    }

    public boolean isViewAttached() {
        return this.mView != null;
    }

    public void checkViewAttached() {
        if (!isViewAttached())
            throw new ViewNotAttachedException();
    }

    public static class ViewNotAttachedException extends RuntimeException {
        public ViewNotAttachedException() {
            super("Call Presenter.attachView(BaseView)before" + " requesting data to the Presenter");
        }
    }
}
