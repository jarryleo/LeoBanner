package cn.leo.leobanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @author : Jarry Leo
 * @date : 2018/11/16 11:23
 */
public class RvAdapter extends RecyclerView.Adapter<RvHolder> {

    @NonNull
    @Override
    public RvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        return new RvHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RvHolder holder, int position) {
        ImageView view = (ImageView) holder.itemView;
        view.setImageResource(R.mipmap.eee);
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
