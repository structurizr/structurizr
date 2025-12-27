var structurizr = structurizr || {
    ui: {}
};

structurizr.ui.Embed = function() {

    var maxHeight = undefined;
    const embeds = {};

    this.receiveStructurizrResponsiveEmbedMessage = function(event) {
        if (event === undefined) {
            return;
        }

        if (event.data) {
            if (event.data.iframe) {
                try {
                    var elementId = event.data.iframe;
                    var aspectRatio = event.data.aspectRatio;
                    var addition = event.data.controlsHeight;
                    var type = event.data.type;
                    if (type === undefined) {
                        type = 'diagram';
                    }
                    var scriptingContext = event.data.scriptingContext;

                    var embed = getEmbed(elementId);
                    embed.aspectRatio = aspectRatio;
                    embed.addition = addition;
                    embed.type = type;
                    embed.scriptingContext = scriptingContext;

                    resize(embed);
                } catch (err) {
                    console.log(event);
                    console.log("Ignoring message: " + err);
                }
            }

        }
    };

    this.setMaxHeight = function(height) {
        maxHeight = height;
    };

    function getEmbed(elementId) {
        var embed = embeds[elementId];

        if (embed === undefined) {
            embed = {
                elementId: elementId,
                aspectRatio: 1,
                addition: 0,
                type: 'diagram',
                scriptingContext: undefined,
                width: window.innerWidth
            };

            const iframe = getElement(embed.elementId);
            if (iframe) {
                const parentNode = iframe.parentNode;
                if (parentNode) {
                    embed.width = parentNode.offsetWidth;
                }
            }

            embeds[elementId] = embed;
        }

        return embed;
    }

    this.resizeEmbeddedDiagrams = function () {
        Object.keys(embeds).forEach(function(key) {
            const embed = embeds[key];
            const iframe = getElement(embed.elementId);
            if (iframe) {
                const parentNode = iframe.parentNode;
                if (parentNode) {
                    embed.width = parentNode.offsetWidth;
                }
            }

            resize(embed);
        });
    };

    var resizeHandler = function(embed) {
        var iframe = getElement(embed.elementId);
        var parentNode = iframe.parentNode;
        if (parentNode) {
            var width = embed.width;

            var aspectRatio = embed.aspectRatio;
            var addition = embed.addition;
            var type = embed.type;

            var height = Math.floor((width / aspectRatio) + addition);

            if (type !== 'exploration') {
                if (height > maxHeight) {
                    width = Math.ceil((maxHeight - addition) * aspectRatio);
                    height = maxHeight;
                }
            }

            // enforce some minimum dimensions
            width = Math.max(width, 200);
            height = Math.max(height, 200);

            iframe.width = width + "px";
            iframe.height = height + "px";
        }
    }

    function getElement(id) {
        var element = document.getElementById(id);

        if (element) {
            return element;
        } else {
            return findElement(id, document);
        }
    }

    function findElement(id, node) {
        if (node.shadowRoot) {
            const element = node.shadowRoot.getElementById(id);
            if (element) {
                return element;
            }
        }

        for (const child of node.children) {
            const element = findElement(id, child);
            if (element) {
                return element;
            }
        }

        return null;
    }

    this.onResize = function(handler) {
        resizeHandler = handler;
    }

    function resize(embed) {
        resizeHandler(embed);
    }
};

structurizr.embed = new structurizr.ui.Embed();
window.addEventListener("message", structurizr.embed.receiveStructurizrResponsiveEmbedMessage, false);
window.addEventListener("resize", structurizr.embed.resizeEmbeddedDiagrams, false);