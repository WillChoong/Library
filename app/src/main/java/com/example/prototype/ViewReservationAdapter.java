package com.example.prototype;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ViewReservationAdapter extends FirestoreRecyclerAdapter<reservation,ViewReservationAdapter.Holder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ViewReservationAdapter(@NonNull FirestoreRecyclerOptions<reservation> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull Holder holder, int position, @NonNull reservation model) {
        holder.date.setText(model.getDate());
        holder.time.setText(model.getTime());
        holder.floor.setText(model.getFloor());
        holder.seat.setText(model.getSeatID());
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reservation_item,parent,false);
        return new Holder(v);
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView date,time,floor,seat;
        CardView cv;
        public Holder(@NonNull View itemView) {
            super(itemView);
            cv  = itemView.findViewById(R.id.layout_cardView);
            date = itemView.findViewById(R.id.tv_date);
            time  = itemView.findViewById(R.id.tv_time);
            floor = itemView.findViewById(R.id.tv_floor);
            seat = itemView.findViewById(R.id.tv_seat);
        }
    }
}
