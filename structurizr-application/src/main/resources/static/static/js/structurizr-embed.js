var structurizr = structurizr || {
    ui: {}
};

structurizr.ui.Embed = function() {

    const ASPECT_RATIO_ATTRIBUTE = 'data-aspect-ratio';
    const TOOLBAR_HEIGHT_ATTRIBUTE = 'data-toolbar-height';
    const MAX_HEIGHT = 'max-height';

    this.receiveStructurizrResponsiveEmbedMessage = function(event) {
        if (event === undefined) {
            return;
        }

        if (event.data && event.data.context === 'iframe.resize') {
            const iframes = document.getElementsByTagName('iframe');
            for (var i = 0; i < iframes.length; i++) {
                const iframe = iframes[i];

                // find iframe (the hash needs to be removed if present because this isn't available to access)
                var eventSrc = event.data.src;
                if (eventSrc.indexOf('#') > 0) {
                    eventSrc = eventSrc.substring(0, eventSrc.indexOf('#'));
                }

                if (iframe.src === eventSrc) {
                    iframe.setAttribute(ASPECT_RATIO_ATTRIBUTE, '' + event.data.aspectRatio);
                    iframe.setAttribute(TOOLBAR_HEIGHT_ATTRIBUTE, '' + event.data.toolbarHeight);
                    resizeIframe(iframe);
                }
            }

        }
    };

    function resizeIframe(iframe) {
        const parentNode = iframe.parentNode;
        if (parentNode) {
            const availableWidth = parentNode.offsetWidth;

            const aspectRatio = parseFloat(iframe.getAttribute(ASPECT_RATIO_ATTRIBUTE));
            const toolbarHeight = parseInt(iframe.getAttribute(TOOLBAR_HEIGHT_ATTRIBUTE));
            const maxHeightAttribute = iframe.style[MAX_HEIGHT];

            var calculatedHeight = ((availableWidth / aspectRatio) + toolbarHeight);

            if (maxHeightAttribute === undefined || maxHeightAttribute === '') {
                iframe.style.width = availableWidth + 'px';
                iframe.style.height = calculatedHeight + 'px';
                return;
            }

            var maxHeight;
            if (maxHeightAttribute.indexOf('px') > 0) {
                maxHeight = parseInt(maxHeightAttribute.substring(0, maxHeightAttribute.indexOf('px')));
            }

            if (calculatedHeight <= maxHeight) {
                iframe.style.width = availableWidth + 'px';
                iframe.style.height = calculatedHeight + 'px';
            } else {
                // shrink width, and set height to max height
                const maxDiagramHeight = (maxHeight - toolbarHeight);
                iframe.style.width = (maxDiagramHeight * aspectRatio) + 'px';
                iframe.style.height = maxHeight + 'px';
            }
        }
    }

    this.resize = function () {
        const iframes = document.getElementsByTagName('iframe');
        for (var i = 0; i < iframes.length; i++) {
            resizeIframe(iframes[i]);
        }
    };

};

structurizr.embed = new structurizr.ui.Embed();
window.addEventListener("message", structurizr.embed.receiveStructurizrResponsiveEmbedMessage, false);
window.addEventListener("resize", structurizr.embed.resize, false);