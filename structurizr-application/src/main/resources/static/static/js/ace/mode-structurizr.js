define(
    "ace/mode/structurizr_highlight_rules", ["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
        "use strict";

        var oop = require("../lib/oop");
        var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

        var MyNewHighlightRules = function() {

            // regexp must not have capturing parentheses. Use (?:) instead.
            // regexps are ordered -> the first match is used
            this.$rules = {
                "whitespace": [
                    {
                        token: "structurizr_whitespace",
                        regex: /^\\s*/,
                        next: 'start'
                    }
                ],
                "start" : [
                    {
                        token: 'structurizr_brace',
                        regex: '(})$',
                        next: 'start'
                    },
                    {
                        token: ["structurizr_variable", "structurizr_keyword", "structurizr_variable", "structutrizr_string"],
                        regex: '(.*)( -> )(.+?)( |$)',
                        next: 'default'
                    },
                    {
                        token: ["structurizr_variable", "structurizr_keyword"],
                        regex: '(.*)( = )'
                    },
                    {
                        token: ["structurizr_keyword", "structurizr_variable"],
                        regex: '(systemContext)( .+? )',
                        next: 'default'
                    },
                    {
                        token: "structurizr_keyword",
                        regex: '(!include https).*$',
                        next: 'start'
                    },
                    {
                        token: "structurizr_keyword_disabled",
                        regex: '(!include|!docs|!adrs|!plugin|!script) .*$'
                    },
                    {
                        token: ["structurizr_keyword", "structurizr_keyword"],
                        regex: '(!identifiers )(.*$)',
                        next: 'start'
                    },
                    {
                        token: ["structurizr_keyword", "structurizr_constant"],
                        regex: '(!constant )(.*$)',
                        next: 'start'
                    },
                    {
                        token: ["structurizr_keyword", "structurizr_variable"],
                        regex: '(include|exclude)( .*$)',
                        next: 'start'
                    },
                    {
                        token: "structurizr_keyword",
                        regex: '(animation)',
                        next: 'animation'
                    },
                    {
                        token: ["structurizr_keyword", "structurizr_variable"],
                        regex: '(softwareSystemInstance|containerInstance|instanceOf)( .*)$',
                        next: 'start'
                    },
                    {
                        token: "structurizr_keyword",
                        regex: '(autolayout)$',
                        next: 'start'
                    },
                    {
                        token: "structurizr_keyword",
                        regex: '(!constant|workspace|model|ref|impliedRelationships|enterprise|archetypes|group|person|softwareSystemInstance|softwareSystem|containerInstance|container|component|deploymentEnvironment|deploymentNode|infrastructureNode|name|description|tags|url|properties|perspectives|perspective|views|systemLandscape|container|component|filtered|dynamic|deployment|autolayout|animationStep|title|styles|element|relationship|shape|icon|logo|width|height|background|color|colour|stroke|fontSize|border|opacity|metadata|description|thickness|dashed|routing|position|themes|theme|branding|terminology|configuration|visibility|scope|users)',
                        next: 'default'
                    },
                    {
                        token: 'structurizr_comment',
                        regex: '(/\\*)(.+?)(\\*/)$'
                    },
                    {
                        token: 'structurizr_comment',
                        regex: '(#)(.*$)'
                    },
                    {
                        token: 'structurizr_comment',
                        regex: '(//)(.*$)'
                    },
                    {
                        token: 'structurizr_comment',
                        regex: '(/\\*)(.*$)',
                        next: 'comment'
                    },
                    {
                        token: 'structurizr_string',
                        regex: '\"',
                        next: "string"
                    },
                    {
                        caseInsensitive: true
                    },
                    {
                        defaultToken : "structurizr_default"
                    }
                ],
                "string" : [
                    {
                        token : "structurizr_string", regex : "\"", next: "start"
                    },
                    {
                        defaultToken : "structurizr_string"
                    }
                ],
                "comment" : [
                    {
                        token : "structurizr_comment",
                        regex : '(.*?)(\\*/)$',
                        next: "start"
                    },
                    {
                        token : "structurizr_comment",
                        regex : "^(.*)$",
                        next: "comment"
                    },
                    {
                        defaultToken : "structurizr_comment"
                    }
                ],
                "animation" : [
                    {
                        token: 'structurizr_brace',
                        regex: '({)$',
                        next: 'animation'
                    },
                    {
                        token: 'structurizr_brace',
                        regex: '(})$',
                        next: 'start'
                    },
                    {
                        defaultToken : "structurizr_variable"
                    }
                ],
                "default" : [
                    {
                        token: 'structurizr_whitespace',
                        regex: '$',
                        next: 'start'
                    },
                    {
                        token: 'structurizr_brace',
                        regex: '({)$',
                        next: 'start'
                    },
                    {
                        token: "structurizr_whitespace",
                        regex: '(\\s)'
                    },
                    {
                        token: "structurizr_constant",
                        regex: '(\\${.+?})'
                    },
                    {
                        defaultToken : "structurizr_string"
                    }
                ]
            };
        };

        oop.inherits(MyNewHighlightRules, TextHighlightRules);

        exports.MyNewHighlightRules = MyNewHighlightRules;
    }
);

