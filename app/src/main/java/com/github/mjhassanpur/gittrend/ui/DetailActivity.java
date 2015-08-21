package com.github.mjhassanpur.gittrend.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mjhassanpur.gittrend.R;
import com.github.mjhassanpur.gittrend.data.RepoContract;
import com.github.mjhassanpur.gittrend.ui.misc.RecyclerItemClickListener;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String KEY_REPO_ID = "key_repo_id";

    private static final int CONTRIBUTOR_LOADER = 1;

    private static final String[] CONTRIBUTOR_COLUMNS = {
            RepoContract.ContributorEntry.TABLE_NAME + "." + RepoContract.ContributorEntry._ID,
            RepoContract.ContributorEntry.COLUMN_NAME,
            RepoContract.ContributorEntry.COLUMN_AVATAR_URL,
            RepoContract.ContributorEntry.COLUMN_HTML_URL,
            RepoContract.ContributorEntry.COLUMN_COMMITS,
            RepoContract.ContributorEntry.COLUMN_ADDITIONS,
            RepoContract.ContributorEntry.COLUMN_DELETIONS
    };

    private static final int COL_CONTRIBUTOR_ID = 0;
    private static final int COL_CONTRIBUTOR_NAME = 1;
    private static final int COL_CONTRIBUTOR_AVATAR_URL = 2;
    private static final int COL_CONTRIBUTOR_HTML_URL= 3;
    private static final int COL_CONTRIBUTOR_COMMITS = 4;
    private static final int COL_CONTRIBUTOR_ADDITIONS = 5;
    private static final int COL_CONTRIBUTOR_DELETIONS = 6;

    private int mRepoId = -1;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewCompat.setElevation(toolbar, getResources().getDimension(R.dimen.toolbar_elevation));
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        setupRecyclerView(rv);

        if (savedInstanceState == null) {
            mRepoId = getIntent().getIntExtra(KEY_REPO_ID, -1);
        }

        getSupportLoaderManager().initLoader(CONTRIBUTOR_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(KEY_REPO_ID, mRepoId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mRepoId = savedInstanceState.getInt(KEY_REPO_ID);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mRecyclerViewAdapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new OnItemClickListener(this)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = RepoContract.ContributorEntry.COLUMN_REPO_KEY + " = ?";
        String[] selectionArgs = { String.valueOf(mRepoId) };
        String sortOrder = RepoContract.ContributorEntry.COLUMN_COMMITS + " DESC, "
                + RepoContract.ContributorEntry.COLUMN_ADDITIONS + " DESC";

        return new CursorLoader(this,
                RepoContract.ContributorEntry.CONTENT_URI,
                CONTRIBUTOR_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            Log.d(LOG_TAG, "Has data");
            mRecyclerViewAdapter.swapCursor(data);
        } else {
            Log.d(LOG_TAG, "Has no data");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerViewAdapter.swapCursor(null);
    }

    private class OnItemClickListener extends RecyclerItemClickListener.SimpleOnItemClickListener {

        private Context mContext;

        private OnItemClickListener(Context context) {
            mContext = context;
        }

        @Override
        public void onItemClick(View childView, int position) {
            Cursor cursor = mRecyclerViewAdapter.getCursor();
            cursor.moveToPosition(position);
            String url = cursor.getString(COL_CONTRIBUTOR_HTML_URL);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    private static class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private Cursor mCursor;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mNameView;
            private final TextView mCommitsView;
            private final TextView mAdditionsView;
            private final TextView mDeletionsView;

            public ViewHolder(View view) {
                super(view);
                mNameView = (TextView) view.findViewById(R.id.contributor_name);
                mCommitsView = (TextView) view.findViewById(R.id.commits);
                mAdditionsView = (TextView) view.findViewById(R.id.additions);
                mDeletionsView = (TextView) view.findViewById(R.id.deletions);
            }
        }

        private RecyclerViewAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contributor_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            mCursor.moveToPosition(position);

            String name = mCursor.getString(COL_CONTRIBUTOR_NAME);
            int commits = mCursor.getInt(COL_CONTRIBUTOR_COMMITS);
            int additions = mCursor.getInt(COL_CONTRIBUTOR_ADDITIONS);
            int deletions = mCursor.getInt(COL_CONTRIBUTOR_DELETIONS);

            String commitStr = String.valueOf(commits);
            if (commits > 1) {
                commitStr = commitStr.concat(" Commits");
            } else {
                commitStr = commitStr.concat(" Commit");
            }

            holder.mNameView.setText(name);
            holder.mCommitsView.setText(commitStr);
            holder.mAdditionsView.setText(String.valueOf(additions));
            holder.mDeletionsView.setText(String.valueOf(deletions));
        }

        @Override
        public int getItemCount() {
            if ( null == mCursor ) return 0;
            return mCursor.getCount();
        }

        public void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
        }

        public Cursor getCursor() {
            return mCursor;
        }
    }
}
