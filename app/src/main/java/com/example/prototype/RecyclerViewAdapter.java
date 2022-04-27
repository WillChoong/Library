package com.example.prototype;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.Holder> {

    List<reservation> list;
    Context context;
    final private ClickListener aOnClickListener;

    interface ClickListener{
        void onListItemClick(int position, ColorStateList colorStateList);
    }


    public RecyclerViewAdapter(List<reservation> list, Context context, ClickListener aOnClickListener) {
        this.list = list;
        this.context = context;
        this.aOnClickListener = aOnClickListener;
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView date,time,floor,seat;
        CardView cv;
        public Holder(@NonNull View itemView)
        {
            super(itemView);
            cv  = itemView.findViewById(R.id.layout_cardView);
            date = itemView.findViewById(R.id.tv_date);
            time  = itemView.findViewById(R.id.tv_time);
            floor = itemView.findViewById(R.id.tv_floor);
            seat = itemView.findViewById(R.id.tv_seat);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            ColorStateList colorStateList = cv.getCardBackgroundColor();
            aOnClickListener.onListItemClick(position,colorStateList);
        }
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.reservation_item,parent,false);
        return new Holder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.Holder holder, int position) {
        reservation reservation = list.get(position);
        ComputeReservation cr = new ComputeReservation();
        Boolean compare =cr.compareDateAndTime(reservation.getDate(), reservation.getTime());
        holder.cv.setCardBackgroundColor(Color.WHITE);
        reservation.setColor(Color.WHITE);
        if(compare == false)
        {
            holder.cv.setCardBackgroundColor(Color.GRAY);
            reservation.setColor(Color.GRAY);
        }
        if(reservation.getCheckOut() == true)
        {
            holder.cv.setCardBackgroundColor(Color.GRAY);
            reservation.setColor(Color.DKGRAY);
        }
        holder.date.setText(reservation.getDate());
        holder.time.setText(reservation.getTime());
        holder.floor.setText(reservation.getFloor());
        holder.seat.setText(reservation.getSeatID());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
