package com.github.mjhassanpur.gittrend.api;

import java.util.List;

public class FullRepository {

    public Repository repository;
    public List<Contributor> contributors;

    public FullRepository(Repository repository, List<Contributor> contributors) {
        this.repository = repository;
        this.contributors = contributors;
    }
}
