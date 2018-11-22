package cn.leo.leobanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author : Jarry Leo
 * @date : 2018/11/16 11:23
 */
public class RvAdapter extends RecyclerView.Adapter<RvHolder> {
    int[] mm = {R.mipmap.mm01, R.mipmap.mm02, R.mipmap.mm03, R.mipmap.mm04, R.mipmap.mm05};

    @NonNull
    @Override
    public RvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new RvHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RvHolder holder, int position) {
        holder.setIdText(position + 1 + "/" + getItemCount());
        holder.setImage(mm[position]);
        System.out.println("加载图片" + position);
    }

    @Override
    public int getItemCount() {
        return mm.length;
    }
}
