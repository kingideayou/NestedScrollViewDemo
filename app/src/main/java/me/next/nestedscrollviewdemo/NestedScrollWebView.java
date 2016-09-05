package me.next.nestedscrollviewdemo;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * Created by NeXT on 16/9/1.
 */
public class NestedScrollWebView extends WebView implements NestedScrollingChild {

    private static final String TAG = "NestedScrollWebView";
    private static final int INVALID_POINTER = -1;
    public ScrollStateChangedListener.ScrollState position = ScrollStateChangedListener.ScrollState.TOP;

    private int mTouchSlop;
    private int mActivePointerId = INVALID_POINTER;

    private NestedScrollingChildHelper mChildHelper;

    private int consumedY;
    private int webviewHeight; //1692
    private int contentHeight; //23859

    private float density;

    private boolean isScrollUp = true;//上滑

    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    private ScrollStateChangedListener scrollStateChangedListener;

    public NestedScrollWebView(Context context) {
        this(context, null);
    }

    public NestedScrollWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        his.mScrollOffset = new int[2];
//        this.mScrollConsumed = new int[2];
        this.mIsBeingDrag = false;
        this.contentHeight = -1;
        this.webviewHeight = -1;
//        this.direction = 0;
        this.position = ScrollStateChangedListener.ScrollState.TOP;
//        this.mScrollPointerId = -1;

        getSettings().setJavaScriptEnabled(true);
        setWebViewClient(new WebViewClient());
        addJavascriptInterface(new JSGetContentHeight(), "InjectedObject");

        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.getContext());
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        int touchSlop = viewConfiguration.getScaledTouchSlop();
//        directionDetector = new j();
        density = this.getResources().getDisplayMetrics().density;
        setOverScrollMode(ViewCompat.OVER_SCROLL_NEVER);
//        settings = this.getSettings();
//        addJSInterface(new JSGetContentHeight(this, null), "InjectedObject", 99);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                        /*
                        me.next.nestedscrollviewdemo E/NestedScrollWebView: NestedScrollWebView getMeasuredHeight : 12381
                        me.next.nestedscrollviewdemo E/NestedScrollView: NestedScrollView getMeasuredHeight : 732
                         */
                ViewGroup.LayoutParams localLayoutParams = NestedScrollWebView.this.getLayoutParams();
                //1692
                //732
                localLayoutParams.height = 732;
                setLayoutParams(localLayoutParams);
            }
        }, 1000);
    }

    private float mLastMotionY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private boolean mIsBeingDrag = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {

                Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN");

                mActivePointerId = MotionEventCompat.getPointerId(event, MotionEvent.ACTION_DOWN);
                final float initialDownY = getMotionEventY(event, mActivePointerId);

                Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN initialDownY : " + initialDownY);

                if (initialDownY == -1) {
                    return false;
                }
                mLastMotionY = initialDownY;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                super.onTouchEvent(event);
                mIsBeingDrag = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                Log.e(TAG, "onInterceptTouchEvent ACTION_MOVE");

                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(event, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                int deltaY = (int) (mLastMotionY - y);
                if (scrollStateChangedListener != null) {
                    scrollStateChangedListener.onChildDirectionChange(DirectionDetector.getDirection(deltaY, true));
                }
                mLastMotionY = y;
                if (Math.abs(deltaY) >= mTouchSlop) {
                    mIsBeingDrag = true;
                }
                Log.e(TAG, "mIsBeingDrag : " + mIsBeingDrag);
                if (mIsBeingDrag) {
                    ViewParent viewParent = this.getParent();
                    if (viewParent != null) {
                        viewParent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (mIsBeingDrag) {
                    setLongClickable(false);
                    setJavaScriptEnable(false);
                }
                if (mIsBeingDrag && dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    Log.e(TAG, "dispatchNestedPreScroll run ");
                    mLastMotionY -= mScrollOffset[1];
                    deltaY -= mScrollConsumed[1];
                    event.offsetLocation(0, mScrollConsumed[1]);
                    if (dispatchNestedScroll(0, 0, 0, deltaY, mScrollOffset)) {
                        mLastMotionY -= mScrollOffset[1];
                        event.offsetLocation(0, mScrollOffset[1]);
                    }
                    return false;
                } else {
                    Log.e(TAG, "dispatchNestedPreScroll does not run");
                    return super.onTouchEvent(event);
                }
            }
            case MotionEvent.ACTION_CANCEL:
                endTouch();
                break;
            case MotionEvent.ACTION_UP: {
                if (mIsBeingDrag && position == ScrollStateChangedListener.ScrollState.BOTTOM) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float)mMaximumVelocity);
                    Log.e(TAG, "ACTION_UP" + " mMaximumVelocity : " + mMaximumVelocity);

                    Log.e(TAG, "ACTION_UP === " + velocityTracker.getYVelocity(mActivePointerId));

                    int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker, mActivePointerId);
                    Log.e(TAG, "ACTION_UP" + " initialVelocity : " + initialVelocity + " === mMinimumVelocity : " + mMinimumVelocity);
                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        flingWithNestedDispatch(-initialVelocity);
                    }
                }

                Log.e(TAG, "onInterceptTouchEvent ACTION_UP");
                endTouch();
                mActivePointerId = INVALID_POINTER;
                mIsBeingDrag = false;
                return super.onTouchEvent(event);
            }
        }

        return super.onTouchEvent(event);
