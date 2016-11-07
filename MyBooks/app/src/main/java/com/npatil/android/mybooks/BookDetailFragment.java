package com.npatil.android.mybooks;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.npatil.android.mybooks.data.BooksContract;
import com.squareup.picasso.Picasso;

public class BookDetailFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    final String LOG_TAG = this.getClass().getSimpleName();
    private static final int BOOK_DETAILS_LOADER = 0;
    static final String DETAIL_URI = "URI";
    private static final String PREFIX_ISBN10 = "ISBN10: ";
    private static final String PREFIX_ISBN13 = "ISBN13: ";

    Uri mBookUri;

    private String mBookId;
    private String mCoverPath;
    private String mTitle;
    private String mIsbn10;
    private String mIsbn13;
    private String mSubtitle;
    private String mDescription;
    private String mComment;
    private Integer mRating;

    private View mRootView;
    private Toolbar mToolbar;
    ImageView mCoverImageView;
    TextView mTitleTextView;
    TextView mSubtitleTextView;
    TextView mAuthorTextView;
    TextView mIsbn10TextView;
    TextView mIsbn13TextView;
    TextView mDescriptionTextView;
    EditText mCommentsEditText;
    SimpleRatingBar mRatingBar;
    Toolbar detailToolbar;
    Button mUpdateButton;


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
        mToolbar = (Toolbar) mRootView.findViewById(R.id.detail_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

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
        mCommentsEditText = (EditText) mRootView.findViewById(R.id.detail_book_comment);
        mCommentsEditText.setTag(mCommentsEditText.getKeyListener());
        mCommentsEditText.setKeyListener(null);
        mRatingBar = (SimpleRatingBar) mRootView.findViewById(R.id.detail_book_rating);
        mUpdateButton = (Button) mRootView.findViewById(R.id.detail_update_button);

        mCommentsEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsEditText.setKeyListener((KeyListener) mCommentsEditText.getTag());;
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCommentAndRating();
                mCommentsEditText.setTag(mCommentsEditText.getKeyListener());
                mCommentsEditText.setKeyListener(null);
            }
        });

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareBookInfo();
            }
        });

        return mRootView;
    }

    private void shareBookInfo() {
        String shareText;
        String title = ((mTitle!=null)?mTitle:"NA");
        String rating = (mRating!=null && mRating!=0)? mRating.toString():"NA";
        String comment = ((mComment!=null && !mComment.isEmpty())?mComment:"NA");

        shareText = getString(R.string.share_text_intro) + "\"" +title + "\"" + "\n"
                + getString(R.string.share_text_rating) + rating + getString(R.string.share_text_stars) + "\n"
                + getString(R.string.share_text_comment) + "\"" + comment + "\"";
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText(shareText)
                .getIntent(), getString(R.string.action_share)));
    }

    private void updateCommentAndRating() {
        Uri updateUri = BooksContract.BooksEntry.buildBooksUriWithBookId(mBookId);

        ContentValues bookValues = new ContentValues();
        bookValues.put(BooksContract.BooksEntry.COLUMN_RATING, Math.round(mRatingBar.getRating()));
        if (mCommentsEditText.getText() != null) bookValues.put(BooksContract.BooksEntry.COLUMN_COMMENT, mCommentsEditText.getText().toString());

        Log.i(LOG_TAG,"updateUri: "+ updateUri);
        Log.i(LOG_TAG,"contentValues: "+bookValues);
        getActivity().getContentResolver().update(updateUri,bookValues,null,null);
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

            mBookId = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_BOOK_ID));
            mCoverPath = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_COVER_PATH));
            mTitle = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_TITLE));
            mIsbn10 = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_ISBN10));
            mIsbn13 = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_ISBN13));
            mSubtitle = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_SUBTITLE));
            mDescription = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_DESCRIPTION));
            mComment = data.getString(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_COMMENT));
            if(!data.isNull(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_RATING))) {
                mRating = data.getInt(data.getColumnIndex(BooksContract.BooksEntry.COLUMN_RATING));
                Log.i(LOG_TAG,"rating: "+ mRating);
            }

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
            if (mComment != null) mCommentsEditText.setText(mComment);
            if (mRating != null) mRatingBar.setRating(mRating);

        }
        Log.d(LOG_TAG,"data null");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
