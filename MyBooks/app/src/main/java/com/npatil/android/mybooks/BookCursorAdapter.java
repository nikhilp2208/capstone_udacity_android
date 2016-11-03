package com.npatil.android.mybooks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.npatil.android.mybooks.data.BooksContract;
import com.squareup.picasso.Picasso;

/**
 * Created by nikhil.p on 02/11/16.
 */

public class BookCursorAdapter extends CursorRecyclerViewAdapter<BookCursorAdapter.ViewHolder> {

    private static Context mContext;

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_TITLE));
        viewHolder.title.setText((title!=null?title:"Title NA"));
        //TODO Change this author
        String author = cursor.getString(cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_SUBTITLE));
        viewHolder.author.setText((author!=null?author:"Author NA"));
        Picasso.with(mContext)
                .load(cursor.getString(cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_COVER_PATH)))
                .error(R.drawable.no_image)
                .into(viewHolder.thumbnail);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,BookDetailActivity.class);
                intent.setData(BooksContract.BooksEntry.buildBooksUri(getItemId(viewHolder.getAdapterPosition())));
                mContext.startActivity(intent);
            }
        });
        return viewHolder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView thumbnail;
        public final TextView title;
        public final TextView author;
        public ViewHolder(View itemView){
            super(itemView);
            itemView.setFocusable(true);
            itemView.setClickable(true);
            thumbnail = (ScaleImageView) itemView.findViewById(R.id.thumbnail);
            title = (TextView) itemView.findViewById(R.id.book_title);
            author = (TextView) itemView.findViewById(R.id.book_author);
        }
    }
}
