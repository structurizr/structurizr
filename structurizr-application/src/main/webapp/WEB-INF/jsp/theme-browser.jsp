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
    }

    .elementStyle {
        position: relative;
        display: inline-block;
        width: 100px;
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
</style>

<div class="section centered">
    <div class="form-inline" style="display: inline-block; margin-bottom: 20px;">
        <select id="themesList" class="form-select">
            <option value="">Choose a theme</option>
            <c:forEach var="theme" items="${themes}">
            <option value="${theme}">${theme}</option>
            </c:forEach>
        </select>
        <span class="smaller">(press <code>Space</code> to search)</span>
    </div>

    <div id="elementStyles"></div>
</div>

<script nonce="${scriptNonce}">
    const LOCAL_STORAGE_THEME_KEY = 'structurizr/theme-browser/theme';

    const themesList = $('#themesList');
    const externalTheme = '${param.url}';
    const theme = localStorage.getItem(LOCAL_STORAGE_THEME_KEY);

    if (externalTheme.length > 0) {
        loadTheme(externalTheme);
    } else {
    themesList.val(theme);
        if (theme && theme.length > 0) {
            loadTheme('${structurizrConfiguration.webUrl}/static/themes/' + theme + '/theme.json');
        }
    }

    themesList.change(function() {
        const theme = $(this).val();
        if (theme && theme.length > 0) {
            loadTheme('${structurizrConfiguration.webUrl}/static/themes/' + theme + '/theme.json');
            localStorage.setItem(LOCAL_STORAGE_THEME_KEY, theme);
        }
    });

    function loadTheme(url) {
        $.get(url, undefined, function (data) {
            try {
                const theme = JSON.parse(data);
                renderElementStyles(theme, url);

                $('#themeName').text(theme.name);
                $('#themeDescription').text(theme.description);
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

    function renderElementStyles(theme, url) {
        var index = 0;
        const baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
        quickNavigation.clear();

        $('#elementStyles').empty();

        if (theme.elements !== undefined) {
            var elementStyles = theme.elements;
            elementStyles.sort(function (a, b) {
                return a.tag.toLowerCase().localeCompare(b.tag.toLowerCase());
            });

            elementStyles.forEach(function (elementStyle) {
                try {
                    var html = '<div class="elementStyle">';

                    if (elementStyle.icon) {
                        if (elementStyle.icon.indexOf('http') === 0) {
                            // do nothing
                        } else {
                            elementStyle.icon = baseUrl + structurizr.util.escapeHtml(elementStyle.icon);
                        }

                    }

                    html += elementStyle.icon ? '<img src="' + structurizr.util.escapeHtml(elementStyle.icon) + '" class="icon" />' : '';

                    html += '<div id="style' + index + '" class="tag">';
                    html += structurizr.util.escapeHtml(elementStyle.tag);
                    html += '</div>';

                    $('#elementStyles').append(html);

                    addHandler("Element", elementStyle, index);
                } catch (error) {
                    console.log(error);
                }

                index++;
            });
        }
    }

    function addHandler(type, elementStyle, index) {
        var item = structurizr.util.escapeHtml(elementStyle.tag);
        if (elementStyle.icon) {
            item = '<img src="' + structurizr.util.escapeHtml(elementStyle.icon) + '" class="img-responsive" style="display: inline-block; height: 20px; margin-right: 7px" />' + item;
        }
        quickNavigation.addHandler(item, function() {
            const element = document.getElementById('style' + index);
            element.scrollIntoView();
            structurizr.util.selectText('style' + index);
        });
    }
</script>

<%@ include file="/WEB-INF/fragments/quick-navigation.jspf" %>