package com.structurizr.server.web.api;

import java.util.ArrayList;
import java.util.List;

class WorkspaceBranchesApiResponse {

    private final long id;
    private final List<String> branches = new ArrayList<>();

    WorkspaceBranchesApiResponse(long id, List<String> branches) {
        this.id = id;
        this.branches.addAll(branches);
    }

    public long getId() {
        return id;
    }

    public List<String> getBranches() {
        return branches;
    }

}