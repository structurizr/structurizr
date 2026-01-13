package com.structurizr.server.component.workspace;

import com.structurizr.AbstractWorkspace;
import com.structurizr.Workspace;
import com.structurizr.configuration.*;
import com.structurizr.configuration.Role;
import com.structurizr.encryption.AesEncryptionStrategy;
import com.structurizr.encryption.EncryptedWorkspace;
import com.structurizr.encryption.EncryptionLocation;
import com.structurizr.encryption.EncryptionStrategy;
import com.structurizr.io.json.EncryptedJsonReader;
import com.structurizr.io.json.EncryptedJsonWriter;
import com.structurizr.server.domain.*;
import com.structurizr.server.domain.User;
import com.structurizr.util.DateUtils;
import com.structurizr.util.ImageUtils;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
class WorkspaceComponentImpl implements WorkspaceComponent {

    private static final Log log = LogFactory.getLog(WorkspaceComponent.class);
    private static final String ENCRYPTION_STRATEGY_STRING = "encryptionStrategy";
    private static final String CIPHERTEXT_STRING = "ciphertext";

    private final WorkspaceAdapter workspaceAdapter;
    private final String encryptionPassphrase;

    private WorkspaceMetadataCache workspaceMetadataCache;
    private ExecutorService executorService;

    WorkspaceComponentImpl() {
        if (Configuration.getInstance().getProfile() == Profile.Local) {
            if (Configuration.getInstance().isSingleWorkspace()) {
                workspaceAdapter = new LocalFileSystemSingleWorkspaceAdapter();
            } else {
                workspaceAdapter = new LocalFileSystemMultipleWorkspaceAdapter();
            }
        } else {
            workspaceAdapter = WorkspaceAdapterFactory.create();
        }

        if (workspaceAdapter == null) {
            System.exit(1);
        }

        encryptionPassphrase = Configuration.getInstance().getProperty(StructurizrProperties.ENCRYPTION_PASSPHRASE);

        initCache();
        initThreadPool();
    }

    // constructor for testing
    WorkspaceComponentImpl(WorkspaceAdapter workspaceAdapter) {
        this.workspaceAdapter = workspaceAdapter;
        encryptionPassphrase = Configuration.getInstance().getProperty(StructurizrProperties.ENCRYPTION_PASSPHRASE);
        workspaceMetadataCache = new NoOpWorkspaceMetadataCache();
        initThreadPool();
    }

    private void initCache() {
        workspaceMetadataCache = WorkspaceMetadataCacheFactory.create();

        if (workspaceMetadataCache == null) {
            System.exit(1);
        }
    }

    private void initThreadPool() {
        int threads = Integer.parseInt(Configuration.getInstance().getProperty(StructurizrProperties.WORKSPACE_THREADS));
         executorService = Executors.newFixedThreadPool(threads);
    }

    public void stop() {
        workspaceMetadataCache.stop();
        executorService.shutdownNow();
    }

