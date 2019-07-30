package cn.leo.banner;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.PowerManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/15 15:36
 */
public class InfiniteLayoutManager
        extends LinearLayoutManager {
    private static final String TAG = "InfiniteLayoutManager";
    /**
     * 现在第一个可见的view的在所有条目中的索引
     */
    private int mFirstVisiblePosition;
    /**
     * 现在最后一个可见的view的在所有条目中的索引
     */
    private int mLastVisiblePosition;
    /**
     * 滑动距离
     */
    private int mHorizontalOffset;


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 1 在RecyclerView初始化时，会被调用两次。
     * 2 在调用adapter.notifyDataSetChanged()时，会被调用。
     * 3 在调用setAdapter替换Adapter时,会被调用。
     * 4 在RecyclerView执行动画时，它也会被调用。
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //没有Item，界面空着吧
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        //state.isPreLayout()是支持动画的（重写supportsPredictiveItemAnimations()方法后必定执行2次，
        // 一次PreLayout，一次真实layout，根据2次位置执行动画）
        if (state.isPreLayout()) {
            return;
        }
        if (mHorizontalOffset > 0) {
            return;
        }
        //初始化时调用 填充childView
        layout(recycler, state);
    }

    /**
     * 填充view 方法，需要支持 notify
     */
    private void layout(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int leftOffset;
        //这是初始化
        //onLayoutChildren方法在RecyclerView 初始化时 会执行两遍,所以要把第一遍填充的轻回收
        detachAndScrapAttachedViews(recycler);
        int index = mFirstVisiblePosition;
        //第一个条目居中
        View firstView = recycler.getViewForPosition(fixPosition(index));
        addView(firstView);
        measureChildWithMargins(firstView, 0, 0);
        int firstWidth = getDecoratedMeasuredWidth(firstView);
        leftOffset = getWidth() / 2 - firstWidth / 2;
        int temLeft = leftOffset;
        mHorizontalOffset = leftOffset - getPaddingLeft();
        layoutDecoratedWithMargins(firstView,
                leftOffset,
                getPaddingTop(),
                leftOffset + firstWidth,
                getPaddingTop() + getDecoratedMeasuredHeight(firstView));
        leftOffset += getDecoratedMeasurementHorizontal(firstView);
        index++;
        //往右排列
        while (leftOffset < getHorizontalSpace()) {
            View view = recycler.getViewForPosition(fixPosition(index));
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int childWidth = getDecoratedMeasuredWidth(view);
            layoutDecoratedWithMargins(view,
                    leftOffset,
                    getPaddingTop(),
                    leftOffset + childWidth,
                    getPaddingTop() + getDecoratedMeasuredHeight(view));
            int viewWidth = getDecoratedMeasurementHorizontal(view);
            leftOffset += viewWidth;
            index++;
            if (viewWidth == 0) {
                throw new IllegalArgumentException(
                        "item layout width can not support wrap_content!");
            }
        }
        mLastVisiblePosition = index - 1;
        //往左排列
        index = mFirstVisiblePosition - 1;
        while (temLeft > getPaddingLeft()) {
            View view = recycler.getViewForPosition(fixPosition(index));
            addView(view, 0);
            measureChildWithMargins(view, 0, 0);
            int childWidth = getDecoratedMeasuredWidth(view);
            layoutDecoratedWithMargins(view,
                    temLeft - childWidth,
                    getPaddingTop(),
                    temLeft,
                    getPaddingTop() + getDecoratedMeasuredHeight(view));
            temLeft -= getDecoratedMeasurementHorizontal(view);
            index--;
        }
        mFirstVisiblePosition = index + 1;
    }


    /**
     * 返回值会被RecyclerView用来判断是否达到边界，
     * 如果返回值！=传入的dx，
     * 则会有一个边缘的发光效果，表示到达了边界。
     * 而且返回值还会被RecyclerView用于计算fling效果。
     */
    @Override
    public int scrollHorizontallyBy(int dx,
                                    RecyclerView.Recycler recycler,
                                    RecyclerView.State state) {
        //位移0、没有子View 当然不移动
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }
        //实际滑动的距离， 可能会在边界处被修复
        //先填充，再位移。
        int realOffset = fill(recycler, state, dx);
        //累加实际滑动距离
        mHorizontalOffset += realOffset;
        //移动所有展示的子条目 , 但是不会自动回收或者出现新的条目，要自己处理
        offsetChildrenHorizontal(-realOffset);
        return realOffset;
    }

    /**
     * 填充新的view，回收越界的view
     *
     * @param dx 真实距离 右滑位负 左滑为正
     * @return 实际距离
     */
    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        //添加新的view,分左右方向添加，左滑添加右边，右滑添加左边
        if (dx > 0) {
            addRight(recycler, dx);
        } else {
            addLeft(recycler, dx);
        }
        //回收越界view
        if (getChildCount() > 0) {
            if (dx >= 0) {
                recycleLeft(recycler, dx);
            }
            if (dx <= 0) {
                recycleRight(recycler, dx);
            }
        }
        return dx;
    }

    /**
     * 根据滑动距离回收越界不显示的view
     * 如果滑动距离很大，可能会全部回收
     */
    private void recycleLeft(RecyclerView.Recycler recycler, int dx) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (getDecoratedRight(child) - dx <= getPaddingLeft()) {
                removeAndRecycleView(child, recycler);
                mFirstVisiblePosition = fixPosition(++mFirstVisiblePosition);
            }
        }
    }

    /**
     * 根据滑动距离回收越界不显示的view
     * 如果滑动距离很大，可能会全部回收
     */
    private void recycleRight(RecyclerView.Recycler recycler, int dx) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (getDecoratedLeft(child) - dx >= getWidth() - getPaddingRight()) {
                removeAndRecycleView(child, recycler);
                mLastVisiblePosition = fixPosition(--mLastVisiblePosition);
            }
        }
    }

    /**
     * 根据滑动距离填补漏出来的空白
     * 如果滑动距离过大，需要填补多个条目
     */
    private void addLeft(RecyclerView.Recycler recycler, int dx) {
        //拿到左边第一个显示的view
        View firstVisibleView = getChildAt(0);

        //获取第一个view的left
        int left = getDecoratedLeft(firstVisibleView);
        int leftOffset = left - getPaddingLeft() - dx;
        while (leftOffset > 0) {
            //左边漏空了，添加新的view，先判断前面是否还有view
            mFirstVisiblePosition = fixPosition(--mFirstVisiblePosition);
            View view = recycler.getViewForPosition(mFirstVisiblePosition);
            //添加在左边
            addView(view, 0);
            measureChildWithMargins(view, 0, 0);
            layoutDecoratedWithMargins(view, left - getDecoratedMeasuredWidth(view),
                    getPaddingTop(), left,
                    getPaddingTop() + getDecoratedMeasuredHeight(view));
            //循环往左填充
            //拿到左边第一个显示的view
            firstVisibleView = getChildAt(0);
            //获取第一个view的left
            left = getDecoratedLeft(firstVisibleView);
            leftOffset = left - getPaddingLeft() - dx;
        }
    }

    /**
     * 根据滑动距离填补漏出来的空白
     * 如果滑动距离过大，需要填补多个条目
     */
    private void addRight(RecyclerView.Recycler recycler, int dx) {
        //获取当前显示的最后一个view
        View child = getChildAt(getChildCount() - 1);
        //找RecyclerView要一个新view（判断现在显示的最后一个view是不是adapter的最后一个）
        while (getDecoratedRight(child) - dx < getWidth() - getPaddingRight()) {
            int left = getDecoratedLeft(child);
            int width = getDecoratedMeasuredWidth(child);
            mLastVisiblePosition = fixPosition(++mLastVisiblePosition);
            View view = recycler.getViewForPosition(mLastVisiblePosition);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            layoutDecoratedWithMargins(view, left + width, getPaddingTop(),
                    left + width + getDecoratedMeasuredWidth(view),
                    getPaddingTop() + getDecoratedMeasuredHeight(view));
            //循环往右填充
            child = getChildAt(getChildCount() - 1);
        }
    }

    /**
     * 修正越界的索引
     */
    private int fixPosition(int position) {
        int itemCount = getItemCount();
        return (position % itemCount + itemCount) % itemCount;
    }


    /**
     * @return 是否允许纵向滑动
     */
    @Override
    public boolean canScrollVertically() {
        return false;
    }

    /**
     * @return 是否允许横向滑动
     */
    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    /**
     * 获取某个childView在水平方向所占的空间
     *
     * @param view 子view
     * @return px
     */
    public int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * 获取某个childView在竖直方向所占的空间
     *
     * @param view 子view
     * @return px
     */
    public int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    /**
     * 获取RecyclerView纵向可用空间
     *
     * @return px
     */
    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * 获取RecyclerView横向可用空间
     *
     * @return px
     */
    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }


    @Override
    public void scrollToPosition(int position) {
        mFirstVisiblePosition = position;
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView,
                                       RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = createSnapScroller();
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public View findViewByPosition(int position) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return null;
        }
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (getPosition(child) == position) {
                return child;
            }
        }
        return super.findViewByPosition(position);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        return new PointF(1, 0);
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        mHorizontalOffset = 0;
        mFirstVisiblePosition = 0;
        mLastVisiblePosition = 0;
    }

    public int getCurrentPosition() {
        int cx = getWidth() / 2;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (getDecoratedLeft(child) < cx &&
                    getDecoratedRight(child) > cx) {
                return getPosition(child);
            }
        }
        return mFirstVisiblePosition;
    }

    public int getFirstVisiblePosition() {
        return mFirstVisiblePosition;
    }

    public int getLastVisiblePosition() {
        return mLastVisiblePosition;
    }

    public int getHorizontalOffset() {
        return mHorizontalOffset;
    }


    //以下自动滚动部分

    public InfiniteLayoutManager(Context context) {
        super(context, LinearLayoutManager.HORIZONTAL, false);
    }

    public InfiniteLayoutManager(Context context, int interval) {
        super(context, LinearLayoutManager.HORIZONTAL, false);
        mInterval = interval;
    }


    private RecyclerView mRecyclerView;
    private int mInterval;

    private void next() {
        if (mRecyclerView == null || mInterval <= 0) {
            return;
        }
        mRecyclerView.removeCallbacks(mRunnable);
        if (isVisible()) {
            mRecyclerView.smoothScrollToPosition(fixPosition(getCurrentPosition() + 1));
        }
        mRecyclerView.postDelayed(mRunnable, mInterval);
    }

    private boolean isVisible() {
        if (!mRecyclerView.isShown()) {
            return false;
        }
        PowerManager pm = (PowerManager) mRecyclerView.getContext()
                .getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            boolean isScreenOn;
            if (android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                isScreenOn = pm.isInteractive();
            } else {
                isScreenOn = pm.isScreenOn();
            }
            if (!isScreenOn) {
                return false;
            }
        }
        Rect rect = new Rect();
        boolean visibility = mRecyclerView.getLocalVisibleRect(rect);
        int visibleArea = rect.width() * rect.height();
        int area = getWidth() * getHeight();
        float v = visibleArea * 1f / area;
        //可见面积小于50%不滚动
        if (v < 0.5f) {
            return false;
        }
        return visibility;
    }

    private void stop() {
        if (mRecyclerView != null) {
            mRecyclerView.removeCallbacks(mRunnable);
        }
    }

    private void run() {
        if (mRecyclerView != null && mInterval > 0) {
            mRecyclerView.postDelayed(mRunnable, mInterval);
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            next();
        }
    };

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mRecyclerView = view;
        if (mInterval > 0) {
            mRecyclerView.postDelayed(mRunnable, mInterval);
            mRecyclerView.addOnItemTouchListener(mSimpleOnItemTouchListener);
            mInfiniteLayoutSnapHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        removeCallbacks(mRunnable);
        super.onDetachedFromWindow(view, recycler);
        if (mRecyclerView != null) {
            mRecyclerView.removeOnItemTouchListener(mSimpleOnItemTouchListener);
            mRecyclerView = null;
        }
    }

    //触摸暂停

    private RecyclerView.SimpleOnItemTouchListener mSimpleOnItemTouchListener =
            new RecyclerView.SimpleOnItemTouchListener() {

                @Override
                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        stop();
                    }
                    if (e.getAction() == MotionEvent.ACTION_UP ||
                            e.getAction() == MotionEvent.ACTION_CANCEL) {
                        run();
                    }
                    return super.onInterceptTouchEvent(rv, e);
                }
            };


    private PagerSnapHelper mInfiniteLayoutSnapHelper = new PagerSnapHelper() {
        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager,
                                          int velocityX, int velocityY) {
            return RecyclerView.NO_POSITION;
        }
    };


    protected LinearSmoothScroller createSnapScroller() {
        return new LinearSmoothScroller(mRecyclerView.getContext()) {
            private static final float MILLISECONDS_PER_INCH = 25f;

            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                if (mRecyclerView == null) {
                    // The associated RecyclerView has been removed so there is no action to take.
                    return;
                }
                int[] snapDistances = mInfiniteLayoutSnapHelper.
                        calculateDistanceToFinalSnap(mRecyclerView.getLayoutManager(),
                                targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            protected int calculateTimeForDeceleration(int dx) {
                // we want to cover same area with the linear interpolator for the first 10% of the
                // interpolation. After that, deceleration will take control.
                // area under curve (1-(1-x)^2) can be calculated as (1 - x/3) * x * x
                // which gives 0.100028 when x = .3356
                // this is why we divide linear scrolling time with .3356
                return (int) Math.ceil(calculateTimeForScrolling(dx) / .3356);
            }
        };
    }
}
