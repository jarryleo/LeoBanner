package cn.leo.banner;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/19 16:27
 */
public class AniamtionDecoration extends RecyclerView.ItemDecoration {

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
            float scale = 0.9f + 0.1f * (1f - distance * 1f / centerX);
            child.setScaleX(scale);
            child.setScaleY(scale);
        }
    }
}
