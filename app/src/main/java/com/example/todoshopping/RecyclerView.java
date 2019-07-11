package com.example.todoshopping;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RecyclerView extends android.support.v7.widget.RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ThingToBuy> item;
    private Context context;

    public RecyclerView(List<ThingToBuy> item, Context context) {
        this.item = item;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
        final ImageView imageView = itemView.findViewById(R.id.check);

        itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup.MarginLayoutParams marginLayoutParams =
                                (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
                        if (imageView.getVisibility() == View.VISIBLE) {
                            imageView.setVisibility(View.INVISIBLE);
                            itemView.setAlpha((float) 1.0);
                            marginLayoutParams.setMargins(6, 0, 0, 0);
                            itemView.setLayoutParams(marginLayoutParams);
                            ((MainActivity) context).removeFromSelected((ThingToBuy) itemView.getTag());
                        }
                        else {
                            imageView.setColorFilter(Color.parseColor("#496a94"));
                            imageView.setVisibility(View.VISIBLE);
                            itemView.setAlpha((float) 0.4);
                            marginLayoutParams.setMargins(14, 0, 0, 0);
                            itemView.setLayoutParams(marginLayoutParams);
                            ((MainActivity) context).addToSelected((ThingToBuy) itemView.getTag());
                        }
                    }
                }
        );
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ThingToBuy s = item.get(position);
        holder.item.setText(s.name);
        View itemView = holder.itemView;
        itemView.setTag(s);
        itemView.setAlpha((float) 1.0);
        ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
        marginLayoutParams.setMargins(6, 0, 0, 0);
        ImageView imageView = itemView.findViewById(R.id.check);
        imageView.setVisibility(View.INVISIBLE);
    }

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public TextView item;

        public ViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
        }
    }
}
