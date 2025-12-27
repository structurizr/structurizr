structurizr.Lock = function(workspaceId, agent) {

    const url = '/workspace/' + workspaceId +'/lock';
    const interval = 1000 * 60; // one minute

    setTimeout(lock, interval);

    function lock() {
        $.ajax({
            url: url,
            type: "POST",
            data: 'agent=' + encodeURIComponent(agent),
            cache: false
        })
        .done(function(data, textStatus, jqXHR) {
            if (data.success === true) {
                // the lock was acquired
                setTimeout(lock, interval);
            } else {
                alert(data.message);
                $('#exitAndUnlockWorkspaceButton').prop('disabled', 'disabled');
            }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(jqXHR.status);
            console.log("Text status: " + textStatus);
            console.log("Error thrown: " + errorThrown);

            // try again
            setTimeout(lock, interval);
        });
    }

};