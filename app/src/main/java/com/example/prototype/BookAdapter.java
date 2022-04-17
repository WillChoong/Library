package com.example.prototype;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> implements Filterable{

    private List<BookData> booksList;
    private List<BookData> booksResultList;
    private static final String TAG = "Adapter Activity";
    final private ListItemClickListener mOnClickListener;

    @Override
    public Filter getFilter() {
        return filter;
    }

    interface ListItemClickListener{
        void onListItemClick(int position);
    }

    public BookAdapter(ListItemClickListener onClickListener){
        this.mOnClickListener = onClickListener;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView bookName,callNumber;
        BookViewHolder(View itemView)
        {
            super(itemView);
            bookName=(TextView) itemView.findViewById(R.id.textview_bookname);
            callNumber=(TextView) itemView.findViewById(R.id.textview_callnumber);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mOnClickListener.onListItemClick(position);
        }
    }

    BookAdapter(List<BookData> booksList, ListItemClickListener mOnClickListener)
    {
        this.booksList=booksList;
        booksResultList=new ArrayList<>(booksList);
        this.mOnClickListener = mOnClickListener;
    }

    @NonNull
    @Override
    public BookAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_list,parent,false);
        return new BookViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookAdapter.BookViewHolder holder, int position) {
        BookData bookData=booksList.get(position);
        holder.bookName.setText(bookData.getBookName());
        holder.callNumber.setText(bookData.getCallNumber());
    }

    @Override
    public int getItemCount() {
        //return 0;
        return booksList.size();
    }

    private Filter filter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            //return null;
            List<BookData> bookData=new ArrayList<>();
            if(charSequence==null||charSequence.length()==0)
            {
                bookData.clear();
            }
            else
            {
                String pattern=charSequence.toString().toLowerCase().trim();
                for(BookData book:booksResultList)
                {
                    if(book.getBookName().toLowerCase().contains(pattern))
                    {
                        booksList.clear();
                        booksList.add(book);
                    }
                }
            }
            FilterResults filterResults=new FilterResults();
            filterResults.values=booksList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            booksList.clear();
            notifyDataSetChanged();
        }
    };
}