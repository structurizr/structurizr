package com.structurizr.dsl;

import java.util.Set;

final class DeploymentGroupParser extends DeploymentElementParser {

    private static final String DEPLOYMENT_GROUP_GRAMMAR = "deploymentGroup <name>";
    private static final String DEPLOYMENT_NODE_GRAMMAR = "deploymentGroup <deploymentGroups>";

    private static final int DEPLOYMENT_GROUP_NAME_INDEX = 1;

    String parse(Tokens tokens) {
        // deploymentGroup <name>

        if (tokens.hasMoreThan(DEPLOYMENT_GROUP_NAME_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: " + DEPLOYMENT_GROUP_GRAMMAR);
        } else if (tokens.size() != DEPLOYMENT_GROUP_NAME_INDEX + 1) {
            throw new RuntimeException("Expected: " + DEPLOYMENT_GROUP_GRAMMAR);
        } else {
            return tokens.get(DEPLOYMENT_GROUP_NAME_INDEX);
        }
    }

    void parse(DeploymentNodeDslContext context, Tokens tokens) {
        // deploymentGroup <deploymentGroups>

        if (tokens.hasMoreThan(DEPLOYMENT_GROUP_NAME_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: " + DEPLOYMENT_NODE_GRAMMAR);
        } else if (tokens.size() != DEPLOYMENT_GROUP_NAME_INDEX + 1) {
            throw new RuntimeException("Expected: " + DEPLOYMENT_NODE_GRAMMAR);
        } else {
            String deploymentGroupNames = tokens.get(DEPLOYMENT_GROUP_NAME_INDEX);
            Set<String> deploymentGroups = getDeploymentGroups(context, deploymentGroupNames);

            for (String deploymentGroup : deploymentGroups) {
                context.getDeploymentNode().addDeploymentGroup(deploymentGroup);
            }
        }
    }

}