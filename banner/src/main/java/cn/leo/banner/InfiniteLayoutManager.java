package cn.leo.banner;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/15 15:36
 */
public class InfiniteLayoutManager extends RecyclerView.LayoutManager {
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
        if (getChildCount() == 0 && state.isPreLayout()) {
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
        if (mHorizontalOffset != 0) {
            return;
        }
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
                throw new IllegalArgumentException("item layout width can not support wrap_content!");
            }
        }
        mLastVisiblePosition = index - 1;
        //往左排列
        index = -1;
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
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //位移0、没有子View 当然不移动
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }
        //实际滑动的距离， 可能会在边界处被修复
        int realOffset = dx;
        //先填充，再位移。
        realOffset = fill(recycler, state, realOffset);
        //累加实际滑动距离
        mHorizontalOffset += realOffset;
        //移动所有展示的子条目 , 但是不会自动回收或者出现新的条目，要自己处理
        offsetChildrenHorizontal(-realOffset);
        return realOffset;
    }

    /**
     * 填充新的view，回收越界的view
     *
     * @param dx 真实距离
     * @return 实际距离
     */
    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        //先回收越界view
        if (getChildCount() > 0) {
            //滑动时进来的
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (dx > 0) {
                    //需要回收当前屏幕，左越界的View
                    if (getDecoratedRight(child) - dx < getPaddingLeft()) {
                        removeAndRecycleView(child, recycler);
                        mFirstVisiblePosition++;
                    }
                } else if (dx < 0) {
                    //回收当前屏幕，右越界的View
                    if (getDecoratedLeft(child) - dx > getWidth() - getPaddingRight()) {
                        removeAndRecycleView(child, recycler);
                        mLastVisiblePosition--;
                    }
                }
            }
        }

        //添加新的view,分左右方向添加，左滑添加右边，右滑添加左边
        if (dx > 0) {
            //左滑
            //获取当前显示的最后一个view
            View child = getChildAt(getChildCount() - 1);
            //找RecyclerView要一个新view（判断现在显示的最后一个view是不是adapter的最后一个）
            if (getDecoratedRight(child) - dx < getWidth() - getPaddingRight()) {
                int left = getDecoratedLeft(child);
                int width = getDecoratedMeasuredWidth(child);
                int position = ++mLastVisiblePosition;
                View view = recycler.getViewForPosition(fixPosition(position));
                addView(view);
                measureChildWithMargins(view, 0, 0);
                layoutDecoratedWithMargins(view, left + width, getPaddingTop(),
                        left + width + getDecoratedMeasuredWidth(view),
                        getPaddingTop() + getDecoratedMeasuredHeight(view));
            }
        } else {
            //右滑
            //拿到左边第一个显示的view
            View firstVisibleView = getChildAt(0);
            //获取第一个view的left
            int left = getDecoratedLeft(firstVisibleView);
            int leftOffset = left - getPaddingLeft() - dx;
            if (leftOffset > 0) {
                //左边漏空了，添加新的view，先判断前面是否还有view
                int position = --mFirstVisiblePosition;
                View view = recycler.getViewForPosition(fixPosition(position));
                //添加在左边
                addView(view, 0);
                measureChildWithMargins(view, 0, 0);
                layoutDecoratedWithMargins(view, left - getDecoratedMeasuredWidth(view),
                        getPaddingTop(), left, getPaddingTop() + getDecoratedMeasuredHeight(view));
            }

        }
        return dx;
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

}