    @Override
    public List<WorkspaceMetadata> getWorkspaces() {
        List<WorkspaceMetadata> workspaces = new ArrayList<>();
        Collection<Long> workspaceIds = workspaceAdapter.getWorkspaceIds();

        List<Future<WorkspaceMetadata>> futures = workspaceIds.stream()
                .map(workspaceId -> executorService.submit(() -> getWorkspaceMetadata(workspaceId)))
                .toList();

        for (Future<WorkspaceMetadata> future : futures) {
            try {
                WorkspaceMetadata workspace = future.get();
                if (workspace != null) {
                    workspaces.add(workspace);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.warn("An error occurred while fetching workspace: " + e.getMessage());
            }
        }

        workspaces.sort(Comparator.comparing(WorkspaceMetadata::getId));

        return workspaces;
    }

    @Override
    public List<WorkspaceMetadata> getWorkspaces(User user) {
        Collection<WorkspaceMetadata> workspaces = new ArrayList<>();

        try {
            workspaces = getWorkspaces();
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        List<WorkspaceMetadata> filteredWorkspaces = new ArrayList<>();

        if (!Configuration.getInstance().isAuthenticationEnabled()) {
            // authentication disabled - all workspaces
            for (WorkspaceMetadata workspace : workspaces) {
                workspace.setUrlPrefix("/workspace");
                filteredWorkspaces.add(workspace);
            }
        } else {
            if (user == null || user.getAuthenticationMethod() == AuthenticationMethod.NONE) {
                // unauthenticated/anonymous request - public workspaces only
                for (WorkspaceMetadata workspace : workspaces) {
                    if (workspace.isPublicWorkspace()) {
                        workspace.setUrlPrefix("/share");
                        filteredWorkspaces.add(workspace);
                    }
                }
            } else {
                // authenticated request
                for (WorkspaceMetadata workspace : workspaces) {
                    Set<Permission> permissions = workspace.getPermissions(user);

                    if (!permissions.isEmpty()) {
                        // user is configured to see the workspace
                        workspace.setUrlPrefix("/workspace");
                        filteredWorkspaces.add(workspace);
                    } else if (workspace.isPublicWorkspace()) {
                        // the workspace is public - anybody can see it
                        workspace.setUrlPrefix("/share");
                        filteredWorkspaces.add(workspace);
                    }
                }
            }
        }

        return filteredWorkspaces;
    }

    @Override
    public WorkspaceMetadata getWorkspaceMetadata(long workspaceId) throws WorkspaceComponentException {
        WorkspaceMetadata wmd = workspaceMetadataCache.get(workspaceId);

        if (wmd == null) {
            wmd = workspaceAdapter.getWorkspaceMetadata(workspaceId);

            if (wmd != null) {
                workspaceMetadataCache.put(wmd);
            }
        }

        if (wmd != null && wmd.isArchived()) {
            return null;
        }

        return wmd;
    }

    @Override
    public void putWorkspaceMetadata(WorkspaceMetadata workspaceMetadata) {
        if (workspaceMetadata == null) {
            throw new IllegalArgumentException("Workspace metadata cannot be null");
        }

        workspaceAdapter.putWorkspaceMetadata(workspaceMetadata);
        workspaceMetadataCache.put(workspaceMetadata);
    }

    @Override
    public String getWorkspace(long workspaceId, String branch, String version) {
        WorkspaceBranch.validateBranchName(branch);
        String json = workspaceAdapter.getWorkspace(workspaceId, branch, version);

        if (json == null) {
            if (!StringUtils.isNullOrEmpty(branch)) {
                // branch likely doesn't exist
                throw new WorkspaceBranchNotFoundException(workspaceId, branch);
            }
        }

        if (json == null) {
            if (!StringUtils.isNullOrEmpty(version)) {
                throw new WorkspaceComponentException("Could not get workspace " + workspaceId + " with version " + version);
            } else {
                throw new WorkspaceComponentException("Could not get workspace " + workspaceId);
            }
        }

        if (json.contains(ENCRYPTION_STRATEGY_STRING) && json.contains(CIPHERTEXT_STRING)) {
            EncryptedJsonReader encryptedJsonReader = new EncryptedJsonReader();
            StringReader stringReader = new StringReader(json);
            try {
                EncryptedWorkspace encryptedWorkspace = encryptedJsonReader.read(stringReader);

                if (encryptedWorkspace.getEncryptionStrategy().getLocation() == EncryptionLocation.Server) {
                    // server-side encrypted
                    if (StringUtils.isNullOrEmpty(encryptionPassphrase)) {
                        log.warn("Workspace " + workspaceId + " seems to be encrypted, but a passphrase has not been set");
                    }

                    encryptedWorkspace.getEncryptionStrategy().setPassphrase(encryptionPassphrase);
                    return encryptedWorkspace.getPlaintext();
                } else if (encryptedWorkspace.getEncryptionStrategy().getLocation() == EncryptionLocation.Client) {
                    // client-side encrypted - do nothing, we'll pass back the JSON as-is
                    return json;
                } else {
                    return json;
                }
            } catch (Exception e) {
                throw new WorkspaceComponentException("Could not get workspace " + workspaceId, e);
            }
        } else {
            // again, do nothing, the JSON was stored unencrypted
            return json;
        }
    }

    @Override
    public long createWorkspace(User user) {
        try {
            long workspaceId;

            List<Long> workspaceIds = workspaceAdapter.getWorkspaceIds();
            if (workspaceIds.isEmpty()) {
                workspaceId = 1;
            } else {
                workspaceId = workspaceIds.stream().reduce(0L, Long::max) + 1;
            }

            try {
                // create and write the workspace metadata
                WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(workspaceId);
                workspaceMetadata.setApiKey(UUID.randomUUID().toString());
                workspaceMetadata.setApiSecret(UUID.randomUUID().toString());

                putWorkspaceMetadata(workspaceMetadata);
            } catch (Exception e) {
                log.error(e);
            }

            NumberFormat format = new DecimalFormat("0000");
            Workspace workspace = new Workspace("Workspace " + format.format(workspaceId), "Description");

            if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_SCOPE_VALIDATION)) {
                workspace.getConfiguration().setScope(WorkspaceScope.SoftwareSystem);
            }

            String json = WorkspaceUtils.toJson(workspace, false);

            putWorkspace(workspaceId, null, json);

            return workspaceId;
        } catch (Exception e) {
            throw new WorkspaceComponentException("Could not create workspace", e);
        }
    }

