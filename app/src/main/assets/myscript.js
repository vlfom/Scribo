var lastFocused = -1;
var sugDiv = -1;

$('.w').click(function(){
	console.log($(this).html());
	console.log($(this).html().toLowerCase().replace(/[^a-z]/g, ''));
	var suggestions = androidInterface.getSuggestions($(this).html().toLowerCase().replace(/[^a-z]/g, '')).split(" ");

	if (lastFocused != -1) {
		$(lastFocused).css("background-color", "transparent");
		sugDiv.hide();
	}

	lastFocused = this;

	$(this).css("background-color", "#FFF9C4");

	var er = $(this)[0].getBoundingClientRect();

	var xleft = er.left;

	var div_style = 
	  "left: 0px;"  +
	  "top:" + (er.top - 26) + "px;";

	sugDiv = $('<div id="sugDiv" class="ma" style="' + div_style + '"></div>');
	$('<div class="sug sug1">' + suggestions[0] + '</div>').appendTo(sugDiv);
	$('<div class="sug sug2">' + suggestions[1] + '</div>').appendTo(sugDiv);
	$('<div class="sug sug3">' + suggestions[2] + '</div>').appendTo(sugDiv);

	sugDiv.css("opacity", "0");

	sugDiv.appendTo($('body')).show();

	var rem_width = sugDiv.width();

	xleft = xleft - rem_width/2 + $(this).width() / 2;

	if (xleft + rem_width > $('body').width()) {
		delta = $('body').width() - (xleft + rem_width);
		xleft += delta;
		xleft += 12;
	}

	if (xleft < 5) {
		xleft = 5;
	}

	sugDiv.remove();

	var div_style = 
	  "left:" + xleft + "px;"  +
	  "top:" + (er.top - 26) + "px;";

	sugDiv = $('<div id="sugDiv" class="ma" style="' + div_style + '"></div>');

	var sug1div = $('<div class="sug sug1">' + suggestions[0] + '</div>');
	var sug2div = $('<div class="sug sug1">' + suggestions[1] + '</div>');
	var sug3div = $('<div class="sug sug1">' + suggestions[2] + '</div>');

	sug1div.click(function(e) {
		var class_list = $(lastFocused).attr('class').split(' ');
		var ind = class_list[class_list.length - 2];
		ind = parseInt(ind.slice(1));

		var propText = sug1div.html();

		var prevText = $(lastFocused).html();
		if (prevText[0] == prevText[0].toUpperCase()) {
			propText = propText.charAt(0).toUpperCase() + propText.slice(1);
		}
		if (prevText[prevText.length - 1] == '.') {
			propText += ".";
		}
		$(lastFocused).text(propText);

		androidInterface.update(ind, propText);
	});
	sug2div.click(function(e) {
		var class_list = $(lastFocused).attr('class').split(' ');
		var ind = class_list[class_list.length - 2];
		ind = parseInt(ind.slice(1));

		var propText = sug2div.html();

		var prevText = $(lastFocused).html();
		if (prevText[0] == prevText[0].toUpperCase()) {
			propText = propText.charAt(0).toUpperCase() + propText.slice(1);
		}
		if (prevText[prevText.length - 1] == '.') {
			propText += ".";
		}
		$(lastFocused).text(propText);

		androidInterface.update(ind, propText);
	});
	sug3div.click(function(e) {
		var class_list = $(lastFocused).attr('class').split(' ');
		var ind = class_list[class_list.length - 2];
		ind = parseInt(ind.slice(1));

		var propText = sug3div.html();

		var prevText = $(lastFocused).html();
		if (prevText[0] == prevText[0].toUpperCase()) {
			propText = propText.charAt(0).toUpperCase() + propText.slice(1);
		}
		if (prevText[prevText.length - 1] == '.') {
			propText += ".";
		}
		$(lastFocused).text(propText);

		androidInterface.update(ind, propText);
	});

	sug1div.appendTo(sugDiv);
	sug2div.appendTo(sugDiv);
	sug3div.appendTo(sugDiv);
	
	sugDiv.appendTo($('body')).show();
});
$('html').click(function(e){
	if($(e.target).hasClass("w"))
  		return;
	if (lastFocused != -1) {
		$(lastFocused).css("background-color", "transparent");
		sugDiv.hide();

		lastFocused = -1;
	}
});