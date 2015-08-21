package com.github.mjhassanpur.gittrend.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.mjhassanpur.gittrend.data.RepoContract.RepoEntry;
import com.github.mjhassanpur.gittrend.data.RepoContract.ContributorEntry;

public class RepoDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "repo.db";

    public RepoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_REPO_TABLE = "CREATE TABLE " + RepoEntry.TABLE_NAME + " (" +
                RepoEntry._ID + " INTEGER PRIMARY KEY," +
                RepoEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                RepoEntry.COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                RepoEntry.COLUMN_OWNER_NAME + " TEXT NOT NULL, " +
                RepoEntry.COLUMN_OWNER_AVATAR + " BLOB NOT NULL, " +
                RepoEntry.COLUMN_OWNER_HTML_URL + " TEXT NOT NULL, " +
                RepoEntry.COLUMN_HTML_URL + " TEXT NOT NULL, " +
                RepoEntry.COLUMN_DESCRIPTION + " TEXT, " +
                RepoEntry.COLUMN_STARS + " INTEGER NOT NULL, " +
                RepoEntry.COLUMN_LANGUAGE + " TEXT, " +
                RepoEntry.COLUMN_FORKS + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_CONTRIBUTOR_TABLE = "CREATE TABLE " + ContributorEntry.TABLE_NAME + " (" +
                ContributorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ContributorEntry.COLUMN_REPO_KEY + " INTEGER NOT NULL, " +
                ContributorEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ContributorEntry.COLUMN_AVATAR + " BLOB NOT NULL, " +
                ContributorEntry.COLUMN_HTML_URL + " TEXT NOT NULL, " +
                ContributorEntry.COLUMN_COMMITS + " INTEGER NOT NULL, " +
                ContributorEntry.COLUMN_ADDITIONS + " INTEGER NOT NULL, " +
                ContributorEntry.COLUMN_DELETIONS + " INTEGER NOT NULL, " +
                // Set up the repo column as a foreign key to repo table.
                " FOREIGN KEY (" + ContributorEntry.COLUMN_REPO_KEY + ") REFERENCES " +
                RepoEntry.TABLE_NAME + " (" + RepoEntry._ID + ") " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_REPO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CONTRIBUTOR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RepoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ContributorEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
