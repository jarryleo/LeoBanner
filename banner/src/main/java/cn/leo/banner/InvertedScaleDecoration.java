package cn.leo.banner;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/19 16:27
 */
public class InvertedScaleDecoration extends RecyclerView.ItemDecoration {
    /**
     * VIEW 最小缩放倍数
     */
    private static float MIN_SCALE = 0.9f;

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
            child.setScaleX(scale);
            child.setScaleY(scale);


            Rect rect = new Rect();
            child.getDrawingRect(rect);
            int height = rect.height() / 2;
            int bottom = layoutManager.getDecoratedBottom(child) -
                    (int) (layoutManager.getDecoratedBottom(child) * (1f - scale) + 0.5f) / 2;

            left += (int) (width * (1f - scale) + 0.5f) / 2;
            rect.left += left;
            rect.right += left;
            rect.top = bottom + 2;
            rect.bottom = rect.top + height;
            drawInverted(c, child, rect, scale);
        }
    }

    private Paint paint = new Paint();
    private Camera camera = new Camera();

    private void drawInverted(Canvas c, View view, Rect rect, float scale) {
        Bitmap sourceBitmap = convertViewToBitmap(view);
        if (sourceBitmap == null) {
            return;
        }

        //1.倒立图
        Matrix matrix = new Matrix();
        //以X轴向下翻转
        int width = rect.width();
        int height = rect.height();
        matrix.preScale(scale, -scale);
        //生成倒立图，宽度和原图一致，高度为原图的一半
        Bitmap revertBitmap = Bitmap.createBitmap(sourceBitmap, 0, height, width,
                height, matrix, false);
        c.drawBitmap(revertBitmap, rect.left, rect.top, paint);
        //3.画笔使用LinearGradient 线性渐变渲染
        LinearGradient lg = new LinearGradient(0, 0,
                0, height * scale + 1,
                0x70ffffff,
                0x00ffffff,
                Shader.TileMode.MIRROR);
        paint.setShader(lg);
        //4.指定画笔的Xfermode 即绘制的模式（不同的模式，绘制的区域不同）
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //5.在倒立图区，绘制矩形渲染图层
        c.drawRect(rect.left, rect.top,
                rect.left + rect.width() * scale + 1,
                rect.top + rect.height() * scale + 1, paint);
        paint.setXfermode(null);
    }

    private Bitmap convertViewToBitmap(View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }
}
