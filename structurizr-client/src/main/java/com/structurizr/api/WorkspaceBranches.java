package com.structurizr.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for a workspace ID and a list of branches.
 */
public class WorkspaceBranches {

    private long id;
    private List<String> branches = new ArrayList<>();

    WorkspaceBranches() {
    }

    /**
     * Gets the workspace ID.
     *
     * @return      the workspace ID as a long
     */
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the list of branches associated with the workspace.
     *
     * @return      a List of branch names
     */
    public List<String> getBranches() {
        return branches;
    }

    void setBranches(List<String> branches) {
        this.branches = branches;
    }

}