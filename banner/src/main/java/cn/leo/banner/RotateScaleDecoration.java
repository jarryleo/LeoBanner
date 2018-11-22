package cn.leo.banner;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/19 16:27
 */
public class RotateScaleDecoration extends RecyclerView.ItemDecoration {
    /**
     * VIEW 最小缩放倍数
     */
    private static float MIN_SCALE = 0.9f;
    /**
     * VIEW 最大旋转角度
     */
    private static float MAX_ROTATE = 30;

    /**
     * RecyclerView 的每次滚动都会调用，适合做滑动动画
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        //当前显示的所有条目
        int childCount = layoutManager.getChildCount();
        int centerX = parent.getMeasuredWidth() / 2;
        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            int left = layoutManager.getDecoratedLeft(child);
            int width = layoutManager.getDecoratedMeasuredWidth(child);
            int center = left + width / 2;
            int distance = Math.abs(center - centerX);
            float scale = MIN_SCALE + (1 - MIN_SCALE) * (1f - distance * 1f / centerX);
            //缩放
//            child.setScaleX(scale);
//            child.setScaleY(scale);
            //旋转
            //float rotate = ((centerX - center) * 1f / centerX) * MAX_ROTATE;
            float rotate = ((center - centerX) * 1f / centerX) * MAX_ROTATE;
            child.setPivotY(child.getMeasuredHeight() / 2);
            if (center < centerX) {
                child.setPivotX(child.getMeasuredWidth());
            } else {
                child.setPivotX(0);
            }

            child.setRotationY(rotate);

        }
    }
}
