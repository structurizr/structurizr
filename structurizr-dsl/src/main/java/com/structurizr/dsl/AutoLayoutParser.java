package com.structurizr.dsl;

import com.structurizr.view.AutomaticLayout;
import com.structurizr.view.ModelView;
import com.structurizr.view.View;

import java.util.HashMap;
import java.util.Map;

final class AutoLayoutParser extends AbstractParser {

    private static final int RANK_DIRECTION_INDEX = 1;
    private static final int RANK_SEPARATION_INDEX = 2;
    private static final int NODE_SEPARATION_INDEX = 3;
    private static final int EDGE_SEPARATION_INDEX = 4;
    private static final int VERTICES_INDEX = 5;

    private static final Map<String, AutomaticLayout.RankDirection> RANK_DIRECTIONS = new HashMap<>();

    static {
        RANK_DIRECTIONS.put("tb", AutomaticLayout.RankDirection.TopBottom);
        RANK_DIRECTIONS.put("bt", AutomaticLayout.RankDirection.BottomTop);
        RANK_DIRECTIONS.put("lr", AutomaticLayout.RankDirection.LeftRight);
        RANK_DIRECTIONS.put("rl", AutomaticLayout.RankDirection.RightLeft);
    }

    void parse(ModelViewDslContext context, Tokens tokens) {
        // autoLayout [rankDirection] [rankSeparation] [nodeSeparation] [edgeSeparation] [vertices]

        if (tokens.hasMoreThan(VERTICES_INDEX)) {
            throw new RuntimeException("Too many tokens, expected: autoLayout [rankDirection] [rankSeparation] [nodeSeparation] [edgeSeparation] [vertices]");
        }

        ModelView view = context.getView();
        if (view != null) {
            AutomaticLayout.RankDirection rankDirection = AutomaticLayout.DEFAULT_RANK_DIRECTION;
            int rankSeparation = AutomaticLayout.DEFAULT_RANK_SEPARATION;
            int nodeSeparation = AutomaticLayout.DEFAULT_NODE_SEPARATION;
            int edgeSeparation = AutomaticLayout.DEFAULT_EDGE_SEPARATION;
            boolean vertices = AutomaticLayout.DEFAULT_VERTICES;

            if (tokens.includes(RANK_DIRECTION_INDEX)) {
                String rankDirectionAsString = tokens.get(RANK_DIRECTION_INDEX);

                if (RANK_DIRECTIONS.containsKey(rankDirectionAsString)) {
                    rankDirection = RANK_DIRECTIONS.get(rankDirectionAsString);
                } else {
                    throw new RuntimeException("Valid rank directions are: tb|bt|lr|rl");
                }
            }

            if (tokens.includes(RANK_SEPARATION_INDEX)) {
                String rankSeparationAsString = tokens.get(RANK_SEPARATION_INDEX);

                try {
                    rankSeparation = Integer.parseInt(rankSeparationAsString);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Rank separation must be positive integer in pixels");
                }
            }

            if (tokens.includes(NODE_SEPARATION_INDEX)) {
                String nodeSeparationAsString = tokens.get(NODE_SEPARATION_INDEX);

                try {
                    nodeSeparation = Integer.parseInt(nodeSeparationAsString);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Node separation must be positive integer in pixels");
                }
            }

            if (tokens.includes(EDGE_SEPARATION_INDEX)) {
                String edgeSeparationAsString = tokens.get(EDGE_SEPARATION_INDEX);

                try {
                    edgeSeparation = Integer.parseInt(edgeSeparationAsString);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Edge separation must be positive integer in pixels");
                }
            }

            if (tokens.includes(VERTICES_INDEX)) {
                String verticesAsString = tokens.get(VERTICES_INDEX);
                if ("true".equalsIgnoreCase(verticesAsString)) {
                    vertices = true;
                } else if ("false".equalsIgnoreCase(verticesAsString)) {
                    vertices = false;
                } else {
                    throw new RuntimeException("Vertices must be true or false");
                }
            }

            view.enableAutomaticLayout(rankDirection, rankSeparation, nodeSeparation, edgeSeparation, vertices);
        }
    }

}