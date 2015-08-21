package com.github.mjhassanpur.gittrend.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.github.mjhassanpur.gittrend.R;
import com.github.mjhassanpur.gittrend.api.FullRepository;
import com.github.mjhassanpur.gittrend.api.GitHubRestApiClient;
import com.github.mjhassanpur.gittrend.sync.SyncAdapter;
import com.github.mjhassanpur.gittrend.ui.misc.RecyclerItemClickListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final String LOG_TAG = MainActivity.class.getSimpleName();

    private List<FullRepository> mFullRepositories;

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

        mFullRepositories = new ArrayList<>();
        final GitHubRestApiClient api = GitHubRestApiClient.getInstance();
        /**
         * @see <a href="http://stackoverflow.com/questions/30269011/chain-two-retrofit-observables-w-rxjava"></a>
         */
/*        api.getWebService()
                .repositories(getSearchQuery(), "stars", "desc", Config.CLIENT_ID, Config.CLIENT_SECRET)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Repositories, Observable<Repository>>() {
                    @Override
                    public Observable<Repository> call(Repositories repositories) {
                        return Observable.from(repositories.repositories);
                    }
                })
                .flatMap(new Func1<Repository, Observable<List<Contributor>>>() {
                             @Override
                             public Observable<List<Contributor>> call(Repository repository) {
                                 return api.getWebService().contributors(
                                         repository.owner.name,
                                         repository.name,
                                         Config.CLIENT_ID,
                                         Config.CLIENT_SECRET);
                             }
                         }, new Func2<Repository, List<Contributor>, FullRepository>() {
                             @Override
                             public FullRepository call(Repository repository, List<Contributor> contributors) {
                                 return new FullRepository(repository, contributors);
                             }
                         }
                )
                .subscribe(new Subscriber<FullRepository>() {
                    @Override
                    public void onCompleted() {
                        Log.d(LOG_TAG, "Data successfully retrieved");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, "Failed to retrieve data", e);
                    }

                    @Override
                    public void onNext(FullRepository fullRepository) {
                        mFullRepositories.add(fullRepository);
                    }
                });*/
    }

    private String getSearchQuery() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return "created:>=" + dateFormat.format(cal.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerViewAdapter(Arrays.asList(new String[50])));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new OnItemClickListener(this)));
    }

    private class OnItemClickListener extends RecyclerItemClickListener.SimpleOnItemClickListener {

        private Context mContext;

        private OnItemClickListener(Context context) {
            mContext = context;
        }

        @Override
        public void onItemClick(View childView, int position) {
            startActivity(new Intent(mContext, DetailActivity.class));
        }
    }

    private static class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<String> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
            }
        }

        private RecyclerViewAdapter(List<String> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.repo_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }

}
