package me.next.nestedscrollviewdemo;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * Created by NeXT on 16/9/1.
 */
public class NestedScrollView extends ScrollView implements NestedScrollingParent {

    private static final String TAG = "NestedScrollView";

    private static int SENSOR_DISTANCE = 0;

    private boolean hasNestedScroll;
    private boolean consumeEvent;

    private int touchSlop;
    private NestedScrollingParentHelper mParentHelper;

    public NestedScrollView(Context paramContext) {
        this(paramContext, null);
    }

    public NestedScrollView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    private void init() {
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        this.mParentHelper = new NestedScrollingParentHelper(this);
        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        SENSOR_DISTANCE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0F, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, TAG + " heightMeasureSpec : " + heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, TAG + " getMeasuredHeight : " + getMeasuredHeight());
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
        return needIntercept;
    }

    /**
     * onStartNestedScroll该方法，一定要按照自己的需求返回true，
     * 该方法决定了当前控件是否能接收到其内部View(非并非是直接子View)滑动时的参数；
     * 假设你只涉及到纵向滑动，这里可以根据nestedScrollAxes这个参数，进行纵向判断。
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
     * @param target
     * @param dx
     * @param dy
     * @param consumed
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);

        Log.d(TAG, "NestedScroll " + "onNestedPreScroll");
    }

    /**
     * onNestedFling你可以捕获对内部View的fling事件，
     * 如果return true则表示拦截掉内部View的事件。
     * @param target
     * @param velocityX
     * @param velocityY
     * @param consumed
     * @return
     */
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {

        Log.d(TAG, "NestedScroll " + "onNestedFling");
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

        Log.d(TAG, "NestedScroll " + "onNestedPreFling");
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    //滑动结束，调用 onStopNestedScroll() 表示本次处理结束
    public void onStopNestedScroll(View target) {

        Log.d(TAG, "onStopNestedScroll onStopNestedScroll onStopNestedScroll");

        hasNestedScroll = false;
        consumeEvent = false;
        this.mParentHelper.onStopNestedScroll(target);
    }

}
