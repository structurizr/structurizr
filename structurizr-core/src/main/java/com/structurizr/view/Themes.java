package com.structurizr.view;

import com.structurizr.Workspace;
import com.structurizr.model.Element;
import com.structurizr.model.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Themes {

    public static boolean isBuiltIn(String theme) {
        return Set.of(
                "amazon-web-services-2020.04.30",
                "amazon-web-services-2022.04.30",
                "amazon-web-services-2023.01.31",
                "google-cloud-platform-v1.5",
                "kubernetes-v0.3",
                "microsoft-azure-2019.09.11",
                "microsoft-azure-2020.07.13",
                "microsoft-azure-2021.01.26",
                "microsoft-azure-2023.01.24",
                "microsoft-azure-2024.07.15",
                "oracle-cloud-infrastructure-2020.04.30",
                "oracle-cloud-infrastructure-2021.04.30",
                "oracle-cloud-infrastructure-2023.04.01"
        ).contains(theme);
    }

}