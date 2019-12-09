package kz.video.watcher;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kz.video.MobileInfo;

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

