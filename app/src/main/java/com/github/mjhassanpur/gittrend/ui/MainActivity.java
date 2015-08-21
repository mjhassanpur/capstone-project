package com.github.mjhassanpur.gittrend.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import com.github.mjhassanpur.gittrend.sync.SyncAdapter;
import com.github.mjhassanpur.gittrend.ui.misc.RecyclerItemClickListener;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REPO_LOADER = 0;

    private static final String[] REPO_COLUMNS = {
            RepoContract.RepoEntry.TABLE_NAME + "." + RepoContract.RepoEntry._ID,
            RepoContract.RepoEntry.COLUMN_FULL_NAME,
            RepoContract.RepoEntry.COLUMN_DESCRIPTION,
            RepoContract.RepoEntry.COLUMN_STARS,
            RepoContract.RepoEntry.COLUMN_FORKS,
            RepoContract.RepoEntry.COLUMN_LANGUAGE
    };

    private static final int COL_REPO_ID = 0;
    private static final int COL_REPO_FULL_NAME = 1;
    private static final int COL_REPO_DESCRIPTION = 2;
    private static final int COL_REPO_STARS= 3;
    private static final int COL_REPO_FORKS = 4;
    private static final int COL_REPO_LANGUAGE = 5;

    private RecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewCompat.setElevation(toolbar, getResources().getDimension(R.dimen.toolbar_elevation));
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        setupRecyclerView(rv);

        SyncAdapter.initializeSyncAdapter(this);
        getSupportLoaderManager().initLoader(REPO_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        String sortOrder = RepoContract.RepoEntry.COLUMN_STARS + " DESC";

        return new CursorLoader(this,
                RepoContract.RepoEntry.CONTENT_URI,
                REPO_COLUMNS,
                null,
                null,
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
            int repoId = cursor.getInt(COL_REPO_ID);
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra(DetailActivity.KEY_REPO_ID, repoId);
            startActivity(intent);
        }
    }

    private static class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private Cursor mCursor;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mNameView;
            private final TextView mDescriptionView;
            private final TextView mStarsView;
            private final TextView mForksView;
            private final TextView mLanguageView;

            public ViewHolder(View view) {
                super(view);
                mNameView = (TextView) view.findViewById(R.id.repo_name);
                mDescriptionView = (TextView) view.findViewById(R.id.repo_description);
                mStarsView = (TextView) view.findViewById(R.id.stars);
                mForksView = (TextView) view.findViewById(R.id.forks);
                mLanguageView = (TextView) view.findViewById(R.id.language);
            }
        }

        private RecyclerViewAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.repo_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            mCursor.moveToPosition(position);

            String fullName = mCursor.getString(COL_REPO_FULL_NAME);
            String description = mCursor.getString(COL_REPO_DESCRIPTION);
            int stars = mCursor.getInt(COL_REPO_STARS);
            int forks = mCursor.getInt(COL_REPO_FORKS);
            String language = mCursor.getString(COL_REPO_LANGUAGE);

            holder.mNameView.setText(fullName);
            holder.mDescriptionView.setText(description);
            holder.mStarsView.setText(String.valueOf(stars));
            holder.mForksView.setText(String.valueOf(forks));
            holder.mLanguageView.setText(language);
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
