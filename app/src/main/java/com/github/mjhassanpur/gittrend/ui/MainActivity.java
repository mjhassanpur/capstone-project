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
import com.github.mjhassanpur.gittrend.sync.SyncAdapter;
import com.github.mjhassanpur.gittrend.ui.misc.RecyclerItemClickListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
