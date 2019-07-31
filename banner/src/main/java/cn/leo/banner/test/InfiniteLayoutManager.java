package cn.leo.banner.test;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/15 15:36
 */
public class InfiniteLayoutManager
        extends LinearLayoutManager {
    private static final String TAG = "InfiniteLayoutManager";
    private static final int TAG_KEY = 2019073100;
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
        if (mHorizontalOffset != 0) {
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
        int firstWidth = 0;
        leftOffset = 0;
        //往右排列
        while (leftOffset < (getHorizontalSpace() + firstWidth) / 2) {
            View view = recycler.getViewForPosition(fixPosition(index));
            view.setTag(TAG_KEY, index);
            index++;
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int childWidth = getDecoratedMeasuredWidth(view);
            if (firstWidth == 0) {
                firstWidth = childWidth;
            }
            layoutDecoratedWithMargins(view,
                    leftOffset,
                    getPaddingTop(),
                    leftOffset + childWidth,
                    getPaddingTop() + getDecoratedMeasuredHeight(view));
            int viewWidth = getDecoratedMeasurementHorizontal(view);
            leftOffset += viewWidth;
        }
        mLastVisiblePosition = index - 1;
        int offset = getWidth() / 2 - firstWidth / 2;
        scrollHorizontallyBy(-offset, recycler, state);
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
                mFirstVisiblePosition++;
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
                mLastVisiblePosition--;
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
            View view = recycler.getViewForPosition(fixPosition(--mFirstVisiblePosition));
            //添加在左边
            addView(view, 0);
            measureChildWithMargins(view, 0, 0);
            layoutDecoratedWithMargins(view, left - getDecoratedMeasuredWidth(view),
                    getPaddingTop(), left,
                    getPaddingTop() + getDecoratedMeasuredHeight(view));
            //循环往左填充
            left = getDecoratedLeft(view);
            leftOffset = left - getPaddingLeft() - dx;
        }
        fixTag();
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
            View view = recycler.getViewForPosition(fixPosition(++mLastVisiblePosition));
            addView(view);
            measureChildWithMargins(view, 0, 0);
            layoutDecoratedWithMargins(view, left + width, getPaddingTop(),
                    left + width + getDecoratedMeasuredWidth(view),
                    getPaddingTop() + getDecoratedMeasuredHeight(view));
            //循环往右填充
            child = view;
        }
        fixTag();
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
     * 获取RecyclerView横向可用空间
     *
     * @return px
     */
    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        mHorizontalOffset = 0;
        mFirstVisiblePosition = 0;
        mLastVisiblePosition = 0;
    }

    public int getCurrentPosition() {
        return getPosition(getCenterView());
    }

    private View getCenterView() {
        return getChildAt(getChildCount() / 2);
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

    //重写惯性滑动部分

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        if (mRecyclerView.getOnFlingListener() instanceof PagerSnapHelper) {
            return new PointF(1, 0);
        }
        return null;
    }

    @Override
    public void startSmoothScroll(RecyclerView.SmoothScroller smoothScroller) {
        int targetPosition = smoothScroller.getTargetPosition();
        System.out.println("targetPosition = " + targetPosition);
        smoothScroller = new SmoothScroller(mRecyclerView.getContext());
        smoothScroller.setTargetPosition(fixPosition(targetPosition));
        super.startSmoothScroll(smoothScroller);
    }


    private class SmoothScroller extends LinearSmoothScroller {
        SmoothScroller(Context context) {
            super(context);
        }

        @Nullable
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return new PointF(1, 0);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int
                snapPreference) {
            switch (snapPreference) {
                case SNAP_TO_START:
                    return boxStart - viewStart;
                case SNAP_TO_END:
                    return boxEnd - viewEnd;
                case SNAP_TO_ANY:
                    int viewWidth = viewEnd - viewStart;
                    int boxWidth = boxEnd - boxStart;
                    int dtStart = (boxWidth - viewWidth) / 2;
                    return dtStart - viewStart;
                default:
                    throw new IllegalArgumentException("snap preference should be one of the"
                            + " constants defined in SmoothScroller, starting with SNAP_");
            }
            //return 0;
        }
    }

    /**
     * 根据position查找view，同屏幕内可能存在多个相同position的view
     * 需要考虑如何处理
     */
    @Override
    public View findViewByPosition(int position) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            Object tag = view.getTag(TAG_KEY);
            if (tag instanceof Integer &&
                    (int) tag == position) {
                return view;
            }
        }
        return super.findViewByPosition(position);
    }

    private void fixTag() {
        int centerPosition = getCurrentPosition();
        int childCount = getChildCount();
        int centerChild = childCount / 2;
        for (int i = 0; i < childCount; i++) {
            if (i < centerChild) {
                getChildAt(i).setTag(TAG_KEY, centerPosition - (centerChild - i));
            } else {
                getChildAt(i).setTag(TAG_KEY, centerPosition + (i - centerChild));
            }
        }
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


    public static class InfiniteSnapHelper extends PagerSnapHelper {
        @Nullable
        private OrientationHelper mVerticalHelper;
        @Nullable
        private OrientationHelper mHorizontalHelper;

        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
            final int itemCount = layoutManager.getItemCount();
            if (itemCount == 0) {
                return RecyclerView.NO_POSITION;
            }

            View mStartMostChildView = null;
            if (layoutManager.canScrollVertically()) {
                mStartMostChildView = findCenterView(layoutManager, getVerticalHelper(layoutManager));
            } else if (layoutManager.canScrollHorizontally()) {
                mStartMostChildView = findCenterView(layoutManager, getHorizontalHelper(layoutManager));
            }

            if (mStartMostChildView == null) {
                return RecyclerView.NO_POSITION;
            }
            final int centerPosition = layoutManager.getPosition(mStartMostChildView);
            if (centerPosition == RecyclerView.NO_POSITION) {
                return RecyclerView.NO_POSITION;
            }
            final boolean forwardDirection;
            if (layoutManager.canScrollHorizontally()) {
                forwardDirection = velocityX > 0;
            } else {
                forwardDirection = velocityY > 0;
            }
            boolean reverseLayout = false;
            if ((layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
                RecyclerView.SmoothScroller.ScrollVectorProvider vectorProvider =
                        (RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager;
                PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
                if (vectorForEnd != null) {
                    reverseLayout = vectorForEnd.x < 0 || vectorForEnd.y < 0;
                }
            }

            System.out.println("velocityX = " + velocityX);
            return reverseLayout
                    ? (forwardDirection ? centerPosition - 1 : centerPosition + 1)
                    : (forwardDirection ? centerPosition + 1 : centerPosition - 1);
        }

        @Nullable
        private View findCenterView(RecyclerView.LayoutManager layoutManager,
                                    OrientationHelper helper) {
            int childCount = layoutManager.getChildCount();
            if (childCount == 0) {
                return null;
            }

            View closestChild = null;
            final int center;
            if (layoutManager.getClipToPadding()) {
                center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
            } else {
                center = helper.getEnd() / 2;
            }
            int absClosest = Integer.MAX_VALUE;

            for (int i = 0; i < childCount; i++) {
                final View child = layoutManager.getChildAt(i);
                int childCenter = helper.getDecoratedStart(child)
                        + (helper.getDecoratedMeasurement(child) / 2);
                int absDistance = Math.abs(childCenter - center);

                /** if child center is closer than previous closest, set it as closest  **/
                if (absDistance < absClosest) {
                    absClosest = absDistance;
                    closestChild = child;
                }
            }
            return closestChild;
        }

        @NonNull
        private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
            if (mVerticalHelper == null || mVerticalHelper.getLayoutManager() != layoutManager) {
                mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
            }
            return mVerticalHelper;
        }

        @NonNull
        private OrientationHelper getHorizontalHelper(
                @NonNull RecyclerView.LayoutManager layoutManager) {
            if (mHorizontalHelper == null || mHorizontalHelper.getLayoutManager() != layoutManager) {
                mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
            }
            return mHorizontalHelper;
        }
    }
}
