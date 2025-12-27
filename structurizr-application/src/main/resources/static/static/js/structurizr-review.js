structurizr.Review = function(json, reviewer, canComment) {

    const self = this;
    var unsavedChanges = false;

    var notSelectedOpacity = '0.1';

    var review = json;
    var reviewSession = {
        author: reviewer,
        comments: []
    };

    var numberOfFiles = 0;
    var files = [];

    if (reviewer !== undefined && reviewer.trim() === '') {
        reviewer = undefined;
    }

    const margin = 40;

    const generalColors = [
        '#E74C3C',
        '#F39C12',
        '#F1C40F',
        '#2ECC71',
        '#16A085',
        '#3498DB',
        '#2471A3',
        '#8E44AD',
        '#F5B7B1',
        '#909497',
        '#000000'
    ];

    const commentTypeColors = {
        'RiskLow': '#2ECC71',
        'RiskMedium': '#F39C12',
        'RiskHigh': '#E74C3C',

        'STRIDE_Spoofing': '#E74C3C',
        'STRIDE_Tampering': '#F39C12',
        'STRIDE_Repudiation': '#F1C40F',
        'STRIDE_InformationDisclosure': '#2ECC71',
        'STRIDE_DenialOfService': '#3498DB',
        'STRIDE_ElevationOfPrivilege': '#8E44AD'
    };

    var commentId = 0;
    var commentColor;
    var commentType;
    var commentBeingEdited;

    var diagramIdBeingHovered = undefined;
    var modalOpen = false;

    if (canComment === true) {
        var commentTypeButtons = $('#commentTypeButtons');
        var floatingControls = $('#floatingControls');
        var commentModalButtons = $('#commentModalButtons');

        if (review.type && review.type === 'Risk') {
            var html = '<button class="btn commentTypeButton riskLowButton" style="background: ' + commentTypeColors['RiskLow'] + '; color: #ffffff" title="Risk - Low"><img src="/static/img/review/comment-RiskLow.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            html ='<button class="btn commentTypeButton riskMediumButton" style="background: ' + commentTypeColors['RiskMedium'] + '; color: #ffffff" title="Risk - Medium"><img src="/static/img/review/comment-RiskMedium.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            html = '<button class="btn commentTypeButton riskHighButton" style="background: ' + commentTypeColors['RiskHigh'] + '; color: #ffffff" title="Risk - High"><img src="/static/img/review/comment-RiskHigh.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            $('.riskLowButton').click(function() {
                self.changeCommentType('riskLowButton', 'RiskLow', commentTypeColors['RiskLow']);
            });

            $('.riskMediumButton').click(function() {
                self.changeCommentType('riskMediumButton', 'RiskMedium', commentTypeColors['RiskMedium']);
            });

            $('.riskHighButton').click(function() {
                self.changeCommentType('riskHighButton', 'RiskHigh', commentTypeColors['RiskHigh']);
            });

            $('.commentTypeButton > img').css('opacity', notSelectedOpacity);
            $('.riskLowButton > img').css('opacity', '1.0');
            commentType = 'RiskLow';
            commentColor = commentTypeColors['RiskLow'];
        } else if (review.type && review.type === 'STRIDE') {
            var html = '<button class="btn commentTypeButton strideButton strideSpoofingButton" style="background: ' + commentTypeColors['STRIDE_Spoofing'] + '; color: #000000" title="Spoofing"><img src="/static/img/review/comment-STRIDE_Spoofing.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            $('.strideSpoofingButton').click(function() {
                self.changeCommentType('strideSpoofingButton', 'STRIDE_Spoofing', commentTypeColors['STRIDE_Spoofing']);
            });

            html = '<button class="btn commentTypeButton strideButton strideTamperingButton" style="background: ' + commentTypeColors['STRIDE_Tampering'] + '; color: #000000" title="Tampering"><img src="/static/img/review/comment-STRIDE_Tampering.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            $('.strideTamperingButton').click(function() {
                self.changeCommentType('strideTamperingButton', 'STRIDE_Tampering', commentTypeColors['STRIDE_Tampering']);
            });

            html = '<button class="btn commentTypeButton strideButton strideRepudiationButton" style="background: ' + commentTypeColors['STRIDE_Repudiation'] + '; color: #000000" title="Repudiation"><img src="/static/img/review/comment-STRIDE_Repudiation.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            $('.strideRepudiationButton').click(function() {
                self.changeCommentType('strideRepudiationButton', 'STRIDE_Repudiation', commentTypeColors['STRIDE_Repudiation']);
            });

            html = '<button class="btn commentTypeButton strideButton strideInformationDisclosureButton" style="background: ' + commentTypeColors['STRIDE_InformationDisclosure'] + '; color: #000000" title="Information Disclosure"><img src="/static/img/review/comment-STRIDE_InformationDisclosure.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            $('.strideInformationDisclosureButton').click(function() {
                self.changeCommentType('strideInformationDisclosureButton', 'STRIDE_InformationDisclosure', commentTypeColors['STRIDE_InformationDisclosure']);
            });

            html = '<button class="btn commentTypeButton strideButton strideDenialOfServiceButton" style="background: ' + commentTypeColors['STRIDE_DenialOfService'] + '; color: #000000" title="Denial of Service"><img src="/static/img/review/comment-STRIDE_DenialOfService.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            $('.strideDenialOfServiceButton').click(function() {
                self.changeCommentType('strideDenialOfServiceButton', 'STRIDE_DenialOfService', commentTypeColors['STRIDE_DenialOfService']);
            });

            html = '<button class="btn commentTypeButton strideButton strideElevationOfPrivilegeButton" style="background: ' + commentTypeColors['STRIDE_ElevationOfPrivilege'] + '; color: #000000" title="Elevation of Privilege"><img src="/static/img/review/comment-STRIDE_ElevationOfPrivilege.svg" class="icon-btn"/></button>';
            commentTypeButtons.append(html);
            floatingControls.append(html);
            commentModalButtons.append(html);

            $('.strideElevationOfPrivilegeButton').click(function() {
                self.changeCommentType('strideElevationOfPrivilegeButton', 'STRIDE_ElevationOfPrivilege', commentTypeColors['STRIDE_ElevationOfPrivilege']);
            });

            $('.commentTypeButton > img').css('opacity', notSelectedOpacity);
            $('.strideSpoofingButton > img').css('opacity', '1.0');
            commentType = 'STRIDE_Spoofing';
            commentColor = commentTypeColors['STRIDE_Spoofing'];
        } else {
            var count = 0;
            generalColors.forEach(function (color) {
                var html = '<button class="commentColor' + count + ' btn commentTypeButton" style="background: ' + color + '"><img src="/static/img/review/comment-General.svg" class="icon-btn"/></button>';
                commentTypeButtons.append(html);
                floatingControls.append(html);
                commentModalButtons.append(html);

                (function (index) {
                    document.getElementsByClassName('commentColor' + index)[0].onclick = function () {
                        self.changeCommentType('commentColor' + index, 'General', generalColors[index])
                    };

                    document.getElementsByClassName('commentColor' + index)[1].onclick = function () {
                        self.changeCommentType('commentColor' + index, 'General', generalColors[index])
                    };

                    document.getElementsByClassName('commentColor' + index)[2].onclick = function () {
                        self.changeCommentType('commentColor' + index, 'General', generalColors[index])
                    };
                }(count));

                count++;
            });

            $('.commentTypeButton > img').css('opacity', notSelectedOpacity);
            $('.commentColor0 > img').css('opacity', '1.0');
            commentType = 'General';
            commentColor = generalColors[0];
        }

    }

    if (review !== undefined) {
        review.diagrams.forEach(function(diagram) {
            renderDiagram(diagram.id);

            if (diagram.comments !== undefined) {
                diagram.comments.forEach(function(comment) {
                    commentId = Math.max(commentId, comment.id);
                    renderComment(comment);
                });
            } else {
                diagram.comments = [];
            }
        });
    } else {
        review = {
            diagrams: []
        };
    }

    $(window).resize(function() {
        self.resize();
    });

    $(window).keypress(function(event) {
        const d = 100;
        const e = 101;
        const h = 104;
        const i = 105;
        const l = 108;
        const m = 109;
        const r = 114;
        const s = 115;
        const t = 116;

        if (event.target.tagName.toLowerCase() !== 'textarea')
        {
            if (review.type === 'Risk') {
                if (event.which === l) {
                    $('.riskLowButton').click();
                } else if (event.which === m) {
                    $('.riskMediumButton').click();
                } else if (event.which === h) {
                    $('.riskHighButton').click();
                }
            } else if (review.type && review.type === 'STRIDE') {
                if (event.which === s) {
                    $('.strideSpoofingButton').click();
                } else if (event.which === t) {
                    $('.strideTamperingButton').click();
                } else if (event.which === r) {
                    $('.strideRepudiationButton').click();
                } else if (event.which === i) {
                    $('.strideInformationDisclosureButton').click();
                } else if (event.which === d) {
                    $('.strideDenialOfServiceButton').click();
                } else if (event.which === e) {
                    $('.strideElevationOfPrivilegeButton').click();
                }
            }
        }
    });

    $('#commentModal').on('shown.bs.modal', function (e) {
        $('#commentDescription').focus();
        modalOpen = true;
    });

    $('#commentModal').on('hidden.bs.modal', function (e) {
        hideTooltip();
        modalOpen = false;

        if (commentBeingEdited !== undefined) {
            var description = $('#commentDescription').val();

            if (description.trim() === '') {
                deleteComment(commentBeingEdited.id);
            } else {
                commentBeingEdited.description = description;
            }

            commentBeingEdited = undefined;
        }
    });

    $('#commentDescription').on("keypress", function(e) {
        if (e.keyCode === 13) {
            e.preventDefault();
            closeCommentModal();
        }
    });

    const createReviewButton = document.getElementById('createReviewButton');
    if (createReviewButton) {
        createReviewButton.onclick = function() {
            var form = document.getElementById('createReviewForm');

            files.forEach(function(file) {
                var input = document.createElement("input");
                input.setAttribute("type", "hidden");
                input.setAttribute("name", "file");
                input.setAttribute("value", file);

                form.appendChild(input);
            });

            setUnsavedChanges(false);
            form.submit();
        }
    }

    var submitReviewButton = document.getElementById('submitReviewButton');
    if (submitReviewButton) {
        submitReviewButton.onclick = function() {
            $('#reviewSessionJson').val(JSON.stringify(reviewSession));
            setUnsavedChanges(false);
            document.getElementById('submitReviewForm').submit();
        }
    }

    this.addDiagram = function() {
        var diagramPicker = document.getElementById('diagramPicker');
        for (var i = 0; i < diagramPicker.files.length; i++) {
            importImageFromFile(diagramPicker.files[i]);
        }
    };

    function setCommentColorAndIcon(comment) {
        var commentTypeClass = 'comment' + comment.type;

        var color = commentTypeColors[comment.type];
        if (color === undefined) {
            color = comment.color;
        }

        var domId = 'comment' + comment.id;
        $('#' + domId).css('background-color', color);
        $('#' + domId).attr('class', 'comment ' + commentTypeClass);
    }

    this.changeCommentType = function(selector, type, color) {
        commentType = type;
        commentColor = color;

        $('.commentTypeButton > img').css('opacity', notSelectedOpacity);
        $('.' + selector + ' > img').css('opacity', '1.0');

        if (commentBeingEdited !== undefined) {
            // change the comment color too
            commentBeingEdited.color = commentColor;
            commentBeingEdited.type = commentType;

            setCommentColorAndIcon(commentBeingEdited);

            $('#commentModalContent').css('background-color', commentColor);
            $('#commentDescription').focus();
        }
    };

    function importImageFromFile(file) {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        numberOfFiles++;

        reader.onload = function (evt) {
            try {
                const fileContent = evt.target.result;
                if (fileContent && fileContent.startsWith('data:image/png;base64,') || fileContent.startsWith('data:image/jpeg;base64,') || fileContent.startsWith('data:image/gif;base64,')) {
                    var diagramId = review.diagrams.length+1;
                    review.diagrams.push({
                        id: diagramId,
                        name: file.name,
                        url: fileContent,
                        comments: []
                    });

                    files.push(fileContent);
                    setUnsavedChanges(true);

                    renderDiagram(diagramId);
                    $('#addDiagramsButton').prop('disabled', 'disabled');
                    $('#createReviewButton').prop('disabled', '');
                } else {
                    console.log("The image should be a PNG/JPG/GIF file.");
                }
            } catch(err) {
                alert("Sorry, there was a problem reading the file: " + err);
                console.log(err);
            }
        };

        reader.onerror = function (evt) {
            alert("Sorry, there was a problem reading the file.");
        };
    }

    function getDiagram(diagramId) {
        var diagram = undefined;

        review.diagrams.forEach(function(d) {
            if (d.id === diagramId) {
                diagram = d;
            }
        });

        return diagram;
    }

    function getComment(commentId) {
        var comment = undefined;

        review.diagrams.forEach(function(d) {
            d.comments.forEach(function(c) {
                if (c.id === commentId) {
                    comment = c;
                }
            });
        });

        return comment;
    }

    function renderDiagram(diagramId) {
        var diagramsDiv = $('#diagrams');

        var diagram = getDiagram(diagramId);

        var html = '';

        html += '<div class="diagramTitle">Diagram ' + diagram.id + (canComment === true ? ' of '+ review.diagrams.length : '') + '</div>';
        html += '  <div id="diagram' + diagramId + 'Container" class="diagramContainer img-thumbnail" style="position: relative">';
        html += '    <div id="diagram' + diagramId + 'Viewport" class="diagramViewport">';
        html += '      <div id="diagram' + diagramId + 'Wrapper" class="diagramWrapper">';
        html += '        <img id="diagram' + diagramId + 'Image" />';
        html += '      </div>';
        html += '    </div>';

        html += '<div class="embeddedControls">';
        html += '<div class="btn-group">';
        html += '<button id="diagram' + diagramId + 'ZoomOutButton" class="btn btn-default" title="Zoom out"><img src="/static/bootstrap-icons/zoom-out.svg" class="icon-btn" /></button>';
        html += '<button id="diagram' + diagramId + 'ZoomInButton" class="btn btn-default" title="Zoom in"><img src="/static/bootstrap-icons/zoom-in.svg" class="icon-btn" /></button>';
        html += '</div>';

        html += '  </div>';
        html += '</div>';

        diagramsDiv.append(html);

        document.getElementById('diagram' + diagramId + 'ZoomInButton').onclick = function() {
            zoomIn(diagramId);
        };

        document.getElementById('diagram' + diagramId + 'ZoomOutButton').onclick = function() {
            zoomOut(diagramId);
        };

        document.getElementById('diagram' + diagramId + 'Image').onmouseenter = function(e) {
            diagramIdBeingHovered = diagramId;
        };

        document.onkeypress = function(e) {
            var plus = 43;
            var equals = 61;
            var minus = 45;

            if (modalOpen === false && diagramIdBeingHovered !== undefined) {
                if (e.which === plus || e.which === equals) {
                    zoomIn(diagramIdBeingHovered);
                } else if (e.which === minus) {
                    zoomOut(diagramIdBeingHovered);
                }
            }
        };

        var diagramImage = document.getElementById('diagram' + diagramId + 'Image');

        var img = new Image();
        img.onload = function(){
            diagram.width = img.naturalWidth;
            diagram.height = img.naturalHeight;

            diagramImage.src = img.src;
            resizeDiagram(diagramId);
        };

        img.src = diagram.url;

        if (canComment === true) {
            diagramImage.onclick = function (e) {
                var x = e.offsetX;
                var y = e.offsetY;

                addComment(diagramId, x, y);
            }
        }
    }

    this.resize = function() {
        review.diagrams.forEach(function(diagram) {
            resizeDiagram(diagram.id);
        });
    };

    function resizeDiagram(diagramId) {
        var diagramContainer = $('#diagram' + diagramId + 'Container');
        diagramContainer.width(window.innerWidth - margin - 10);
        diagramContainer.height(window.innerHeight - margin - 10);

        var diagramViewport = $('#diagram' + diagramId + 'Viewport');
        diagramViewport.width(window.innerWidth - margin - 10);
        diagramViewport.height(window.innerHeight - margin - 10);

        scaleToFit(diagramId);
    }

    function getMaximumWidth() {
        return window.innerWidth - margin - 10;
    }

    function scaleToFit(diagramId) {
        var diagram = getDiagram(diagramId);
        var targetWidth = getMaximumWidth();
        var scale = (targetWidth / diagram.width);

        zoomTo(diagram, scale);
    }

    function zoomIn(diagramId) {
        var diagram = getDiagram(diagramId);
        var scale = diagram.scale * 2;

        zoomTo(diagram, scale);
    }

    function zoomOut(diagramId) {
        var diagram = getDiagram(diagramId);
        var scale = diagram.scale / 2;

        zoomTo(diagram, scale);
    }

    function zoomTo(diagram, scale) {
        var diagramId = diagram.id;

        var minimumScale = getMaximumWidth() / diagram.width;
        var maximumScale = 2;
        scale = Math.min(scale, maximumScale);
        scale = Math.max(scale, minimumScale);

        diagram.scale = scale;

        var diagramWrapper = document.getElementById('diagram' + diagramId + 'Wrapper');
        diagramWrapper.style['transform'] = 'scale(' + scale + ')';

        var actualHeight = diagram.height * diagram.scale;
        $('#diagram' + diagramId + 'Wrapper').height(actualHeight);
        $('#diagram' + diagramId + 'Viewport').height(actualHeight);
        $('#diagram' + diagramId + 'Container').height(actualHeight);

        if (scale > 1) {
            $('#diagram' + diagramId + 'Wrapper').width(diagram.width / scale);
        }

        scaleComments(diagramId);
    }

    function scaleComments(diagramId) {
        var diagram = getDiagram(diagramId);
        if (diagram.comments) {
            var targetWidthAndHeight = 40;
            var scaledWidthAndHeight = targetWidthAndHeight / diagram.scale;
            //const scaledPadding = targetPadding / diagram.scale;
            var scaledBorderRadius = scaledWidthAndHeight / 10;
            var scaledFontSize = scaledWidthAndHeight / 2;
            var scaledPadding = scaledWidthAndHeight / 2;

            var allComments = $('#diagram' + diagramId + 'Wrapper > .comment');
            allComments.css('width', scaledWidthAndHeight + 'px');
            allComments.css('height', scaledWidthAndHeight + 'px');
            allComments.css('background-size', (scaledWidthAndHeight-scaledPadding) + 'px ' + (scaledWidthAndHeight-scaledPadding) + 'px');
            allComments.css('border-radius', scaledBorderRadius + 'px');
            allComments.css('font-size', scaledFontSize + 'px');

            diagram.comments.forEach(function (comment) {
                var left = comment.x - (scaledWidthAndHeight / 2);
                var top = comment.y - (scaledWidthAndHeight / 2);

                if (left < 0) {
                    left = 0;
                } else if ((left + scaledWidthAndHeight) > diagram.width) {
                    left = diagram.width - scaledWidthAndHeight;
                }

                if (top < 0) {
                    top = 0;
                } else if ((top + scaledWidthAndHeight) > diagram.height) {
                    top = diagram.height - scaledWidthAndHeight;
                }

                var domId = '#comment' + comment.id;
                var commentDiv = $(domId);
                commentDiv.css('left', left + 'px');
                commentDiv.css('top', top + 'px');
            });
        }
    }

    function addComment(diagramId, x, y) {
        var diagram = getDiagram(diagramId);
        commentId++;
        var id = commentId;

        var comment = {
            id: id,
            diagramId: diagramId,
            x: x,
            y: y,
            type: commentType,
            color: commentColor,
            description: '',
            author: reviewer
        };

        diagram.comments.push(comment);
        reviewSession.comments.push(comment);
        $('#submitReviewButton').prop('disabled', '');
        $('#submitReviewButton').addClass('btn-danger');

        renderComment(comment);

        var domId = 'comment' + comment.id;
        dragElement(document.getElementById(domId), comment);
        setUnsavedChanges(true);

        openCommentModal(commentId);
    }

    function renderComment(comment) {
        var domId = 'comment' + comment.id;
        var html = '';
        var commentTypeClass = 'commentGeneral';

        html += '<div id="' + domId + '" class="comment"></div>';

        $('#diagram' + comment.diagramId + 'Wrapper').append(html);
        setCommentColorAndIcon(comment);
        scaleComments(comment.diagramId);

        document.getElementById(domId).onclick = function(e) {
            if (dragging === false) {
                openCommentModal(comment.id);
            }
        };
        document.getElementById(domId).onmouseover = function(e) {
            if (dragging === false) {
                showTooltip(e, comment.id);
            }
        };
        document.getElementById(domId).onmouseout = function(e) {
            hideTooltip();
        };
    }

    function deleteComment(id) {
        var comment = getComment(id);

        if (comment !== undefined) {
            var domId = 'comment' + id;
            document.getElementById(domId).remove();

            reviewSession.comments.splice(reviewSession.comments.indexOf(comment), 1);

            review.diagrams.forEach(function(diagram) {
                if (diagram.comments.indexOf(comment) > -1) {
                    diagram.comments.splice(diagram.comments.indexOf(comment), 1);
                }
            });

            if (reviewSession.comments.length === 0) {
                $('#submitReviewButton').prop('disabled', 'disabled');
            }
        }
    }

    function openCommentModal(id) {
        var comment = getComment(id);

        if (reviewSession.comments.indexOf(comment) > -1) {
            commentBeingEdited = comment;
            $('#commentDescription').val(comment.description);
            $('#commentModalContent').css('background-color', comment.color);
            $('#commentModal').modal();
        } else {
            // the comment cannot be edited
            commentBeingEdited = undefined;
        }
    }

    function closeCommentModal() {
        $('#commentModal').modal('hide');
    }

    function dragElement(element, comment) {
        var diagram = getDiagram(comment.diagramId);

        var pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
        element.onmousedown = dragMouseDown;
        element.style['cursor'] = 'move';

        function dragMouseDown(e) {
            e.preventDefault();
            pos3 = (e.clientX / diagram.scale);
            pos4 = (e.clientY / diagram.scale);
            document.onmouseup = closeDragElement;
            document.onmousemove = elementDrag;
        }

        function elementDrag(e) {
            e.preventDefault();

            dragging = true;

            pos1 = pos3 - (e.clientX / diagram.scale);
            pos2 = pos4 - (e.clientY / diagram.scale);
            pos3 = (e.clientX / diagram.scale);
            pos4 = (e.clientY / diagram.scale);

            element.style.top = (element.offsetTop - pos2) + "px";
            element.style.left = (element.offsetLeft - pos1) + "px";
            element.style.opacity = '0.5';
        }

        function closeDragElement(e) {
            document.onmousemove = null;
            document.onmouseup = null;

            element.style.opacity = '';

            // find the centre point, and use this to update the underlying comment
            var x = element.offsetLeft + ((element.offsetWidth) / 2);
            var y = element.offsetTop + ((element.offsetHeight) / 2);

            var targetWidthAndHeight = 40;
            var scaledWidthAndHeight = targetWidthAndHeight / diagram.scale;

            if (x < 0) {
                x = 0;
            } else if ((x + scaledWidthAndHeight) > diagram.width) {
                x = diagram.width - (scaledWidthAndHeight/2);
            }

            if (y < 0) {
                y = 0;
            } else if ((y + scaledWidthAndHeight) > diagram.height) {
                y = diagram.height - (scaledWidthAndHeight/2);
            }

            comment.x = x;
            comment.y = y;


            scaleComments(diagram.id);

            setTimeout(function() {
                dragging = false;
            }, 100);
        }
    }

    var dragging = false;

    var tooltip = $('#tooltip');
    var tooltipAuthor = $('#tooltipAuthor');
    var tooltipAuthorName = $('#tooltipAuthorName');
    var tooltipDescription = $('#tooltipDescription');

    function repositionTooltip(x, y) {
        var windowWidth = window.innerWidth;
        var windowHeight = window.innerHeight;
        var tooltipWidth = tooltip.outerWidth(true);
        var tooltipHeight = tooltip.outerHeight(true);

        if ((x + tooltipWidth) < windowWidth) {
            // do nothing
        } else {
            x = x - tooltipWidth;
        }

        if ((y + tooltipHeight) < windowHeight) {
            // do nothing
        } else {
            y = y - tooltipHeight;
        }

        tooltip.css({left: Math.max(0, x), top: Math.max(0, y)});
    }

    function hideTooltip() {
        tooltip.addClass("hidden");
    }

    function showTooltip(e, commentId) {
        var comment = getComment(commentId);
        if (comment.description !== '') {
            if (comment.author !== undefined) {
                tooltipAuthorName.text(comment.author);
                tooltipAuthor.removeClass("hidden");
            } else {
                tooltipAuthorName.text('');
                tooltipAuthor.addClass("hidden");
            }
            tooltipDescription.text(comment.description);

            tooltip.removeClass("hidden");
            tooltip.css("background", comment.color);
            tooltip.css("color", '#ffffff');

            repositionTooltip(e.pageX, e.pageY);
        }
    }

    function setUnsavedChanges(bool) {
        unsavedChanges = bool;

        $('#exportToJsonButton').prop('disabled', bool);
    }

    this.isUnsavedChanges = function() {
        return unsavedChanges;
    };

};