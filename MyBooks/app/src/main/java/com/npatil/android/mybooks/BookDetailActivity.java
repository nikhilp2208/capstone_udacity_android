package com.npatil.android.mybooks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class BookDetailActivity extends AppCompatActivity {

    final String LOG_TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,getIntent().getData().toString());
        setContentView(R.layout.activity_book_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(BookDetailFragment.DETAIL_URI, getIntent().getData());

            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_book_detail, fragment)
                    .commit();

            // Being here means we are in animation mode
//            supportPostponeEnterTransition();
        }
    }
}
