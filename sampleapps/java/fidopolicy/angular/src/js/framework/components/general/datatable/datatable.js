(function ($) {

	if (typeof mUtil === 'undefined') throw new Error('mUtil is required and must be included before mDatatable.');

	// plugin setup
	$.fn.mDatatable = function (options) {
		if ($(this).length === 0) {
			console.log('No mDatatable element exist.');
			return;
		}

		// global variables
		var datatable = this;

		// debug enabled?
		// 1) state will be cleared on each refresh
		// 2) enable some logs
		// 3) etc.
		datatable.debug = false;

		datatable.API = {
			record: null,
			value: null,
			params: null,
		};

		var Plugin = {
			/********************
			 ** PRIVATE METHODS
			 ********************/
			isInit: false,
			offset: 110,
			stateId: 'meta',
			ajaxParams: {},

			init: function (options) {
				var isHtmlTable = false;
				// data source option empty is normal table
				if (options.data.source === null) {
					Plugin.extractTable();
					isHtmlTable = true;
				}

				Plugin.setupBaseDOM.call();
				Plugin.setupDOM(datatable.table);
				Plugin.spinnerCallback(true);

				// set custom query from options
				Plugin.setDataSourceQuery(Plugin.getOption('data.source.read.params.query'));

				// on event after layout had done setup, show datatable
				$(datatable).on('m-datatable--on-layout-updated', Plugin.afterRender);

				if (datatable.debug) Plugin.stateRemove(Plugin.stateId);

				// initialize extensions
				$.each(Plugin.getOption('extensions'), function (extName, extOptions) {
					if (typeof $.fn.mDatatable[extName] === 'function')
						new $.fn.mDatatable[extName](datatable, extOptions);
				});

				// get data
				if (options.data.type === 'remote' || options.data.type === 'local') {
					if (options.data.saveState === false
						|| options.data.saveState.cookie === false
						&& options.data.saveState.webstorage === false) {
						Plugin.stateRemove(Plugin.stateId);
					}
					// get data for local datatable and local table
					if (options.data.type === 'local' && typeof options.data.source === 'object') {
						datatable.dataSet = datatable.originalDataSet = Plugin.dataMapCallback(options.data.source);
					}
					Plugin.dataRender();
				}

				if (!isHtmlTable) {
					// if not a html table, setup header
					Plugin.setHeadTitle();
					if (Plugin.getOption('layout.footer')) {
						Plugin.setHeadTitle(datatable.tableFoot);
					}
				}

				// hide header
				if (typeof options.layout.header !== 'undefined' &&
					options.layout.header === false) {
					$(datatable.table).find('thead').remove();
				}

				// hide footer
				if (typeof options.layout.footer !== 'undefined' &&
					options.layout.footer === false) {
					$(datatable.table).find('tfoot').remove();
				}

				// for normal and local data type, run layoutUpdate
				if (options.data.type === null ||
					options.data.type === 'local') {
					Plugin.setupCellField.call();
					Plugin.setupTemplateCell.call();

					// setup nested datatable, if option enabled
					Plugin.setupSubDatatable.call();

					// setup extra system column properties
					Plugin.setupSystemColumn.call();
					Plugin.redraw();
				}

				$(window).resize(Plugin.fullRender);

				$(datatable).height('');

				$(Plugin.getOption('search.input')).on('keyup', function (e) {
					if (Plugin.getOption('search.onEnter') && e.which !== 13) return;
					Plugin.search($(this).val());
				});

				return datatable;
			},

			/**
			 * Extract static HTML table content into datasource
			 */
			extractTable: function () {
				var columns = [];
				var headers = $(datatable).find('tr:first-child th').get().map(function (cell, i) {
					var field = $(cell).data('field');
					if (typeof field === 'undefined') {
						field = $(cell).text().trim();
					}
					var column = {field: field, title: field};
					for (var ii in options.columns) {
						if (options.columns[ii].field === field) {
							column = $.extend(true, {}, options.columns[ii], column);
						}
					}
					columns.push(column);
					return field;
				});
				// auto create columns config
				options.columns = columns;

				var rowProp = [];
				var source = [];

				$(datatable).find('tr').each(function () {
					if ($(this).find('td').length) {
						rowProp.push($(this).prop('attributes'));
					}
					var td = {};
					$(this).find('td').each(function (i, cell) {
						td[headers[i]] = cell.innerHTML.trim();
					});
					if (!mUtil.isEmpty(td)) {
						source.push(td);
					}
				});

				options.data.attr.rowProps = rowProp;
				options.data.source = source;
			},

			/**
			 * One time layout update on init
			 */
			layoutUpdate: function () {
				// setup nested datatable, if option enabled
				Plugin.setupSubDatatable.call();

				// setup extra system column properties
				Plugin.setupSystemColumn.call();

				// setup cell hover event
				Plugin.setupHover.call();

				if (typeof options.detail === 'undefined'
					// temporary disable lock column in subtable
					&& Plugin.getDepth() === 1) {
					// lock columns handler
					Plugin.lockTable.call();
				}

				Plugin.columnHide.call();

				Plugin.resetScroll();

				if (!Plugin.isInit) {
					$(datatable).trigger('m-datatable--on-init', {table: $(datatable.wrap).attr('id'), options: options});
					Plugin.isInit = true;
				}

				$(datatable).trigger('m-datatable--on-layout-updated', {table: $(datatable.wrap).attr('id')});
			},

			lockTable: function () {
				// todo; revise lock table responsive
				var lock = {
					lockEnabled: false,
					init: function () {
						// check if table should be locked columns
						lock.lockEnabled = Plugin.lockEnabledColumns();
						if (lock.lockEnabled.left.length === 0 &&
							lock.lockEnabled.right.length === 0) {
							return;
						}
						lock.enable();
					},
					enable: function () {
						var enableLock = function (tablePart) {
							// check if already has lock column
							if ($(tablePart).find('.m-datatable__lock').length > 0) {
								Plugin.log('Locked container already exist in: ', tablePart);
								return;
							}
							// check if no rows exists
							if ($(tablePart).find('.m-datatable__row').length === 0) {
								Plugin.log('No row exist in: ', tablePart);
								return;
							}

							// locked div container
							var lockLeft = $('<div/>').addClass('m-datatable__lock m-datatable__lock--left');
							var lockScroll = $('<div/>').addClass('m-datatable__lock m-datatable__lock--scroll');
							var lockRight = $('<div/>').addClass('m-datatable__lock m-datatable__lock--right');

							$(tablePart).find('.m-datatable__row').each(function () {
								var rowLeft = $('<tr/>').addClass('m-datatable__row').appendTo(lockLeft);
								var rowScroll = $('<tr/>').addClass('m-datatable__row').appendTo(lockScroll);
								var rowRight = $('<tr/>').addClass('m-datatable__row').appendTo(lockRight);
								$(this).find('.m-datatable__cell').each(function () {
									var locked = $(this).data('locked');
									if (typeof locked !== 'undefined') {
										if (typeof locked.left !== 'undefined' || locked === true) {
											// default locked to left
											$(this).appendTo(rowLeft);
										}
										if (typeof locked.right !== 'undefined') {
											$(this).appendTo(rowRight);
										}
									} else {
										$(this).appendTo(rowScroll);
									}
								});
								// remove old row
								$(this).remove();
							});

							if (lock.lockEnabled.left.length > 0) {
								$(datatable.wrap).addClass('m-datatable--lock');
								$(lockLeft).appendTo(tablePart);
							}
							if (lock.lockEnabled.left.length > 0 || lock.lockEnabled.right.length > 0) {
								$(lockScroll).appendTo(tablePart);
							}
							if (lock.lockEnabled.right.length > 0) {
								$(datatable.wrap).addClass('m-datatable--lock');
								$(lockRight).appendTo(tablePart);
							}
						};

						$(datatable.table).find('thead,tbody,tfoot').each(function () {
							var tablePart = this;
							if ($(this).find('.m-datatable__lock').length === 0) {
								$(this).ready(function () {
									enableLock(tablePart);
								});
							}
						});
					},
				};
				lock.init();
				return lock;
			},

			/**
			 * Render everything for resize
			 */
			fullRender: function () {
				if (Plugin.isLocked()) {

					$(datatable.tableHead).empty();
					Plugin.setHeadTitle();
					if (Plugin.getOption('layout.footer')) {
						$(datatable.tableFoot).empty();
						Plugin.setHeadTitle(datatable.tableFoot);
					}

					// todo; full render datatable for specific condition only
					Plugin.spinnerCallback(true);
					$(datatable.wrap).removeClass('m-datatable--loaded');

					Plugin.insertData();
				}
			},

			lockEnabledColumns: function () {
				var screen = $(window).width();
				var columns = options.columns;
				var enabled = {left: [], right: []};
				$.each(columns, function (i, column) {
					if (typeof column.locked !== 'undefined') {
						if (typeof column.locked.left !== 'undefined') {
							if (mUtil.getBreakpoint(column.locked.left) <= screen) {
								enabled['left'].push(column.locked.left);
							}
						}
						if (typeof column.locked.right !== 'undefined') {
							if (mUtil.getBreakpoint(column.locked.right) <= screen) {
								enabled['right'].push(column.locked.right);
							}
						}
					}
				});
				return enabled;
			},

			/**
			 * After render event, called by m-datatable--on-layout-updated
			 * @param e
			 * @param args
			 */
			afterRender: function (e, args) {
				if (args.table == $(datatable.wrap).attr('id')) {
					$(datatable).ready(function () {
						if (!Plugin.isLocked()) {
							Plugin.redraw();
							// work on non locked columns
							if (Plugin.getOption('rows.autoHide')) {
								Plugin.autoHide();
								// reset row
								$(datatable.table).find('.m-datatable__row').css('height', '');
							}
						}

						Plugin.rowEvenOdd.call();

						// redraw locked columns table
						if (Plugin.isLocked()) Plugin.redraw();
						$(datatable.tableBody).css('visibility', '');
						$(datatable.wrap).addClass('m-datatable--loaded');

						Plugin.sorting.call();
						Plugin.scrollbar.call();

						// Plugin.hoverColumn.call();
						Plugin.spinnerCallback(false);
					});
				}
			},

			hoverTimer: 0,
			isScrolling: false,
			setupHover: function () {
				$(window).scroll(function (e) {
					// stop hover when scrolling
					clearTimeout(Plugin.hoverTimer);
					Plugin.isScrolling = true;
				});

				$(datatable.tableBody).find('.m-datatable__cell').off('mouseenter', 'mouseleave').on('mouseenter', function () {
					// reset scroll timer to hover class
					Plugin.hoverTimer = setTimeout(function () {
						Plugin.isScrolling = false;
					}, 200);
					if (Plugin.isScrolling) return;

					// normal table
					var row = $(this).closest('.m-datatable__row').addClass('m-datatable__row--hover');
					var index = $(row).index() + 1;

					// lock table
					$(row).closest('.m-datatable__lock').parent().find('.m-datatable__row:nth-child(' + index + ')').addClass('m-datatable__row--hover');
				}).on('mouseleave', function () {
					// normal table
					var row = $(this).closest('.m-datatable__row').removeClass('m-datatable__row--hover');
					var index = $(row).index() + 1;

					// look table
					$(row).closest('.m-datatable__lock').parent().find('.m-datatable__row:nth-child(' + index + ')').removeClass('m-datatable__row--hover');
				});
			},

			/**
			 * Adjust width of locked table containers by resize handler
			 * @returns {number}
			 */
			adjustLockContainer: function () {
				if (!Plugin.isLocked()) return 0;

				// refer to head dimension
				var containerWidth = $(datatable.tableHead).width();
				var lockLeft = $(datatable.tableHead).find('.m-datatable__lock--left').width();
				var lockRight = $(datatable.tableHead).find('.m-datatable__lock--right').width();

				if (typeof lockLeft === 'undefined') lockLeft = 0;
				if (typeof lockRight === 'undefined') lockRight = 0;

				var lockScroll = Math.floor(containerWidth - lockLeft - lockRight);
				$(datatable.table).find('.m-datatable__lock--scroll').css('width', lockScroll);

				return lockScroll;
			},

			/**
			 * todo; not in use
			 */
			dragResize: function () {
				var pressed = false;
				var start = undefined;
				var startX, startWidth;
				$(datatable.tableHead).find('.m-datatable__cell').mousedown(function (e) {
					start = $(this);
					pressed = true;
					startX = e.pageX;
					startWidth = $(this).width();
					$(start).addClass('m-datatable__cell--resizing');

				}).mousemove(function (e) {
					if (pressed) {
						var i = $(start).index();
						var tableBody = $(datatable.tableBody);
						var ifLocked = $(start).closest('.m-datatable__lock');

						if (ifLocked) {
							var lockedIndex = $(ifLocked).index();
							tableBody = $(datatable.tableBody).find('.m-datatable__lock').eq(lockedIndex);
						}

						$(tableBody).find('.m-datatable__row').each(function (tri, tr) {
							$(tr).find('.m-datatable__cell').eq(i).width(startWidth + (e.pageX - startX)).children().width(startWidth + (e.pageX - startX));
						});

						$(start).children().css('width', startWidth + (e.pageX - startX));
					}

				}).mouseup(function () {
					$(start).removeClass('m-datatable__cell--resizing');
					pressed = false;
				});

				$(document).mouseup(function () {
					$(start).removeClass('m-datatable__cell--resizing');
					pressed = false;
				});
			},

			/**
			 * To prepare placeholder for table before content is loading
			 */
			initHeight: function () {
				if (options.layout.height && options.layout.scroll) {
					var theadHeight = $(datatable.tableHead).find('.m-datatable__row').height();
					var tfootHeight = $(datatable.tableFoot).find('.m-datatable__row').height();
					var bodyHeight = options.layout.height;
					if (theadHeight > 0) {
						bodyHeight -= theadHeight;
					}
					if (tfootHeight > 0) {
						bodyHeight -= tfootHeight;
					}
					$(datatable.tableBody).css('max-height', bodyHeight);

					// set scrollable area fixed height
					$(datatable.tableBody).find('.m-datatable__lock--scroll').css('height', bodyHeight);
				}
			},

			/**
			 * Setup base DOM (table, thead, tbody, tfoot) and create if not exist.
			 */
			setupBaseDOM: function () {
				// keep original state before mDatatable initialize
				datatable.initialDatatable = $(datatable).clone();

				// main element
				if ($(datatable).prop('tagName') === 'TABLE') {
					// if main init element is <table>, wrap with div
					datatable.table = $(datatable).removeClass('m-datatable').addClass('m-datatable__table');
					if ($(datatable.table).parents('.m-datatable').length === 0) {
						datatable.table.wrap($('<div/>').addClass('m-datatable').addClass('m-datatable--' + options.layout.theme));
						datatable.wrap = $(datatable.table).parent();
					}
				} else {
					// create table
					datatable.wrap = $(datatable).addClass('m-datatable').addClass('m-datatable--' + options.layout.theme);
					datatable.table = $('<table/>').addClass('m-datatable__table').appendTo(datatable);
				}

				if (typeof options.layout.class !== 'undefined') {
					$(datatable.wrap).addClass(options.layout.class);
				}

				$(datatable.table).removeClass('m-datatable--destroyed').css('display', 'block');

				// force disable save state
				if (typeof $(datatable).attr('id') === 'undefined') {
					Plugin.setOption('data.saveState', false);
					$(datatable.table).attr('id', mUtil.getUniqueID('m-datatable--'));
				}

				// predefine table height
				if (Plugin.getOption('layout.minHeight'))
					$(datatable.table).css('min-height', Plugin.getOption('layout.minHeight'));

				if (Plugin.getOption('layout.height'))
					$(datatable.table).css('max-height', Plugin.getOption('layout.height'));

				// for normal table load
				if (options.data.type === null) {
					$(datatable.table).css('width', '').css('display', '');
				}

				// create table head element
				datatable.tableHead = $(datatable.table).find('thead');
				if ($(datatable.tableHead).length === 0) {
					datatable.tableHead = $('<thead/>').prependTo(datatable.table);
				}

				// create table head element
				datatable.tableBody = $(datatable.table).find('tbody');
				if ($(datatable.tableBody).length === 0) {
					datatable.tableBody = $('<tbody/>').appendTo(datatable.table);
				}

				if (typeof options.layout.footer !== 'undefined' &&
					options.layout.footer) {
					// create table foot element
					datatable.tableFoot = $(datatable.table).find('tfoot');
					if ($(datatable.tableFoot).length === 0) {
						datatable.tableFoot = $('<tfoot/>').appendTo(datatable.table);
					}
				}
			},

			/**
			 * Set column data before table manipulation.
			 */
			setupCellField: function (tableParts) {
				if (typeof tableParts === 'undefined') tableParts = $(datatable.table).children();
				var columns = options.columns;
				$.each(tableParts, function (part, tablePart) {
					$(tablePart).find('.m-datatable__row').each(function (tri, tr) {
						// prepare data
						$(tr).find('.m-datatable__cell').each(function (tdi, td) {
							if (typeof columns[tdi] !== 'undefined') {
								$(td).data(columns[tdi]);
							}
						});
					});
				});
			},

			/**
			 * Set column template callback
			 * @param tablePart
			 */
			setupTemplateCell: function (tablePart) {
				if (typeof tablePart === 'undefined') tablePart = datatable.tableBody;
				var columns = options.columns;
				$(tablePart).find('.m-datatable__row').each(function (tri, tr) {
					// row data object, if any
					var obj = $(tr).data('obj') || {};

					// @deprecated in v5.0.6
					// obj['getIndex'] = function() {
					// 	return tri;
					// };
					// @deprecated in v5.0.6
					// obj['getDatatable'] = function() {
					// 	return datatable;
					// };

					// @deprecated in v5.0.6
					var rowCallback = Plugin.getOption('rows.callback');
					if (typeof rowCallback === 'function') {
						rowCallback($(tr), obj, tri);
					}
					// before template row callback
					var beforeTemplate = Plugin.getOption('rows.beforeTemplate');
					if (typeof beforeTemplate === 'function') {
						beforeTemplate($(tr), obj, tri);
					}
					// if data object is undefined, collect from table
					if (typeof obj === 'undefined') {
						obj = {};
						$(tr).find('.m-datatable__cell').each(function (tdi, td) {
							// get column settings by field
							var column = $.grep(columns, function (n, i) {
								return $(td).data('field') === n.field;
							})[0];
							if (typeof column !== 'undefined') {
								obj[column['field']] = $(td).text();
							}
						});
					}

					$(tr).find('.m-datatable__cell').each(function (tdi, td) {
						// get column settings by field
						var column = $.grep(columns, function (n, i) {
							return $(td).data('field') === n.field;
						})[0];
						if (typeof column !== 'undefined') {
							// column template
							if (typeof column.template !== 'undefined') {
								var finalValue = '';
								// template string
								if (typeof column.template === 'string') {
									finalValue = Plugin.dataPlaceholder(column.template, obj);
								}
								// template callback function
								if (typeof column.template === 'function') {
									finalValue = column.template(obj, tri, datatable);
								}
								var span = document.createElement('span');
								span.innerHTML = finalValue;
								// insert to cell, wrap with span
								$(td).html(span);

								// set span overflow
								if (typeof column.overflow !== 'undefined') {
									$(span).css('overflow', column.overflow);
									$(span).css('position', 'relative');
								}
							}
						}
					});

					// after template row callback
					var afterTemplate = Plugin.getOption('rows.afterTemplate');
					if (typeof afterTemplate === 'function') {
						afterTemplate($(tr), obj, tri);
					}
				});
			},

			/**
			 * Setup extra system column properties
			 * Note: selector checkbox, subtable toggle
			 */
			setupSystemColumn: function () {
				datatable.dataSet = datatable.dataSet || [];
				// no records available
				if (datatable.dataSet.length === 0) return;

				var columns = options.columns;
				$(datatable.tableBody).find('.m-datatable__row').each(function (tri, tr) {
					$(tr).find('.m-datatable__cell').each(function (tdi, td) {
						// get column settings by field
						var column = $.grep(columns, function (n, i) {
							return $(td).data('field') === n.field;
						})[0];
						if (typeof column !== 'undefined') {
							var value = $(td).text();

							// enable column selector
							if (typeof column.selector !== 'undefined' &&
								column.selector !== false) {
								// check if checkbox exist
								if ($(td).find('.m-checkbox [type="checkbox"]').length > 0) return;
								$(td).addClass('m-datatable__cell--check');
								// append checkbox
								var chk = $('<label/>').addClass('m-checkbox m-checkbox--single').append($('<input/>').attr('type', 'checkbox').attr('value', value).on('click', function () {
									if ($(this).is(':checked')) {
										// add checkbox active row class
										Plugin.setActive(this);
									} else {
										// add checkbox active row class
										Plugin.setInactive(this);
									}
								})).append($('<span/>'));

								// checkbox selector has outline style
								if (typeof column.selector.class !== 'undefined') {
									$(chk).addClass(column.selector.class);
								}

								$(td).children().html(chk);
							}

							// enable column subtable toggle
							if (typeof column.subtable !== 'undefined' && column.subtable) {
								// check if subtable toggle exist
								if ($(td).find('.m-datatable__toggle-subtable').length > 0) return;
								// append subtable toggle
								$(td).children().html($('<a/>').addClass('m-datatable__toggle-subtable').attr('href', '#').attr('data-value', value).append($('<i/>').addClass(Plugin.getOption('layout.icons.rowDetail.collapse'))));
							}
						}
					});
				});

				// init checkbox for header/footer
				var initCheckbox = function (tr) {
					// get column settings by field
					var column = $.grep(columns, function (n, i) {
						return typeof n.selector !== 'undefined' && n.selector !== false;
					})[0];

					if (typeof column !== 'undefined') {
						// enable column selector
						if (typeof column.selector !== 'undefined' && column.selector !== false) {
							var td = $(tr).find('[data-field="' + column.field + '"]');
							// check if checkbox exist
							if ($(td).find('.m-checkbox [type="checkbox"]').length > 0) return;
							$(td).addClass('m-datatable__cell--check');

							// todo; check all, for server pagination
							// append checkbox
							var chk = $('<label/>').addClass('m-checkbox m-checkbox--single m-checkbox--all').append($('<input/>').attr('type', 'checkbox').on('click', function () {
								if ($(this).is(':checked')) {
									Plugin.setActiveAll(true);
								} else {
									Plugin.setActiveAll(false);
								}
							})).append($('<span/>'));

							// checkbox selector has outline style
							if (typeof column.selector.class !== 'undefined') {
								$(chk).addClass(column.selector.class);
							}

							$(td).children().html(chk);
						}
					}
				};

				if (options.layout.header) {
					initCheckbox($(datatable.tableHead).find('.m-datatable__row').first());
				}
				if (options.layout.footer) {
					initCheckbox($(datatable.tableFoot).find('.m-datatable__row').first());
				}
			},

			/**
			 * Adjust width to match container size
			 */
			adjustCellsWidth: function () {
				// get table width
				var containerWidth = $(datatable.tableHead).width();

				// offset reserved for sort icon
				var sortOffset = 20;

				// get total number of columns
				var columns = $(datatable.tableHead).find('.m-datatable__row:first-child').find('.m-datatable__cell:visible').length;
				if (columns > 0) {
					//  remove reserved sort icon width
					containerWidth = containerWidth - (sortOffset * columns);
					var minWidth = Math.floor(containerWidth / columns);

					// minimum width
					if (minWidth <= Plugin.offset) {
						minWidth = Plugin.offset;
					}

					$(datatable.table).find('.m-datatable__row').find('.m-datatable__cell:visible').each(function (tdi, td) {
						var width = minWidth;
						var dataWidth = $(td).data('width');
						if (typeof dataWidth !== 'undefined') {
							width = dataWidth;
						}
						$(td).children().css('width', parseInt(width));
					});
				}

				return datatable;
			},

			/**
			 * Adjust height to match container size
			 */
			adjustCellsHeight: function () {
				$.each($(datatable.table).children(), function (part, tablePart) {
					var totalRows = $(tablePart).find('.m-datatable__row').first().parent().find('.m-datatable__row').length;
					for (var i = 1; i <= totalRows; i++) {
						var rows = $(tablePart).find('.m-datatable__row:nth-child(' + i + ')');
						if ($(rows).length > 0) {
							var maxHeight = Math.max.apply(null, $(rows).map(function () {
								return $(this).height();
							}).get());
							$(rows).css('height', Math.ceil(parseInt(maxHeight)));
						}
					}
				});
			},

			/**
			 * Setup table DOM and classes
			 */
			setupDOM: function (table) {
				// set table classes
				$(table).find('> thead').addClass('m-datatable__head');
				$(table).find('> tbody').addClass('m-datatable__body');
				$(table).find('> tfoot').addClass('m-datatable__foot');
				$(table).find('tr').addClass('m-datatable__row');
				$(table).find('tr > th, tr > td').addClass('m-datatable__cell');
				$(table).find('tr > th, tr > td').each(function (i, td) {
					if ($(td).find('span').length === 0) {
						$(td).wrapInner($('<span/>').css('width', Plugin.offset));
					}
				});
			},

			/**
			 * Default scrollbar
			 * @returns {{tableLocked: null, init: init, onScrolling: onScrolling}}
			 */
			scrollbar: function () {
				var scroll = {
					scrollable: null,
					tableLocked: null,
					mcsOptions: {
						scrollInertia: 0,
						autoDraggerLength: true,
						autoHideScrollbar: true,
						autoExpandScrollbar: false,
						alwaysShowScrollbar: 0,
						mouseWheel: {
							scrollAmount: 120,
							preventDefault: false,
						},
						advanced: {
							updateOnContentResize: true,
							autoExpandHorizontalScroll: true,
						},
						theme: 'minimal-dark',
					},
					init: function () {
						var screen = mUtil.getViewPort().width;
						// setup scrollable datatable
						if (options.layout.scroll) {
							// add scrollable datatable class
							$(datatable.wrap).addClass('m-datatable--scroll');

							var scrollable = $(datatable.tableBody).find('.m-datatable__lock--scroll');

							// check if scrollable area have rows
							if ($(scrollable).find('.m-datatable__row').length > 0 && $(scrollable).length > 0) {
								scroll.scrollHead = $(datatable.tableHead).find('> .m-datatable__lock--scroll > .m-datatable__row');
								scroll.scrollFoot = $(datatable.tableFoot).find('> .m-datatable__lock--scroll > .m-datatable__row');
								scroll.tableLocked = $(datatable.tableBody).find('.m-datatable__lock:not(.m-datatable__lock--scroll)');
								if (Plugin.getOption('layout.customScrollbar') && mUtil.detectIE() != 10 && screen > mUtil.getBreakpoint('lg')) {
									scroll.initCustomScrollbar(scrollable[0]);
								} else {
									scroll.initDefaultScrollbar(scrollable);
								}
							} else if ($(datatable.tableBody).find('.m-datatable__row').length > 0) {
								scroll.scrollHead = $(datatable.tableHead).find('> .m-datatable__row');
								scroll.scrollFoot = $(datatable.tableFoot).find('> .m-datatable__row');
								if (Plugin.getOption('layout.customScrollbar') && mUtil.detectIE() != 10 && screen > mUtil.getBreakpoint('lg')) {
									scroll.initCustomScrollbar(datatable.tableBody);
								} else {
									scroll.initDefaultScrollbar(datatable.tableBody);
								}
							}
						} else {
							$(datatable.table).// css('height', 'auto').
							css('overflow-x', 'auto');
						}
					},
					initDefaultScrollbar: function (scrollable) {
						$(scrollable).css('overflow', 'auto').off().on('scroll', scroll.onScrolling);
					},
					onScrolling: function (e) {
						var left = $(this).scrollLeft();
						var top = $(this).scrollTop();
						$(scroll.scrollHead).css('left', -left);
						$(scroll.scrollFoot).css('left', -left);
						$(scroll.tableLocked).each(function (i, table) {
							$(table).css('top', -top);
						});
					},
					initCustomScrollbar: function (scrollable) {
						scroll.scrollable = scrollable;
						// create a new instance for table body with scrollbar
						Plugin.initScrollbar(scrollable);
						$(scrollable).off().on('scroll', scroll.onScrolling);
					},
				};
				scroll.init();
				return scroll;
			},

			/**
			 * Init custom scrollbar and reset position
			 * @param element
			 * @param options
			 */
			initScrollbar: function (element, options) {
				$(datatable.tableBody).css('overflow', '');
				if (mUtil.hasClass(element, 'ps')) {
					$(element).data('ps').update();
				} else {
					var ps = new PerfectScrollbar(element);
					$(element).data('ps', ps);
				}
			},

			/**
			 * Set column title from options.columns settings
			 */
			setHeadTitle: function (tablePart) {
				if (typeof tablePart === 'undefined') tablePart = datatable.tableHead;
				tablePart = $(tablePart)[0];
				var columns = options.columns;
				var row = tablePart.getElementsByTagName('tr')[0];
				var ths = tablePart.getElementsByTagName('td');

				if (typeof row === 'undefined') {
					row = document.createElement('tr');
					tablePart.appendChild(row);
				}

				$.each(columns, function (i, column) {
					var th = ths[i];
					if (typeof th === 'undefined') {
						th = document.createElement('th');
						row.appendChild(th);
					}

					// set column title
					if (typeof column['title'] !== 'undefined') {
						th.innerHTML = column.title;
						th.setAttribute('data-field', column.field);
						mUtil.addClass(th, column.class);
						$(th).data(column);
					}

					// set header attr option
					if (typeof column.attr !== 'undefined') {
						$.each(column.attr, function (key, val) {
							th.setAttribute(key, val);
						});
					}

					// apply text align to thead/tfoot
					if (typeof column.textAlign !== 'undefined') {
						var align = typeof datatable.textAlign[column.textAlign] !== 'undefined' ? datatable.textAlign[column.textAlign] : '';
						mUtil.addClass(th, align);
					}
				});
				Plugin.setupDOM(tablePart);
			},

			/**
			 * Initiate to get remote or local data via ajax
			 */
			dataRender: function (action) {
				$(datatable.table).siblings('.m-datatable__pager').removeClass('m-datatable--paging-loaded');

				var buildMeta = function () {
					datatable.dataSet = datatable.dataSet || [];
					Plugin.localDataUpdate();
					// local pagination meta
					var meta = Plugin.getDataSourceParam('pagination');
					if (meta.perpage === 0) {
						meta.perpage = options.data.pageSize || 10;
					}
					meta.total = datatable.dataSet.length;
					var start = Math.max(meta.perpage * (meta.page - 1), 0);
					var end = Math.min(start + meta.perpage, meta.total);
					datatable.dataSet = $(datatable.dataSet).slice(start, end);
					return meta;
				};

				var afterGetData = function (result) {
					var localPagingCallback = function (ctx, meta) {
						if (!$(ctx.pager).hasClass('m-datatable--paging-loaded')) {
							$(ctx.pager).remove();
							ctx.init(meta);
						}
						$(ctx.pager).off().on('m-datatable--on-goto-page', function (e) {
							$(ctx.pager).remove();
							ctx.init(meta);
						});

						var start = Math.max(meta.perpage * (meta.page - 1), 0);
						var end = Math.min(start + meta.perpage, meta.total);

						Plugin.localDataUpdate();
						datatable.dataSet = $(datatable.dataSet).slice(start, end);

						// insert data into table content
						Plugin.insertData();
					};

					$(datatable.wrap).removeClass('m-datatable--error');
					// pagination enabled
					if (options.pagination) {
						if (options.data.serverPaging && options.data.type !== 'local') {
							// server pagination
							var serverMeta = Plugin.getObject('meta', result || null);
							if (serverMeta !== null) {
								Plugin.paging(serverMeta);
							} else {
								// no meta object from server response, fallback to local pagination
								Plugin.paging(buildMeta(), localPagingCallback);
							}
						} else {
							// local pagination can be used by remote data also
							Plugin.paging(buildMeta(), localPagingCallback);
						}
					} else {
						// pagination is disabled
						Plugin.localDataUpdate();
					}
					// insert data into table content
					Plugin.insertData();
				};

				// get local datasource
				if (options.data.type === 'local'
					// for remote json datasource
					// || typeof options.data.source.read === 'undefined' && datatable.dataSet !== null
					// for remote datasource, server sorting is disabled and data already received from remote
					|| options.data.serverSorting === false && action === 'sort'
					|| options.data.serverFiltering === false && action === 'search'
				) {
					afterGetData();
					return;
				}

				// getting data from remote only
				Plugin.getData().done(afterGetData);
			},

			/**
			 * Process ajax data
			 */
			insertData: function () {
				datatable.dataSet = datatable.dataSet || [];
				var params = Plugin.getDataSourceParam();

				// get row attributes
				var pagination = params.pagination;
				var start = (Math.max(pagination.page, 1) - 1) * pagination.perpage;
				var end = Math.min(pagination.page, pagination.pages) * pagination.perpage;
				var rowProps = {};
				if (typeof options.data.attr.rowProps !== 'undefined' && options.data.attr.rowProps.length) {
					rowProps = options.data.attr.rowProps.slice(start, end);
				}

				// todo; fix performance
				var tableBody = document.createElement('tbody');
				tableBody.style.visibility = 'hidden';
				var colLength = options.columns.length;

				$.each(datatable.dataSet, function (rowIndex, row) {
					var tr = document.createElement('tr');
					tr.setAttribute('data-row', rowIndex);
					// keep data object to row
					$(tr).data('obj', row);

					if (typeof rowProps[rowIndex] !== 'undefined') {
						$.each(rowProps[rowIndex], function () {
							tr.setAttribute(this.name, this.value);
						});
					}

					var cellIndex = 0;
					var tds = [];
					for (var a = 0; a < colLength; a += 1) {
						var column = options.columns[a];
						var classes = [];
						// add sorted class to cells
						if (Plugin.getObject('sort.field', params) === column.field) {
							classes.push('m-datatable__cell--sorted');
						}

						// apply text align
						if (typeof column.textAlign !== 'undefined') {
							var align = typeof datatable.textAlign[column.textAlign] !==
							'undefined' ? datatable.textAlign[column.textAlign] : '';
							classes.push(align);
						}

						// var classAttr = '';
						if (typeof column.class !== 'undefined') {
							classes.push(column.class);
						}

						var td = document.createElement('td');
						mUtil.addClass(td, classes.join(' '));
						td.setAttribute('data-field', column.field);
						td.innerHTML = Plugin.getObject(column.field, row);
						tr.appendChild(td);
					}

					tableBody.appendChild(tr);
				});

				// display no records message
				if (datatable.dataSet.length === 0) {
					var errorSpan = document.createElement('span');
					mUtil.addClass(errorSpan, 'm-datatable--error');
					errorSpan.innerHTML = Plugin.getOption('translate.records.noRecords');
					tableBody.appendChild(errorSpan);
					$(datatable.wrap).addClass('m-datatable--error m-datatable--loaded');
					Plugin.spinnerCallback(false);
				}

				// replace existing table body
				$(datatable.tableBody).replaceWith(tableBody);
				datatable.tableBody = tableBody;

				// layout update
				Plugin.setupDOM(datatable.table);
				Plugin.setupCellField([datatable.tableBody]);
				Plugin.setupTemplateCell(datatable.tableBody);
				Plugin.layoutUpdate();
			},

			updateTableComponents: function () {
				datatable.tableHead = $(datatable.table).children('thead');
				datatable.tableBody = $(datatable.table).children('tbody');
				datatable.tableFoot = $(datatable.table).children('tfoot');
			},

			/**
			 * Call ajax for raw JSON data
			 */
			getData: function () {
				Plugin.spinnerCallback(true);

				var ajaxParams = {
					dataType: 'json',
					method: 'GET',
					data: {},
					timeout: Plugin.getOption('data.source.read.timeout') || 30000,
				};

				if (options.data.type === 'local') {
					ajaxParams.url = options.data.source;
				}

				if (options.data.type === 'remote') {
					ajaxParams.url = Plugin.getOption('data.source.read.url');
					if (typeof ajaxParams.url !== 'string') ajaxParams.url = Plugin.getOption('data.source.read');
					if (typeof ajaxParams.url !== 'string') ajaxParams.url = Plugin.getOption('data.source');
					ajaxParams.headers = Plugin.getOption('data.source.read.headers');
					ajaxParams.method = Plugin.getOption('data.source.read.method') || 'POST';

					var data = Plugin.getDataSourceParam();
					// remove if server params is not enabled
					if (!Plugin.getOption('data.serverPaging')) {
						delete data['pagination'];
					}
					if (!Plugin.getOption('data.serverSorting')) {
						delete data['sort'];
					}
					ajaxParams.data = $.extend(true, ajaxParams.data, data, Plugin.getOption('data.source.read.params'));
				}

				return $.ajax(ajaxParams).done(function (response, textStatus, jqXHR) {
					datatable.lastResponse = response;
					// extendible data map callback for custom datasource
					datatable.dataSet = datatable.originalDataSet = Plugin.dataMapCallback(response);
					Plugin.setAutoColumns();
					$(datatable).trigger('m-datatable--on-ajax-done', [datatable.dataSet]);
				}).fail(function (jqXHR, textStatus, errorThrown) {
					$(datatable).trigger('m-datatable--on-ajax-fail', [jqXHR]);
					$(datatable.tableBody).html($('<span/>').addClass('m-datatable--error').html(Plugin.getOption('translate.records.noRecords')));
					$(datatable.wrap).addClass('m-datatable--error m-datatable--loaded');
					Plugin.spinnerCallback(false);
				}).always(function () {
				});
			},

			/**
			 * Pagination object
			 * @param meta if null, local pagination, otherwise remote pagination
			 * @param callback for update data when navigating page
			 */
			paging: function (meta, callback) {
				var pg = {
					meta: null,
					pager: null,
					paginateEvent: null,
					pagerLayout: {pagination: null, info: null},
					callback: null,
					init: function (meta) {
						pg.meta = meta;

						// parse pagination meta to integer
						pg.meta.page = parseInt(pg.meta.page);
						pg.meta.pages = parseInt(pg.meta.pages);
						pg.meta.perpage = parseInt(pg.meta.perpage);
						pg.meta.total = parseInt(pg.meta.total);

						// todo; if meta object not exist will cause error
						// always recount total pages
						pg.meta.pages = Math.max(Math.ceil(pg.meta.total / pg.meta.perpage), 1);

						// current page must be not over than total pages
						if (pg.meta.page > pg.meta.pages) pg.meta.page = pg.meta.pages;

						// set unique event name between tables
						pg.paginateEvent = Plugin.getTablePrefix();

						pg.pager = $(datatable.table).siblings('.m-datatable__pager');
						if ($(pg.pager).hasClass('m-datatable--paging-loaded')) return;

						// if class .m-datatable--paging-loaded not exist, recreate pagination
						$(pg.pager).remove();

						// if no pages available
						if (pg.meta.pages === 0) return;

						// update datasource params
						Plugin.setDataSourceParam('pagination', {
							page: pg.meta.page,
							pages: pg.meta.pages,
							perpage: pg.meta.perpage,
							total: pg.meta.total,
						});

						// default callback function, contains remote pagination handler
						pg.callback = pg.serverCallback;
						// custom callback function
						if (typeof callback === 'function') pg.callback = callback;

						pg.addPaginateEvent();
						pg.populate();

						pg.meta.page = Math.max(pg.meta.page || 1, pg.meta.page);

						$(datatable).trigger(pg.paginateEvent, pg.meta);

						pg.pagingBreakpoint.call();
						$(window).resize(pg.pagingBreakpoint);
					},
					serverCallback: function (ctx, meta) {
						Plugin.dataRender();
					},
					populate: function () {
						var icons = Plugin.getOption('layout.icons.pagination');
						var title = Plugin.getOption('translate.toolbar.pagination.items.default');
						// pager root element
						pg.pager = $('<div/>').addClass('m-datatable__pager m-datatable--paging-loaded clearfix');
						// numbering links
						var pagerNumber = $('<ul/>').addClass('m-datatable__pager-nav');
						pg.pagerLayout['pagination'] = pagerNumber;

						// pager first/previous button
						$('<li/>').append($('<a/>').attr('title', title.first).addClass('m-datatable__pager-link m-datatable__pager-link--first').append($('<i/>').addClass(icons.first)).on('click', pg.gotoMorePage).attr('data-page', 1)).appendTo(pagerNumber);
						$('<li/>').append($('<a/>').attr('title', title.prev).addClass('m-datatable__pager-link m-datatable__pager-link--prev').append($('<i/>').addClass(icons.prev)).on('click', pg.gotoMorePage)).appendTo(pagerNumber);

						// more previous pages
						$('<li/>').append($('<a/>').attr('title', title.more).addClass('m-datatable__pager-link m-datatable__pager-link--more-prev').html($('<i/>').addClass(icons.more)).on('click', pg.gotoMorePage)).appendTo(pagerNumber);

						$('<li/>').append($('<input/>').attr('type', 'text').addClass('m-pager-input form-control').attr('title', title.input).on('keyup', function () {
							// on keyup update [data-page]
							$(this).attr('data-page', Math.abs($(this).val()));
						}).on('keypress', function (e) {
							// on keypressed enter button
							if (e.which === 13) pg.gotoMorePage(e);
						})).appendTo(pagerNumber);

						var pagesNumber = Plugin.getOption('toolbar.items.pagination.pages.desktop.pagesNumber');
						var end = Math.ceil(pg.meta.page / pagesNumber) * pagesNumber;
						var start = end - pagesNumber;
						if (end > pg.meta.pages) {
							end = pg.meta.pages;
						}
						for (var x = start; x < end; x++) {
							var pageNumber = x + 1;
							$('<li/>').append($('<a/>').addClass('m-datatable__pager-link m-datatable__pager-link-number').text(pageNumber).attr('data-page', pageNumber).attr('title', pageNumber).on('click', pg.gotoPage)).appendTo(pagerNumber);
						}

						// more next pages
						$('<li/>').append($('<a/>').attr('title', title.more).addClass('m-datatable__pager-link m-datatable__pager-link--more-next').html($('<i/>').addClass(icons.more)).on('click', pg.gotoMorePage)).appendTo(pagerNumber);

						// pager next/last button
						$('<li/>').append($('<a/>').attr('title', title.next).addClass('m-datatable__pager-link m-datatable__pager-link--next').append($('<i/>').addClass(icons.next)).on('click', pg.gotoMorePage)).appendTo(pagerNumber);
						$('<li/>').append($('<a/>').attr('title', title.last).addClass('m-datatable__pager-link m-datatable__pager-link--last').append($('<i/>').addClass(icons.last)).on('click', pg.gotoMorePage).attr('data-page', pg.meta.pages)).appendTo(pagerNumber);

						// page info
						if (Plugin.getOption('toolbar.items.info')) {
							pg.pagerLayout['info'] = $('<div/>').addClass('m-datatable__pager-info').append($('<span/>').addClass('m-datatable__pager-detail'));
						}

						$.each(Plugin.getOption('toolbar.layout'), function (i, layout) {
							$(pg.pagerLayout[layout]).appendTo(pg.pager);
						});

						// page size select
						var pageSizeSelect = $('<select/>').addClass('selectpicker m-datatable__pager-size').attr('title', Plugin.getOption('translate.toolbar.pagination.items.default.select')).attr('data-width', '70px').val(pg.meta.perpage).on('change', pg.updatePerpage).prependTo(pg.pagerLayout['info']);

						var pageSizes = Plugin.getOption('toolbar.items.pagination.pageSizeSelect');
						// default value here, to fix override option by user
						if (pageSizes.length == 0) pageSizes = [10, 20, 30, 50, 100];
						$.each(pageSizes, function (i, size) {
							var display = size;
							if (size === -1) display = 'All';
							$('<option/>').attr('value', size).html(display).appendTo(pageSizeSelect);
						});

						// init selectpicker to dropdown
						$(datatable).ready(function () {
							$('.selectpicker').selectpicker().siblings('.dropdown-toggle').attr('title', Plugin.getOption(
								'translate.toolbar.pagination.items.default.select'));
						});

						pg.paste();
					},
					paste: function () {
						// insert pagination based on placement position, top|bottom
						$.each($.unique(Plugin.getOption('toolbar.placement')),
							function (i, position) {
								if (position === 'bottom') {
									$(pg.pager).clone(true).insertAfter(datatable.table);
								}
								if (position === 'top') {
									// pager top need some extra space
									$(pg.pager).clone(true).addClass('m-datatable__pager--top').insertBefore(datatable.table);
								}
							});
					},
					gotoMorePage: function (e) {
						e.preventDefault();
						// $(this) is a link of .m-datatable__pager-link

						if ($(this).attr('disabled') === 'disabled') return false;

						var page = $(this).attr('data-page');

						// event from text input
						if (typeof page === 'undefined') {
							page = $(e.target).attr('data-page');
						}

						pg.openPage(parseInt(page));
						return false;
					},
					gotoPage: function (e) {
						e.preventDefault();
						// prevent from click same page number
						if ($(this).hasClass('m-datatable__pager-link--active')) return;

						pg.openPage(parseInt($(this).data('page')));
					},
					openPage: function (page) {
						// currentPage is 1-based index
						pg.meta.page = parseInt(page);

						$(datatable).trigger(pg.paginateEvent, pg.meta);
						pg.callback(pg, pg.meta);

						// update page callback function
						$(pg.pager).trigger('m-datatable--on-goto-page', pg.meta);
					},
					updatePerpage: function (e) {
						e.preventDefault();
						// if (Plugin.getOption('layout.height') === null) {
						// fix white space, when perpage is set from many records to less records
						// $('html, body').animate({scrollTop: $(datatable).position().top});
						// }

						pg.pager = $(datatable.table).siblings('.m-datatable__pager').removeClass('m-datatable--paging-loaded');

						// on change select page size
						if (e.originalEvent) {
							pg.meta.perpage = parseInt($(this).val());
						}

						$(pg.pager).find('select.m-datatable__pager-size').val(pg.meta.perpage).attr('data-selected', pg.meta.perpage);

						// update datasource params
						Plugin.setDataSourceParam('pagination', {
							page: pg.meta.page,
							pages: pg.meta.pages,
							perpage: pg.meta.perpage,
							total: pg.meta.total,
						});

						// update page callback function
						$(pg.pager).trigger('m-datatable--on-update-perpage', pg.meta);
						$(datatable).trigger(pg.paginateEvent, pg.meta);
						pg.callback(pg, pg.meta);

						// update pagination info
						pg.updateInfo.call();
					},
					addPaginateEvent: function (e) {
						// pagination event
						$(datatable).off(pg.paginateEvent).on(pg.paginateEvent, function (e, meta) {
							Plugin.spinnerCallback(true);

							pg.pager = $(datatable.table).siblings('.m-datatable__pager');
							var pagerNumber = $(pg.pager).find('.m-datatable__pager-nav');

							// set sync active page class
							$(pagerNumber).find('.m-datatable__pager-link--active').removeClass('m-datatable__pager-link--active');
							$(pagerNumber).find('.m-datatable__pager-link-number[data-page="' + meta.page + '"]').addClass('m-datatable__pager-link--active');

							// set next and previous link page number
							$(pagerNumber).find('.m-datatable__pager-link--prev').attr('data-page', Math.max(meta.page - 1, 1));
							$(pagerNumber).find('.m-datatable__pager-link--next').attr('data-page', Math.min(meta.page + 1, meta.pages));

							// current page input value sync
							$(pg.pager).each(function () {
								$(this).find('.m-pager-input[type="text"]').prop('value', meta.page);
							});

							$(pg.pager).find('.m-datatable__pager-nav').show();
							if (meta.pages <= 1) {
								// hide pager if has 1 page
								$(pg.pager).find('.m-datatable__pager-nav').hide();
							}

							// update datasource params
							Plugin.setDataSourceParam('pagination', {
								page: pg.meta.page,
								pages: pg.meta.pages,
								perpage: pg.meta.perpage,
								total: pg.meta.total,
							});

							$(pg.pager).find('select.m-datatable__pager-size').val(meta.perpage).attr('data-selected', meta.perpage);

							// clear active rows
							$(datatable.table).find('.m-checkbox > [type="checkbox"]').prop('checked', false);
							$(datatable.table).find('.m-datatable__row--active').removeClass('m-datatable__row--active');

							pg.updateInfo.call();
							pg.pagingBreakpoint.call();
							// Plugin.resetScroll();
						});
					},
					updateInfo: function () {
						var start = Math.max(pg.meta.perpage * (pg.meta.page - 1) + 1, 1);
						var end = Math.min(start + pg.meta.perpage - 1, pg.meta.total);
						// page info update
						$(pg.pager).find('.m-datatable__pager-info').find('.m-datatable__pager-detail').html(Plugin.dataPlaceholder(
							Plugin.getOption('translate.toolbar.pagination.items.info'), {
								start: start,
								end: pg.meta.perpage === -1 ? pg.meta.total : end,
								pageSize: pg.meta.perpage === -1 ||
								pg.meta.perpage >= pg.meta.total
									? pg.meta.total
									: pg.meta.perpage,
								total: pg.meta.total,
							}));
					},

					/**
					 * Update pagination layout breakpoint
					 */
					pagingBreakpoint: function () {
						// keep page links reference
						var pagerNumber = $(datatable.table).siblings('.m-datatable__pager').find('.m-datatable__pager-nav');
						if ($(pagerNumber).length === 0) return;

						var currentPage = Plugin.getCurrentPage();
						var pagerInput = $(pagerNumber).find('.m-pager-input').closest('li');

						// reset
						$(pagerNumber).find('li').show();

						// pagination update
						$.each(Plugin.getOption('toolbar.items.pagination.pages'),
							function (mode, option) {
								if (mUtil.isInResponsiveRange(mode)) {
									switch (mode) {
										case 'desktop':
										case 'tablet':
											var end = Math.ceil(currentPage / option.pagesNumber) *
												option.pagesNumber;
											var start = end - option.pagesNumber;
											$(pagerInput).hide();
											pg.meta = Plugin.getDataSourceParam('pagination');
											pg.paginationUpdate();
											break;

										case 'mobile':
											$(pagerInput).show();
											$(pagerNumber).find('.m-datatable__pager-link--more-prev').closest('li').hide();
											$(pagerNumber).find('.m-datatable__pager-link--more-next').closest('li').hide();
											$(pagerNumber).find('.m-datatable__pager-link-number').closest('li').hide();
											break;
									}

									return false;
								}
							});
					},

					/**
					 * Update pagination number and button display
					 */
					paginationUpdate: function () {
						var pager = $(datatable.table).siblings('.m-datatable__pager').find('.m-datatable__pager-nav'),
							pagerMorePrev = $(pager).find('.m-datatable__pager-link--more-prev'),
							pagerMoreNext = $(pager).find('.m-datatable__pager-link--more-next'),
							pagerFirst = $(pager).find('.m-datatable__pager-link--first'),
							pagerPrev = $(pager).find('.m-datatable__pager-link--prev'),
							pagerNext = $(pager).find('.m-datatable__pager-link--next'),
							pagerLast = $(pager).find('.m-datatable__pager-link--last');

						// get visible page
						var pagerNumber = $(pager).find('.m-datatable__pager-link-number');
						// get page before of first visible
						var morePrevPage = Math.max($(pagerNumber).first().data('page') - 1,
							1);
						$(pagerMorePrev).each(function (i, prev) {
							$(prev).attr('data-page', morePrevPage);
						});
						// show/hide <li>
						if (morePrevPage === 1) {
							$(pagerMorePrev).parent().hide();
						} else {
							$(pagerMorePrev).parent().show();
						}

						// get page after of last visible
						var moreNextPage = Math.min($(pagerNumber).last().data('page') + 1,
							pg.meta.pages);
						$(pagerMoreNext).each(function (i, prev) {
							$(pagerMoreNext).attr('data-page', moreNextPage).show();
						});

						// show/hide <li>
						if (moreNextPage === pg.meta.pages
							// missing dot fix when last hidden page is one left
							&& moreNextPage === $(pagerNumber).last().data('page')) {
							$(pagerMoreNext).parent().hide();
						} else {
							$(pagerMoreNext).parent().show();
						}

						// begin/end of pages
						if (pg.meta.page === 1) {
							$(pagerFirst).attr('disabled', true).addClass('m-datatable__pager-link--disabled');
							$(pagerPrev).attr('disabled', true).addClass('m-datatable__pager-link--disabled');
						} else {
							$(pagerFirst).removeAttr('disabled').removeClass('m-datatable__pager-link--disabled');
							$(pagerPrev).removeAttr('disabled').removeClass('m-datatable__pager-link--disabled');
						}
						if (pg.meta.page === pg.meta.pages) {
							$(pagerNext).attr('disabled', true).addClass('m-datatable__pager-link--disabled');
							$(pagerLast).attr('disabled', true).addClass('m-datatable__pager-link--disabled');
						} else {
							$(pagerNext).removeAttr('disabled').removeClass('m-datatable__pager-link--disabled');
							$(pagerLast).removeAttr('disabled').removeClass('m-datatable__pager-link--disabled');
						}

						// display more buttons
						var nav = Plugin.getOption('toolbar.items.pagination.navigation');
						if (!nav.first) $(pagerFirst).remove();
						if (!nav.prev) $(pagerPrev).remove();
						if (!nav.next) $(pagerNext).remove();
						if (!nav.last) $(pagerLast).remove();
					},
				};
				pg.init(meta);
				return pg;
			},

			/**
			 * Hide/show table cell defined by
			 * options[columns][i][responsive][visible/hidden]
			 */
			columnHide: function () {
				var screen = mUtil.getViewPort().width;
				// foreach columns setting
				$.each(options.columns, function (i, column) {
					if (typeof column.responsive !== 'undefined') {
						var field = column.field;
						var tds = $.grep($(datatable.table).find('.m-datatable__cell'), function (n, i) {
							return field === $(n).data('field');
						});
						if (mUtil.getBreakpoint(column.responsive.hidden) >= screen) {
							$(tds).hide();
						} else {
							$(tds).show();
						}
						if (mUtil.getBreakpoint(column.responsive.visible) <= screen) {
							$(tds).show();
						} else {
							$(tds).hide();
						}
					}
				});
			},

			/**
			 * Setup sub datatable
			 */
			setupSubDatatable: function () {
				var subTableCallback = Plugin.getOption('detail.content');
				if (typeof subTableCallback !== 'function') return;

				// subtable already exist
				if ($(datatable.table).find('.m-datatable__subtable').length > 0) return;

				$(datatable.wrap).addClass('m-datatable--subtable');

				options.columns[0]['subtable'] = true;

				// toggle on open sub table
				var toggleSubTable = function (e) {
					e.preventDefault();
					// get parent row of this subtable
					var parentRow = $(this).closest('.m-datatable__row');

					// get subtable row for sub table
					var subTableRow = $(parentRow).next('.m-datatable__row-subtable');
					if ($(subTableRow).length === 0) {
						// prepare DOM for sub table, each <tr> as parent and add <tr> as child table
						subTableRow = $('<tr/>').addClass('m-datatable__row-subtable m-datatable__row-loading').hide().append($('<td/>').addClass('m-datatable__subtable').attr('colspan', Plugin.getTotalColumns()));
						$(parentRow).after(subTableRow);
						// add class to even row
						if ($(parentRow).hasClass('m-datatable__row--even')) {
							$(subTableRow).addClass('m-datatable__row-subtable--even');
						}
					}

					$(subTableRow).toggle();

					var subTable = $(subTableRow).find('.m-datatable__subtable');

					// get id from first column of parent row
					var primaryKey = $(this).closest('[data-field]:first-child').find('.m-datatable__toggle-subtable').data('value');

					var icon = $(this).find('i').removeAttr('class');

					// prevent duplicate datatable init
					if ($(parentRow).hasClass('m-datatable__row--subtable-expanded')) {
						$(icon).addClass(Plugin.getOption('layout.icons.rowDetail.collapse'));
						// remove expand class from parent row
						$(parentRow).removeClass('m-datatable__row--subtable-expanded');
						// trigger event on collapse
						$(datatable).trigger('m-datatable--on-collapse-subtable', [parentRow]);
					} else {
						// expand and run callback function
						$(icon).addClass(Plugin.getOption('layout.icons.rowDetail.expand'));
						// add expand class to parent row
						$(parentRow).addClass('m-datatable__row--subtable-expanded');
						// trigger event on expand
						$(datatable).trigger('m-datatable--on-expand-subtable', [parentRow]);
					}

					// prevent duplicate datatable init
					if ($(subTable).find('.m-datatable').length === 0) {
						// get data by primary id
						$.map(datatable.dataSet, function (n, i) {
							// primary id must be at the first column, otherwise e.data will be undefined
							if (primaryKey === n[options.columns[0].field]) {
								e.data = n;
								return true;
							}
							return false;
						});

						// deprecated in v5.0.6
						e.detailCell = subTable;

						e.parentRow = parentRow;
						e.subTable = subTable;

						// run callback with event
						subTableCallback(e);

						$(subTable).children('.m-datatable').on('m-datatable--on-init', function (e) {
							$(subTableRow).removeClass('m-datatable__row-loading');
						});
						if (Plugin.getOption('data.type') === 'local') {
							$(subTableRow).removeClass('m-datatable__row-loading');
						}
					}
				};

				var columns = options.columns;
				$(datatable.tableBody).find('.m-datatable__row').each(function (tri, tr) {
					$(tr).find('.m-datatable__cell').each(function (tdi, td) {
						// get column settings by field
						var column = $.grep(columns, function (n, i) {
							return $(td).data('field') === n.field;
						})[0];
						if (typeof column !== 'undefined') {
							var value = $(td).text();
							// enable column subtable toggle
							if (typeof column.subtable !== 'undefined' && column.subtable) {
								// check if subtable toggle exist
								if ($(td).find('.m-datatable__toggle-subtable').length > 0) return;
								// append subtable toggle
								$(td).html($('<a/>').addClass('m-datatable__toggle-subtable').attr('href', '#').attr('data-value', value).attr('title', Plugin.getOption('detail.title')).on('click', toggleSubTable).append($('<i/>').css('width', $(td).data('width')).addClass(Plugin.getOption('layout.icons.rowDetail.collapse'))));
							}
						}
					});
				});

				// $(datatable.tableHead).find('.m-datatable__row').first()
			},

			/**
			 * Datasource mapping callback
			 */
			dataMapCallback: function (raw) {
				// static dataset array
				var dataSet = raw;
				// dataset mapping callback
				if (typeof Plugin.getOption('data.source.read.map') === 'function') {
					return Plugin.getOption('data.source.read.map')(raw);
				} else {
					// default data mapping fallback
					if (typeof raw !== 'undefined' && typeof raw.data !== 'undefined') {
						dataSet = raw.data;
					}
				}
				return dataSet;
			},

			isSpinning: false,
			/**
			 * BlockUI spinner callback
			 * @param block
			 */
			spinnerCallback: function (block) {
				if (block) {
					if (!Plugin.isSpinning) {
						// get spinner options
						var spinnerOptions = Plugin.getOption('layout.spinner');
						if (spinnerOptions.message === true) {
							// use default spinner message from translation
							spinnerOptions.message = Plugin.getOption('translate.records.processing');
						}
						Plugin.isSpinning = true;
						if (typeof mApp !== 'undefined') {
							mApp.block(datatable, spinnerOptions);
						}
					}
				} else {
					Plugin.isSpinning = false;
					if (typeof mApp !== 'undefined') {
						mApp.unblock(datatable);
					}
				}
			},

			/**
			 * Default sort callback function
			 * @param data
			 * @param sort
			 * @param column
			 * @returns {*|Array.<T>|{sort, field}|{asc, desc}}
			 */
			sortCallback: function (data, sort, column) {
				var type = column['type'] || 'string';
				var format = column['format'] || '';
				var field = column['field'];

				return $(data).sort(function (a, b) {
					var aField = a[field];
					var bField = b[field];

					switch (type) {
						case 'date':
							if (typeof moment === 'undefined') {
								throw new Error('Moment.js is required.');
							}
							var diff = moment(aField, format).diff(moment(bField, format));
							if (sort === 'asc') {
								return diff > 0 ? 1 : diff < 0 ? -1 : 0;
							} else {
								return diff < 0 ? 1 : diff > 0 ? -1 : 0;
							}
							break;

						case 'number':
							if (isNaN(parseFloat(aField)) && aField != null) {
								aField = Number(aField.replace(/[^0-9\.-]+/g, ''));
							}
							if (isNaN(parseFloat(bField)) && bField != null) {
								bField = Number(bField.replace(/[^0-9\.-]+/g, ''));
							}
							aField = parseFloat(aField);
							bField = parseFloat(bField);
							if (sort === 'asc') {
								return aField > bField ? 1 : aField < bField ? -1 : 0;
							} else {
								return aField < bField ? 1 : aField > bField ? -1 : 0;
							}
							break;

						case 'string':
						default:
							if (sort === 'asc') {
								return aField > bField ? 1 : aField < bField ? -1 : 0;
							} else {
								return aField < bField ? 1 : aField > bField ? -1 : 0;
							}
							break;
					}
				});
			},

			/**
			 * Custom debug log
			 * @param text
			 * @param obj
			 */
			log: function (text, obj) {
				if (typeof obj === 'undefined') obj = '';
				if (datatable.debug) {
					console.log(text, obj);
				}
			},

			/**
			 * Auto hide columnds overflow in row
			 */
			autoHide: function () {
				$(datatable.table).find('.m-datatable__cell').show();
				$(datatable.tableBody).each(function () {
					while ($(this)[0].offsetWidth < $(this)[0].scrollWidth) {
						$(datatable.table).find('.m-datatable__row').each(function (i) {
							var cell = $(this).find('.m-datatable__cell').not(':hidden').last();
							$(cell).hide();
						});
						Plugin.adjustCellsWidth.call();
					}
				});

				var toggleHiddenColumns = function (e) {
					e.preventDefault();

					var row = $(this).closest('.m-datatable__row');
					var detailRow = $(row).next();

					if (!$(detailRow).hasClass('m-datatable__row-detail')) {
						$(this).find('i').removeClass(Plugin.getOption('layout.icons.rowDetail.collapse')).addClass(Plugin.getOption('layout.icons.rowDetail.expand'));

						var hidden = $(row).find('.m-datatable__cell:hidden').clone().show();

						detailRow = $('<tr/>').addClass('m-datatable__row-detail').insertAfter(row);
						var detailRowTd = $('<td/>').addClass('m-datatable__detail').attr('colspan', Plugin.getTotalColumns()).appendTo(detailRow);

						var detailSubTable = $('<table/>');
						$(hidden).each(function () {
							var field = $(this).data('field');
							var column = $.grep(options.columns, function (n, i) {
								return field === n.field;
							})[0];
							$(detailSubTable).append($('<tr class="m-datatable__row"></tr>').append($('<td class="m-datatable__cell"></td>').append($('<span/>').css('width', Plugin.offset).append(column.title))).append(this));
						});
						$(detailRowTd).append(detailSubTable);

					} else {
						$(this).find('i').removeClass(Plugin.getOption('layout.icons.rowDetail.expand')).addClass(Plugin.getOption('layout.icons.rowDetail.collapse'));
						$(detailRow).remove();
					}
				};

				// toggle show hidden columns
				$(datatable.tableBody).find('.m-datatable__row').each(function () {
					$(this).prepend($('<td/>').addClass('m-datatable__cell m-datatable__toggle--detail').append($('<a/>').addClass('m-datatable__toggle-detail').attr('href', '').on('click', toggleHiddenColumns).append($('<i/>').css('width', '21px').// maintain width for both icons expand and collapse
					addClass(Plugin.getOption('layout.icons.rowDetail.collapse')))));

					// check if subtable toggle exist
					if ($(datatable.tableHead).find('.m-datatable__toggle-detail').length === 0) {
						$(datatable.tableHead).find('.m-datatable__row').first().prepend('<th class="m-datatable__cell m-datatable__toggle-detail"><span style="width: 21px"></span></th>');
						$(datatable.tableFoot).find('.m-datatable__row').first().prepend('<th class="m-datatable__cell m-datatable__toggle-detail"><span style="width: 21px"></span></th>');
					} else {
						$(datatable.tableHead).find('.m-datatable__toggle-detail').find('span').css('width', '21px');
					}
				});
			},

			/**
			 * todo; implement hover column
			 */
			hoverColumn: function () {
				$(datatable.tableBody).on('mouseenter', '.m-datatable__cell', function () {
					var colIdx = $(Plugin.cell(this).nodes()).index();
					$(Plugin.cells().nodes()).removeClass('m-datatable__cell--hover');
					$(Plugin.column(colIdx).nodes()).addClass('m-datatable__cell--hover');
				});
			},

			/**
			 * To enable auto columns features for remote data source
			 */
			setAutoColumns: function () {
				if (Plugin.getOption('data.autoColumns')) {
					$.each(datatable.dataSet[0], function (k, v) {
						var found = $.grep(options.columns, function (n, i) {
							return k === n.field;
						});
						if (found.length === 0) {
							options.columns.push({field: k, title: k});
						}
					});
					$(datatable.tableHead).find('.m-datatable__row').remove();
					Plugin.setHeadTitle();
					if (Plugin.getOption('layout.footer')) {
						$(datatable.tableFoot).find('.m-datatable__row').remove();
						Plugin.setHeadTitle(datatable.tableFoot);
					}
				}
			},

			/********************
			 ** HELPERS
			 ********************/

			/**
			 * Check if table is a locked colums table
			 */
			isLocked: function () {
				return mUtil.hasClass(datatable.wrap[0], 'm-datatable--lock') || false;
			},

			/**
			 * Get total extra space of an element for width calculation, including
			 * padding, margin, border
			 * @param element
			 * @returns {number}
			 */
			getExtraSpace: function (element) {
				var padding = parseInt($(element).css('paddingRight')) +
					parseInt($(element).css('paddingLeft'));
				var margin = parseInt($(element).css('marginRight')) +
					parseInt($(element).css('marginLeft'));
				var border = Math.ceil(
					$(element).css('border-right-width').replace('px', ''));
				return padding + margin + border;
			},

			/**
			 * Insert data of array into {{ }} template placeholder
			 * @param template
			 * @param data
			 * @returns {*}
			 */
			dataPlaceholder: function (template, data) {
				var result = template;
				$.each(data, function (key, val) {
					result = result.replace('{{' + key + '}}', val);
				});
				return result;
			},

			/**
			 * Get table unique ID
			 * Note: table unique change each time refreshed
			 * @param suffix
			 * @returns {*}
			 */
			getTableId: function (suffix) {
				if (typeof suffix === 'undefined') suffix = '';
				var id = $(datatable).attr('id');
				if (typeof id === 'undefined') {
					id = $(datatable).attr('class').split(' ')[0];
				}
				return id + suffix;
			},

			/**
			 * Get table prefix with depth number
			 */
			getTablePrefix: function (suffix) {
				if (typeof suffix !== 'undefined') suffix = '-' + suffix;
				return Plugin.getTableId() + '-' + Plugin.getDepth() + suffix;
			},

			/**
			 * Get current table depth of sub table
			 * @returns {number}
			 */
			getDepth: function () {
				var depth = 0;
				var table = datatable.table;
				do {
					table = $(table).parents('.m-datatable__table');
					depth++;
				} while ($(table).length > 0);
				return depth;
			},

			/**
			 * Keep state item
			 * @param key
			 * @param value
			 */
			stateKeep: function (key, value) {
				key = Plugin.getTablePrefix(key);
				if (Plugin.getOption('data.saveState') === false) return;
				if (Plugin.getOption('data.saveState.webstorage') && localStorage) {
					localStorage.setItem(key, JSON.stringify(value));
				}
				if (Plugin.getOption('data.saveState.cookie')) {
					Cookies.set(key, JSON.stringify(value));
				}
			},

			/**
			 * Get state item
			 * @param key
			 * @param defValue
			 */
			stateGet: function (key, defValue) {
				key = Plugin.getTablePrefix(key);
				if (Plugin.getOption('data.saveState') === false) return;
				var value = null;
				if (Plugin.getOption('data.saveState.webstorage') && localStorage) {
					value = localStorage.getItem(key);
				} else {
					value = Cookies.get(key);
				}
				if (typeof value !== 'undefined' && value !== null) {
					return JSON.parse(value);
				}
			},

			/**
			 * Update data in state without clear existing
			 * @param key
			 * @param value
			 */
			stateUpdate: function (key, value) {
				var ori = Plugin.stateGet(key);
				if (typeof ori === 'undefined' || ori === null) ori = {};
				Plugin.stateKeep(key, $.extend({}, ori, value));
			},

			/**
			 * Remove state item
			 * @param key
			 */
			stateRemove: function (key) {
				key = Plugin.getTablePrefix(key);
				if (localStorage) {
					localStorage.removeItem(key);
				}
				Cookies.remove(key);
			},

			/**
			 * Get total columns.
			 */
			getTotalColumns: function (tablePart) {
				if (typeof tablePart === 'undefined') tablePart = datatable.tableBody;
				return $(tablePart).find('.m-datatable__row').first().find('.m-datatable__cell').length;
			},

			/**
			 * Get table row. Useful to get row when current table is in lock mode.
			 * Can be used for both lock and normal table mode.
			 * By default, returning result will be in a list of <td>.
			 * @param tablePart
			 * @param row 1-based index
			 * @param tdOnly Optional. Default true
			 * @returns {*}
			 */
			getOneRow: function (tablePart, row, tdOnly) {
				if (typeof tdOnly === 'undefined') tdOnly = true;
				// get list of <tr>
				var result = $(tablePart).find('.m-datatable__row:not(.m-datatable__row-detail):nth-child(' + row + ')');
				if (tdOnly) {
					// get list of <td> or <th>
					result = result.find('.m-datatable__cell');
				}
				return result;
			},

			/**
			 * Check if element has vertical overflow
			 * @param element
			 * @returns {boolean}
			 */
			hasOverflowY: function (element) {
				var children = $(element).find('.m-datatable__row');
				var maxHeight = 0;

				if (children.length > 0) {
					$(children).each(function (tdi, td) {
						maxHeight += Math.floor($(td).innerHeight());
					});

					return maxHeight > $(element).innerHeight();
				}

				return false;
			},

			/**
			 * Sort table row at HTML level by column index.
			 * todo; Not in use.
			 * @param header Header sort clicked
			 * @param sort asc|desc. Optional. Default asc
			 * @param int Boolean. Optional. Comparison value parse to integer.
			 *     Default false
			 */
			sortColumn: function (header, sort, int) {
				if (typeof sort === 'undefined') sort = 'asc'; // desc
				if (typeof int === 'undefined') int = false;

				var column = $(header).index();
				var rows = $(datatable.tableBody).find('.m-datatable__row');
				var hIndex = $(header).closest('.m-datatable__lock').index();
				if (hIndex !== -1) {
					rows = $(datatable.tableBody).find('.m-datatable__lock:nth-child(' + (hIndex + 1) + ')').find('.m-datatable__row');
				}

				var container = $(rows).parent();
				$(rows).sort(function (a, b) {
					var tda = $(a).find('td:nth-child(' + column + ')').text();
					var tdb = $(b).find('td:nth-child(' + column + ')').text();

					if (int) {
						// useful for integer type sorting
						tda = parseInt(tda);
						tdb = parseInt(tdb);
					}

					if (sort === 'asc') {
						return tda > tdb ? 1 : tda < tdb ? -1 : 0;
					} else {
						return tda < tdb ? 1 : tda > tdb ? -1 : 0;
					}
				}).appendTo(container);
			},

			/**
			 * Perform sort remote and local
			 */
			sorting: function () {
				var sortObj = {
					init: function () {
						if (options.sortable) {
							$(datatable.tableHead).find('.m-datatable__cell:not(.m-datatable__cell--check)').addClass('m-datatable__cell--sort').off('click').on('click', sortObj.sortClick);
							// first init
							sortObj.setIcon();
						}
					},
					setIcon: function () {
						var meta = Plugin.getDataSourceParam('sort');
						if ($.isEmptyObject(meta)) return;

						// sort icon beside column header
						var td = $(datatable.tableHead).find('.m-datatable__cell[data-field="' + meta.field + '"]').attr('data-sort', meta.sort);
						var sorting = $(td).find('span');
						var icon = $(sorting).find('i');

						var icons = Plugin.getOption('layout.icons.sort');
						// update sort icon; desc & asc
						if ($(icon).length > 0) {
							$(icon).removeAttr('class').addClass(icons[meta.sort]);
						} else {
							$(sorting).append($('<i/>').addClass(icons[meta.sort]));
						}
					},
					sortClick: function (e) {
						var meta = Plugin.getDataSourceParam('sort');
						var field = $(this).data('field');
						var column = Plugin.getColumnByField(field);
						// sort is disabled for this column
						if (typeof column.sortable !== 'undefined' &&
							column.sortable === false) return;

						$(datatable.tableHead).find('.m-datatable__cell > span > i').remove();

						if (options.sortable) {
							Plugin.spinnerCallback(true);

							var sort = 'desc';
							if (Plugin.getObject('field', meta) === field) {
								sort = Plugin.getObject('sort', meta);
							}

							// toggle sort
							sort = typeof sort === 'undefined' || sort === 'desc'
								? 'asc'
								: 'desc';

							// update field and sort params
							meta = {field: field, sort: sort};
							Plugin.setDataSourceParam('sort', meta);

							sortObj.setIcon();

							setTimeout(function () {
								Plugin.dataRender('sort');
								$(datatable).trigger('m-datatable--on-sort', meta);
							}, 300);
						}
					},
				};
				sortObj.init();
			},

			/**
			 * Update JSON data list linked with sort, filter and pagination.
			 * Call this method, before using dataSet variable.
			 * @returns {*|null}
			 */
			localDataUpdate: function () {
				// todo; fix twice execution
				var params = Plugin.getDataSourceParam();
				if (typeof datatable.originalDataSet === 'undefined') {
					datatable.originalDataSet = datatable.dataSet;
				}

				var field = Plugin.getObject('sort.field', params);
				var sort = Plugin.getObject('sort.sort', params);
				var column = Plugin.getColumnByField(field);
				if (typeof column !== 'undefined' && Plugin.getOption('data.serverSorting') !== true) {
					if (typeof column.sortCallback === 'function') {
						datatable.dataSet = column.sortCallback(datatable.originalDataSet, sort, column);
					} else {
						datatable.dataSet = Plugin.sortCallback(datatable.originalDataSet, sort, column);
					}
				} else {
					datatable.dataSet = datatable.originalDataSet;
				}

				// if server filter enable, don't pass local filter
				if (typeof params.query === 'object' && !Plugin.getOption('data.serverFiltering')) {
					params.query = params.query || {};

					var nestedSearch = function (obj) {
						for (var field in obj) {
							if (!obj.hasOwnProperty(field)) continue;
							if (typeof obj[field] === 'string') {
								if (obj[field].toLowerCase() == search || obj[field].toLowerCase().indexOf(search) !== -1) {
									return true;
								}
							}
							else if (typeof obj[field] === 'number') {
								if (obj[field] === search) {
									return true;
								}
							}
							else if (typeof obj[field] === 'object') {
								return nestedSearch(obj[field]);
							}
						}
						return false;
					};

					var search = $(Plugin.getOption('search.input')).val();
					if (typeof search !== 'undefined' && search !== '') {
						search = search.toLowerCase();
						datatable.dataSet = $.grep(datatable.dataSet, nestedSearch);
						// remove generalSearch as we don't need this for next columns filter
						delete params.query[Plugin.getGeneralSearchKey()];
					}

					// remove empty element from array
					$.each(params.query, function (k, v) {
						if (v === '') {
							delete params.query[k];
						}
					});

					// filter array by query
					datatable.dataSet = Plugin.filterArray(datatable.dataSet, params.query);

					// reset array index
					datatable.dataSet = datatable.dataSet.filter(function () {
						return true;
					});
				}

				return datatable.dataSet;
			},

			/**
			 * Utility helper to filter array by object pair of {key:value}
			 * @param list
			 * @param args
			 * @param operator
			 * @returns {*}
			 */
			filterArray: function (list, args, operator) {
				if (typeof list !== 'object') {
					return [];
				}

				if (typeof operator === 'undefined') operator = 'AND';

				if (typeof args !== 'object') {
					return list;
				}

				operator = operator.toUpperCase();

				if ($.inArray(operator, ['AND', 'OR', 'NOT']) === -1) {
					return [];
				}

				var count = Object.keys(args).length;
				var filtered = [];

				$.each(list, function (key, obj) {
					var to_match = obj;

					var matched = 0;
					$.each(args, function (m_key, m_value) {
						m_value = m_value instanceof Array ? m_value : [m_value];
						if (to_match.hasOwnProperty(m_key)) {
							var lhs = to_match[m_key].toString().toLowerCase();
							m_value.forEach(function (item, index) {
								if (item.toString().toLowerCase() == lhs || lhs.indexOf(item.toString().toLowerCase()) !== -1) {
									matched++;
								}
							});
						}
					});

					if (('AND' == operator && matched == count) ||
						('OR' == operator && matched > 0) ||
						('NOT' == operator && 0 == matched)) {
						filtered[key] = obj;
					}
				});

				list = filtered;

				return list;
			},

			/**
			 * Reset lock column scroll to 0 when resize
			 */
			resetScroll: function () {
				if (typeof options.detail === 'undefined' && Plugin.getDepth() === 1) {
					$(datatable.table).find('.m-datatable__row').css('left', 0);
					$(datatable.table).find('.m-datatable__lock').css('top', 0);
					$(datatable.tableBody).scrollTop(0);
				}
			},

			/**
			 * Get column options by field
			 * @param field
			 * @returns {boolean}
			 */
			getColumnByField: function (field) {
				if (typeof field === 'undefined') return;
				var result;
				$.each(options.columns, function (i, column) {
					if (field === column.field) {
						result = column;
						return false;
					}
				});
				return result;
			},

			/**
			 * Get default sort column
			 */
			getDefaultSortColumn: function () {
				var result;
				$.each(options.columns, function (i, column) {
					if (typeof column.sortable !== 'undefined'
						&& $.inArray(column.sortable, ['asc', 'desc']) !== -1) {
						result = {sort: column.sortable, field: column.field};
						return false;
					}
				});
				return result;
			},

			/**
			 * Helper to get element dimensions, when the element is hidden
			 * @param element
			 * @param includeMargin
			 * @returns {{width: number, height: number, innerWidth: number,
			 *     innerHeight: number, outerWidth: number, outerHeight: number}}
			 */
			getHiddenDimensions: function (element, includeMargin) {
				var props = {
						position: 'absolute',
						visibility: 'hidden',
						display: 'block',
					},
					dim = {
						width: 0,
						height: 0,
						innerWidth: 0,
						innerHeight: 0,
						outerWidth: 0,
						outerHeight: 0,
					},
					hiddenParents = $(element).parents().addBack().not(':visible');
				includeMargin = (typeof includeMargin === 'boolean')
					? includeMargin
					: false;

				var oldProps = [];
				hiddenParents.each(function () {
					var old = {};

					for (var name in props) {
						old[name] = this.style[name];
						this.style[name] = props[name];
					}

					oldProps.push(old);
				});

				dim.width = $(element).width();
				dim.outerWidth = $(element).outerWidth(includeMargin);
				dim.innerWidth = $(element).innerWidth();
				dim.height = $(element).height();
				dim.innerHeight = $(element).innerHeight();
				dim.outerHeight = $(element).outerHeight(includeMargin);

				hiddenParents.each(function (i) {
					var old = oldProps[i];
					for (var name in props) {
						this.style[name] = old[name];
					}
				});

				return dim;
			},

			getGeneralSearchKey: function () {
				var searchInput = $(Plugin.getOption('search.input'));
				return $(searchInput).prop('name') || $(searchInput).prop('id');
			},

			/**
			 * Get value by dot notation path string and to prevent undefined errors
			 * @param path String Dot notation path in string
			 * @param object Object to iterate
			 * @returns {*}
			 */
			getObject: function (path, object) {
				return path.split('.').reduce(function (obj, i) {
					return obj !== null && typeof obj[i] !== 'undefined' ? obj[i] : null;
				}, object);
			},

			/**
			 * Extend object
			 * @param obj
			 * @param path
			 * @param value
			 * @returns {*}
			 */
			extendObj: function (obj, path, value) {
				var levels = path.split('.'),
					i = 0;

				function createLevel(child) {
					var name = levels[i++];
					if (typeof child[name] !== 'undefined' && child[name] !== null) {
						if (typeof child[name] !== 'object' &&
							typeof child[name] !== 'function') {
							child[name] = {};
						}
					} else {
						child[name] = {};
					}
					if (i === levels.length) {
						child[name] = value;
					} else {
						createLevel(child[name]);
					}
				}

				createLevel(obj);
				return obj;
			},

			rowEvenOdd: function () {
				// row even class
				$(datatable.tableBody).find('.m-datatable__row').removeClass('m-datatable__row--even');
				if ($(datatable.wrap).hasClass('m-datatable--subtable')) {
					$(datatable.tableBody).find('.m-datatable__row:not(.m-datatable__row-detail):even').addClass('m-datatable__row--even');
				} else {
					$(datatable.tableBody).find('.m-datatable__row:nth-child(even)').addClass('m-datatable__row--even');
				}
			},

			/********************
			 ** PUBLIC API METHODS
			 ********************/

			// delay timer
			timer: 0,

			/**
			 * Redraw datatable by recalculating its DOM elements, etc.
			 * @returns {jQuery}
			 */
			redraw: function () {
				Plugin.adjustCellsWidth.call();
				if (Plugin.isLocked()) {
					// fix hiding cell width issue
					Plugin.scrollbar();
					Plugin.resetScroll();

					Plugin.adjustCellsHeight.call();
				}
				Plugin.adjustLockContainer.call();
				Plugin.initHeight.call();
				return datatable;
			},

			/**
			 * Shortcode to reload
			 * @returns {jQuery}
			 */
			load: function () {
				Plugin.reload();
				return datatable;
			},

			/**
			 * Datasource reload
			 * @returns {jQuery}
			 */
			reload: function () {
				var delay = (function () {
					return function (callback, ms) {
						clearTimeout(Plugin.timer);
						Plugin.timer = setTimeout(callback, ms);
					};
				})();
				delay(function () {
					// local only. remote pagination will skip this block
					if (!options.data.serverFiltering) {
						Plugin.localDataUpdate();
					}
					Plugin.dataRender();
					$(datatable).trigger('m-datatable--on-reloaded');
				}, Plugin.getOption('search.delay'));
				return datatable;
			},

			/**
			 * Get record by record ID
			 * @param id
			 * @returns {jQuery}
			 */
			getRecord: function (id) {
				if (typeof datatable.tableBody === 'undefined') datatable.tableBody = $(datatable.table).children('tbody');
				$(datatable.tableBody).find('.m-datatable__cell:first-child').each(function (i, cell) {
					if (id == $(cell).text()) {
						var rowNumber = $(cell).closest('.m-datatable__row').index() + 1;
						datatable.API.record = datatable.API.value = Plugin.getOneRow(datatable.tableBody, rowNumber);
						return datatable;
					}
				});
				return datatable;
			},

			/**
			 * @deprecated in v5.0.6
			 * Get column of current record ID
			 * @param columnName
			 * @returns {jQuery}
			 */
			getColumn: function (columnName) {
				Plugin.setSelectedRecords();
				datatable.API.value = $(datatable.API.record).find('[data-field="' + columnName + '"]');
				return datatable;
			},

			/**
			 * Destroy datatable to original DOM state before datatable was
			 * initialized
			 * @returns {jQuery}
			 */
			destroy: function () {
				$(datatable).parent().find('.m-datatable__pager').remove();
				var initialDatatable = $(datatable.initialDatatable).addClass('m-datatable--destroyed').show();
				$(datatable).replaceWith(initialDatatable);
				datatable = initialDatatable;
				$(datatable).trigger('m-datatable--on-destroy');
				Plugin.isInit = false;
				initialDatatable = null;
				return initialDatatable;
			},

			/**
			 * Sort by column field
			 * @param field
			 * @param sort
			 */
			sort: function (field, sort) {
				// toggle sort
				sort = typeof sort === 'undefined' ? 'asc' : sort;

				Plugin.spinnerCallback(true);

				// update field and sort params
				var meta = {field: field, sort: sort};
				Plugin.setDataSourceParam('sort', meta);

				setTimeout(function () {
					Plugin.dataRender('sort');
					$(datatable).trigger('m-datatable--on-sort', meta);
					$(datatable.tableHead).find('.m-datatable__cell > span > i').remove();
				}, 300);

				return datatable;
			},

			/**
			 * @deprecated in v5.0.6
			 * Get current selected column value
			 * @returns {jQuery}
			 */
			getValue: function () {
				return $(datatable.API.value).text();
			},

			/**
			 * Set checkbox active
			 * @param cell JQuery selector or checkbox ID
			 */
			setActive: function (cell) {
				if (typeof cell === 'string') {
					// set by checkbox id
					cell = $(datatable.tableBody).find('.m-checkbox--single > [type="checkbox"][value="' + cell + '"]');
				}

				$(cell).prop('checked', true);

				// normal table
				var row = $(cell).closest('.m-datatable__row').addClass('m-datatable__row--active');

				var index = $(row).index() + 1;
				// lock table
				$(row).closest('.m-datatable__lock').parent().find('.m-datatable__row:nth-child(' + index + ')').addClass('m-datatable__row--active');

				var ids = [];
				$(row).each(function (i, td) {
					var id = $(td).find('.m-checkbox--single:not(.m-checkbox--all) > [type="checkbox"]').val();
					if (typeof id !== 'undefined') {
						ids.push(id);
					}
				});

				$(datatable).trigger('m-datatable--on-check', [ids]);
			},

			/**
			 * Set checkbox inactive
			 * @param cell JQuery selector or checkbox ID
			 */
			setInactive: function (cell) {
				if (typeof cell === 'string') {
					// set by checkbox id
					cell = $(datatable.tableBody).find('.m-checkbox--single > [type="checkbox"][value="' + cell + '"]');
				}

				$(cell).prop('checked', false);

				// normal table
				var row = $(cell).closest('.m-datatable__row').removeClass('m-datatable__row--active');
				var index = $(row).index() + 1;

				// lock table
				$(row).closest('.m-datatable__lock').parent().find('.m-datatable__row:nth-child(' + index + ')').removeClass('m-datatable__row--active');

				var ids = [];
				$(row).each(function (i, td) {
					var id = $(td).find('.m-checkbox--single:not(.m-checkbox--all) > [type="checkbox"]').val();
					if (typeof id !== 'undefined') {
						ids.push(id);
					}
				});

				$(datatable).trigger('m-datatable--on-uncheck', [ids]);
			},

			/**
			 * Set all checkboxes active or inactive
			 * @param active
			 */
			setActiveAll: function (active) {
				// todo; check if child table also will set active?
				var checkboxes = $(datatable.table).find('.m-datatable__body .m-datatable__row').find('.m-datatable__cell--check .m-checkbox [type="checkbox"]');
				if (active) {
					Plugin.setActive(checkboxes);
				} else {
					Plugin.setInactive(checkboxes);
				}
			},

			/**
			 * @deprecated in v5.0.6
			 * Get selected rows which are active
			 * @returns {jQuery}
			 */
			setSelectedRecords: function () {
				datatable.API.record = $(datatable.tableBody).find('.m-datatable__row--active');
				return datatable;
			},

			/**
			 * Get selected records
			 * @returns {null}
			 */
			getSelectedRecords: function () {
				// support old method
				Plugin.setSelectedRecords();
				datatable.API.record = datatable.rows('.m-datatable__row--active').nodes();
				return datatable.API.record;
			},

			/**
			 * Get options by dots notation path
			 * @param path String Dot notation path in string
			 * @returns {*}
			 */
			getOption: function (path) {
				return Plugin.getObject(path, options);
			},

			/**
			 * Set global options nodes by dots notation path
			 * @param path
			 * @param object
			 */
			setOption: function (path, object) {
				options = Plugin.extendObj(options, path, object);
			},

			/**
			 * Search filter for local & remote
			 * @param value
			 * @param columns. Optional list of columns to be filtered.
			 */
			search: function (value, columns) {
				if (typeof columns !== 'undefined') columns = $.makeArray(columns);
				var delay = (function () {
					return function (callback, ms) {
						clearTimeout(Plugin.timer);
						Plugin.timer = setTimeout(callback, ms);
					};
				})();

				delay(function () {
					// get query parameters
					var query = Plugin.getDataSourceQuery();

					// search not by columns
					if (typeof columns === 'undefined' && typeof value !== 'undefined') {
						var key = Plugin.getGeneralSearchKey();
						query[key] = value;
					}

					// search by columns, support multiple columns
					if (typeof columns === 'object') {
						$.each(columns, function (k, column) {
							query[column] = value;
						});
						// remove empty element from arrays
						$.each(query, function (k, v) {
							if (v === '' || $.isEmptyObject(v)) {
								delete query[k];
							}
						});
					}

					Plugin.setDataSourceQuery(query);

					// local filter only. remote pagination will skip this block
					if (!options.data.serverFiltering) {
						Plugin.localDataUpdate();
					}
					Plugin.dataRender('search');
				}, Plugin.getOption('search.delay'));
			},

			/**
			 * Set datasource paramsextractextract
			 * @param param
			 * @param value
			 */
			setDataSourceParam: function (param, value) {
				datatable.API.params = $.extend({}, {
					pagination: {page: 1, perpage: Plugin.getOption('data.pageSize')},
					sort: Plugin.getDefaultSortColumn(),
					query: {},
				}, datatable.API.params, Plugin.stateGet(Plugin.stateId));

				datatable.API.params = Plugin.extendObj(datatable.API.params, param, value);

				Plugin.stateKeep(Plugin.stateId, datatable.API.params);
			},

			/**
			 * Get datasource params
			 * @param param
			 */
			getDataSourceParam: function (param) {
				datatable.API.params = $.extend({}, {
					pagination: {page: 1, perpage: Plugin.getOption('data.pageSize')},
					sort: Plugin.getDefaultSortColumn(),
					query: {},
				}, datatable.API.params, Plugin.stateGet(Plugin.stateId));

				if (typeof param === 'string') {
					return Plugin.getObject(param, datatable.API.params);
				}

				return datatable.API.params;
			},

			/**
			 * Shortcode to datatable.getDataSourceParam('query');
			 * @returns {*}
			 */
			getDataSourceQuery: function () {
				return Plugin.getDataSourceParam('query') || {};
			},

			/**
			 * Shortcode to datatable.setDataSourceParam('query', query);
			 * @param query
			 */
			setDataSourceQuery: function (query) {
				Plugin.setDataSourceParam('query', query);
			},

			/**
			 * Get current page number
			 * @returns {number}
			 */
			getCurrentPage: function () {
				return $(datatable.table).siblings('.m-datatable__pager').last().find('.m-datatable__pager-nav').find('.m-datatable__pager-link.m-datatable__pager-link--active').data('page') || 1;
			},

			/**
			 * Get selected dropdown page size
			 * @returns {*|number}
			 */
			getPageSize: function () {
				return $(datatable.table).siblings('.m-datatable__pager').last().find('select.m-datatable__pager-size').val() || 10;
			},

			/**
			 * Get total rows
			 */
			getTotalRows: function () {
				return datatable.API.params.pagination.total;
			},

			/**
			 * Get full dataset in grid
			 * @returns {*|null|Array}
			 */
			getDataSet: function () {
				return datatable.originalDataSet;
			},

			/**
			 * @deprecated in v5.0.6
			 * Hide column by column's field name
			 * @param fieldName
			 */
			hideColumn: function (fieldName) {
				// add hide option for this column
				$.map(options.columns, function (column) {
					if (fieldName === column.field) {
						column.responsive = {hidden: 'xl'};
					}
					return column;
				});
				// hide current displayed column
				var tds = $.grep($(datatable.table).find('.m-datatable__cell'), function (n, i) {
					return fieldName === $(n).data('field');
				});
				$(tds).hide();
			},

			/**
			 * @deprecated in v5.0.6
			 * Show column by column's field name
			 * @param fieldName
			 */
			showColumn: function (fieldName) {
				// add hide option for this column
				$.map(options.columns, function (column) {
					if (fieldName === column.field) {
						delete column.responsive;
					}
					return column;
				});
				// hide current displayed column
				var tds = $.grep($(datatable.table).find('.m-datatable__cell'), function (n, i) {
					return fieldName === $(n).data('field');
				});
				$(tds).show();
			},

			nodeTr: [],
			nodeTd: [],
			nodeCols: [],
			recentNode: [],

			table: function () {
				return datatable.table;
			},

			/**
			 * Select a single row from the table
			 * @param selector
			 * @returns {jQuery}
			 */
			row: function (selector) {
				Plugin.rows(selector);
				Plugin.nodeTr = Plugin.recentNode = $(Plugin.nodeTr).first();
				return datatable;
			},

			/**
			 * Select multiple rows from the table
			 * @param selector
			 * @returns {jQuery}
			 */
			rows: function (selector) {
				Plugin.nodeTr = Plugin.recentNode = $(datatable.tableBody).find(selector).filter('.m-datatable__row');
				return datatable;
			},

			/**
			 * Select a single column from the table
			 * @param index zero-based index
			 * @returns {jQuery}
			 */
			column: function (index) {
				Plugin.nodeCols = Plugin.recentNode = $(datatable.tableBody).find('.m-datatable__cell:nth-child(' + (index + 1) + ')');
				return datatable;
			},

			/**
			 * Select multiple columns from the table
			 * @param selector
			 * @returns {jQuery}
			 */
			columns: function (selector) {
				var context = datatable.table;
				if (Plugin.nodeTr === Plugin.recentNode) {
					context = Plugin.nodeTr;
				}
				var columns = $(context).find('.m-datatable__cell[data-field="' + selector + '"]');
				if (columns.length > 0) {
					Plugin.nodeCols = Plugin.recentNode = columns;
				} else {
					Plugin.nodeCols = Plugin.recentNode = $(context).find(selector).filter('.m-datatable__cell');
				}
				return datatable;
			},

			cell: function (selector) {
				Plugin.cells(selector);
				Plugin.nodeTd = Plugin.recentNode = $(Plugin.nodeTd).first();
				return datatable;
			},

			cells: function (selector) {
				var cells = $(datatable.tableBody).find('.m-datatable__cell');
				if (typeof selector !== 'undefined') {
					cells = $(cells).filter(selector);
				}
				Plugin.nodeTd = Plugin.recentNode = cells;
				return datatable;
			},

			/**
			 * Delete the selected row from the table
			 * @returns {jQuery}
			 */
			remove: function () {
				if ($(Plugin.nodeTr.length) && Plugin.nodeTr === Plugin.recentNode) {
					$(Plugin.nodeTr).remove();
				}
				Plugin.layoutUpdate();
				return datatable;
			},

			/**
			 * Show or hide the columns or rows
			 */
			visible: function (bool) {
				if ($(Plugin.recentNode.length)) {
					var locked = Plugin.lockEnabledColumns();
					if (Plugin.recentNode === Plugin.nodeCols) {
						var index = Plugin.recentNode.index();

						if (Plugin.isLocked()) {
							var scrollColumns = $(Plugin.recentNode).closest('.m-datatable__lock--scroll').length;
							if (scrollColumns) {
								// is at center of scrollable area
								index += locked.left.length + 1;
							} else if ($(Plugin.recentNode).closest('.m-datatable__lock--right').length) {
								// is at the right locked table
								index += locked.left.length + scrollColumns + 1;
							}
						}
					}

					if (bool) {
						if (Plugin.recentNode === Plugin.nodeCols) {
							delete options.columns[index].responsive;
						}
						$(Plugin.recentNode).show();
					} else {
						if (Plugin.recentNode === Plugin.nodeCols) {
							Plugin.setOption('columns.' + index + '.responsive', {hidden: 'xl'});
						}
						$(Plugin.recentNode).hide();
					}
					Plugin.redraw();
				}
			},

			/**
			 * Get the the DOM element for the selected rows or columns
			 * @returns {Array}
			 */
			nodes: function () {
				return Plugin.recentNode;
			},

			/**
			 * will be implemented soon
			 * @returns {jQuery}
			 */
			dataset: function () {
				return datatable;
			},

		};

		/**
		 * Public API methods can be used directly by datatable
		 */
		$.each(Plugin, function (funcName, func) {
			datatable[funcName] = func;
		});

		// initialize main datatable plugin
		if (typeof options !== 'undefined') {
			if (typeof options === 'string') {
				var method = options;
				datatable = $(this).data('mDatatable');
				if (typeof datatable !== 'undefined') {
					options = datatable.options;
					Plugin[method].apply(this, Array.prototype.slice.call(arguments, 1));
				}
			} else {
				if (!datatable.data('mDatatable') && !$(this).hasClass('m-datatable--loaded')) {
					datatable.dataSet = null;
					datatable.textAlign = {
						left: 'm-datatable__cell--left',
						center: 'm-datatable__cell--center',
						right: 'm-datatable__cell--right',
					};

					// merge default and user defined options
					options = $.extend(true, {}, $.fn.mDatatable.defaults, options);

					datatable.options = options;

					// init plugin process
					Plugin.init.apply(this, [options]);

					$(datatable.wrap).data('mDatatable', datatable);
				}
			}
		} else {
			// get existing instance datatable
			datatable = $(this).data('mDatatable');
			if (typeof datatable === 'undefined') {
				$.error('mDatatable not initialized');
			}
			options = datatable.options;
		}

		return datatable;
	};

	// default options
	$.fn.mDatatable.defaults = {
		// datasource definition
		data: {
			type: 'local',
			source: null,
			pageSize: 10, // display records per page
			saveState: {
				// save datatable state(pagination, filtering, sorting, etc) in cookie or browser webstorage
				cookie: false,
				webstorage: true,
			},

			serverPaging: false,
			serverFiltering: false,
			serverSorting: false,

			autoColumns: false,
			attr: {
				rowProps: [],
			},
		},

		// layout definition
		layout: {
			theme: 'default', // datatable will support multiple themes and designs
			class: 'm-datatable--brand', // custom wrapper class
			scroll: false, // enable/disable datatable scroll both horizontal and vertical when needed.
			height: null, // datatable's body's fixed height
			minHeight: 300,
			footer: false, // display/hide footer
			header: true, // display/hide header
			customScrollbar: true, // set false to disable custom scrollbar

			// datatable spinner
			spinner: {
				overlayColor: '#000000',
				opacity: 0,
				type: 'loader',
				state: 'brand',
				message: true,
			},

			// datatable UI icons
			icons: {
				sort: {asc: 'la la-arrow-up', desc: 'la la-arrow-down'},
				pagination: {
					next: 'la la-angle-right',
					prev: 'la la-angle-left',
					first: 'la la-angle-double-left',
					last: 'la la-angle-double-right',
					more: 'la la-ellipsis-h',
				},
				rowDetail: {expand: 'fa fa-caret-down', collapse: 'fa fa-caret-right'},
			},
		},

		// column sorting
		sortable: true,

		// resize column size with mouse drag coming soon)
		resizable: false,

		// column based filtering (coming soon)
		filterable: false,

		pagination: true,

		// inline and bactch editing (cooming soon)
		editable: false,

		// columns definition
		columns: [],

		search: {
			// enable trigger search by keyup enter
			onEnter: false,
			// input text for search
			input: null,
			// search delay in milliseconds
			delay: 400,
		},

		rows: {
			// deprecated
			callback: function () {
			},
			// call before row template
			beforeTemplate: function () {
			},
			// call after row template
			afterTemplate: function () {
			},
			// auto hide columns, if rows overflow. work on non locked columns
			autoHide: false,
		},

		// toolbar
		toolbar: {
			// place pagination and displayInfo blocks according to the array order
			layout: ['pagination', 'info'],

			// toolbar placement can be at top or bottom or both top and bottom repeated
			placement: ['bottom'],  //'top', 'bottom'

			// toolbar items
			items: {
				// pagination
				pagination: {
					// pagination type(default or scroll)
					type: 'default',

					// number of pages to display by breakpoints
					pages: {
						desktop: {
							layout: 'default',
							pagesNumber: 6,
						},
						tablet: {
							layout: 'default',
							pagesNumber: 3,
						},
						mobile: {
							layout: 'compact',
						},
					},

					// navigation buttons
					navigation: {
						prev: true, // display prev link
						next: true, // display next link
						first: true, // display first link
						last: true // display last link
					},

					// page size select
					pageSizeSelect: [] // display dropdown to select pagination size. -1 is used for "ALl" option
				},

				// records info
				info: true,
			},
		},

		// here we will keep all strings and message used by datatable UI so developer can easiliy translate to any language.
		// By default the stirngs will be in the plugin source and here can override it
		translate: {
			records: {
				processing: 'Please wait...',
				noRecords: 'No records found',
			},
			toolbar: {
				pagination: {
					items: {
						default: {
							first: 'First',
							prev: 'Previous',
							next: 'Next',
							last: 'Last',
							more: 'More pages',
							input: 'Page number',
							select: 'Select page size',
						},
						info: 'Displaying {{start}} - {{end}} of {{total}} records',
					},
				},
			},
		},

		extensions: {},
	};

}(jQuery));