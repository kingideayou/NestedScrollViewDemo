package me.next.nestedscrollviewdemo;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
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
    private int webviewHeight;
    private int contentHeight;

    private float density;

    private boolean isScrollUp = true;//上滑

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
//        this.mIsBeingDragged = false;
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
        ViewConfiguration v0 = ViewConfiguration.get(this.getContext());
        int mMinimumVelocity = v0.getScaledMinimumFlingVelocity();
        int mMaximumVelocity = v0.getScaledMaximumFlingVelocity();
        int touchSlop = v0.getScaledTouchSlop();
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
                        localLayoutParams.height = 1692;
                        setLayoutParams(localLayoutParams);
                    }
                }, 1000);
    }

    private float mLastMotionY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private boolean mIsBeginDrag = false;

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

        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {

                Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN");

                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                final float initialDownY = getMotionEventY(event, mActivePointerId);

                Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN initialDownY : " + initialDownY);

                if (initialDownY == -1) {
                    return false;
                }
                mLastMotionY = initialDownY;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                super.onTouchEvent(event);
                mIsBeginDrag = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                Log.e(TAG, "onInterceptTouchEvent ACTION_MOVE");

                Log.e(TAG, "mActivePointerId : " + mActivePointerId);
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(event, mActivePointerId);
                Log.e(TAG, "mActivePointerId yyy : " + y);
                if (y == -1) {
                    return false;
                }
                int deltaY = (int)(mLastMotionY - y);
                mLastMotionY  = y;
                if (Math.abs(deltaY) >= mTouchSlop) {
                    mIsBeginDrag = true;
                }
                if (mIsBeginDrag && dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
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
            case MotionEvent.ACTION_UP: {

                Log.e(TAG, "onInterceptTouchEvent ACTION_UP");

                stopNestedScroll();
                mActivePointerId = INVALID_POINTER;
                mIsBeginDrag = false;
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
//        setJavaScriptEnable(true);
//        this.mIsBeingDragged = false;
        this.mActivePointerId = -1;
//        recycleVelocityTracker();
        stopNestedScroll();
    }

    @Override
    public void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        this.consumedY = (y - oldy);// <0：下滑 >0：上拉
        if (y <= 0) {
            this.position = ScrollStateChangedListener.ScrollState.TOP;
            return;
        }
        /*
        if (null != this.scrollStateChangedListener) {
            this.scrollStateChangedListener.onChildPositionChange(this.position);
        }
        if (this.onScrollChangeListener != null) {
            this.onScrollChangeListener.onScrollChanged(x, y, oldx, oldy, this.position);
        }
        */
        /*
        if (consumedY > 0) {
            isScrollUp = true;
            if (!ViewCompat.canScrollVertically(this, 1)) {
                if (y + this.webviewHeight >= this.contentHeight - 10) {
                    this.position = ScrollStateChangedListener.ScrollState.BOTTOM;
                } else {
                    this.position = ScrollStateChangedListener.ScrollState.MIDDLE;
                }
            } else {
                this.position = ScrollStateChangedListener.ScrollState.MIDDLE;
            }
        } else {
            isScrollUp = false;
            this.position = ScrollStateChangedListener.ScrollState.MIDDLE;
        }
        */
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        this.webviewHeight = h;
        Log.e(TAG, "onSizeChanged: " + h + " contentHeight : " + h);
        if(this.contentHeight < 1) {
            this.setContentHeight(webviewHeight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, TAG + " heightMeasureSpec : " + heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, TAG + " getMeasuredHeight : " + getMeasuredHeight());
    }


    public void invalidate() {
        super.invalidate();
        contentHeight = ((int)((((float)getContentHeight())) * this.density));
//        Log.e(TAG, "invalidate contentHeight ------------ " + contentHeight);
        if(contentHeight != 0) {
            loadUrl("javascript:window.InjectedObject.getContentHeight(document.getElementsByTagName('html')[0].scrollHeight)");
        }
    }

    public void computeScroll() {
//        if (this.position == ScrollStateChangedListener.ScrollState.MIDDLE) {
//            super.computeScroll();
//        }
        super.computeScroll();
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

}