//        if (this.position == ScrollStateChangedListener.ScrollState.MIDDLE) {
        /*
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
//                    this.mIsBeingDragged = false;
                    this.mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                    this.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    break;
                }
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_CANCEL: {
                    this.endTouch();
                    break;
                }
            }
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {// 4.x WebView 不能滑动
//                requestDisallowInterceptTouchEvent(true);
//            }
            super.onTouchEvent(event);
            return true;
//        }
          */

//        MotionEvent tempMotionEvent = MotionEvent.obtain(event);
//        return super.onTouchEvent(event);
    }

    private void endTouch() {
        setJavaScriptEnable(true);
        this.mIsBeingDrag = false;
        this.mActivePointerId = -1;
        recycleVelocityTracker();
        stopNestedScroll();
    }

    @Override
    public void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        Log.d(TAG, "onScrollChanged yyyyyy : " + y);
        if (y <= 0) {
            this.position = ScrollStateChangedListener.ScrollState.TOP;
            Log.d(TAG, "onScrollChanged position : " + position);
            return;
        }
        this.consumedY = (y - oldy);// <0：下滑 >0：上拉
        if (consumedY > 0) {
            isScrollUp = true;
            Log.e(TAG, "onScrollChanged onScrollChanged : webviewHeight == " + this.webviewHeight + "  contentHeight == " + this.contentHeight);
//            if (y + this.webviewHeight >= this.contentHeight) {
            if (y + this.webviewHeight >= contentHeight) {//23796
                this.position = ScrollStateChangedListener.ScrollState.BOTTOM;
            } else {
                this.position = ScrollStateChangedListener.ScrollState.MIDDLE;
            }
        } else {
            isScrollUp = false;
            position = ScrollStateChangedListener.ScrollState.MIDDLE;
        }

        Log.d(TAG, "onScrollChanged position : " + position);

        if (null != scrollStateChangedListener) {
            scrollStateChangedListener.onChildPositionChange(position);
            scrollStateChangedListener.onChildDirectionChange(position.ordinal());
        }
        /*
        if (this.onScrollChangeListener != null) {
            this.onScrollChangeListener.onScrollChanged(x, y, oldx, oldy, this.position);
        }
        */
    }

    private void recycleVelocityTracker() {
        if(this.mVelocityTracker != null) {
            this.mVelocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        this.webviewHeight = h;
        //会调用多次
        Log.e(TAG, "onSizeChanged: " + h + " contentHeight : " + h);//22686
        if (this.contentHeight < 1) {
            this.setContentHeight(webviewHeight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, TAG + " heightMeasureSpec : " + heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, TAG + " getMeasuredHeight : " + getMeasuredHeight()); // 1692
        int measureHeight = getMeasuredHeight();
        if (measureHeight > 0) {
            setContentHeight(measureHeight);
        }
    }


    public void invalidate() {
        super.invalidate();
        contentHeight = ((int) ((((float) getContentHeight())) * this.density));
//        Log.e(TAG, "invalidate contentHeight ------------ " + contentHeight);
        if (contentHeight != 0) {
            loadUrl("javascript:window.InjectedObject.getContentHeight(document.getElementsByTagName('html')[0].scrollHeight)");
        }
    }

    private void setJavaScriptEnable(boolean enable) {
        if(this.getSettings().getJavaScriptEnabled() != enable) {
            this.getSettings().setJavaScriptEnabled(enable);
        }
    }

    public void computeScroll() {
        if (this.position == ScrollStateChangedListener.ScrollState.MIDDLE) {
            super.computeScroll();
        }
    }

    private void flingWithNestedDispatch(int velocityY) {
        if(!dispatchNestedPreFling(0f, ((float)velocityY))) {
            Log.e(TAG, "dispatchNestedPreFling : velocityY : " + velocityY);
        }
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getEmbeddedParent(this);
    }

    private void getEmbeddedParent(View view) {
        ViewParent viewParent = view.getParent();
        if (viewParent != null) {
            if ((viewParent instanceof NestedScrollingParent)) {
                setScrollStateChangedListener((ScrollStateChangedListener) viewParent);
            } else if ((viewParent instanceof ViewGroup)) {
                getEmbeddedParent(((View) viewParent));
            }
        }
    }

    private class JSGetContentHeight {

        @JavascriptInterface
        public void getContentHeight(int paramInt) {
            int viewHeight = NestedScrollWebView.this.getHeight();
//      if (EmbeddedWebView.this.originHeight == 0)
//        EmbeddedWebView.access$102(EmbeddedWebView.this, viewHeight);
//            Log.e(TAG, "JS getContentHeight contentHeight " + paramInt +
//                    " viewHeight = " + viewHeight);
            int webContentHeight = (int) (paramInt * density);
            setContentHeight(webContentHeight);
//            if(webContentHeight>0 && viewHeight>0){
//                if (webContentHeight>viewHeight){
//                    webContentHeight = viewHeight;
//                }
//                final int finalWebContentHeight = webContentHeight;
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        ViewGroup.LayoutParams localLayoutParams = GoldWebView.this.getLayoutParams();
//                        localLayoutParams.height = finalWebContentHeight;
//                        setLayoutParams(localLayoutParams);
//                        AppLogger.i( "resize height = " + localLayoutParams.height);
//                    }
//                });
//            }

//            int j = (int) (paramInt * GoldWebView.this.density);
//
//            int k = 0;
//            if ((viewHeight > 0) && (j > 0)) {
//                if (viewHeight - j <= 200) {
//                    k = 0;
//                    if (j > viewHeight) {
//                        k = j;
//                    }
//                } else {
//                    k = j + 100;
//                }
//
//            }
//            if (k > GoldWebView.this.originHeight)
//                k = GoldWebView.this.originHeight;
//            if ((k > 0) && (k != viewHeight)) {
//                final int m = k;
//                GoldWebView.this.post(new Runnable() {
//                    public void run() {
//                        ViewGroup.LayoutParams localLayoutParams = GoldWebView.this.getLayoutParams();
//                        localLayoutParams.height = m;
//                        Log.viewHeight(GoldWebView.TAG, "resize height = " + localLayoutParams.height);
//                        GoldWebView.this.setLayoutParams(localLayoutParams);
//                    }
//                });
//            }
        }
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        public WebViewClient() {
            super();
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.postDelayed(new Runnable() {
                public void run() {
                    loadUrl("javascript:window.InjectedObject.getContentHeight(document.getElementsByTagName('html')[0].scrollHeight)");
                    /*
                    if(this.get != null) {
                        Context v0 = this.val$view.getContext();
                        if(v0 != null) {
                            if(((v0 instanceof com.baiji.jianshu.a)) && (((com.baiji.jianshu.a)v0).isDestroyed())
                                    ) {
                                return;
                            }

                            loadUrl("javascript:window.InjectedObject.getContentHeight(document.getElementsByTagName(\'html\')[0].scrollHeight)");
                        }
                    }
                    */
                }
            }, 500);
        }

    }

    public void setScrollStateChangedListener(ScrollStateChangedListener scrollStateChangedListener) {
        this.scrollStateChangedListener = scrollStateChangedListener;
    }

}
