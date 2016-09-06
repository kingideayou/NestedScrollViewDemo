package me.next.nestedscrollviewdemo;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by NeXT on 16/9/1.
 */
public class NestedScrollView extends ScrollView implements NestedScrollingParent, ScrollStateChangedListener {

    private static final String TAG = "NestedScrollView";

    private static final int WEBVIEW_CONTENT_HEIGHT = 23859;
    private static int SENSOR_DISTANCE = 0;

    private boolean hasNestedScroll;
    private boolean consumeEvent;
    private boolean firstInitSize = true;

    private int touchSlop;
    private int direction = 0;
    private boolean isTouchUp;
    private float lastY;

    private int scrollViewMeasureHeight = 0;

    private List<View> scrollingChildList;
    private ScrollState mScrollState;
    private NestedScrollingParentHelper mParentHelper;

    private int childMeasureHeight = 0;

    public NestedScrollView(Context paramContext) {
        this(paramContext, null);
    }

    public NestedScrollView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    private void init() {

        mScrollState = ScrollState.TOP;

        setOverScrollMode(View.OVER_SCROLL_NEVER);
        this.mParentHelper = new NestedScrollingParentHelper(this);
        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        SENSOR_DISTANCE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0F, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, TAG + " heightMeasureSpec : " + heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        scrollViewMeasureHeight = getMeasuredHeight();

        childMeasureHeight = ((ViewGroup)getChildAt(0)).getChildAt(1).getMeasuredHeight();
        Log.e(TAG, TAG + " childMeasureHeight : " + childMeasureHeight);

        Log.e(TAG, TAG + " getMeasuredHeight : " + getMeasuredHeight());
        setNestedScrollViewHeight();
    }


    private void stopScrolling() {
        this.smoothScrollBy(0, 0);
        this.smoothScrollBy(0, 0);
    }

    private void consumeEvent(int dx, int dy, int[] consumed) {
        this.scrollBy(dx, dy);
        consumed[0] = 0;
        consumed[1] = dy;
        this.consumeEvent = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        stopScrolling();
        boolean needIntercept = super.onInterceptTouchEvent(ev);
        if (hasNestedScroll) {
            needIntercept = false;//将事件发放给 childView
        }
        setTouchState(ev);
        return needIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        setTouchState(ev);
        return super.onTouchEvent(ev);
    }

