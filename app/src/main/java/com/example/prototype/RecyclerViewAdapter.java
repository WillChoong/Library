package com.example.prototype;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.Holder> {

    ArrayList<Reservation> list;
    Context context;

    public RecyclerViewAdapter(ArrayList<Reservation> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public static class Holder extends RecyclerView.ViewHolder{
        TextView date,time,floor,seat;
        public Holder(@NonNull View itemView)
        {
            super(itemView);
            date = itemView.findViewById(R.id.tv_date);
            time  = itemView.findViewById(R.id.tv_time);
            floor = itemView.findViewById(R.id.tv_floor);
            seat = itemView.findViewById(R.id.tv_seat);
        }
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.reservation_item,parent,false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.Holder holder, int position) {
        Reservation reservation = list.get(position);
        holder.date.setText(reservation.Date);
        holder.time.setText(reservation.Time);
        holder.floor.setText(reservation.Floor);
        holder.seat.setText(reservation.SeatID);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
