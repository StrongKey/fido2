/*
 * Mapplic - Custom Interactive Map Plugin by @sekler
 * Version 4.1
 * http://www.mapplic.com/
 */

;(function($) {
	"use strict";

	var Mapplic = function(element) {

		var self = this;

		self.o = {
			source: 'locations.json',
			selector: '[id^=landmark] > *',
			landmark: false,
			mapfill: false,
			height: 420,
			markers: true,
			minimap: true,
			sidebar: true,
			search: true,
			deeplinking: true,
			clearbutton: true,
			zoombuttons: true,
			zoomoutclose: false,
			action: 'tooltip',
			lightbox: false,
			hovertip: true,
			smartip: true,
			fillcolor: '#4d5e6d',
			fullscreen: true,
			developer: false,
			mousewheel: true,
			alphabetic: false,
			maxscale: 3,
			zoom: true
		};
		self.el = element;

		self.init = function(options) {
			// Merging options with defaults
			self.o = $.extend(self.o, options);

			self.el.addClass('mapplic-element mapplic-loading').height(self.o.height);

			if (typeof self.o.source === 'string') {
				// Loading .json file with AJAX
				$.getJSON(self.o.source, function(data) { // Success
					processData(data);
					self.el.removeClass('mapplic-loading');

				}).fail(function() { // Failure: couldn't load JSON file or it is invalid.
					console.error('Couldn\'t load map data. (Make sure to run the script through web server)');
					self.el.removeClass('mapplic-loading').addClass('mapplic-error');
					alert('Data file missing or invalid!\nMake sure to run the script through web server.');
				});
			}
			else {
				// Inline json object
				processData(self.o.source);
				self.el.removeClass('mapplic-loading');
			}

			return self;
		}

		// Tooltip
		function Tooltip() {
			this.el = null;
			this.pin = null;
			this.shift = 6;
			this.drop = 0;
			this.location = null;
			this.tooltipthumb = true;
			this.tooltiplink = true;

			this.init = function() {
				var s = this;

				// Construct
				this.el = $('<div></div>').addClass('mapplic-tooltip');
				this.close = $('<a></a>').addClass('mapplic-tooltip-close').attr('href', '#').appendTo(this.el);
				this.close.on('click touchend', function(e) {
					e.preventDefault();
					self.hideLocation();
					if (!self.o.zoom || self.o.zoomoutclose) self.moveTo(0.5, 0.5, self.fitscale, 400, 'easeInOutCubic');
				});
				if (this.tooltipthumb) this.thumbnail = $('<img>').addClass('mapplic-tooltip-thumbnail').hide().appendTo(this.el);
				this.content = $('<div></div>').addClass('mapplic-tooltip-content').appendTo(this.el);
				this.title = $('<h4></h4>').addClass('mapplic-tooltip-title').appendTo(this.content);
				this.desc = $('<div></div>').addClass('mapplic-tooltip-description').appendTo(this.content);
				if (this.tooltiplink) this.link = $('<a>More</a>').addClass('mapplic-popup-link').attr('href', '#').hide().appendTo(this.el);
				this.triangle = $('<div></div>').addClass('mapplic-tooltip-triangle').prependTo(this.el);

				// Append
				self.map.append(this.el);

				return this;
			}

			this.show = function(location) {
				if (location) {
					var s = this;

					this.location = location;
					if (self.hovertip) self.hovertip.hide();

					if (this.tooltipthumb) {
						if (location.thumbnail) this.thumbnail.attr('src', location.thumbnail).show();
						else this.thumbnail.hide();
					}
					if (this.tooltiplink) {
						if (location.link) this.link.attr('href', location.link).show();
						else this.link.hide();
					}
					this.title.text(location.title);
					if (location.description) this.desc.html(location.description);
					else this.desc.empty();
					this.content[0].scrollTop = 0;

					// Shift
					this.pin = $('.mapplic-pin[data-location="' + location.id + '"]');
					if (this.pin.length == 0) {
						this.shift = 20;
					}
					else this.shift = Math.abs(parseFloat(this.pin.css('margin-top'))) + 20;

					// Loading & positioning
					$('img', this.el).off('load').on('load', function() {
						s.position();
						s.zoom(location);
					});
					this.position();
				
					// Making it visible
					this.el.stop().show();
					this.zoom(location);
				}
			}

			this.position = function() {
				if (this.location) {
					var cx = self.map.offset().left + self.map.width() * this.location.x - self.container.offset().left,
						cy = self.map.offset().top + self.map.height() * this.location.y - self.container.offset().top;

					var x = this.location.x * 100,
						y = this.location.y * 100,
						mt = -this.el.outerHeight() - this.shift,
						ml = -this.el.outerWidth() / 2;

					if (self.o.smartip) {
						var verticalPos = 0.5;

						// Top check
						if (Math.abs(mt) > cy) {
							mt = 8 + 2;
							if (this.pin && this.pin.length) mt = this.pin.height() + parseFloat(this.pin.css('margin-top')) + 20;
							this.el.addClass('mapplic-bottom');
						}
						else this.el.removeClass('mapplic-bottom');

						// Left-right check
						if (this.el.outerWidth()/2 > cx)
							verticalPos = 0.5 - (this.el.outerWidth()/2 - cx)/this.el.outerWidth();
						else if ((self.container.width() - cx - this.el.outerWidth()/2) < 0)
							verticalPos = 0.5 + (cx + this.el.outerWidth()/2 - self.container.width())/this.el.outerWidth(); 

						verticalPos = Math.max(0, Math.min(1, verticalPos));
						ml = -this.el.outerWidth() * verticalPos;
						this.triangle.css('left', Math.max(5, Math.min(95, verticalPos * 100)) + '%');
					}

					this.el.css({
						left: x + '%',
						top: y + '%',
						marginTop: mt,
						marginLeft: ml
					});
					this.drop = this.el.outerHeight() + this.shift;
				}
			}

			this.zoom = function(location) {
				var ry = 0.5,
					zoom = location.zoom ? parseFloat(location.zoom) : self.o.maxscale;
				
				ry = ((self.container.height() - this.drop) / 2 + this.drop) / self.container.height();
				self.moveTo(location.x, location.y, zoom, 600, 'easeInOutCubic', ry);
			}

			this.hide = function() {
				var s = this;

				this.location = null;
				this.el.stop().fadeOut(300, function() {
					if (s.desc) s.desc.empty();
				});
			}
		}

		// Lightbox
		function Lightbox() {
			this.el = null;

			this.init = function() {
				// Construct
				this.el = $('<div></div>').addClass('mapplic-lightbox mfp-hide');
				this.title = $('<h2></h2>').addClass('mapplic-lightbox-title').appendTo(this.el);
				this.desc = $('<div></div>').addClass('mapplic-lightbox-description').appendTo(this.el);
				this.link = $('<a>More</a>').addClass('mapplic-popup-link').attr('href', '#').hide().appendTo(this.el);

				// Popup Image
				$('body').magnificPopup({
					delegate: '.mapplic-popup-image',
					type: 'image',
					removalDelay: 300,
					mainClass: 'mfp-fade'
				});

				// Append
				self.el.append(this.el);

				return this;
			}

			this.show = function(location) {
				this.location = location;

				this.title.text(location.title);
				this.desc.html(location.description);

				if (location.link) this.link.attr('href', location.link).show();
				else this.link.hide();

				var s = this;

				$.magnificPopup.open({
					items: {
						src: this.el
					},
					type: 'inline',
					removalDelay: 300,
					mainClass: 'mfp-fade',
					callbacks: {
						beforeClose: function() {
							s.hide();
						}
					}
				});

				// Zoom
				var zoom = location.zoom ? parseFloat(location.zoom) : self.o.maxscale;
				self.moveTo(location.x, location.y, zoom, 600, 'easeInOutCubic');

				// Hide tooltip
				if (self.tooltip) self.tooltip.hide();
			}

			this.hide = function() {
				this.location = null;
				self.hideLocation();
				if (!self.o.zoom || self.o.zoomoutclose) self.moveTo(0.5, 0.5, self.fitscale, 400, 'easeInOutCubic');
			}
		}

		// HoverTooltip
		function HoverTooltip() {
			this.el = null;
			this.pin = null;
			this.shift = 6;
			this.hovertipdesc = false;

			this.init = function() {
				var s = this;

				// Construct
				this.el = $('<div></div>').addClass('mapplic-tooltip mapplic-hovertip');
				this.title = $('<h4></h4>').addClass('mapplic-tooltip-title').appendTo(this.el);
				if (this.hovertipdesc) this.desc = $('<div></div>').addClass('mapplic-tooltip-description').appendTo(this.el);
				this.triangle = $('<div></div>').addClass('mapplic-tooltip-triangle').appendTo(this.el);

				// Events 
				// pins + old svg
				$(self.map).on('mouseover', '.mapplic-layer a', function() {
					var id = '';
					if ($(this).hasClass('mapplic-pin')) {
						id = $(this).data('location');
						s.pin = $('.mapplic-pin[data-location="' + id + '"]');
						s.shift = Math.abs(parseFloat(s.pin.css('margin-top'))) + 20;
					}
					else {
						id = $(this).attr('xlink:href').slice(1);
						s.shift = 20;
					}

					var location = self.getLocationData(id);
					if (location) s.show(location);
				}).on('mouseout', function() {
					s.hide();
				});

				// new svg
				if (self.o.selector) {
					$(self.map).on('mouseover', self.o.selector, function() {
						var location = self.getLocationData($(this).attr('id'));
						s.shift = 20;
						if (location) s.show(location);
					}).on('mouseout', function() {
						s.hide();
					});
				}

				self.map.append(this.el);

				return this;
			}

			this.show = function(location) {
				if (self.tooltip.location != location) {
					this.title.text(location.title);
					if (this.hovertipdesc) this.desc.html(location.description);
					this.position(location);

					this.el.stop().fadeIn(100);
				}
			}

			this.position = function(location) {
				var cx = self.map.offset().left + self.map.width() * location.x - self.container.offset().left,
					cy = self.map.offset().top + self.map.height() * location.y - self.container.offset().top;

				var x = location.x * 100,
					y = location.y * 100,
					mt = -this.el.outerHeight() - this.shift,
					ml = 0;

				var verticalPos = 0.5;

				// Top check
				if (Math.abs(mt) > cy) {
					mt = 8 + 2;
					if (this.pin && this.pin.length) mt = this.pin.height() + parseFloat(this.pin.css('margin-top')) + 20;
					this.el.addClass('mapplic-bottom');
				}
				else this.el.removeClass('mapplic-bottom');

				// Left-right check
				if (this.el.outerWidth()/2 > cx)
					verticalPos = 0.5 - (this.el.outerWidth()/2 - cx)/this.el.outerWidth();
				else if ((self.container.width() - cx - this.el.outerWidth()/2) < 0)
					verticalPos = 0.5 + (cx + this.el.outerWidth()/2 - self.container.width())/this.el.outerWidth(); 

				ml = -this.el.outerWidth() * verticalPos;
				this.triangle.css('left', Math.max(10, Math.min(90, verticalPos * 100)) + '%');
				this.el.css({
					left: x + '%',
					top: y + '%',
					marginTop: mt,
					marginLeft: ml
				});
			}

			this.hide = function() {
				this.el.stop().fadeOut(200);
			}
		}

		// Deeplinking
		function Deeplinking() {
			this.param = 'location';

			this.init = function() {
				var s = this;
				this.check(0);

				window.onpopstate = function(e) {
					if (e.state) {
						s.check(600);
					}
					return false;
				}
			}

			this.check = function(easing) {
				var id = this.getUrlParam(this.param);
				self.showLocation(id, easing, true);
			}

			this.getUrlParam = function(name) {
				name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
				var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
					results = regex.exec(location.search);
				return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
			}

			this.update = function(id) {
				var url = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + this.param + '=' + id;
				window.history.pushState({path: url}, '', url);
			}

			// Clear
			this.clear = function() {
				history.pushState('', document.title, window.location.pathname);
			}
		}

		// Old hash deeplinking method for old browsers
		function DeeplinkingHash() {
			this.param = 'location';

			this.init = function() {
				var s = this;
				this.check(0);

				$(window).on('hashchange', function() {
					s.check(600);
				});
			}

			this.check = function(easing) {
				var id = location.hash.slice(this.param.length + 2);
				self.showLocation(id, easing, true);
			}

			this.update = function(id) {
				window.location.hash = this.param + '-' + id;
			}

			this.clear = function() {
				window.location.hash = this.param;
			}
		}

		// Minimap
		function Minimap() {
			this.el = null;
			this.opacity = null;

			this.init = function() {
				this.el = $('<div></div>').addClass('mapplic-minimap').appendTo(self.container);
				this.el.click(function(e) {
					e.preventDefault();

					var x = (e.pageX - $(this).offset().left) / $(this).width(),
						y = (e.pageY - $(this).offset().top) / $(this).height();

					self.moveTo(x, y, self.scale / self.fitscale, 100);
				});
			}

			this.addLayer = function(data) {
				var layer = $('<div></div>').addClass('mapplic-minimap-layer').attr('data-floor', data.id).appendTo(this.el),
					s = this;

				$('<img>').attr('src', data.minimap).addClass('mapplic-minimap-background').appendTo(layer);
				$('<div></div>').addClass('mapplic-minimap-overlay').appendTo(layer);
				$('<img>').attr('src', data.minimap).addClass('mapplic-minimap-active').on('load', function() {
					s.update();
					$(this).addClass('mapplic-clip-transition');
				}).appendTo(layer);
			}

			this.show = function(target) {
				$('.mapplic-minimap-layer', this.el).hide();
				$('.mapplic-minimap-layer[data-floor="' + target + '"]', this.el).show();
			}

			this.update = function(x, y) {
				var active = $('.mapplic-minimap-active', this.el);

				if (x === undefined) x = self.x;
				if (y === undefined) y = self.y;

				var width = Math.round(self.container.width() / self.contentWidth / self.scale * this.el.width()),
					height = Math.round(self.container.height() / self.contentHeight / self.scale * this.el.height()),
					top = Math.round(-y / self.contentHeight / self.scale * this.el.height()),
					left = Math.round(-x / self.contentWidth / self.scale * this.el.width()),
					right = left + width,
					bottom = top + height;

				active.each(function() {
					$(this)[0].style.clip = 'rect(' + top + 'px, ' + right + 'px, ' + bottom + 'px, ' + left + 'px)';
				});

				// Fade out effect
				var s = this;
				this.el.show();
				this.el.css('opacity', 1.0);
				clearTimeout(this.opacity);
				this.opacity = setTimeout(function() {
					s.el.css('opacity', 0);
					setTimeout(function() { s.el.hide(); }, 600);
				}, 2000);
			}
		}

		// Sidebar
		function Sidebar() {
			this.el = null;
			this.list = null;

			this.init = function() {
				var s = this;

				this.el = $('<div></div>').addClass('mapplic-sidebar').appendTo(self.el);

				if (self.o.search) {
					var form = $('<form></form>').addClass('mapplic-search-form').submit(function() {
						return false;
					}).appendTo(this.el);
					self.clear = $('<button></button>').attr('type', 'button').addClass('mapplic-search-clear').click(function() {
						input.val('');
						input.keyup();
					}).appendTo(form);
					var input = $('<input>').attr({'type': 'text', 'spellcheck': 'false', 'placeholder': 'Search...'}).addClass('mapplic-search-input').keyup(function() {
						var keyword = $(this).val();
						s.search(keyword);
					}).prependTo(form);
				}

				var listContainer = $('<div></div>').addClass('mapplic-list-container').appendTo(this.el);
				this.list = $('<ol></ol>').addClass('mapplic-list').appendTo(listContainer);
				this.notfound = $('<p></p>').addClass('mapplic-not-found').text('Nothing found. Please try a different search.').appendTo(listContainer);

				if (!self.o.search) listContainer.css('padding-top', '0');
			}

			this.addCategories = function(categories) {
				var list = this.list;

				if (categories) {
					$.each(categories, function(index, category) {
						var item = $('<li></li>').addClass('mapplic-list-category').attr('data-category', category.id);
						var ol = $('<ol></ol>').css('border-color', category.color).appendTo(item);
						if (category.show == 'false') ol.hide();
						else item.addClass('mapplic-opened');
						var link = $('<a></a>').attr('href', '#').attr('title', category.title).css('background-color', category.color).text(category.title).prependTo(item);
						link.on('click', function(e) {
							e.preventDefault();
							item.toggleClass('mapplic-opened');
							ol.slideToggle(200);
						});
						if (category.icon) $('<img>').attr('src', category.icon).addClass('mapplic-list-thumbnail').prependTo(link);
						$('<span></span>').text('0').addClass('mapplic-list-count').prependTo(link);
						list.append(item);
					});
				}
			}

			this.addLocation = function(data) {
				var item = $('<li></li>').addClass('mapplic-list-location').addClass('mapplic-list-shown').attr('data-location', data.id);
				var link = $('<a></a>').attr('href', '#').click(function(e) {
					e.preventDefault();
					self.showLocation(data.id, 600);

					// Scroll back to map on mobile
					if ($(window).width() < 668) {
						$('html, body').animate({
							scrollTop: self.container.offset().top
						}, 400);
					}
				}).appendTo(item);

				if (data.thumbnail) $('<img>').attr('src', data.thumbnail).addClass('mapplic-list-thumbnail').appendTo(link);
				$('<h4></h4>').text(data.title).appendTo(link)
				$('<span></span>').html(data.about).appendTo(link);
				var category = $('.mapplic-list-category[data-category="' + data.category + '"]');

				if (category.length) $('ol', category).append(item);
				else this.list.append(item);

				// Count
				$('.mapplic-list-count', category).text($('.mapplic-list-shown', category).length);
			}

			this.search = function(keyword) {
				if (keyword) self.clear.fadeIn(100);
				else self.clear.fadeOut(100);

				$('.mapplic-list li', self.el).each(function() {
					if ($(this).text().search(new RegExp(keyword, "i")) < 0) {
						$(this).removeClass('mapplic-list-shown');
						$(this).slideUp(200);
					} else {
						$(this).addClass('mapplic-list-shown');
						$(this).show();
					}
				});

				$('.mapplic-list > li', self.el).each(function() {
					var count = $('.mapplic-list-shown', this).length;
					$('.mapplic-list-count', this).text(count);
				});

				// Show not-found text
				if ($('.mapplic-list > li.mapplic-list-shown').length > 0) this.notfound.fadeOut(200);
				else this.notfound.fadeIn(200);
			}
		}

		// Developer tools
		function DevTools() {
			this.el = null;

			this.init = function() {
				this.el = $('<div></div>').addClass('mapplic-coordinates').appendTo(self.container);
				this.el.append('x: ');
				$('<code></code>').addClass('mapplic-coordinates-x').appendTo(this.el);
				this.el.append(' y: ');
				$('<code></code>').addClass('mapplic-coordinates-y').appendTo(this.el);

				$('.mapplic-layer', self.map).on('mousemove', function(e) {
					var x = (e.pageX - self.map.offset().left) / self.map.width(),
						y = (e.pageY - self.map.offset().top) / self.map.height();
					$('.mapplic-coordinates-x').text(parseFloat(x).toFixed(4));
					$('.mapplic-coordinates-y').text(parseFloat(y).toFixed(4));
				});

				return this;
			}
		}

		// Clear Button
		function ClearButton() {
			this.el = null;
			
			this.init = function() {
				this.el = $('<a></a>').attr('href', '#').addClass('mapplic-clear-button').appendTo(self.container);

				this.el.on('click touchstart', function(e) {
					e.preventDefault();
					self.hideLocation();
					self.moveTo(0.5, 0.5, self.fitscale, 400, 'easeInOutCubic');
				});

				return this;
			}
		}

		// Zoom Buttons
		function ZoomButtons() {
			this.el = null;
		
			this.init = function() {
				this.el = $('<div></div>').addClass('mapplic-zoom-buttons').appendTo(self.container);

				if (!self.o.clearbutton) this.el.css('bottom', '0');

				// Zoom in button
				this.zoomin = $('<a></ha>').attr('href', '#').addClass('mapplic-zoomin-button').appendTo(this.el);
				this.zoomin.on('click touchstart', function(e) {
					e.preventDefault();

					var scale = self.scale;
					self.scale = normalizeScale(scale + scale * 0.8);

					self.x = normalizeX(self.x - (self.container.width() / 2 - self.x) * (self.scale / scale - 1));
					self.y = normalizeY(self.y - (self.container.height() / 2 - self.y) * (self.scale / scale - 1));

					zoomTo(self.x, self.y, self.scale, 400, 'easeInOutCubic');
				});

				// Zoom out button
				this.zoomout = $('<a></ha>').attr('href', '#').addClass('mapplic-zoomout-button').appendTo(this.el);
				this.zoomout.on('click touchstart', function(e) {
					e.preventDefault();

					var scale = self.scale;
					self.scale = normalizeScale(scale - scale * 0.4);

					self.x = normalizeX(self.x - (self.container.width() / 2 - self.x) * (self.scale / scale - 1));
					self.y = normalizeY(self.y - (self.container.height() / 2 - self.y) * (self.scale / scale - 1));

					zoomTo(self.x, self.y, self.scale, 400, 'easeInOutCubic');
				});

				return this;
			}

			this.update = function(scale) {
				this.zoomin.removeClass('mapplic-disabled');
				this.zoomout.removeClass('mapplic-disabled');
				if (scale == self.fitscale) this.zoomout.addClass('mapplic-disabled');
				else if (scale == self.o.maxscale) this.zoomin.addClass('mapplic-disabled');
			}
		}

		// Fullscreen
		function Fullscreen() {
			this.el = null;

			this.init = function() {
				// Fullscreen Button
				$('<a></a>').attr('href', '#').attr('href', '#').addClass('mapplic-fullscreen-button').click(function(e) {
					e.preventDefault();
					self.el.toggleClass('mapplic-fullscreen');
					$(document).resize();
				}).appendTo(self.container);

				// Esc key
				$(document).keyup(function(e) {
					if (e.keyCode === 27) {
						$('.mapplic-element.mapplic-fullscreen').removeClass('mapplic-fullscreen');
						$(document).resize();
					}
				});
			}
		}

		// Functions
		var processData = function(data) {
			self.data = data;
			var shownLevel = null;

			// Disable modules when landmark mode is active
			self.o.landmark = self.el.data('landmark');
			if (self.o.landmark) {
				self.o.sidebar = false;
				self.o.zoombuttons = false;
				self.o.deeplinking = false;
			}

			self.container = $('<div></div>').addClass('mapplic-container').appendTo(self.el);
			self.map = $('<div></div>').addClass('mapplic-map').appendTo(self.container);
			if (self.o.zoom) self.map.addClass('mapplic-zoomable');

			self.levelselect = $('<select></select>').addClass('mapplic-levels-select');

			self.contentWidth = parseFloat(data.mapwidth);
			self.contentHeight = parseFloat(data.mapheight);

			// Create minimap
			if (self.o.minimap) {
				self.minimap = new Minimap();
				self.minimap.init();
			}

			// Create sidebar
			if (self.o.sidebar) {
				self.sidebar = new Sidebar();
				self.sidebar.init();
				self.sidebar.addCategories(data.categories);
			}
			else self.container.css('width', '100%');

			// Iterate through levels
			var nrlevels = 0;

			if (data.levels) {
				$.each(data.levels, function(index, level) {
					var source = level.map,
						extension = source.substr((source.lastIndexOf('.') + 1)).toLowerCase();

					// Create new map layer
					var layer = $('<div></div>').addClass('mapplic-layer').attr('data-floor', level.id).hide().appendTo(self.map);
					switch (extension) {

						// Image formats
						case 'jpg': case 'jpeg': case 'png': case 'gif':
							$('<img>').attr('src', source).addClass('mapplic-map-image').appendTo(layer);
							break;

						// Vector format
						case 'svg':
							$('<div></div>').addClass('mapplic-map-image').load(source, function() {
								// Setting up the locations on the map
								$(self.o.selector, this).each(function() {
									var location = self.getLocationData($(this).attr('id'));
									if (location) {
										$(this).attr('class', 'mapplic-clickable');
										location.el = $(this);

										var fill = null;
										if (location.fill) fill = location.fill;
										else if (self.o.fillcolor) fill = self.o.fillcolor;

										if (fill) {
											$(this).css('fill', fill);
											$('> *', this).css('fill', fill);
										}

										// Landmark mode
										if (self.o.landmark === location.id) $(this).attr('class', 'mapplic-active'); 
									}
								});

								// Click event
								$(self.o.selector, this).on('click touchend', function() {
									if (!self.dragging) self.showLocation($(this).attr('id'), 600);
								});

								// Support for the old map format
								$('svg a', this).each(function() {
									var location = self.getLocationData($(this).attr('xlink:href').substr(1)); 
									if (location) {
										$(this).attr('class', 'mapplic-clickable');
										location.el = $(this);
									}
								});

								$('svg a', this).click(function(e) {
									var id = $(this).attr('xlink:href').substr(1);
									self.showLocation(id, 600);
									e.preventDefault();
								});
							}).appendTo(layer);
							break;

						// Other 
						default:
							alert('File type ' + extension + ' is not supported!');
					}

					// Create new minimap layer
					if (self.minimap) self.minimap.addLayer(level);

					// Build layer control
					self.levelselect.prepend($('<option></option>').attr('value', level.id).text(level.title));

					// Shown level
					if (!shownLevel || level.show)	shownLevel = level.id;
					
					// Iterate through locations
					$.each(level.locations, function(index, location) {
						// Geolocation
						if (location.lat && location.lng) {
							var pos = latlngToPos(location.lat, location.lng);
							location.x = pos.x;
							location.y = pos.y;
						}

						var top = location.y * 100,
							left = location.x * 100;

						if (!location.pin) location.pin = 'default';
						if (location.pin.indexOf('hidden') == -1) {
							if (self.o.markers) {
								var pin = $('<a></a>').attr('href', '#').addClass('mapplic-pin').css({'top': top + '%', 'left': left + '%'}).appendTo(layer);
								pin.on('click touchend', function(e) {
									e.preventDefault();
									self.showLocation(location.id, 600);
								});
								if (location.label) pin.html(location.label);
								if (location.fill) pin.css('background-color', location.fill);
								pin.attr('data-location', location.id);
								pin.addClass(location.pin);
								location.el = pin;
							}
						}

						if (self.sidebar) self.sidebar.addLocation(location);
					});

					nrlevels++;
				});
			}

			// COMPONENTS

			// Tooltip (default)
			self.tooltip = new Tooltip().init();

			// Lightbox
			if (self.o.lightbox) self.lightbox = new Lightbox().init();

			// Hover Tooltip
			if (self.o.hovertip) self.hovertip = new HoverTooltip().init();

			// Clear button
			if (self.o.clearbutton) self.clearbutton = new ClearButton().init();

			// Zoom buttons
			if (self.o.zoombuttons) self.zoombuttons = new ZoomButtons().init();

			// Fullscreen
			if (self.o.fullscreen) self.fullscreen = new Fullscreen().init();

			// Developer tools
			if (self.o.developer) self.devtools = new DevTools().init();
			console.log(self.devtools);

			// Level switcher
			if (nrlevels > 1) {
				self.levels = $('<div></div>').addClass('mapplic-levels');
				var up = $('<a href="#"></a>').addClass('mapplic-levels-up').appendTo(self.levels);
				self.levelselect.appendTo(self.levels);
				var down = $('<a href="#"></a>').addClass('mapplic-levels-down').appendTo(self.levels);
				self.container.append(self.levels);
			
				self.levelselect.change(function() {
					var value = $(this).val();
					self.switchLevel(value);
				});
			
				up.click(function(e) {
					e.preventDefault();
					if (!$(this).hasClass('mapplic-disabled')) self.switchLevel('+');
				});

				down.click(function(e) {
					e.preventDefault();
					if (!$(this).hasClass('mapplic-disabled')) self.switchLevel('-');
				});
			}
			self.switchLevel(shownLevel);

			// Browser resize
			$(window).resize(function() {
				// Mobile
				if ($(window).width() < 668) {
					if (self.el.hasClass('mapplic-fullscreen')) self.container.height($(window).height());
					else {
						var height = Math.min(Math.max(self.container.width() * self.contentHeight / self.contentWidth, $(window).height() * 2/3), $(window).height() - 66); 
						self.container.height(height);
					}
				}
				else self.container.height('100%');

				var wr = self.container.width() / self.contentWidth,
					hr = self.container.height() / self.contentHeight;

				if (self.o.mapfill) {
					if (wr > hr) self.fitscale = wr;
					else self.fitscale = hr;
				}
				else {
					if (wr < hr) self.fitscale = wr;
					else self.fitscale = hr;
				}
				//if (!self.data.zoomlimit) self.o.maxscale = self.fitscale;

				self.scale = normalizeScale(self.scale);
				self.x = normalizeX(self.x);
				self.y = normalizeY(self.y);

				zoomTo(self.x, self.y, self.scale, 0);

			}).resize();

			// Landmark mode
			if (self.o.landmark) {
				self.showLocation(self.o.landmark, 0);
			}
			else {
				var init = self.getLocationData('init'); // Landmark with ID 'init'
				if (init) self.moveTo(init.x, init.y, parseFloat(init.zoom), 0);
				else self.moveTo(0.5, 0.5, self.fitscale, 0);
			}

			// Deeplinking
			if (self.o.deeplinking) {
				if (history.pushState) self.deeplinking = new Deeplinking();
				else self.deeplinking = new DeeplinkingHash();

				self.deeplinking.init();
			}

			// Trigger event
			self.el.trigger('mapready', self);

			// Controls
			if (self.o.zoom) addControls();
		}

		var addControls = function() {
			var map = self.map,
				mapbody = $('.mapplic-map-image', self.map);

			document.ondragstart = function() { return false; } // IE drag fix

			// Drag & drop
			mapbody.on('mousedown', function(e) {
				self.dragging = false;

				map.data('mouseX', e.pageX);
				map.data('mouseY', e.pageY);
				if (map.is(':animated')) {
					map.stop();
					self.x = normalizeX(map.offset().left - self.container.offset().left);
					self.y = normalizeY(map.offset().top - self.container.offset().top);
					self.scale = normalizeScale(map.width() / self.contentWidth);
				}
				map.addClass('mapplic-dragging');

				self.map.on('mousemove', function(e) {
					self.dragging = true;

					var x = e.pageX - map.data('mouseX') + self.x,
						y = e.pageY - map.data('mouseY') + self.y;

					x = normalizeX(x);
					y = normalizeY(y);

					zoomTo(x, y);
					map.data('lastX', x);
					map.data('lastY', y);
				});
			
				$(document).on('mouseup', function() {
					self.x = map.data('lastX');
					self.y = map.data('lastY');

					self.map.off('mousemove');
					$(document).off('mouseup');

					map.removeClass('mapplic-dragging');
				});
			});

			// Double click
			self.el.on('dblclick', '.mapplic-map-image', function(e) {
				e.preventDefault();

				var scale = self.scale;
				self.scale = normalizeScale(scale * 2);

				self.x = normalizeX(self.x - (e.pageX - self.container.offset().left - self.x) * (self.scale/scale - 1));
				self.y = normalizeY(self.y - (e.pageY - self.container.offset().top - self.y) * (self.scale/scale - 1));

				zoomTo(self.x, self.y, self.scale, 400, 'easeInOutCubic');
			});

			// Mousewheel
			if (self.o.mousewheel) {
				$('.mapplic-layer', self.el).bind('mousewheel DOMMouseScroll', function(e, delta) {
					var scale = self.scale;

					self.scale = normalizeScale(scale + scale * delta / 5);

					// Disable page scroll when zoom is applicable
					//if (scale != self.scale)
					e.preventDefault();

					self.x = normalizeX(self.x - (e.pageX - self.container.offset().left - self.x) * (self.scale/scale - 1));
					self.y = normalizeY(self.y - (e.pageY - self.container.offset().top - self.y) * (self.scale/scale - 1));

					zoomTo(self.x, self.y, self.scale, 200, 'easeOutCubic');
				});
			}

			// Touch support
			if (!('ontouchstart' in window || 'onmsgesturechange' in window)) return true;
			mapbody.on('touchstart', function(e) {
				self.dragging = false;

				var orig = e.originalEvent,
					pos = map.position();

				map.data('touchY', orig.changedTouches[0].pageY - pos.top);
				map.data('touchX', orig.changedTouches[0].pageX - pos.left);

				mapbody.on('touchmove', function(e) {
					e.preventDefault();
					self.dragging = true;

					var orig = e.originalEvent;
					var touches = orig.touches.length;

					if (touches == 1) {
						self.x = normalizeX(orig.changedTouches[0].pageX - map.data('touchX'));
						self.y = normalizeY(orig.changedTouches[0].pageY - map.data('touchY'));

						zoomTo(self.x, self.y, self.scale, 50);
					}
					else {
						mapbody.off('touchmove');
					}
				});

				mapbody.on('touchend', function(e) {
					mapbody.off('touchmove touchend');
				});
			});
			
			// Pinch zoom
			var hammer = new Hammer(self.map[0], {
				transform_always_block: true,
				drag_block_horizontal: true,
				drag_block_vertical: true
			});
			hammer.get('pinch').set({ enable: true });

			var scale = 1, last_scale;
			hammer.on('pinchstart', function(e) {
				self.dragging = false;

				scale = self.scale / self.fitscale;
				last_scale = scale;
			});

			hammer.on('pinch', function(e) {
				self.dragging = true;

				if (e.scale != 1) scale = Math.max(1, Math.min(last_scale * e.scale, 100));
				
				var oldscale = self.scale;
				self.scale = normalizeScale(scale * self.fitscale);

				self.x = normalizeX(self.x - (e.center.x - self.container.offset().left - self.x) * (self.scale / oldscale - 1));
				self.y = normalizeY(self.y - (e.center.y - self.y) * (self.scale / oldscale - 1)); // - self.container.offset().top

				zoomTo(self.x, self.y, self.scale, 100);
			});
		}

		/* PRIVATE METHODS */

		// Web Mercator (EPSG:3857) lat/lng projection
		var latlngToPos = function(lat, lng) {
			var deltaLng = self.data.rightLng - self.data.leftLng,
				bottomLatDegree = self.data.bottomLat * Math.PI / 180,
				mapWidth = ((self.data.mapwidth / deltaLng) * 360) / (2 * Math.PI),
				mapOffsetY = (mapWidth / 2 * Math.log((1 + Math.sin(bottomLatDegree)) / (1 - Math.sin(bottomLatDegree))));

			lat = lat * Math.PI / 180;

			return {
				x: ((lng - self.data.leftLng) * (self.data.mapwidth / deltaLng)) / self.data.mapwidth,
				y: (self.data.mapheight - ((mapWidth / 2 * Math.log((1 + Math.sin(lat)) / (1 - Math.sin(lat)))) - mapOffsetY)) / self.data.mapheight
			};
		}

		// jQuery bug add/remove class workaround (will be fixed in jQuery 3)
		var addClass = function(element, c) {
			var classes = element.attr('class');
			if (classes.indexOf(c) == -1) element.attr('class', classes + ' ' + c);
		}

		var removeClass = function(element, c) {
			var classes = element.attr('class');
			if (classes) element.attr('class', classes.replace(c, '').trim());
		}

		var hasClass = function(element, c) {
			var classes = element.attr('class');
			return (classes.indexOf(c) > -1);
		}

		// Normalizing x, y and scale
		var normalizeX = function(x) {
			var minX = self.container.width() - self.contentWidth * self.scale;

			if (minX < 0) {
				if (x > 0) x = 0;
				else if (x < minX) x = minX;
			}
			else x = minX/2;

			return x;
		}

		var normalizeY = function(y) {
			var minY = self.container.height() - self.contentHeight * self.scale;

			if (minY < 0) {
				if (y >= 0) y = 0;
				else if (y < minY) y = minY;
			}
			else y = minY/2;

			return y;
		}

		var normalizeScale = function(scale) {
			if (scale < self.fitscale) scale = self.fitscale;
			else if (scale > self.o.maxscale) scale = self.o.maxscale;

			if (self.zoombuttons) self.zoombuttons.update(scale);

			return scale;
		}

		var zoomTo = function(x, y, scale, d, easing) {
			if (scale !== undefined) {
				self.map.stop().animate({
					'left': x,
					'top': y,
					'width': self.contentWidth * scale,
					'height': self.contentHeight * scale
				}, d, easing, function() {
					if (self.tooltip) self.tooltip.position();
				});
			}
			else {
				self.map.css({
					'left': x,
					'top': y
				});
			}
			if (self.tooltip) self.tooltip.position();
			if (self.minimap) self.minimap.update(x, y);

			// Trigger event
			self.el.trigger('positionchanged', location);
		}

		/* PUBLIC METHODS */
		self.switchLevel = function(target) {
			switch (target) {
				case '+':
					target = $('option:selected', self.levelselect).removeAttr('selected').prev().prop('selected', 'selected').val();
					break;
				case '-':
					target = $('option:selected', self.levelselect).removeAttr('selected').next().prop('selected', 'selected').val();
					break;
				default:
					$('option[value="' + target + '"]', self.levelselect).prop('selected', 'selected');
			}

			// No such layer
			if (!target) return;

			var layer = $('.mapplic-layer[data-floor="' + target + '"]', self.el);

			// Target layer is already active
			if (layer.is(':visible')) return;

			// Hide Tooltip
			if (self.tooltip) self.tooltip.hide();

			// Show target layer
			$('.mapplic-layer:visible', self.map).hide();
			layer.show();

			// Show target minimap layer
			if (self.minimap) self.minimap.show(target);

			// Update control
			var index = self.levelselect.get(0).selectedIndex,
				up = $('.mapplic-levels-up', self.el),
				down = $('.mapplic-levels-down', self.el);

			up.removeClass('mapplic-disabled');
			down.removeClass('mapplic-disabled');
			if (index == 0) up.addClass('mapplic-disabled');
			else if (index == self.levelselect.get(0).length - 1) down.addClass('mapplic-disabled');

			// Trigger event
			self.el.trigger('levelswitched', target);
		}

		self.moveTo = function(x, y, s, duration, easing, ry) {
			duration = typeof duration !== 'undefined' ? duration : 400;
			ry = typeof ry !== 'undefined' ? ry : 0.5;
			s = typeof s !== 'undefined' ? s : self.scale/self.fitscale;

			self.scale = normalizeScale(s);

			self.x = normalizeX(self.container.width() * 0.5 - self.scale * self.contentWidth * x);
			self.y = normalizeY(self.container.height() * ry - self.scale * self.contentHeight * y);

			zoomTo(self.x, self.y, self.scale, duration, easing);
		}

		self.getLocationData = function(id) {
			var data = null;
			$.each(self.data.levels, function(index, level) {
				$.each(level.locations, function(index, location) {
					if (location.id == id) {
						data = location;
					}
				});
			});
			return data;
		}

		self.showLocation = function(id, duration, check) {
			$.each(self.data.levels, function(index, level) {
				if (level.id == id) {
					self.switchLevel(level.id);
					return false;
				}
				$.each(level.locations, function(index, location) {
					if (location.id == id) {
						var action = (location.action && location.action != 'default') ? location.action : self.o.action;
						switch (action) {
							case 'open-link':
								window.location.href = location.link;
								return false;
							case 'open-link-new-tab':
								window.open(location.link);
								return false;
							case 'select':
								if (location.el) {
									if (hasClass(location.el, 'mapplic-active')) removeClass(location.el, 'mapplic-active');
									else addClass(location.el, 'mapplic-active');
								}
								return false;
							case 'none':
								var zoom = location.zoom ? parseFloat(location.zoom) : self.o.maxscale;
								self.switchLevel(level.id);
								self.moveTo(location.x, location.y, zoom, 600, 'easeInOutCubic');
								break;
							case 'lightbox':
								self.switchLevel(level.id);
								self.lightbox.show(location);
								break;
							default:
								self.switchLevel(level.id);
								self.tooltip.show(location);
						}

						// Active state
						removeClass($('.mapplic-active', self.el), 'mapplic-active');
						if (location.el) addClass(location.el, 'mapplic-active');

						// Deeplinking
						if ((self.deeplinking) && (!check)) self.deeplinking.update(id);

						// Trigger event
						self.el.trigger('locationopened', location);
					}
				});
			});
		}

		self.hideLocation = function() {
			removeClass($('.mapplic-active', self.el), 'mapplic-active');
			if (self.deeplinking) self.deeplinking.clear();
			if (self.tooltip) self.tooltip.hide();

			// Trigger event
			self.el.trigger('locationclosed');
		}

		self.updateLocation = function(id) {
			var location = self.getLocationData(id);

			if ((location.id == id) && (location.el.is('a')))  {
				// Geolocation
				if (location.lat && location.lng) {
					var pos = latlngToPos(location.lat, location.lng);
					location.x = pos.x;
					location.y = pos.y;
				}

				var top = location.y * 100,
					left = location.x * 100;

				location.el.css({'top': top + '%', 'left': left + '%'});
			}
		}

	};

	// Easing functions used by default
	// For the full list of easing functions use jQuery Easing Plugin
	$.extend($.easing,
	{
		def: 'easeOutQuad',
		swing: function (x, t, b, c, d) {
			//alert(jQuery.easing.default);
			return jQuery.easing[jQuery.easing.def](x, t, b, c, d);
		},
		easeOutQuad: function (x, t, b, c, d) {
			return -c *(t/=d)*(t-2) + b;
		},
		easeOutCubic: function (x, t, b, c, d) {
			return c*((t=t/d-1)*t*t + 1) + b;
		},
		easeInOutCubic: function (x, t, b, c, d) {
			if ((t/=d/2) < 1) return c/2*t*t*t + b;
			return c/2*((t-=2)*t*t + 2) + b;
		}
	});

	// jQuery Plugin
	$.fn.mapplic = function(options) {

		return this.each(function() {
			var element = $(this);

			// Plugin already initiated on element
			if (element.data('mapplic')) return;

			var instance = (new Mapplic(element)).init(options);

			// Store plugin object in element's data
			element.data('mapplic', instance);
		});
	};

})(jQuery);