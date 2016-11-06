package com.npatil.android.mybooks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.npatil.android.mybooks.data.BooksContract;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookListActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOKS_LOADER = 0;

    private static final String[] BOOK_COLUMNS = {
            BooksContract.BooksEntry._ID,
            BooksContract.BooksEntry.COLUMN_BOOK_ID,
            BooksContract.BooksEntry.COLUMN_ISBN10,
            BooksContract.BooksEntry.COLUMN_ISBN13,
            BooksContract.BooksEntry.COLUMN_LIST_ID,
            BooksContract.BooksEntry.COLUMN_TITLE,
            BooksContract.BooksEntry.COLUMN_SUBTITLE,
            BooksContract.BooksEntry.COLUMN_DESCRIPTION,
            BooksContract.BooksEntry.COLUMN_COVER_PATH,
            BooksContract.BooksEntry.COLUMN_RATING,
            BooksContract.BooksEntry.COLUMN_COMMENT
    };

    final String LOG_TAG = this.getClass().getSimpleName();
    private Context mContext;
    private BookCursorAdapter mCursorAdapter;
    private Cursor mCursor;
    private RecyclerView mRecyclerView;
    private String[] mNavDrawerStrings;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Toolbar mToolbar;
    boolean isConnected;

    String mListId;

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

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

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
                                        sendAddBookHit(input.toString());
                                    }
                                }
                            }).show();
                } else {
                    networkToast();
                }

            }
        });

        mNavDrawerStrings = getResources().getStringArray(R.array.navigation_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mNavDrawerStrings));

        //Initially pointing to first item in the NavDrawerList
        mDrawerList.setItemChecked(0,true);
        mListId = mNavDrawerStrings[0];

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerList.setItemChecked(position, true);
                mListId = ((TextView) view).getText().toString();
                getSupportLoaderManager().restartLoader(BOOKS_LOADER,null,BookListActivity.this);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });



        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportLoaderManager().initLoader(BOOKS_LOADER, null, this);
        mCursorAdapter = new BookCursorAdapter(this, null);

        mRecyclerView.setAdapter(mCursorAdapter);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
                Log.d(LOG_TAG, "navigation clicked");
            }
        });

        MobileAds.initialize(getApplicationContext(),getString(R.string.ad_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        ((MyApplication) getApplication()).startTracking();
    }

    private void sendAddBookHit(String bookId) {
        Tracker tracker = ((MyApplication) getApplication()).getTracker();

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Book actions")
                .setAction("Add books")
                .setLabel(bookId)
                .build());
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case BOOKS_LOADER:
                return new CursorLoader(this, BooksContract.BooksEntry.CONTENT_URI,BOOK_COLUMNS,BooksContract.BooksEntry.COLUMN_LIST_ID + "= ?",new String[] {mListId},null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case BOOKS_LOADER: {
                Log.v("ON_BOOK_LOAD_FINISHED", Integer.toString(data.getCount()));
                handleOnLoadFinished(data);
                break;
            }
        }
    }

    private void handleOnLoadFinished(Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
        Utils.updateWidgets(mContext);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        public ImageView thumbnailView;
//        public TextView titleView;
//        public TextView authorView;
//
//        public ViewHolder(View view) {
//            super(view);
//            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
//            titleView = (TextView) view.findViewById(R.id.book_title);
//            authorView = (TextView) view.findViewById(R.id.book_author);
//        }
//    }

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
                if (book!=null) Log.i(LOG_TAG,book.toString());
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
