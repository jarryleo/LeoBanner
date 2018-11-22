package cn.leo.banner.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

/**
 * Created by JarryLeo on 2017/4/20.
 */

@SuppressLint("AppCompatCustomView")
public class RhombusImageView extends ImageView {

    private Bitmap bitmap;
    private Paint paint;
    private Paint borderPaint;
    private Matrix matrix;
    private float radius;
    private int viewWidth;
    private int bitmapWidth;
    private float mBorderWidth;
    private int mBorderSize;

    public RhombusImageView(Context context) {
        this(context, null);
    }

    public RhombusImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RhombusImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initBitmap();
    }

    private void initPaint() {
        //图形画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //边框画笔
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //模式为画边框
        borderPaint.setStyle(Paint.Style.STROKE);
        //边框粗细
        borderPaint.setStrokeWidth(mBorderWidth);
        matrix = new Matrix();
    }

    private void initBitmap() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap srcBitmap = drawableToBitmap(drawable);
            //获取图片宽高最小值
            bitmapWidth = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());
            //获取图片中间矩形部分
            int x = (srcBitmap.getWidth() - bitmapWidth) / 2;
            int y = (srcBitmap.getHeight() - bitmapWidth) / 2;
            bitmap = Bitmap.createBitmap(srcBitmap, x, y, bitmapWidth, bitmapWidth);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureSize(widthMeasureSpec);
        int height = measureSize(heightMeasureSpec);
        //View边长
        viewWidth = Math.min(width, height);
        //边框大小
        mBorderSize = (int) (viewWidth + (mBorderWidth * 2 + 0.5));
        //改变本控件宽高为正方形
        int measureSpec = MeasureSpec.makeMeasureSpec(mBorderSize, MeasureSpec.EXACTLY);
        setMeasuredDimension(measureSpec, measureSpec);
        //圆形图片半径，不包含边框
        radius = viewWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //计算图片缩放比例
        float scaleRatio = getMeasuredWidth() * 1.0f / bitmapWidth;
        //图片着色器
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        //着色器矩阵缩放
        matrix.setScale(scaleRatio, scaleRatio);
        //着色器设置矩阵
        bitmapShader.setLocalMatrix(matrix);
        //着色器赋值给画笔
        paint.setShader(bitmapShader);
        //画圆
        //canvas.drawCircle(mBorderSize / 2, mBorderSize / 2, radius, paint);
        //画边框
        //canvas.drawCircle(mBorderSize / 2, mBorderSize / 2, radius, borderPaint);
        Path path = new Path();
        path.moveTo(200, 0);
        path.lineTo(viewWidth, 0);
        path.lineTo(viewWidth - 200, viewWidth);
        path.lineTo(0, viewWidth);
        path.close();
        canvas.drawPath(path, paint);
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initBitmap();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        initBitmap();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initBitmap();
    }

    /**
     * 设置边框宽度 dp
     *
     * @param borderWidth
     */
    public void setBorderWidth(float borderWidth) {
        mBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
                borderWidth, getResources().getDisplayMetrics());
        borderPaint.setStrokeWidth(borderWidth);
        requestLayout();//请求改变大小
        invalidate();//重新绘制
    }

    /**
     * 设置边框的颜色
     *
     * @param borderColor 边框颜色
     */
    public void setBorderColor(int borderColor) {
        borderPaint.setColor(borderColor);
        invalidate();
    }

    private int measureSize(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int result = 0;
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = 1000;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(size, result);
            }
        }
        return result;
    }

    /**
     * Drawable转Bitmap
     *
     * @param drawable
     * @return bitmap
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
