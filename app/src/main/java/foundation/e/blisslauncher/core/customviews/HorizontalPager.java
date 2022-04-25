package foundation.e.blisslauncher.core.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

import androidx.core.view.GestureDetectorCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.features.launcher.DetectSwipeGestureListener;
import foundation.e.blisslauncher.features.launcher.LauncherActivity;
import foundation.e.blisslauncher.features.launcher.OnSwipeDownListener;

public class HorizontalPager extends ViewGroup implements Insettable {
    private static final String TAG = "HorizontalPager";
    private static final int INVALID_SCREEN = -1;
    public static final int SPEC_UNDEFINED = -1;

    private static final int SNAP_VELOCITY = 1000;

    private int pageWidthSpec, pageWidth;

    private boolean firstLayout = true;

    private int currentPage;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private int mTouchSlop;
    private int mMaximumVelocity;

    private float mLastMotionX;
    private float mLastMotionY;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_HORIZONTAL_SCROLLING = 1;
    private final static int TOUCH_STATE_VERTICAL_SCROLLING = 2;

    private int mTouchState = TOUCH_STATE_REST;

    private boolean mAllowLongPress;
    private DockGridLayout mDock;

    private Set<OnScrollListener> mListeners = new HashSet<>();
    private boolean mIsUiCreated;
    private GestureDetectorCompat gestureDetectorCompat;
    private Rect insets;
    private float mLastMotionRawY;

