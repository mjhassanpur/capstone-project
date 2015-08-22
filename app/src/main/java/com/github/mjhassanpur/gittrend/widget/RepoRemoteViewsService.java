package com.github.mjhassanpur.gittrend.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.mjhassanpur.gittrend.R;
import com.github.mjhassanpur.gittrend.data.RepoContract;

/**
 * @see <a href="https://github.com/udacity/Advanced_Android_Development/blob/7.05_Pretty_Wallpaper_Time/app/src/main/java/com/example/android/sunshine/app/widget/DetailWidgetRemoteViewsService.java"></a>
 */
public class RepoRemoteViewsService extends RemoteViewsService {

    private static final String[] REPO_COLUMNS = {
            RepoContract.RepoEntry.TABLE_NAME + "." + RepoContract.RepoEntry._ID,
            RepoContract.RepoEntry.COLUMN_FULL_NAME,
            RepoContract.RepoEntry.COLUMN_HTML_URL,
            RepoContract.RepoEntry.COLUMN_DESCRIPTION,
            RepoContract.RepoEntry.COLUMN_STARS,
            RepoContract.RepoEntry.COLUMN_FORKS,
            RepoContract.RepoEntry.COLUMN_LANGUAGE
    };

    private static final int COL_REPO_ID = 0;
    private static final int COL_REPO_FULL_NAME = 1;
    private static final int COL_REPO_HTML_URL = 2;
    private static final int COL_REPO_DESCRIPTION = 3;
    private static final int COL_REPO_STARS= 4;
    private static final int COL_REPO_FORKS = 5;
    private static final int COL_REPO_LANGUAGE = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RepoRemoteViewsFactory(getApplicationContext());
    }

    public class RepoRemoteViewsFactory implements RemoteViewsFactory {

        private Context mContext;
        private Cursor data = null;

        public RepoRemoteViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {
            // Nothing to do
        }

        @Override
        public void onDataSetChanged() {
            if (data != null) {
                data.close();
            }
            // This method is called by the app hosting the widget (e.g., the launcher)
            // However, our ContentProvider is not exported so it doesn't have access to the
            // data. Therefore we need to clear (and finally restore) the calling identity so
            // that calls use our process and permission
            final long identityToken = Binder.clearCallingIdentity();
            String sortOrder = RepoContract.RepoEntry.COLUMN_STARS + " DESC";
            data = getContentResolver().query(
                    RepoContract.RepoEntry.CONTENT_URI,
                    REPO_COLUMNS,
                    null,
                    null,
                    sortOrder);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    data == null || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews views = new RemoteViews(getPackageName(),
                    R.layout.widget_repo_list_item);

            String fullName = data.getString(COL_REPO_FULL_NAME);
            String description = data.getString(COL_REPO_DESCRIPTION);
            int stars = data.getInt(COL_REPO_STARS);
            int forks = data.getInt(COL_REPO_FORKS);
            String language = data.getString(COL_REPO_LANGUAGE);

            views.setTextViewText(R.id.repo_name, fullName);
            views.setTextViewText(R.id.repo_description, description);
            views.setTextViewText(R.id.stars, String.valueOf(stars));
            views.setTextViewText(R.id.forks, String.valueOf(forks));
            views.setTextViewText(R.id.language, language);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (data.moveToPosition(position))
                return data.getLong(COL_REPO_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
