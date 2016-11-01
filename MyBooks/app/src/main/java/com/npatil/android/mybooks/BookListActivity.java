package com.npatil.android.mybooks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npatil.android.mybooks.data.BooksContract;
import com.npatil.android.mybooks.data.BooksDbHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookListActivity extends AppCompatActivity {
    final String LOG_TAG = this.getClass().getSimpleName();
    private Context mContext;

    boolean isConnected;

    String mListId = "Reading Now";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        setContentView(R.layout.activity_book_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Log.d("fdfgdfg","clicked!");
                if (isConnected){
                    new MaterialDialog.Builder(mContext).title(R.string.add_book)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the book doesn't already exist
                                    // in the DB and proceed accordingly
                                    //TODO: Add ISBN validation here
                                    Cursor c10 = getContentResolver().query(BooksContract.BooksEntry.CONTENT_URI,
                                            new String[] { BooksContract.BooksEntry.COLUMN_BOOK_ID}, BooksContract.BooksEntry.COLUMN_ISBN10 + "= ?",
                                            new String[] { input.toString() }, null);
                                    Cursor c13 = getContentResolver().query(BooksContract.BooksEntry.CONTENT_URI,
                                            new String[] { BooksContract.BooksEntry.COLUMN_BOOK_ID}, BooksContract.BooksEntry.COLUMN_ISBN13 + "= ?",
                                            new String[] { input.toString() }, null);
                                    if (c10.getCount() != 0 || c13.getCount() != 0) {
                                        Toast toast =
                                                Toast.makeText(BookListActivity.this, getString(R.string.book_already_saved_toast),
                                                        Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                        return;
                                    } else {
                                        Log.d("fdfgdfg","gfghfghfgh");
                                        new FetchBookData().execute(input.toString());
                                        //TODO: Add the book to DB
                                    }
                                }
                            }).show();
                } else {
                    networkToast();
                }

            }
        });
    }

    public void networkToast(){
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView authorView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.book_title);
            authorView = (TextView) view.findViewById(R.id.book_author);
        }
    }

    public void addBookToDb(Book book, String listId) {
        ContentValues bookValues = new ContentValues();

        bookValues.put(BooksContract.BooksEntry.COLUMN_BOOK_ID, book.getBookId());
        bookValues.put(BooksContract.BooksEntry.COLUMN_ISBN10, book.getIsbn10());
        bookValues.put(BooksContract.BooksEntry.COLUMN_ISBN13, book.getIsbn13());
        bookValues.put(BooksContract.BooksEntry.COLUMN_TITLE, book.getTitle());
        bookValues.put(BooksContract.BooksEntry.COLUMN_SUBTITLE, book.getSubtitle());
        bookValues.put(BooksContract.BooksEntry.COLUMN_DESCRIPTION, book.getDescription());
        bookValues.put(BooksContract.BooksEntry.COLUMN_COVER_PATH, book.getCoverPath());
        bookValues.put(BooksContract.BooksEntry.COLUMN_RATING, book.getRating());
        bookValues.put(BooksContract.BooksEntry.COLUMN_COMMENT, book.getComment());
        bookValues.put(BooksContract.BooksEntry.COLUMN_LIST_ID,listId);

        Uri insertedUri = getContentResolver().insert(BooksContract.BooksEntry.CONTENT_URI, bookValues);

        long insertedId = ContentUris.parseId(insertedUri);

        Log.d(LOG_TAG,"inserted id: "+insertedId);

    }

    private class FetchBookData extends AsyncTask<String, Void, Book> {
        private OkHttpClient httpClient = new OkHttpClient();
        final String OPENLIB_BASE_URL = "http://openlibrary.org/api/books?";
        final String LOG_TAG = this.getClass().getSimpleName();

        @Override
        protected Book doInBackground(String... params) {
            String isbn = params[0];
            Uri builtUri = Uri.parse(OPENLIB_BASE_URL).buildUpon()
                    .appendQueryParameter("bibkeys", "ISBN:"+isbn)
                    .appendQueryParameter("jscmd", "details")
                    .appendQueryParameter("format", "json")
                    .build();

            try {
                URL url = new URL(builtUri.toString());
                Request request = new Request.Builder().url(url).build();
                Response response = null;
                response = httpClient.newCall(request).execute();
                Book book = Utils.JsonToBook(response.body().string());
                Log.i(LOG_TAG,book.toString());
                return book;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Book book) {
            super.onPostExecute(book);
            if (book == null) {
                Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
                return;
            }
            addBookToDb(book,mListId);
        }
    }
}
