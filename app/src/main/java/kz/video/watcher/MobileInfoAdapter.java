package kz.video.watcher;

import android.content.Context;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by tolya on 26.12.2017.
 */

public class MobileInfoAdapter extends RecyclerView.Adapter<MobileInfoAdapter.ViewHolder> {

    MobileInfo data;
    private Context context;

    public MobileInfoAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MobileInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutInfo = R.layout.recycler_view_spec;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutInfo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MobileInfoAdapter.ViewHolder holder, int position) {
        holder.tvCh.setText(data.getTech_spec().get(position));
    }

    @Override
    public int getItemCount() {
        return data.getTech_spec().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvCh;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCh = itemView.findViewById(R.id.tv_ch);
        }
    }

    public void setList(MobileInfo info) {
        data = info;
    }
}

