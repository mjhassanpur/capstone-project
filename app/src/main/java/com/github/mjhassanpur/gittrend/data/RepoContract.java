package com.github.mjhassanpur.gittrend.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class RepoContract {

    public static final String CONTENT_AUTHORITY = "com.github.mjhassanpur.gittrend";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_REPO = "repo";
    public static final String PATH_CONTRIBUTOR = "contributor";

    public static final class RepoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPO;

        public static final String TABLE_NAME = "repo";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_OWNER_NAME = "owner_name";
        public static final String COLUMN_OWNER_AVATAR = "owner_avatar";
        public static final String COLUMN_OWNER_HTML_URL = "owner_html_url";
        public static final String COLUMN_HTML_URL = "html_url";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_STARS = "stars";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_FORKS = "forks";

        public static Uri buildRepoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ContributorEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTRIBUTOR).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTRIBUTOR;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTRIBUTOR;

        public static final String TABLE_NAME = "contributor";

        public static final String COLUMN_REPO_KEY = "repo_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AVATAR = "avatar";
        public static final String COLUMN_HTML_URL = "html_url";
        public static final String COLUMN_COMMITS = "commits";
        public static final String COLUMN_ADDITIONS = "additions";
        public static final String COLUMN_DELETIONS = "deletions";

        public static Uri buildContributorUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
