structurizr.util.Stack = class Stack {

    #stack = [];

    pop() {
        return this.#stack.pop();
    }

    push(item){
        this.#stack.push(item);
    }

    peek() {
        if (this.isEmpty()) {
            return undefined;
        } else {
            return this.#stack[this.#stack.length - 1];
        }
    };

    isEmpty() {
        return this.#stack.length === 0;
    };

    count() {
        return this.#stack.length;
    }

}

structurizr.util.selectText = function(id) {
    if (window.getSelection()) {
        var range = document.createRange();
        range.selectNode(document.getElementById(id));
        window.getSelection().removeAllRanges();
        window.getSelection().addRange(range);
    }
};

structurizr.util.dataURIToBlob = function(dataURI) {
    // data:image/png;base64,xxx
    var binaryString = atob(dataURI.split(',')[1]);
    var length = binaryString.length;
    var array = new Uint8Array(length);
    var mimeType = dataURI.split(',')[0].split(':')[1].split(';')[0];

    for (var i = 0; i < length; i++) {
        array[i] = binaryString.charCodeAt(i);
    }

    return new Blob([array], {
        type: mimeType
    });
};

structurizr.util.downloadFile = function(content, contentType, filename) {
    var blob = new Blob([content], {type: contentType});
    var url = URL.createObjectURL(blob);

    var link = document.createElement("a");
    link.download = filename;
    link.href = url;

    document.body.appendChild(link);
    link.click();
    link.remove();
};

structurizr.util.toBlob = function(content, contentType) {
    return new Blob([content], {type: contentType});
};

structurizr.util.escapeHtml = function(html) {
    if (html === undefined || html === null) {
        return '';
    }

    return html
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
};

structurizr.util.trim = function(s) {
    if (s === undefined || s === null) {
        return '';
    } else {
        return s.trim();
    }
};

structurizr.util.btoa = function(plain) {
    return CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(plain));
};

structurizr.util.atob = function(encoded) {
    return CryptoJS.enc.Utf8.stringify(CryptoJS.enc.Base64.parse(encoded));
};

structurizr.util.exportWorkspace = function(id, json) {
    const jsonAsString = JSON.stringify(json, null, '    ');
    const filename = 'structurizr-' + id + '-workspace.json';
    structurizr.util.downloadFile(jsonAsString, "text/plain;charset=utf-8", filename);
};

structurizr.util.copyAttributeIfSpecified = function(source, destination, name) {
    if (source.hasOwnProperty(name)) {
        destination[name] = source[name];
    }
};

structurizr.util.shadeColor = function(color, percentAsInteger, darkMode) {
    if (darkMode === true) {
        percentAsInteger = -percentAsInteger;
    }

    var percent = 0;
    if (percentAsInteger === 0) {
        percent = 0;
    } else {
        if (percentAsInteger > 90) {
            percent = 0.9; // let's cap how much we shade the colour, so it doesn't become white
        } else {
            percent = percentAsInteger / 100;
        }
    }
    const f=parseInt(color.slice(1),16),t=percent<0?0:255,p=percent<0?percent*-1:percent,R=f>>16,G=f>>8&0x00FF,B=f&0x0000FF;
    return "#"+(0x1000000+(Math.round((t-R)*p)+R)*0x10000+(Math.round((t-G)*p)+G)*0x100+(Math.round((t-B)*p)+B)).toString(16).slice(1);
};

structurizr.util.sortStyles = function(a, b) {
    if (a.colorScheme === undefined && b.colorScheme === undefined) {
        return a.tag.localeCompare(b.tag);
    }

    if (a.colorScheme === undefined) {
        return -1;
    }

    if (b.colorScheme === undefined) {
        return 1;
    }

    return (a.colorScheme + '/' + a.tag).localeCompare(b.colorScheme + '/' + b.tag);
};

if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(searchString, position){
        return this.substr(position || 0, searchString.length) === searchString;
    };
}