package cn.leo.banner;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author : Jarry Leo
 * @date : 2018/11/19 16:27
 */
public class IndicatorDecoration extends RecyclerView.ItemDecoration {
    /**
     * 待完善：
     * 指示器设置支持drawable文件和图片
     */
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_RIGHT = 1 << 1;
    public static final int ALIGN_TOP = 1 << 2;
    public static final int ALIGN_BOTTOM = 1 << 3;
    @Align
    private int mAlign = ALIGN_BOTTOM;

    @IntDef(value = {ALIGN_LEFT, ALIGN_RIGHT, ALIGN_TOP, ALIGN_BOTTOM}, flag = true)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Align {
    }

    private int mSelectedSize = dip2px(7);
    private int mUnselectedSize = dip2px(7);
    private int mSelectedColor = Color.RED;
    private int mUnselectedColor = Color.WHITE;
    private int mSpace = dip2px(6);
    private int mVerticalPadding = dip2px(16);
    private int mHorizontalPadding = dip2px(16);
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
        //指示器总体宽度
        int indicatorWidth = mUnselectedSize * itemCount +
                mSpace * (itemCount - 1) +
                mSelectedSize - mUnselectedSize;
        //指示器位置
        int indicatorLeft = (width - indicatorWidth) / 2;
        if ((mAlign & ALIGN_LEFT) == ALIGN_LEFT) {
            indicatorLeft = mVerticalPadding;
        }
        if ((mAlign & ALIGN_RIGHT) == ALIGN_RIGHT) {
            indicatorLeft = width - indicatorWidth - mVerticalPadding;
        }

        int indicatorTop = (height - mUnselectedSize) / 2;
        if ((mAlign & ALIGN_TOP) == ALIGN_TOP) {
            indicatorTop = mHorizontalPadding;
        }
        if ((mAlign & ALIGN_BOTTOM) == ALIGN_BOTTOM) {
            indicatorTop = height - mUnselectedSize - mVerticalPadding;
        }

        //绘制指示器
        for (int i = 0; i < itemCount; i++) {
            int r = mUnselectedSize;
            if (centerItemPosition == i) {
                //选中条目指示器颜色
                mPaint.setColor(mSelectedColor);
                r = mSelectedSize;
            } else {
                mPaint.setColor(mUnselectedColor);
            }
            int left = i * (mUnselectedSize + mSpace) + indicatorLeft;

            int cx = left + (mUnselectedSize / 2);
            int cy = indicatorTop + (mUnselectedSize / 2);
            c.drawCircle(cx, cy, r / 2f, mPaint);
        }
    }

    public void setSelectedSize(int selectedSize) {
        mSelectedSize = dip2px(selectedSize);
    }

    public void setUnselectedSize(int unselectedSize) {
        mUnselectedSize = dip2px(unselectedSize);
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public void setUnselectedColor(int unselectedColor) {
        mUnselectedColor = unselectedColor;
    }

    public void setSpace(int space) {
        mSpace = dip2px(space);
    }

    public void setVerticalPadding(int verticalPadding) {
        mVerticalPadding = dip2px(verticalPadding);
    }

    public void setHorizontalPadding(int horizontalPadding) {
        mHorizontalPadding = dip2px(horizontalPadding);
    }

    public void setAlign(@Align int align) {
        mAlign = align;
    }

    private int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
