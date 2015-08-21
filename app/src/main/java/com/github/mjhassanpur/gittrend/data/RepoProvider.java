package com.github.mjhassanpur.gittrend.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class RepoProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RepoDbHelper mOpenHelper;

    static final int CONTRIBUTOR = 100;
    static final int REPO = 200;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RepoContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, RepoContract.PATH_CONTRIBUTOR, CONTRIBUTOR);
        matcher.addURI(authority, RepoContract.PATH_REPO, REPO);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RepoDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CONTRIBUTOR:
                return RepoContract.ContributorEntry.CONTENT_TYPE;
            case REPO:
                return RepoContract.RepoEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "contributor"
            case CONTRIBUTOR: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RepoContract.ContributorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "repo"
            case REPO: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RepoContract.RepoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CONTRIBUTOR: {
                long _id = db.insert(RepoContract.ContributorEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RepoContract.ContributorEntry.buildContributorUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REPO: {
                long _id = db.insert(RepoContract.RepoEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RepoContract.RepoEntry.buildRepoUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case CONTRIBUTOR:
                rowsDeleted = db.delete(
                        RepoContract.ContributorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REPO:
                rowsDeleted = db.delete(
                        RepoContract.RepoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CONTRIBUTOR:
                rowsUpdated = db.update(RepoContract.ContributorEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REPO:
                rowsUpdated = db.update(RepoContract.RepoEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case CONTRIBUTOR:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RepoContract.ContributorEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case REPO:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RepoContract.RepoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
