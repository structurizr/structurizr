package com.structurizr.server.component.workspace;

import com.structurizr.util.StringUtils;

public final class WorkspaceBranch {

    public static final String NO_BRANCH = "";
    public static final String MAIN_BRANCH = "main";

    private static final String BRANCH_NAME_REGEX = "[a-zA-Z0-9][a-zA-Z0-9-_.]*";
    private static final int MAX_LENGTH = 100;

    private final String name;

    public WorkspaceBranch(String name) {
        this.name = name;
    }

    public static void validateBranchName(String name) {
        if (!StringUtils.isNullOrEmpty(name) && !isValidBranchName(name)) {
            throw new IllegalArgumentException("The branch name \"" + name + "\" is invalid");
        }
    }

    public static boolean isMainBranch(String branch) {
        return StringUtils.isNullOrEmpty(branch) || branch.equalsIgnoreCase(MAIN_BRANCH);
    }

    public String getName() {
        return name;
    }

    public static boolean isValidBranchName(String name) {
        return name.matches(BRANCH_NAME_REGEX) && name.length() < MAX_LENGTH;
    }

}