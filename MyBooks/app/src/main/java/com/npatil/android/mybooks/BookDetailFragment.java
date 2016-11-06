package com.npatil.android.mybooks;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.npatil.android.mybooks.data.BooksContract;
import com.squareup.picasso.Picasso;

public class BookDetailFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    final String LOG_TAG = this.getClass().getSimpleName();
    private static final int BOOK_DETAILS_LOADER = 0;
    static final String DETAIL_URI = "URI";
    private static final String PREFIX_ISBN10 = "ISBN10: ";
    private static final String PREFIX_ISBN13 = "ISBN13: ";

    Uri mBookUri;

    private String mCoverPath;
    private String mTitle;
    private String mIsbn10;
    private String mIsbn13;
    private String mSubtitle;
    private String mDescription;

    private View mRootView;

    ImageView mCoverImageView;
    TextView mTitleTextView;
    TextView mSubtitleTextView;
    TextView mAuthorTextView;
    TextView mIsbn10TextView;
    TextView mIsbn13TextView;
    TextView mDescriptionTextView;
    TextView mCommentsTextView;
    RatingBar mRatingBar;
    Toolbar detailToolbar;


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

    public BookDetailFragment() {
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(BOOK_DETAILS_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_book_detail, container, false);


        Bundle arguments = getArguments();
        if (arguments != null) {
            mBookUri = arguments.getParcelable(BookDetailFragment.DETAIL_URI);
        } else {
            mBookUri = getActivity().getIntent().getData();
        }

        Log.i(LOG_TAG,mBookUri.toString());
        detailToolbar = (Toolbar) mRootView.findViewById(R.id.detail_toolbar);
        mCoverImageView = (ImageView) mRootView.findViewById(R.id.detail_cover);
        mTitleTextView = (TextView) mRootView.findViewById(R.id.detail_book_title);
        mSubtitleTextView = (TextView) mRootView.findViewById(R.id.detail_book_subtitle);
        mIsbn10TextView = (TextView) mRootView.findViewById(R.id.detail_book_isbn10);
        mIsbn13TextView = (TextView) mRootView.findViewById(R.id.detail_book_isbn13);
        mDescriptionTextView = (TextView) mRootView.findViewById(R.id.detail_book_description);
        mAuthorTextView = (TextView) mRootView.findViewById(R.id.detail_book_author);

        return mRootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mBookUri) {
            return new CursorLoader(getActivity(),mBookUri,BOOK_COLUMNS,null,null,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            Log.d(LOG_TAG,"data not null");
            Context context = getContext();

            mCoverPath = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_COVER_PATH));
            mTitle = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_TITLE));
            mIsbn10 = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_ISBN10));
            mIsbn13 = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_ISBN13));
            mSubtitle = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_SUBTITLE));
            mDescription = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_DESCRIPTION));

            Picasso.with(context)
                    .load(mCoverPath)
                    .error(R.drawable.no_image)
                    .into(mCoverImageView);

            mTitleTextView.setText(mTitle);
            detailToolbar.setTitle(mTitle);
            mIsbn10TextView.setText((mIsbn10 != null?PREFIX_ISBN10+mIsbn10:getString(R.string.na)));
            mIsbn13TextView.setText((mIsbn13 != null?PREFIX_ISBN13+mIsbn13:getString(R.string.na)));
            mDescriptionTextView.setText((mDescription != null?mDescription:getString(R.string.decription_not_available)));
            if (mSubtitle != null) mSubtitleTextView.setText(mSubtitle);

        }
        Log.d(LOG_TAG,"data null");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
