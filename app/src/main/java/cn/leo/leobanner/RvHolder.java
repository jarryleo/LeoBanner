package cn.leo.leobanner;

import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * @author : Jarry Leo
 * @date : 2018/11/16 11:24
 */
public class RvHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;
    private final ImageView mImageView;

    public RvHolder(View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.tvId);
        mImageView = itemView.findViewById(R.id.ivMM);
    }

    public void setIdText(String text) {
        mTextView.setText(text);
    }

    public void setImage(@DrawableRes int imageResId) {
        Glide.with(itemView.getContext()).load(imageResId).into(mImageView);
    }
}
