$(function() {
	var jsonElement = $(".json");
	var jsonContent = $.parseJSON(jsonElement.html());
	var jsonPretty = JSON.stringify(jsonContent, undefined, 4);
	jsonElement.html(jsonPretty);
});