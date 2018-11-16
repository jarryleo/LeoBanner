package cn.leo.banner;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Jarry Leo
 * @date : 2018/11/13 9:35
 */
public class LeoBanner extends FrameLayout {
    /**
     * 利用recyclerView实现轮播
     */
    private RecyclerView mRecyclerView;
    /**
     * 是否自动轮播
     */
    private boolean mIsAutoScroll;

    /**
     * 条目宽度占空间宽度比值
     */
    private float mItemWidthRatio = 0.8f;
    /**
     * 条目宽
     */
    private float mItemWidth;
    /**
     * 自动轮播间隔
     */
    private float mScrollInterval = 3000;
    /**
     * 存储所有图片路径
     */
    private List<String> mImages = new ArrayList<>();
    /**
     * 图片适配器
     */
    private ItemAdapter mAdapter;
    /**
     * 图片加载器
     */
    private ImageLoader mImageLoader;
    /**
     * 点击事件
     */
    private OnItemClickListener mOnItemClickListener;

    private Handler mHandler;


    public LeoBanner(@NonNull Context context) {
        this(context, null);
    }

    public LeoBanner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeoBanner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initRecyclerView();
        initHandler();
    }

    private void initHandler() {
        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    private void autoScroll() {

    }

    private void initRecyclerView() {
        mRecyclerView = new RecyclerView(getContext());
        mAdapter = new ItemAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        addView(mRecyclerView);
        //条目居中显示
        new LinearSnapHelper().attachToRecyclerView(mRecyclerView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });
        mRecyclerView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mItemWidth = (right - left) * mItemWidthRatio;
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置轮播的图片地址数组
     *
     * @param images 图片地址集合
     */
    public void setImages(List<String> images, ImageLoader imageLoader) {
        mImageLoader = imageLoader;
        mImages.clear();
        mImages.addAll(images);
        mAdapter.notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setAutoScroll(boolean autoScroll) {
        mIsAutoScroll = autoScroll;
    }

    public void setItemWidthRatio(float itemWidthRatio) {
        mItemWidthRatio = itemWidthRatio;
    }

    class ItemAdapter extends RecyclerView.Adapter<ItemHolder> implements OnClickListener {

        private int mPosition;

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return new ItemHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            holder.mImageView.setLayoutParams(new RecyclerView.LayoutParams(
                    (int) (mItemWidth + 0.5f),
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mPosition = position;
            holder.mImageView.setOnClickListener(this);
            holder.loadImage(mImages.get(position));
        }

        @Override
        public int getItemCount() {
            return mImages.size();
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(mPosition);
            }
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        ItemHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
        }

        public void loadImage(String path) {
            if (mImageLoader != null) {
                mImageLoader.loadImage(mImageView, path);
            }
        }
    }

    /**
     * 图片加载器用户自己实现
     */
    public interface ImageLoader {
        /**
         * 加载图片
         *
         * @param imageView 图片控件
         * @param imagePath 图片路径
         */
        void loadImage(ImageView imageView, String imagePath);
    }

    public interface OnItemClickListener {
        /**
         * 条目点击事件
         *
         * @param position 点击的序号
         */
        void onItemClick(int position);
    }
}
