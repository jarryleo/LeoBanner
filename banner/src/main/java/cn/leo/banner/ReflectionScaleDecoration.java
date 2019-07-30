package cn.leo.banner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.View;

/**
 * @author : Jarry Leo
 * @date : 2018/11/19 16:27
 */
public class ReflectionScaleDecoration extends RecyclerView.ItemDecoration {
    /**
     * VIEW 最小缩放倍数
     */
    private static float MIN_SCALE = 0.9f;
    private RecyclerView mRecyclerView;
    private boolean init = false;

    /**
     * RecyclerView 的每次滚动都会调用，适合做滑动动画
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int childCount = layoutManager.getChildCount();
        if (mRecyclerView != parent) {
            mRecyclerView = parent;
            //计算需要的缓存大小
            View firstChild = layoutManager.getChildAt(0);
            Bitmap bitmap = convertViewToBitmap(firstChild);
            int byteCount = bitmap.getByteCount();
            bitmap.recycle();
            //设置阴影缓存大小
            mBitmapCache = new BitmapCache((childCount + 1) * byteCount);
            //注册条目更新监听，为了重新生产阴影
            mRecyclerView.getAdapter().registerAdapterDataObserver(
                    new RecyclerView.AdapterDataObserver() {
                        @Override
                        public void onChanged() {
                            mBitmapCache.evictAll();
                        }
                    });
            mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    init = true;
                    mBitmapCache.evictAll();
                }
            });
        }
        if (!init) {
            //布局完成前不绘制
            return;
        }
        //当前显示的所有条目

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
            drawReflection(c, child, rect, scale);
        }
    }

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private void drawReflection(Canvas c, View view, Rect rect, float scale) {
        //绘制倒影
        Bitmap reflectionBitmap = getReflectionBitmap(view);
        Rect srcRect = new Rect(0, 0,
                reflectionBitmap.getWidth(),
                reflectionBitmap.getHeight());
        Rect dstRect = new Rect(0, 0,
                (int) (rect.width() * scale + 0.5f),
                (int) (rect.height() * scale + 0.5f));
        c.save();
        c.translate(rect.left, rect.top);
        c.drawBitmap(reflectionBitmap, srcRect, dstRect, paint);
        c.restore();
        //3.画笔使用LinearGradient 线性渐变渲染
        LinearGradient lg = new LinearGradient(0, 0,
                0, rect.height() * scale + 1,
                0x70ffffff,
                0x00ffffff,
                Shader.TileMode.MIRROR);
        paint.setShader(lg);
        //4.指定画笔的Xfermode 即绘制的模式（不同的模式，绘制的区域不同）
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //5.在倒立图区，绘制矩形渲染图层
        c.drawRect(rect.left, rect.top,
                rect.left + rect.width() * scale + 0.5f,
                rect.top + rect.height() * scale + 0.5f, paint);
        paint.setXfermode(null);
    }

    private Bitmap convertViewToBitmap(View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }

    private Bitmap getReflectionBitmap(View view) {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        int position = layoutManager.getPosition(view);
        Bitmap reflectionBitmap = mBitmapCache.get(position);
        if (reflectionBitmap == null) {
            Bitmap sourceBitmap = convertViewToBitmap(view);
            //1.倒立图
            Matrix matrix = new Matrix();
            //以X轴向下翻转
            int width = view.getWidth();
            int height = view.getHeight() / 2;
            matrix.preScale(1, -1);
            //生成倒立图，宽度和原图一致，高度为原图的一半
            reflectionBitmap = Bitmap.createBitmap(
                    sourceBitmap, 0, height, width,
                    height, matrix, false);
            mBitmapCache.put(position, reflectionBitmap);
        }
        return reflectionBitmap;
    }


    private BitmapCache mBitmapCache;

    private class BitmapCache extends LruCache<Integer, Bitmap> {

        private BitmapCache(int size) {
            super(size);
        }

        @Override
        protected int sizeOf(Integer key, Bitmap value) {
            return value.getByteCount();
        }

        @Override
        protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
            oldValue.recycle();
        }
    }
}
