package foundation.e.blisslauncher.core.mvp;

public interface MvpContract {

    interface View {
    }

    interface Presenter<V extends View> {
        void attachView(V view);

        void resume();

        void pause();

        void detachView();
    }
}
