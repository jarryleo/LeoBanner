package cn.leo.leobanner;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * @author : Jarry Leo
 * @date : 2018/11/16 11:24
 */
public class RvHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;

    public RvHolder(View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.tvId);
    }

    public void setIdText(int id) {
        mTextView.setText(String.valueOf(id));
    }
}
