package com.github.mjhassanpur.gittrend.api;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
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

    public GitHubWebService getWebService() {
        return mWebService;
    }

    public interface GitHubWebService {

        @GET("/search/repositories")
        Observable<Repositories> repositories(
                @Query("q") String search,
                @Query("sort") String sort,
                @Query("order") String order,
                @Query("client_id") String id,
                @Query("client_secret") String secret
        );

        @GET("/repos/{owner}/{repo}/stats/contributors")
        Observable<List<Contributor>> contributors(
                @Path("owner") String owner,
                @Path("repo") String repo,
                @Query("client_id") String id,
                @Query("client_secret") String secret
        );
    }
}
