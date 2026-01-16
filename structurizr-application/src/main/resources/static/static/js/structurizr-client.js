structurizr.io.StructurizrApiClient = class StructurizrApiClient {

    #apiUrl = 'https://api.structurizr.com';
    #workspaceId;
    #branch;
    #username;
    #agent;

    constructor(apiUrl, workspaceId, branch, username, agent) {
        this.#workspaceId = workspaceId;
        this.#branch = branch;
        this.#username = username;
        this.#agent = agent;

        if (this.#agent === undefined || this.#agent.length === 0) {
            this.#agent = 'structurizr-ui';
        }

        if (apiUrl !== undefined) {
            this.#apiUrl = apiUrl;
        }
    }

    getWorkspace(version, callback) {
        var branchPath;
        if (this.#branch === undefined || this.#branch === '') {
            branchPath = '';
        } else {
            branchPath = '/branch/' + this.#branch;
        }

        var url = this.#apiUrl + "/workspace/" + this.#workspaceId + branchPath;
        if (version !== undefined && version.trim().length > 0) {
            url = url + '?version=' + version;
        }

        $.ajax({
            url: url,
            type: "GET",
            cache: false,
            dataType: 'json'
        })
            .done(function(json) {
                if (callback) {
                    callback(
                        {
                            success: true,
                            message: undefined,
                            json: json
                        }
                    );
                }
            })
            .fail(function(jqXHR, textStatus, errorThrown) {
                var message = textStatus;

                if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                    message = jqXHR.responseJSON.message;
                }

                if (callback) {
                    callback(
                        {
                            success: false,
                            message: message,
                            workspace: undefined
                        }
                    );
                }
            });
    }

    putWorkspace(workspace, callback) {
        workspace.lastModifiedDate = new Date().toISOString();
        workspace.lastModifiedAgent = this.#agent;
        workspace.lastModifiedUser = this.#username;

        const jsonAsString = JSON.stringify(workspace);
        const contentType = 'application/json; charset=UTF-8';

        var branchPath;
        if (this.#branch === undefined || this.#branch === '') {
            branchPath = '';
        } else {
            branchPath = '/branch/' + this.#branch;
        }

        $.ajax({
            url: this.#apiUrl + "/workspace/" + this.#workspaceId + branchPath,
            type: "PUT",
            contentType: contentType,
            cache: false,
            headers: {
                'Content-Type': contentType,
                'X-User-Agent': workspace.lastModifiedAgent
            },
            dataType: 'json',
            data: jsonAsString
        })
        .done(function(data, textStatus, jqXHR) {
            if (callback) {
                callback(
                    {
                        success: true,
                        message: undefined
                    }
                );
            }
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            var message = textStatus;

            if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                message = jqXHR.responseJSON.message;
            }

            if (callback) {
                callback(
                    {
                        success: false,
                        message: message
                    }
                );
            }
        });
    };

    setBranch(branch) {
        this.#branch = branch;
    }

    #getPath() {
        if (this.#apiUrl === '/api') {
            return this.#apiUrl;
        } else {
            var path = this.#apiUrl;
            if (path.slice(-1) === "/") { // String.endsWith() doesn't work on IE
                path = path.substr(0, path.length() - 1);
            }

            path = path.replace("http://", "");
            path = path.replace("https://", "");

            var index = path.indexOf("/");
            if (index === -1) {
                path = "";
            } else {
                path = path.substr(index);
            }

            return path;
        }
    }

}