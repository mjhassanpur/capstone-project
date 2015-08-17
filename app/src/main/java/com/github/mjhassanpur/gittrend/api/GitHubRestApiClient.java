package com.github.mjhassanpur.gittrend.api;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * @see <a href="http://square.github.io/retrofit/"></a>
 */
public class GitHubRestApiClient {

    private static final String WEB_SERVICE_BASE_URL = "https://api.github.com";
    private final GitHubWebService mWebService;
    private static GitHubRestApiClient mInstance;

    protected GitHubRestApiClient() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WEB_SERVICE_BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        mWebService = restAdapter.create(GitHubWebService.class);
    }

    public static GitHubRestApiClient getInstance() {
        if (mInstance == null) {
            mInstance = new GitHubRestApiClient();
        }
        return mInstance;
    }

    private interface GitHubWebService {

        @GET("/search/repositories")
        Observable<Repositories> repositories(
                @Query("q") String search,
                @Query("sort") String sort,
                @Query("order") String order
        );
    }
}