define("ace/mode/folding/cstyle",["require","exports","module","ace/lib/oop","ace/range","ace/mode/folding/fold_mode"], function(require, exports, module) {
    "use strict";

    var oop = require("../../lib/oop");
    var Range = require("../../range").Range;
    var BaseFoldMode = require("./fold_mode").FoldMode;

    var FoldMode = exports.FoldMode = function(commentRegex) {
        if (commentRegex) {
            this.foldingStartMarker = new RegExp(
                this.foldingStartMarker.source.replace(/\|[^|]*?$/, "|" + commentRegex.start)
            );
            this.foldingStopMarker = new RegExp(
                this.foldingStopMarker.source.replace(/\|[^|]*?$/, "|" + commentRegex.end)
            );
        }
    };
    oop.inherits(FoldMode, BaseFoldMode);

    (function() {

        this.foldingStartMarker = /([\{\[\(])[^\}\]\)]*$|^\s*(\/\*)/;
        this.foldingStopMarker = /^[^\[\{\(]*([\}\]\)])|^[\s\*]*(\*\/)/;
        this.singleLineBlockCommentRe= /^\s*(\/\*).*\*\/\s*$/;
        this.tripleStarBlockCommentRe = /^\s*(\/\*\*\*).*\*\/\s*$/;
        this.startRegionRe = /^\s*(\/\*|\/\/)#?region\b/;
        this._getFoldWidgetBase = this.getFoldWidget;
        this.getFoldWidget = function(session, foldStyle, row) {
            var line = session.getLine(row);

            if (this.singleLineBlockCommentRe.test(line)) {
                if (!this.startRegionRe.test(line) && !this.tripleStarBlockCommentRe.test(line))
                    return "";
            }

            var fw = this._getFoldWidgetBase(session, foldStyle, row);

            if (!fw && this.startRegionRe.test(line))
                return "start"; // lineCommentRegionStart

            return fw;
        };

        this.getFoldWidgetRange = function(session, foldStyle, row, forceMultiline) {
            var line = session.getLine(row);

            if (this.startRegionRe.test(line))
                return this.getCommentRegionBlock(session, line, row);

            var match = line.match(this.foldingStartMarker);
            if (match) {
                var i = match.index;

                if (match[1])
                    return this.openingBracketBlock(session, match[1], row, i);

                var range = session.getCommentFoldRange(row, i + match[0].length, 1);

                if (range && !range.isMultiLine()) {
                    if (forceMultiline) {
                        range = this.getSectionRange(session, row);
                    } else if (foldStyle != "all")
                        range = null;
                }

                return range;
            }

            if (foldStyle === "markbegin")
                return;

            var match = line.match(this.foldingStopMarker);
            if (match) {
                var i = match.index + match[0].length;

                if (match[1])
                    return this.closingBracketBlock(session, match[1], row, i);

                return session.getCommentFoldRange(row, i, -1);
            }
        };

        this.getSectionRange = function(session, row) {
            var line = session.getLine(row);
            var startIndent = line.search(/\S/);
            var startRow = row;
            var startColumn = line.length;
            row = row + 1;
            var endRow = row;
            var maxRow = session.getLength();
            while (++row < maxRow) {
                line = session.getLine(row);
                var indent = line.search(/\S/);
                if (indent === -1)
                    continue;
                if  (startIndent > indent)
                    break;
                var subRange = this.getFoldWidgetRange(session, "all", row);

                if (subRange) {
                    if (subRange.start.row <= startRow) {
                        break;
                    } else if (subRange.isMultiLine()) {
                        row = subRange.end.row;
                    } else if (startIndent == indent) {
                        break;
                    }
                }
                endRow = row;
            }

            return new Range(startRow, startColumn, endRow, session.getLine(endRow).length);
        };
        this.getCommentRegionBlock = function(session, line, row) {
            var startColumn = line.search(/\s*$/);
            var maxRow = session.getLength();
            var startRow = row;

            var re = /^\s*(?:\/\*|\/\/|--)#?(end)?region\b/;
            var depth = 1;
            while (++row < maxRow) {
                line = session.getLine(row);
                var m = re.exec(line);
                if (!m) continue;
                if (m[1]) depth--;
                else depth++;

                if (!depth) break;
            }

            var endRow = row;
            if (endRow > startRow) {
                return new Range(startRow, startColumn, endRow, line.length);
            }
        };

    }).call(FoldMode.prototype);

});

define("ace/mode/structurizr",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/structurizr_highlight_rules","ace/mode/folding/cstyle"], function(require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var ApacheConfHighlightRules = require("./structurizr_highlight_rules").MyNewHighlightRules;
    var FoldMode = require("./folding/cstyle").FoldMode;

    var Mode = function() {
        this.HighlightRules = ApacheConfHighlightRules;
        this.foldingRules = new FoldMode();
        this.$behaviour = this.$defaultBehaviour;
    };
    oop.inherits(Mode, TextMode);

    (function() {
        this.lineCommentStart = "#";
        this.$id = "ace/mode/structurizr";
    }).call(Mode.prototype);

    exports.Mode = Mode;
});                (function() {
    window.require(["ace/mode/structurizr"], function(m) {
        if (typeof module == "object" && typeof exports == "object" && module) {
            module.exports = m;
        }
    });
})();
