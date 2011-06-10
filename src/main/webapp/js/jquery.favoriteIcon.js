/* FavoriteIcon v1.1 - Liviu Holhos - http://liviuholhos.com/blog/add-a-favicon-near-external-links-with-jquery */
(function($){
	$.fn.favoriteIcon = function(options) {
		var defaults = {
			iconClass    : 'favoriteIcon',
			insertMethod : 'appendTo',
			iconSearched : 'favicon.ico',
                        missingImgUrl: 'http://code.google.com/edu/images/external.png'
		};
		var options = $.extend(defaults, options);
		$(this).filter(function(){
			return this.hostname && this.hostname !== location.hostname;
		}).each(function() {
			var link = jQuery(this);
			var faviconURL = link.attr('href').replace(/^(http:\/\/[^\/]+).*$/, '$1')+'/'+options.iconSearched;
			var faviconIMG = jQuery('<img class="'+options.iconClass+'" src="'+options.missingImgUrl+'" alt="" />')[options.insertMethod](link);
			var extImg = new Image();
			extImg.src = faviconURL;
			if (extImg.complete) {
                            faviconIMG.attr('src', faviconURL);
                        } else {
                            extImg.onload = function() { faviconIMG.attr('src', faviconURL); };
                        }
                        // check for broken favicon URL and replace with plain icon
                        $("img."+options.iconClass).error(function () {
                                $(this).unbind("error").hide();
                        });
                        $("img."+options.iconClass).css('max-height','16px').css('max-width','16px');
		});
	}
})(jQuery);