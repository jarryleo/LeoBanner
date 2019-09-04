package cn.leo.banner;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/19 16:27
 */
public class FlyScaleDecoration extends RecyclerView.ItemDecoration {
    /**
     * VIEW 最小缩放倍数
     */
    private float mMinScale = 0.7f;
    /**
     * 左右缩进距离，单位 px
     */
    private int mPadding = 200;

    public FlyScaleDecoration() {
    }

    public FlyScaleDecoration(float minScale, int padding) {
        mMinScale = minScale;
        mPadding = padding;
    }

    /**
     * RecyclerView 的每次滚动都会调用，适合做滑动动画
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        //当前显示的所有条目
        int childCount = layoutManager.getChildCount();
        int cx = layoutManager.getWidth() / 2;
        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            int left = layoutManager.getDecoratedLeft(child);
            int width = layoutManager.getDecoratedMeasuredWidth(child);
            int center = left + width / 2;
            int distance = Math.abs(center - cx);
            float scale = mMinScale + (1 - mMinScale) * (1f - distance * 1f / cx);
            child.setScaleX(scale);
            child.setScaleY(scale);
            setElevation(parent, child);
        }
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = -mPadding / 2;
        outRect.right = -mPadding / 2;
        setElevation(parent, view);
    }

    private void setElevation(RecyclerView parent, View view) {
        int centerX = getCenterX(parent);
        int viewCenterX = getViewCenterX(parent, view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(centerX - Math.abs(centerX - viewCenterX));
        }
    }

    private int getCenterX(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        return layoutManager.getWidth() / 2;
    }

    private int getViewCenterX(RecyclerView parent, View view) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int left = layoutManager.getDecoratedLeft(view);
        int width = layoutManager.getDecoratedMeasuredWidth(view);
        int center = left + width / 2;
        return center;
    }

}
