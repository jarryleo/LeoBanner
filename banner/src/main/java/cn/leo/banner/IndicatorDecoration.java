package cn.leo.banner;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/19 16:27
 */
public class IndicatorDecoration extends RecyclerView.ItemDecoration {
    /**
     * 待完善：
     * 1。指示器选中和非选中大小可设置
     * 2。指示器选中和非选中颜色可设置
     * 3。指示器位置上下左右和四个角位置可设置
     * 4。指示器设置支持drawable文件和图片
     */
    private int mSize = dip2px(8);
    private int mSpace = dip2px(6);
    private int mPadding = dip2px(16);
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * RecyclerView 的每次滚动都会调用，适合做滑动动画
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        //宽高
        int width = parent.getMeasuredWidth();
        int height = parent.getMeasuredHeight();
        //条目个数
        int itemCount = layoutManager.getItemCount();
        //当前显示的所有条目
        int childCount = layoutManager.getChildCount();
        int centerX = width / 2;
        int centerY = height / 2;
        Rect rect = new Rect();
        //中心位置的条目position
        int centerItemPosition = 0;
        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            layoutManager.getDecoratedBoundsWithMargins(child, rect);
            if (rect.contains(centerX, centerY)) {
                centerItemPosition = layoutManager.getPosition(child);
                break;
            }
        }
        //绘制指示器
        int indicatorWidth = mSize * itemCount + mSpace * (itemCount - 1);
        int indicatorLeft = (width - indicatorWidth) / 2;
        int indicatorTop = height - mSize - mPadding;
        for (int i = 0; i < itemCount; i++) {
            if (centerItemPosition == i) {
                //选中条目指示器颜色
                mPaint.setColor(Color.RED);
            } else {
                mPaint.setColor(Color.WHITE);
            }
            int left = i * (mSize + mSpace) + indicatorLeft;
            int cx = left + (mSize / 2);
            int cy = indicatorTop + (mSize / 2);
            c.drawCircle(cx, cy, mSize / 2f, mPaint);
        }
    }

    private int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