    public HorizontalPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalPager);
        pageWidthSpec = a.getDimensionPixelSize(R.styleable.HorizontalPager_pageWidth,
                SPEC_UNDEFINED);
        a.recycle();

        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        currentPage = 0;
        mIsUiCreated = false;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        // Create a common gesture listener object.
        DetectSwipeGestureListener gestureListener = new DetectSwipeGestureListener();

        // Set activity in the listener.
        if (getContext() instanceof LauncherActivity) {
            gestureListener.setListener((OnSwipeDownListener) getContext());
        }

        gestureDetectorCompat = new GestureDetectorCompat(getContext(), gestureListener);
    }

    public void setDock(DockGridLayout dock) {
        mDock = dock;
    }

    int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(0, Math.min(currentPage, getChildCount()));
        scrollTo(getScrollXForPage(this.currentPage), 0);
        invalidate();
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidthSpec = pageWidth;
    }

    /**
     * Gets the value that getScrollX() should return if the specified page is the current page (and
     * no other scrolling is occurring).
     * Use this to pass a value to scrollTo(), for example.
     */
    private int getScrollXForPage(int whichPage) {
        return (whichPage * pageWidth) - pageWidthPadding();
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
            postInvalidateOnAnimation();
            return;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        final long drawingTime = getDrawingTime();
        // todo be smarter about which children need drawing
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawChild(canvas, getChildAt(i), drawingTime);
        }

        for (OnScrollListener mListener : mListeners) {
            int adjustedScrollX = getScrollX() + pageWidthPadding();
            if (adjustedScrollX % pageWidth == 0) {
                mListener.onViewScrollFinished(adjustedScrollX / pageWidth);
            } else {
                mListener.onScroll(adjustedScrollX);
            }
        }
    }

    int pageWidthPadding() {
        return ((getMeasuredWidth() - pageWidth) / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        pageWidth = pageWidthSpec == SPEC_UNDEFINED ? getMeasuredWidth() : pageWidthSpec;
        pageWidth = Math.min(pageWidth, getMeasuredWidth());
        int pageHeight = MeasureSpec.getSize(heightMeasureSpec);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            int childWidth = pageWidth;
            int childHeight = pageHeight;
            View child = getChildAt(i);
            if (!(child instanceof Insettable)) {
                childWidth -= insets.width();
                childHeight -= insets.height();
            }
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
        }

        if (firstLayout) {
            scrollTo(getScrollXForPage(currentPage), 0);
            firstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int childWidth = child.getMeasuredWidth();
                int childTop = 0;
                if (!(child instanceof Insettable)) {
                    childLeft += insets.left;
                    childTop += insets.top;
                }
                child.layout(childLeft, childTop, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
                if (!(child instanceof Insettable)) {
                    childLeft += insets.right;
                }
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        return screen != currentPage || !mScroller.isFinished();
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (getChildCount() <= 1) {
            return false;
        }
        int focusableScreen;
        focusableScreen = currentPage;
        getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentPage() > 0) {
                snapToPage(getCurrentPage() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentPage() < getChildCount() - 1) {
                snapToPage(getCurrentPage() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction) {
        getChildAt(currentPage).addFocusables(views, direction);
        if (direction == View.FOCUS_LEFT) {
            if (currentPage > 0) {
                getChildAt(currentPage - 1).addFocusables(views, direction);
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (currentPage < getChildCount() - 1) {
                getChildAt(currentPage + 1).addFocusables(views, direction);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Log.d(TAG, "onInterceptTouchEvent::action=" + ev.getAction());

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            Log.d(TAG, "onInterceptTouchEvent::shortcut=true");
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        mLastMotionRawY = ev.getRawY();


        switch (action) {
            case MotionEvent.ACTION_MOVE:
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */
                if (mTouchState == TOUCH_STATE_REST) {
                    checkStartScroll(x, y);
                }

                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mAllowLongPress = true;

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_HORIZONTAL_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }

    private void checkStartScroll(float x, float y) {
        /*
         * Locally do absolute value. mLastMotionX is set to the y value
         * of the down event.
         */
        final int xDiff = (int) Math.abs(x - mLastMotionX);
        final int yDiff = (int) Math.abs(y - mLastMotionY);

        boolean xMoved = xDiff > mTouchSlop;
        boolean yMoved = yDiff > mTouchSlop;

        if (xMoved || yMoved) {

            if (yMoved && (y - mLastMotionY) > 0 && yDiff > xDiff && inThresholdRegion() && currentPage != 0) {
                mTouchState = TOUCH_STATE_VERTICAL_SCROLLING;
                ((OnSwipeDownListener) getContext()).onSwipeStart();
            } else if (xMoved && yDiff < xDiff) {
                // Scroll if the user moved far enough along the X axis
                mTouchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
                enableChildrenCache();
            }
            // Either way, cancel any pending longpress
            if (mAllowLongPress) {
                mAllowLongPress = false;
                // Try canceling the long press. It could also have been scheduled
                // by a distant descendant, so use the mAllowLongPress flag to block
                // everything
                final View currentScreen = getChildAt(currentPage);
                if (currentScreen != null) {
                    currentScreen.cancelLongPress();
                }
            }
        }
    }

    private boolean inThresholdRegion() {
        return (mLastMotionRawY / BlissLauncher.getApplication(getContext()).getDeviceProfile().availableHeightPx) > (float) 1 / 5;
    }

    void enableChildrenCache() {
        setChildrenDrawingCacheEnabled(true);
    }

    public void setUiCreated(boolean isUiCreated) {
        mIsUiCreated = isUiCreated;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*if (gestureDetectorCompat.onTouchEvent(ev)) {
            return true;
        } else {

        }*/
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        if (mIsUiCreated) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    /*
                     * If being flinged and user touches, stop the fling. isFinished
                     * will be false if being flinged.
                     */
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }

                    // Remember where the motion event started
                    mLastMotionX = x;
                    mLastMotionY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mTouchState == TOUCH_STATE_REST) {
                        checkStartScroll(x, y);
                    } else if (mTouchState == TOUCH_STATE_VERTICAL_SCROLLING) {
                        int diffY = (int) (y - mLastMotionY);
                        ((OnSwipeDownListener) getContext()).onSwipe(diffY);
                    } else if (mTouchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                        // Scroll to follow the motion event
                        int deltaX = (int) (mLastMotionX - x);
                        mLastMotionX = x;

                        // Apply friction to scrolling past boundaries.
                        if (getScrollX() < 0 || getScrollX() > getChildAt(
                                getChildCount() - 1).getLeft()) {
                            deltaX /= 2;
                        }

                        scrollBy(deltaX, 0);
                        /*if ((currentPage == 0 && deltaX > 0) || (currentPage == 1 && deltaX <
                        0)) {
                            Log.i(TAG, "onTouchEvent: "+getChildAt(currentPage).getLeft());
                            mDock.setTranslationX(getChildAt(currentPage).getLeft());
                        }*/
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTouchState == TOUCH_STATE_VERTICAL_SCROLLING) {
                        ((OnSwipeDownListener) getContext()).onSwipeFinish();
                    }
                    if (mTouchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int velocityX = (int) velocityTracker.getXVelocity();

                        if (velocityX > SNAP_VELOCITY && currentPage > 0) {
                            // Fling hard enough to move left
                            snapToPage(currentPage - 1);
                        } else if (velocityX < -SNAP_VELOCITY
                                && currentPage < getChildCount() - 1) {
                            // Fling hard enough to move right
                            snapToPage(currentPage + 1);
                        } else {
                            snapToDestination();
                        }

                        if (mVelocityTracker != null) {
                            mVelocityTracker.recycle();
                            mVelocityTracker = null;
                        }
                    }
                    mTouchState = TOUCH_STATE_REST;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mTouchState = TOUCH_STATE_REST;
            }
        }

        return true;
    }

    private void snapToDestination() {
        final int startX = getScrollXForPage(currentPage);
        int whichPage = currentPage;
        if (getScrollX() < startX - getWidth() / 8) {
            whichPage = Math.max(0, whichPage - 1);
        } else if (getScrollX() > startX + getWidth() / 8) {
            whichPage = Math.min(getChildCount() - 1, whichPage + 1);
        }
        snapToPage(whichPage);
    }

    public void snapToPage(int whichPage, int duration) {
        enableChildrenCache();

        boolean changingPages = whichPage != currentPage;
        currentPage = whichPage;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && changingPages && focusedChild == getChildAt(currentPage)) {
            focusedChild.clearFocus();
        }

        final int newX = getScrollXForPage(whichPage);
        final int delta = newX - getScrollX();
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        invalidate();
    }

    public void snapToPage(int whichPage) {
        snapToPage(whichPage, 400);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = currentPage;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != INVALID_SCREEN) {
            currentPage = savedState.currentScreen;
        }
    }

    public void scrollLeft(int duration) {
        if (currentPage > 0 && mScroller.isFinished()) {
            snapToPage(currentPage - 1, duration);
        }
    }

    public void scrollRight(int duration) {
        if (currentPage < getChildCount() - 1
                && mScroller.isFinished()) {
            snapToPage(currentPage + 1, duration);
        }
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }

    @Override
    public void setInsets(Rect insets) {
        if (insets == null) return;
        updateInsetsForChildren();
        this.insets = insets;
        postInvalidate();
    }

    private void updateInsetsForChildren() {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; ++index) {
            View child = getChildAt(index);
            if (child instanceof Insettable) {
                Log.d(TAG, "child is instance of insettable");
                ((Insettable) child).setInsets(insets);
            }
        }
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        Log.d(TAG, "onViewAdded() called with: child = [" + child + "]");
        if (child instanceof Insettable) {
            Log.d(TAG, "child is instance of insettable");
            ((Insettable) child).setInsets(insets);
        }
    }

    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    public void addOnScrollListener(OnScrollListener listener) {
        mListeners.add(listener);
    }

    public void removeOnScrollListener(OnScrollListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Implement to receive events on scroll position and page snaps.
     */
    public interface OnScrollListener {
        /**
         * Receives the current scroll X value.  This value will be adjusted to assume the left edge
         * of the first
         * page has a scroll position of 0.  Note that values less than 0 and greater than the right
         * edge of the
         * last page are possible due to touch events scrolling beyond the edges.
         *
         * @param scrollX Scroll X value
         */
        void onScroll(int scrollX);

        /**
         * Invoked when scrolling is finished (settled on a page, centered).
         *
         * @param currentPage The current page
         */
        void onViewScrollFinished(int currentPage);
    }
}
