structurizr.HealthCheck = function(callback) {

    var enabled = false;
    const healthChecks = [];

    if (structurizr.workspace.hasElements()) {
        const elements = structurizr.workspace.getElements();
        Object.keys(elements).forEach(function (key) {
            const element = elements[key];
            if (element.hasOwnProperty('healthChecks')) {
                element.healthChecks.forEach(function (healthCheck) {
                    healthChecks.push({
                        "elementId": element.id,
                        "name": healthCheck.name,
                        "url": healthCheck.url,
                        "headers": healthCheck.headers,
                        "interval": healthCheck.interval,
                        "status": true,
                        "timestamp": undefined
                    });
                });
            }
        });
    }

    this.start = function() {
        enabled = true;

        healthChecks.forEach(function(healthCheck) {
            run(healthCheck);
        });
    };

    this.stop = function() {
        enabled = false;
    }

    function run(healthCheck) {
        try {
            $.ajax({
                url: healthCheck.url,
                headers: healthCheck.headers,
                dataType: 'text',
                timeout: healthCheck.timeout
            })
            .done(function (data, textStatus, jqXHR) {
                healthCheck.status = true;
                healthCheck.message = undefined;
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                healthCheck.status = false;
                healthCheck.message = 'HTTP status code returned = ' + jqXHR.status;
                console.log("Health check for '" + getElementName(healthCheck.elementId) + "' failed: " + textStatus + ' (HTTP status code=' + jqXHR.status + ')');
            })
            .always(function () {
                try {
                    callback(healthChecks, healthCheck);
                } catch (err) {
                    console.log(err);
                }

                if (enabled) {
                    setTimeout(function () {
                        run(healthCheck);
                    }, healthCheck.interval * 1000)
                }
            });
        } catch (err) {
            healthCheck.status = false;
            healthCheck.message = 'There was an error executing health check: ' + err;
            console.log('There was an error executing health check: ' + err);

            callback(healthChecks, healthCheck);

            if (enabled) {
                setTimeout(function () {
                    run(healthCheck);
                }, healthCheck.interval * 1000);
            }
        }
    }

    function getElementName(elementId) {
        var element = structurizr.workspace.findElementById(elementId);
        var name = element.name;

        while (element.parentId !== undefined) {
            element = structurizr.workspace.findElementById(element.parentId);
            name = element.name + ' - ' + name;
        }

        return name;
    }

};