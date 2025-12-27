structurizr.io.StructurizrApiClient = class StructurizrApiClient {

    #apiUrl = 'https://api.structurizr.com';
    #workspaceId;
    #apiKey;
    #apiSecret;
    #branch;
    #username;
    #agent;

    constructor(apiUrl, workspaceId, apiKey, apiSecret, branch, username, agent) {
        this.#workspaceId = workspaceId;
        this.#apiKey = apiKey;
        this.#apiSecret = apiSecret;
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
        const contentMd5 = CryptoJS.MD5("");
        const contentType = '';
        const nonce = new Date().getTime();

        var branchPath;
        if (this.#branch === undefined || this.#branch === '') {
            branchPath = '';
        } else {
            branchPath = '/branch/' + this.#branch;
        }
        const content = "GET" + "\n" + this.#getPath() + "/workspace/" + this.#workspaceId + branchPath + "\n" + contentMd5 + "\n" + contentType + "\n" + nonce + "\n";
        const hmac = CryptoJS.HmacSHA256(content, this.#apiSecret).toString(CryptoJS.enc.Hex);

        var url = this.#apiUrl + "/workspace/" + this.#workspaceId + branchPath;
        if (version !== undefined && version.trim().length > 0) {
            url = url + '?version=' + version;
        }

        $.ajax({
            url: url,
            type: "GET",
            cache: false,
            headers: {
                'Content-MD5': btoa(contentMd5),
                'Nonce': nonce,
                'X-Authorization': this.#apiKey + ":" + btoa(hmac)
            },
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
        const contentMd5 = CryptoJS.MD5(jsonAsString);
        const contentType = 'application/json; charset=UTF-8';
        const nonce = new Date().getTime();

        var branchPath;
        if (this.#branch === undefined || this.#branch === '') {
            branchPath = '';
        } else {
            branchPath = '/branch/' + this.#branch;
        }

        const content = "PUT" + "\n" +
            this.#getPath() + "/workspace/" + this.#workspaceId + branchPath + "\n" +
            contentMd5 + "\n" +
            contentType + "\n" +
            nonce + "\n";
        const hmac = CryptoJS.HmacSHA256(content, this.#apiSecret).toString(CryptoJS.enc.Hex);

        $.ajax({
            url: this.#apiUrl + "/workspace/" + this.#workspaceId + branchPath,
            type: "PUT",
            contentType: contentType,
            cache: false,
            headers: {
                'Content-Type': contentType,
                'Content-MD5': btoa(contentMd5),
                'Nonce': nonce,
                'X-Authorization': this.#apiKey + ":" + btoa(hmac),
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