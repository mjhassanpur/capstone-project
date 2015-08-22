package com.github.mjhassanpur.gittrend.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.github.mjhassanpur.gittrend.Config;
import com.github.mjhassanpur.gittrend.R;
import com.github.mjhassanpur.gittrend.api.Contributor;
import com.github.mjhassanpur.gittrend.api.FullRepository;
import com.github.mjhassanpur.gittrend.api.GitHubRestApiClient;
import com.github.mjhassanpur.gittrend.api.Repositories;
import com.github.mjhassanpur.gittrend.api.Repository;
import com.github.mjhassanpur.gittrend.api.Week;
import com.github.mjhassanpur.gittrend.data.RepoContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 *
 * @see <a href="https://developer.android.com/training/sync-adapters/creating-sync-adapter.html"></a>
 * @see <a href="https://github.com/udacity/Advanced_Android_Development/blob/7.05_Pretty_Wallpaper_Time/app/src/main/java/com/example/android/sunshine/app/sync/SunshineSyncAdapter.java"></a>
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // Global variables
    public final String LOG_TAG = SyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED =
            "com.github.mjhassanpur.gittrend.ACTION_DATA_UPDATED";
    public static final int SYNC_INTERVAL = 60 * 60 * 6;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/6;
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }
    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }
    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
    /*
     * Put the data transfer code here.
     */
        final List<FullRepository> fullRepositories = new ArrayList<>();
        final GitHubRestApiClient api = GitHubRestApiClient.getInstance();
        /**
         * @see <a href="http://stackoverflow.com/questions/30269011/chain-two-retrofit-observables-w-rxjava"></a>
         */
        api.getWebService()
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
                        saveToLocal(fullRepositories);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, "Failed to retrieve data", e);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(FullRepository fullRepository) {
                        fullRepositories.add(fullRepository);
                    }
                });
    }

    private String getSearchQuery() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return "created:>=" + dateFormat.format(cal.getTime());
    }

    private void saveToLocal(List<FullRepository> fullRepositories) {
        Vector<ContentValues> cVRepoVector = new Vector<>(fullRepositories.size());
        int contributorsSize = 0;
        for (FullRepository fullRepository : fullRepositories) {
            final Repository repository = fullRepository.repository;
            contributorsSize += fullRepository.contributors.size();

            ContentValues repoValues = new ContentValues();

            repoValues.put(RepoContract.RepoEntry.COLUMN_NAME, repository.name);
            repoValues.put(RepoContract.RepoEntry.COLUMN_FULL_NAME, repository.fullName);
            repoValues.put(RepoContract.RepoEntry.COLUMN_OWNER_NAME, repository.owner.name);
            repoValues.put(RepoContract.RepoEntry.COLUMN_OWNER_AVATAR_URL, repository.owner.avatarUrl);
            repoValues.put(RepoContract.RepoEntry.COLUMN_OWNER_HTML_URL, repository.owner.htmlUrl);
            repoValues.put(RepoContract.RepoEntry.COLUMN_HTML_URL, repository.htmlUrl);
            repoValues.put(RepoContract.RepoEntry.COLUMN_DESCRIPTION, repository.description);
            repoValues.put(RepoContract.RepoEntry.COLUMN_STARS, repository.stars);
            repoValues.put(RepoContract.RepoEntry.COLUMN_LANGUAGE, repository.language);
            repoValues.put(RepoContract.RepoEntry.COLUMN_FORKS, repository.forks);

            cVRepoVector.add(repoValues);
        }

        // add to database
        if ( cVRepoVector.size() > 0 ) {
            ContentValues[] cvRepoArray = new ContentValues[cVRepoVector.size()];
            cVRepoVector.toArray(cvRepoArray);
            // delete old data
            final int deleted = getContext().getContentResolver().delete(RepoContract.RepoEntry.CONTENT_URI, null, null);
            Log.d(LOG_TAG, "deleted " + deleted + " repo rows");
            // insert new data
            final int inserted = getContext().getContentResolver().bulkInsert(RepoContract.RepoEntry.CONTENT_URI, cvRepoArray);
            Log.d(LOG_TAG, "inserted " + inserted + " repo rows");

            updateWidget();
        }

        Vector<ContentValues> cVContributorVector = new Vector<>(contributorsSize);
        for (FullRepository fullRepository : fullRepositories) {
            final Repository repository = fullRepository.repository;
            long repoId = getRepoId(repository);

            final List<Contributor> contributors = fullRepository.contributors;
            for (Contributor contributor : contributors) {
                if (contributor != null && contributor.author != null) {
                    ContentValues contributorValues = new ContentValues();

                    final List<Week> weeks = contributor.weeks;
                    final Week week = weeks.get(weeks.size() - 1);

                    contributorValues.put(RepoContract.ContributorEntry.COLUMN_REPO_KEY, repoId);
                    contributorValues.put(RepoContract.ContributorEntry.COLUMN_NAME, contributor.author.name);
                    contributorValues.put(RepoContract.ContributorEntry.COLUMN_AVATAR_URL, contributor.author.avatarUrl);
                    contributorValues.put(RepoContract.ContributorEntry.COLUMN_HTML_URL, contributor.author.htmlUrl);
                    contributorValues.put(RepoContract.ContributorEntry.COLUMN_COMMITS, week.commits);
                    contributorValues.put(RepoContract.ContributorEntry.COLUMN_ADDITIONS, week.additions);
                    contributorValues.put(RepoContract.ContributorEntry.COLUMN_DELETIONS, week.deletions);

                    cVContributorVector.add(contributorValues);
                }
            }
        }

        // add to database
        if ( cVContributorVector.size() > 0 ) {
            ContentValues[] cvContributorArray = new ContentValues[cVContributorVector.size()];
            cVContributorVector.toArray(cvContributorArray);
            // delete old data
            final int deleted = getContext().getContentResolver().delete(RepoContract.ContributorEntry.CONTENT_URI, null, null);
            Log.d(LOG_TAG, "deleted " + deleted + " contributor rows");
            // insert new data
            final int inserted = getContext().getContentResolver().bulkInsert(RepoContract.ContributorEntry.CONTENT_URI, cvContributorArray);
            Log.d(LOG_TAG, "inserted " + inserted + " contributor rows");
        }
    }

    private void updateWidget() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private long getRepoId(Repository repository) {
        long repoId;

        Cursor repoCursor = getContext().getContentResolver().query(
                RepoContract.RepoEntry.CONTENT_URI,
                new String[]{RepoContract.RepoEntry._ID},
                RepoContract.RepoEntry.COLUMN_FULL_NAME + " = ?",
                new String[]{repository.fullName},
                null);

        if (repoCursor.moveToFirst()) {
            int repoIdIndex = repoCursor.getColumnIndex(RepoContract.RepoEntry._ID);
            repoId = repoCursor.getLong(repoIdIndex);
        } else {
            return -1;
        }

        repoCursor.close();
        return repoId;
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