    private void setTouchState(MotionEvent ev) {
        this.isTouchUp = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                stopScrolling();
                this.isTouchUp = false;
                this.lastY = ev.getY();
                this.direction = 0;
            }
            break;
            case MotionEvent.ACTION_MOVE:
                float eventY = ev.getY();
                int deltaY = (int) (this.lastY - eventY);
                if (deltaY != 0) {
                    this.direction = DirectionDetector.getDirection(deltaY, true);
                }
                this.lastY = eventY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.isTouchUp = true;
                break;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.e(TAG, "scrollChildTop : " + scrollingChildList.get(0).getTop());
        direction = DirectionDetector.getDirection(t - oldt, true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (firstInitSize) {
            Log.e(TAG, "scrollChildTop : " + scrollingChildList.get(0).getTop());
//            setNestedScrollViewHeight();
            firstInitSize = false;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        analyNestedScrollingChildViews();
    }

    private void setNestedScrollViewHeight() {
        Iterator iterator = scrollingChildList.iterator();
        while(iterator.hasNext()) {
            Object childView = iterator.next();
            ViewGroup.LayoutParams params = ((View) childView).getLayoutParams();
            if(params.height != -1 && params.height != -2) {
                continue;
            }
            params.height = getMeasuredHeight();
            ((View)childView).setLayoutParams(params);
        }
    }

    private void analyNestedScrollingChildViews() {
        View childView = this.getChildAt(0); //LinearLayout
        if(childView != null && ((childView instanceof ViewGroup))) {
            this.scrollingChildList = new ArrayList();
            int childCount = ((ViewGroup) childView).getChildCount();
            for(int childIndex = 0; childIndex < childCount; ++childIndex) {
                View view = ((ViewGroup)childView).getChildAt(childIndex);
                if((view instanceof NestedScrollingChild)) {
                    scrollingChildList.add(view);
                }
            }
        }
    }

    /**
     * onStartNestedScroll该方法，一定要按照自己的需求返回true，
     * 该方法决定了当前控件是否能接收到其内部View(非并非是直接子View)滑动时的参数；
     * 假设你只涉及到纵向滑动，这里可以根据nestedScrollAxes这个参数，进行纵向判断。
     *
     * @param child
     * @param target
     * @param nestedScrollAxes
     * @return
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    /**
     * 从上面的 Child 分析可知，滑动开始的调用 startNestedScroll()，
     * Parent 收到 onStartNestedScroll() 回调，决定是否需要配合 Child 一起进行处理滑动，
     * 如果需要配合，还会回调 onNestedScrollAccepted()。
     *
     * @param child
     * @param target
     * @param axes
     */
    public void onNestedScrollAccepted(View child, View target, int axes) {
        Log.d(TAG, "onNestedScrollAccepted onNestedScrollAccepted onNestedScrollAccepted");
        hasNestedScroll = true;
        consumeEvent = false;
        this.mParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        Log.d(TAG, "NestedScroll " + "onNestedScroll");
    }

    /**
     * onNestedPreScroll该方法的会传入内部View移动的dx,dy，
     * 如果你需要消耗一定的dx,dy，就通过最后一个参数consumed进行指定，
     * 例如我要消耗一半的dy，就可以写consumed[1]=dy/2
     *
     * @param target
     * @param dx
     * @param dy
     * @param consumed
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        Log.d(TAG, "onNestedPreScroll run");

        /**
         * ScrollY 为 ScrollView 滑动的距离
         * 与 WebView match_parent 高度比较
         */
        int scrollY = getScrollY();
        Log.d(TAG, "onNestedPreScroll " + "scrollY : " + scrollY);
        Log.d(TAG, "onNestedPreScroll " + "dy : " + dy);
        Log.d(TAG, "onNestedPreScroll " + "direction : " + direction);
        Log.d(TAG, "onNestedPreScroll " + "mScrollState : " + mScrollState);

        //direction = 1 上拉
        if (direction == 1 && mScrollState == ScrollStateChangedListener.ScrollState.BOTTOM) {
            consumeEvent(dx, dy, consumed);
        }
        //direction = 2 -> dy < 0
        if (direction != 1) {
            Log.d(TAG, "onNestedPreScroll " + "scrollViewMeasureHeight : " + scrollViewMeasureHeight);
            if (dy < 0) { //下拉
                if (scrollY != 0) {
                    consumeEvent(dx, dy, consumed);
                }
            }
        }

        /*
        if (scrollY < WEBVIEW_CONTENT_HEIGHT) {
            if ((direction == 1) && (WEBVIEW_CONTENT_HEIGHT != -1) && (scrollY + dy > WEBVIEW_CONTENT_HEIGHT)) {
                dy = WEBVIEW_CONTENT_HEIGHT - scrollY;
            }
            consumeEvent(dx, dy, consumed);
            return;
        }

        if (scrollY == WEBVIEW_CONTENT_HEIGHT && direction == 1 && WEBVIEW_CONTENT_HEIGHT != -1
                && !ViewCompat.canScrollVertically(target, 1)) {
            consumeEvent(dx, dy, consumed);
            return;
        }

        if (scrollY <= WEBVIEW_CONTENT_HEIGHT)
            return;
        if ((this.direction == 2) && (WEBVIEW_CONTENT_HEIGHT != -1) && (scrollY + dy < WEBVIEW_CONTENT_HEIGHT)) {
            dy = WEBVIEW_CONTENT_HEIGHT - scrollY;
        }
        consumeEvent(dx, dy, consumed);
        */
    }

    /**
     * onNestedFling你可以捕获对内部View的fling事件，
     * 如果return true则表示拦截掉内部View的事件。
     *
     * @param target
     * @param velocityX
     * @param velocityY
     * @param consumed
     * @return
     */
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {

        Log.d(TAG, "NestedScroll " + "onNestedFling : velocityY = " + velocityY + " " + consumed);

        boolean interceptEvent = true;
        if (!consumed) {
            this.fling(((int) velocityY));
            this.consumeEvent = true;
        } else {
            interceptEvent = false;
        }

        return interceptEvent;

//        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    /**
     * https://developer.android.com/reference/android/support/v4/view/NestedScrollingParent.html#onNestedPreFling(android.view.View, float, float)
     * By returning true from this method,
     * the parent indicates that the child should not fling its own internal content as well.
     */
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

//        this.fling(((int) velocityY));
//        return super.onNestedPreFling(target, velocityX, velocityY);
        boolean consumeFling = true;
        int scrollY = getScrollY();
        Log.d(TAG, "NestedScroll " + "onNestedPreFling scrollY : " + scrollY);
        if (scrollY < childMeasureHeight) {
            fling(((int) velocityY));
            consumeEvent = true;
        } else if (scrollY > childMeasureHeight) {
            fling(((int) velocityY));
            consumeEvent = true;
        } else {
            consumeFling = false;
        }
        return consumeFling;
    }

    //滑动结束，调用 onStopNestedScroll() 表示本次处理结束
    public void onStopNestedScroll(View target) {

        Log.d(TAG, "onStopNestedScroll onStopNestedScroll onStopNestedScroll");

        hasNestedScroll = false;
        consumeEvent = false;
        this.mParentHelper.onStopNestedScroll(target);
    }

    @Override
    public void onChildDirectionChange(int orientation) {
        this.direction = orientation;
        Log.e(TAG, "onChildDirectionChange = " + orientation);
    }

    @Override
    public void onChildPositionChange(ScrollState scrollState) {
        this.mScrollState = scrollState;
    }
}
