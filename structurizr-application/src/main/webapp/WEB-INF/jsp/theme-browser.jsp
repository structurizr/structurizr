<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>

<style>
    html {
        scroll-padding-top: 100px;
    }

    .section {
        padding-left: 20px;
        padding-right: 20px;
        padding-bottom: 0;
    }

    #elementStyles {
        text-align: center;
        margin-top: 20px;
    }

    .elementStyle {
        position: relative;
        display: inline-block;
        width: 120px;
        text-align: center;
        margin: 0px 10px 0px 10px;
        min-height: 200px;
        max-height: 200px;
        height: 200px;
        padding: 10px;
        border-radius: 4px;
        overflow-y: hidden;
    }
    .icon {
        width: 60px;
    }
    .tag {
        height: 60px;
        font-size: 11px;
        margin-top: 5px;
    }
    .selected {
        border: solid 1px #777777;
    }

    #themeName {
        font-size: 20px;
    }
</style>

<div class="section centered">
    <div class="form-inline" style="display: inline-block; margin-bottom: 20px;">
        <select id="themesList" class="form-select">
            <option value="">Choose a theme</option>
            <c:forEach var="theme" items="${themes}">
            <option value="${theme}">${theme}</option>
            </c:forEach>
        </select>
    </div>

    <div id="themeName">Hello</div>
    <span class="smaller">(press <code>Space</code> to search, click to copy to clipboard)</span>

    <div id="elementStyles"></div>
</div>

<script nonce="${scriptNonce}">
    const LOCAL_STORAGE_THEME_KEY = 'structurizr/theme-browser/theme';

    const themesList = $('#themesList');
    const externalTheme = '${param.url}';
    const previousTheme = localStorage.getItem(LOCAL_STORAGE_THEME_KEY);

    if (previousTheme && previousTheme.length > 0) {
        themesList.val(previousTheme);
    }

    if (externalTheme && externalTheme.length > 0) {
        themesList.val('');
    }

    loadSelectedTheme();

    themesList.change(function() {
        loadSelectedTheme();
    });

    function loadSelectedTheme() {
        const theme = themesList.val();
        if (theme && theme.length > 0) {
            loadTheme('${structurizrConfiguration.webUrl}/static/themes/' + theme + '/theme.json');
            localStorage.setItem(LOCAL_STORAGE_THEME_KEY, theme);

            $('#themeName').html(theme);
        } else {
            if (externalTheme.length > 0) {
                loadTheme(externalTheme);
                localStorage.setItem(LOCAL_STORAGE_THEME_KEY, '');

                $('#themeName').html(externalTheme);
            } else {
                reset();
                $('#themeName').html('');
            }
        }
    }

    function loadTheme(url) {
        $.get(url, undefined, function (data) {
            try {
                const theme = JSON.parse(data);
                renderElementStyles(theme, url);
            } catch (e) {
                console.log('Could not load theme from ' + url);
                console.log(e);
            }
        }, 'text')
            .fail(function (xhr, textStatus, errorThrown) {
                var errorMessage = 'Could not load theme from ' + url + '; error ' + xhr.status + ' (' + xhr.statusText + ')';
                console.log(errorMessage);
                alert(errorMessage);
            });
    }

    function reset() {
        quickNavigation.clear();
        $('#elementStyles').empty();
    }

    function renderElementStyles(theme, url) {
        var index = 0;
        const baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
        reset();

        if (theme.elements !== undefined) {
            var elementStyles = theme.elements;
            elementStyles.sort(function (a, b) {
                return a.tag.toLowerCase().localeCompare(b.tag.toLowerCase());
            });

            elementStyles.forEach(function(elementStyle) {
                if (elementStyle.icon && elementStyle.icon.length > 0) {
                    try {
                        if (elementStyle.icon) {
                            if (elementStyle.icon.indexOf('http') === 0) {
                                // do nothing
                            } else {
                                elementStyle.icon = baseUrl + structurizr.util.escapeHtml(elementStyle.icon);
                            }

                        }

                        var html = '';
                        html += '<div id="style' + index + '" class="elementStyle">';
                        html += elementStyle.icon ? '<img src="' + structurizr.util.escapeHtml(elementStyle.icon) + '" class="icon" />' : '';
                        html += '<div id="style' + index + 'Tag" class="tag">';
                        html += structurizr.util.escapeHtml(elementStyle.tag);
                        html += '</div>';

                        $('#elementStyles').append(html);

                        addHandler("Element", elementStyle, index);
                    } catch (error) {
                        console.log(error);
                    }
                }

                index++;
            });

            $('.elementStyle').click(function() {
                const id = $(this).attr('id');
                selectStyle(id);
                copyTagToClipboard(id)
            });
        }
    }

    function addHandler(type, elementStyle, index) {
        var item = structurizr.util.escapeHtml(elementStyle.tag);
        if (elementStyle.icon) {
            item = '<img src="' + structurizr.util.escapeHtml(elementStyle.icon) + '" class="img-responsive" style="display: inline-block; height: 20px; margin-right: 7px" />' + item;
        }
        quickNavigation.addHandler(item, function() {
            const id = 'style' + index;
            const element = document.getElementById(id);
            element.scrollIntoView();
            selectStyle(id);
            copyTagToClipboard(id);
        });
    }

    function selectStyle(id) {
        $('.elementStyle').removeClass('selected');
        $('#' + id).addClass('selected');
    }

    function copyTagToClipboard(id) {
        const tag = document.getElementById(id).textContent
        navigator.clipboard.writeText(tag);
    }
</script>