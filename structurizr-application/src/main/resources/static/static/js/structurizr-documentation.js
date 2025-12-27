var headingAnchors = {};

function registerHeadingAnchor(sectionNumber, sectionTitle) {
    const key = sectionNumber + ' ' + sectionTitle;
    var anchor = convertToHeadingAnchor(sectionTitle);

    // has this link been registered already?
    var counter = 0;
    while (headingAnchors[key] === undefined) {
        var duplicate = false;
        Object.keys(headingAnchors).forEach(function(key) {
            if (anchor === headingAnchors[key]) {
                duplicate = true;
                counter++;
                anchor = convertToHeadingAnchor(sectionTitle) + '-' + counter;
            }
        });

        if (duplicate === false) {
            headingAnchors[key] = anchor;
        }
    }

    return anchor;
}

function findHeadingAnchor(sectionNumber, sectionTitle) {
    const key = sectionNumber + ' ' + sectionTitle;

    return headingAnchors[key];
}

function convertToHeadingAnchor(title) {
    if (title === undefined) {
        title = '';
    }

    var anchor = title.toLowerCase();
    const punctuationRegex = /[!@$%^&*()=_+[\]{}|<>,.?~`\/\\]/g
    anchor = anchor.replaceAll(punctuationRegex, '');
    anchor = anchor.replaceAll("'", '');
    anchor = anchor.replaceAll('"', '');
    anchor = anchor.replaceAll(' ', '-');

    return anchor;
}