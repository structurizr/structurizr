var structurizr = structurizr || {
    ui: {}
};

structurizr.ui.Embed = function() {

    const ASPECT_RATIO_ATTRIBUTE = 'data-aspect-ratio';

    this.receiveStructurizrResponsiveEmbedMessage = function(event) {
        if (event === undefined) {
            return;
        }

        if (event.data && event.data.context === 'iframe.resize') {
            const iframes = document.getElementsByTagName('iframe');
            for (var i = 0; i < iframes.length; i++) {
                const iframe = iframes[i];
                const eventHeight = event.data.height;

                // find iframe (the hash needs to be removed if present)
                var eventSrc = event.data.src;
                if (eventSrc.indexOf('#') > 0) {
                    eventSrc = eventSrc.substring(0, eventSrc.indexOf('#'));
                }

                if (iframe.src === eventSrc) {
                    const aspectRatio = iframe.offsetWidth / eventHeight;
                    iframe.setAttribute(ASPECT_RATIO_ATTRIBUTE, '' + aspectRatio);

                    resize(iframe, iframe.offsetWidth, eventHeight);
                }
            }

        }
    };

    function resize(iframe, width, height) {
        const maxHeightAttribute = iframe.style['max-height'];

        if (maxHeightAttribute === undefined || maxHeightAttribute === '') {
            // set to requested size
            iframe.style.width = width;
            iframe.style.height = height + 'px';
            return;
        }

        var maxHeight;
        if (maxHeightAttribute.indexOf('px') > 0) {
            maxHeight = parseInt(maxHeightAttribute.substring(0, maxHeightAttribute.indexOf('px')));
        }

        if (height <= maxHeight) {
            // set to requested size
            iframe.style.width = width;
            iframe.style.height = height + 'px';
        } else {
            // shrink width, and set height to max height
            const aspectRatio = iframe.getAttribute(ASPECT_RATIO_ATTRIBUTE);
            iframe.style.width = (maxHeight * aspectRatio) + 'px';
            iframe.style.height = maxHeight + 'px';
        }
    }

    this.resize = function () {
        const iframes = document.getElementsByTagName('iframe');
        for (var i = 0; i < iframes.length; i++) {
            const iframe = iframes[i];
            const parentNode = iframe.parentNode;
            if (parentNode) {
                iframe.width = parentNode.offsetWidth;

                const aspectRatio = iframe.getAttribute(ASPECT_RATIO_ATTRIBUTE);

                resize(iframe, parentNode.offsetWidth, parentNode.offsetWidth / aspectRatio);
            }
        }
    };
};

structurizr.embed = new structurizr.ui.Embed();
window.addEventListener("message", structurizr.embed.receiveStructurizrResponsiveEmbedMessage, false);
window.addEventListener("resize", structurizr.embed.resize, false);
