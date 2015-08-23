package com.github.mjhassanpur.gittrend.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mjhassanpur.gittrend.GTApplication;
import com.github.mjhassanpur.gittrend.R;
import com.github.mjhassanpur.gittrend.data.RepoContract;
import com.github.mjhassanpur.gittrend.ui.misc.RecyclerItemClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public final String LOG_TAG = DetailActivity.class.getSimpleName();

    public static final String REPO_TRANSITION = "repo_transition";

    public static final String KEY_REPO_ID = "key_repo_id";
    public static final String KEY_REPO_URL = "key_repo_url";
    public static final String KEY_REPO_FULL_NAME = "key_repo_full_name";
    public static final String KEY_REPO_DESCRIPTION = "key_repo_description";
    public static final String KEY_REPO_STARS = "key_repo_stars";
    public static final String KEY_REPO_FORKS = "key_repo_forks";
    public static final String KEY_REPO_LANGUAGE = "key_repo_language";

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
    private String mRepoUrl;
    private String mRepoFullName;
    private String mRepoDescription;
    private int mRepoStars = -1;
    private int mRepoForks = -1;
    private String mRepoLanguage;


    private RecyclerViewAdapter mRecyclerViewAdapter;
    private Tracker mTracker;

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

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        setupRecyclerView(rv);

        // Obtain the shared Tracker instance.
        GTApplication application = (GTApplication) getApplication();
        mTracker = application.getDefaultTracker();

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mRepoId = intent.getIntExtra(KEY_REPO_ID, -1);
            mRepoUrl = intent.getStringExtra(KEY_REPO_URL);
            mRepoFullName = intent.getStringExtra(KEY_REPO_FULL_NAME);
            mRepoDescription = intent.getStringExtra(KEY_REPO_DESCRIPTION);
            mRepoStars = intent.getIntExtra(KEY_REPO_STARS, -1);
            mRepoForks = intent.getIntExtra(KEY_REPO_FORKS, -1);
            mRepoLanguage = intent.getStringExtra(KEY_REPO_LANGUAGE);
            updateRepoView();
        }

        getSupportLoaderManager().initLoader(CONTRIBUTOR_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (shareActionProvider != null ) {
            shareActionProvider.setShareIntent(createShareRepoIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

        return true;
    }

    private Intent createShareRepoIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                getResources().getString(R.string.share_message) + mRepoUrl);
        return shareIntent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = DetailActivity.class.getSimpleName();
        Log.i(LOG_TAG, "Screen name: " + name);
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_REPO_ID, mRepoId);
        outState.putString(KEY_REPO_URL, mRepoUrl);
        outState.putString(KEY_REPO_FULL_NAME, mRepoFullName);
        outState.putString(KEY_REPO_DESCRIPTION, mRepoDescription);
        outState.putInt(KEY_REPO_STARS, mRepoStars);
        outState.putInt(KEY_REPO_FORKS, mRepoForks);
        outState.putString(KEY_REPO_LANGUAGE, mRepoLanguage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mRepoId = savedInstanceState.getInt(KEY_REPO_ID);
        mRepoUrl = savedInstanceState.getString(KEY_REPO_URL);
        mRepoFullName = savedInstanceState.getString(KEY_REPO_FULL_NAME);
        mRepoDescription = savedInstanceState.getString(KEY_REPO_DESCRIPTION);
        mRepoStars = savedInstanceState.getInt(KEY_REPO_STARS);
        mRepoForks = savedInstanceState.getInt(KEY_REPO_FORKS);
        mRepoLanguage = savedInstanceState.getString(KEY_REPO_LANGUAGE);
        updateRepoView();
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        if (getResources().getBoolean(R.bool.isTablet)) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mRecyclerViewAdapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new OnItemClickListener(this)));
    }

    private void updateRepoView() {
        View view = findViewById(R.id.repo_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mRepoUrl));
                startActivity(intent);
            }
        });
        ViewCompat.setTransitionName(view, REPO_TRANSITION);
        ((TextView) view.findViewById(R.id.repo_name)).setText(mRepoFullName);
        ((TextView) view.findViewById(R.id.repo_description)).setText(mRepoDescription);
        ((TextView) view.findViewById(R.id.stars)).setText(String.valueOf(mRepoStars));
        ((TextView) view.findViewById(R.id.forks)).setText(String.valueOf(mRepoForks));
        ((TextView) view.findViewById(R.id.language)).setText(mRepoLanguage);
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
                commitStr = commitStr.concat(" commits");
            } else {
                commitStr = commitStr.concat(" commit");
            }

            holder.mNameView.setText(name);
            holder.mCommitsView.setText(commitStr);
            holder.mAdditionsView.setText("++" + String.valueOf(additions));
            holder.mDeletionsView.setText("--" + String.valueOf(deletions));
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