    @Override
    public boolean deleteWorkspace(long workspaceId) {
        if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_ARCHIVING)) {
            log.debug("Archiving workspace with ID " + workspaceId);
            WorkspaceMetadata workspaceMetadata = getWorkspaceMetadata(workspaceId);
            workspaceMetadata.setArchived(true);
            putWorkspaceMetadata(workspaceMetadata);

            return true;
        } else {
            log.debug("Deleting workspace with ID " + workspaceId);
            return workspaceAdapter.deleteWorkspace(workspaceId);
        }
    }

    @Override
    public void putWorkspace(long workspaceId, String branch, String json) {
        WorkspaceBranch.validateBranchName(branch);

        try {
            AbstractWorkspace workspaceToBeStored;
            String jsonToBeStored;
            WorkspaceConfiguration configuration;

            WorkspaceMetadata workspaceMetadata = getWorkspaceMetadata(workspaceId);
            if (workspaceMetadata == null) {
                workspaceMetadata = new WorkspaceMetadata(workspaceId);
            }

            if (json.contains(ENCRYPTION_STRATEGY_STRING) && json.contains(CIPHERTEXT_STRING)) {
                EncryptedJsonReader jsonReader = new EncryptedJsonReader();
                StringReader stringReader = new StringReader(json);
                EncryptedWorkspace encryptedWorkspace = jsonReader.read(stringReader);

                encryptedWorkspace.setId(workspaceId);
                encryptedWorkspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));

                // also remove the workspace configuration
                configuration = encryptedWorkspace.getConfiguration();
                encryptedWorkspace.clearConfiguration();
                encryptedWorkspace.getConfiguration().setScope(configuration.getScope());

                // copy the last modified details from the workspace
                workspaceMetadata.setLastModifiedDate(encryptedWorkspace.getLastModifiedDate());
                workspaceMetadata.setLastModifiedAgent(encryptedWorkspace.getLastModifiedAgent());
                workspaceMetadata.setLastModifiedUser(encryptedWorkspace.getLastModifiedUser());

                // write it back as an encrypted workspace JSON
                EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
                StringWriter stringWriter = new StringWriter();
                encryptedJsonWriter.write(encryptedWorkspace, stringWriter);

                workspaceMetadata.setClientSideEncrypted(true);
                workspaceToBeStored = encryptedWorkspace;
                jsonToBeStored = stringWriter.toString();
            } else {
                Workspace workspace = WorkspaceUtils.fromJson(json);

                WorkspaceValidationUtils.validateWorkspaceScope(workspace);

                workspace.setId(workspaceId);
                workspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));

                // also remove the configuration
                configuration = workspace.getConfiguration();
                workspace.clearConfiguration();
                workspace.getConfiguration().setScope(configuration.getScope());

                // copy the last modified details from the workspace
                workspaceMetadata.setLastModifiedDate(workspace.getLastModifiedDate());
                workspaceMetadata.setLastModifiedAgent(workspace.getLastModifiedAgent());
                workspaceMetadata.setLastModifiedUser(workspace.getLastModifiedUser());

                workspaceMetadata.setClientSideEncrypted(false);
                workspaceToBeStored = workspace;

                if (!StringUtils.isNullOrEmpty(encryptionPassphrase)) {
                    EncryptionStrategy encryptionStrategy = new AesEncryptionStrategy(128, 1000, encryptionPassphrase);
                    encryptionStrategy.setLocation(EncryptionLocation.Server);

                    EncryptedWorkspace encryptedWorkspace = new EncryptedWorkspace(workspace, json, encryptionStrategy);
                    encryptedWorkspace.setLastModifiedDate(workspace.getLastModifiedDate());

                    EncryptedJsonWriter encryptedJsonWriter = new EncryptedJsonWriter(false);
                    StringWriter stringWriter = new StringWriter();
                    encryptedJsonWriter.write(encryptedWorkspace, stringWriter);
                    jsonToBeStored = stringWriter.toString();
                } else {
                    jsonToBeStored = WorkspaceUtils.toJson(workspace, false);
                }
            }

            workspaceMetadata.setSize(jsonToBeStored.length());

            // check the workspace lock
            if (workspaceMetadata.isLocked() && !workspaceMetadata.isLockedBy(workspaceToBeStored.getLastModifiedUser(), workspaceToBeStored.getLastModifiedAgent())) {
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.USER_FRIENDLY_DATE_FORMAT);
                throw new WorkspaceComponentException("The workspace could not be saved because the workspace was locked by " + workspaceMetadata.getLockedUser() + " at " + sdf.format(workspaceMetadata.getLockedDate()) + ".");
            }

            workspaceAdapter.putWorkspace(workspaceMetadata, jsonToBeStored, branch);

            if (StringUtils.isNullOrEmpty(branch)) {
                // only store workspace metadata for the main branch
                try {
                    workspaceMetadata.setName(workspaceToBeStored.getName());
                    workspaceMetadata.setDescription(workspaceToBeStored.getDescription());

                    // configure workspace visibility and users
                    if (configuration != null) {

                        // only configure workspace visibility and users if no admin users/roles are defined
                        if (Configuration.getInstance().isAuthenticationEnabled() && !Configuration.getInstance().adminUsersEnabled()) {
                            if (configuration.getVisibility() != null) {
                                workspaceMetadata.setPublicWorkspace(configuration.getVisibility() == Visibility.Public);
                            }

                            if (!configuration.getUsers().isEmpty()) {
                                workspaceMetadata.clearWriteUsers();
                                workspaceMetadata.clearReadUsers();

                                for (com.structurizr.configuration.User user : configuration.getUsers()) {
                                    if (user.getRole() == Role.ReadWrite) {
                                        workspaceMetadata.addWriteUser(user.getUsername());
                                    } else {
                                        workspaceMetadata.addReadUser(user.getUsername());
                                    }
                                }
                            }
                        }
                    }

                    putWorkspaceMetadata(workspaceMetadata);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        } catch (WorkspaceComponentException wce) {
            throw wce;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WorkspaceComponentException(e.getMessage(), e);
        }
    }

    @Override
    public List<WorkspaceVersion> getWorkspaceVersions(long workspaceId, String branch) {
        List<WorkspaceVersion> versions = new ArrayList<>();

        if (Configuration.getInstance().getProfile() == Profile.Server) {
            WorkspaceBranch.validateBranchName(branch);

            int maxVersions = Integer.parseInt(Configuration.getInstance().getProperty(StructurizrProperties.MAX_WORKSPACE_VERSIONS));
            versions = workspaceAdapter.getWorkspaceVersions(workspaceId, branch, maxVersions);
            versions.sort((v1, v2) -> v2.getLastModifiedDate().compareTo(v1.getLastModifiedDate()));

            if (versions.size() > maxVersions) {
                versions = versions.subList(0, maxVersions);
            }
        }
        return versions;
    }

    @Override
    public List<WorkspaceBranch> getWorkspaceBranches(long workspaceId) {
        List<WorkspaceBranch> branches = new ArrayList<>();

        if (Configuration.getInstance().getProfile() == Profile.Server) {
            try {
                return workspaceAdapter.getWorkspaceBranches(workspaceId);
            } catch (Exception e) {
                log.error(e);
            }
        }

        return branches;
    }

    @Override
    public boolean deleteBranch(long workspaceId, String branch) {
        return workspaceAdapter.deleteBranch(workspaceId, branch);
    }

    @Override
    public boolean lockWorkspace(long workspaceId, String username, String agent) {
        WorkspaceMetadata workspaceMetadata = getWorkspaceMetadata(workspaceId);
        if (!workspaceMetadata.isLocked() || workspaceMetadata.isLockedBy(username, agent)) {
            workspaceMetadata.addLock(username, agent);

            try {
                putWorkspaceMetadata(workspaceMetadata);
                return true;
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        }

        return false;
    }

    @Override
    public boolean unlockWorkspace(long workspaceId) {
        WorkspaceMetadata workspaceMetadata = getWorkspaceMetadata(workspaceId);
        workspaceMetadata.clearLock();

        try {
            putWorkspaceMetadata(workspaceMetadata);
            return true;
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return false;
    }

    @Override
    public void makeWorkspacePublic(long workspaceId) {
        WorkspaceMetadata workspace = getWorkspaceMetadata(workspaceId);
        workspace.setPublicWorkspace(true);
        putWorkspaceMetadata(workspace);
    }

    @Override
    public void makeWorkspacePrivate(long workspaceId) {
        WorkspaceMetadata workspace = getWorkspaceMetadata(workspaceId);
        workspace.setPublicWorkspace(false);
        putWorkspaceMetadata(workspace);
    }

    @Override
    public void shareWorkspace(long workspaceId) {
        WorkspaceMetadata workspace = getWorkspaceMetadata(workspaceId);
        workspace.setSharingToken(UUID.randomUUID().toString());
        putWorkspaceMetadata(workspace);
    }

    @Override
    public void unshareWorkspace(long workspaceId) {
        WorkspaceMetadata workspace = getWorkspaceMetadata(workspaceId);
        workspace.setSharingToken("");
        putWorkspaceMetadata(workspace);
    }

    @Override
    public boolean putImage(long workspaceId, String branch, String filename, File file) {
        if (ImageUtils.isImage(filename)) {
            return workspaceAdapter.putImage(workspaceId, branch, filename, file);
        } else {
            throw new WorkspaceComponentException(filename + " is not an image");
        }
    }

    @Override
    public InputStreamAndContentLength getImage(long workspaceId, String branch, String filename) {
        return workspaceAdapter.getImage(workspaceId, branch, filename);
    }

    @Override
    public List<Image> getImages(long workspaceId) {
        return workspaceAdapter.getImages(workspaceId);
    }

    @Override
    public boolean deleteImages(long workspaceId) {
        return workspaceAdapter.deleteImages(workspaceId);
    }

    @Override
    public long getLastModifiedDate() {
        return workspaceAdapter.getLastModifiedDate();
    }

    @Scheduled(cron="@midnight")
    public void removeOldWorkspaceVersions() {
        log.debug("Removing old workspace versions");
        workspaceAdapter.removeOldWorkspaceVersions();
    }

}