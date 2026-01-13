package com.example.projet_tp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.model.OrderItem;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<OrderItem> items;
    private OnItemQuantityChangedListener listener;

    public interface OnItemQuantityChangedListener {
        void onQuantityChanged(String menuId, int quantity);
        void onItemRemoved(String menuId);
    }

    public CartAdapter(List<OrderItem> items, OnItemQuantityChangedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateItems(List<OrderItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMenuName;
        private TextView textViewPrice;
        private TextView textViewQuantity;
        private TextView textViewTotal;
        private ImageButton buttonDecrease;
        private ImageButton buttonIncrease;
        private ImageButton buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMenuName = itemView.findViewById(R.id.textViewMenuName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }

        public void bind(OrderItem item) {
            textViewMenuName.setText(item.getMenuName());
            
            DecimalFormat df = new DecimalFormat("#.##");
            textViewPrice.setText(df.format(item.getPrice()) + " TND");
            textViewQuantity.setText(String.valueOf(item.getQuantity()));
            textViewTotal.setText(df.format(item.getTotalPrice()) + " TND");

            buttonDecrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (listener != null) {
                    listener.onQuantityChanged(item.getMenuId(), newQuantity);
                }
            });

            buttonIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChanged(item.getMenuId(), newQuantity);
                }
            });

            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item.getMenuId());
                }
            });
        }
    }
}


