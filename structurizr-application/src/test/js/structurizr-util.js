QUnit.test("structurizr.util.btoa()", function(assert) {
    assert.equal("SGVsbG8gV29ybGQ=", structurizr.util.btoa("Hello World"));
});

QUnit.test("structurizr.util.atob()", function(assert) {
    assert.equal("Hello World", structurizr.util.atob("SGVsbG8gV29ybGQ="));
});

QUnit.test("shadeColour_DoesNothing_WhenPassedWhiteWithAPositivePercentage", function( assert ) {
    var colour = structurizr.util.shadeColor("#ffffff", 50);
    assert.equal(colour, "#ffffff");
});

QUnit.test("shadeColour_DoesNothing_WhenPassedPureBlackWithANegativePercentage", function( assert ) {
    var colour = structurizr.util.shadeColor("#000000", -50);
    assert.equal(colour, "#000000");
});

QUnit.test("shadeColour_DoesNothing_WhenPassedAZeroPercentage", function( assert ) {
    var colour = structurizr.util.shadeColor("#aabbcc", 0);
    assert.equal(colour, "#aabbcc");
});

QUnit.test("shadeColour_LightensTheColour_WhenPassedAPositivePercentage", function( assert ) {
    var colour = structurizr.util.shadeColor("#6699CC", 20);
    assert.equal(colour, "#85add6");
});

QUnit.test("shadeColour_LightensTheColour_WhenPassedANegativePercentage", function( assert ) {
    var colour = structurizr.util.shadeColor("#6699CC", -50);
    assert.equal(colour, "#334d66");
});

QUnit.test("shadeColour_Lightens_WhenPassedBlackAndAPositivePercentage", function( assert ) {
    var colour = structurizr.util.shadeColor("#000000", 50);
    assert.equal(colour, "#808080");
});