// plugin setup
var mQuicksearch = function(elementId, options) {
    //== Main object
    var the = this;
    var init = false;

    //== Get element object
    var element = mUtil.get(elementId);
    var body = mUtil.get('body');  

    if (!element) {
        return;
    }

    //== Default options
    var defaultOptions = {
        mode: 'default', //'default/dropdown'
        minLength: 1,
        maxHeight: 300,
        requestTimeout: 200, // ajax request fire timeout in milliseconds 
        inputTarget: 'm_quicksearch_input',
        iconCloseTarget: 'm_quicksearch_close',
        iconCancelTarget: 'm_quicksearch_cancel',
        iconSearchTarget: 'm_quicksearch_search',
        
        spinnerClass: 'm-loader m-loader--skin-light m-loader--right',
        hasResultClass: 'm-list-search--has-result',
        
        templates: {
            error: '<div class="m-search-results m-search-results--skin-light"><span class="m-search-result__message">{{message}}</div></div>'
        }
    };

    ////////////////////////////
    // ** Private Methods  ** //
    ////////////////////////////

    var Plugin = {
        /**
         * Construct
         */

        construct: function(options) {
            if (mUtil.data(element).has('quicksearch')) {
                the = mUtil.data(element).get('quicksearch');
            } else {
                // reset menu
                Plugin.init(options);

                // build menu
                Plugin.build();

                mUtil.data(element).set('quicksearch', the);
            }

            return the;
        },

        init: function(options) {
            the.element = element;
            the.events = [];

            // merge default and user defined options
            the.options = mUtil.deepExtend({}, defaultOptions, options);

            // search query
            the.query = '';

            // form
            the.form = mUtil.find(element, 'form');

            // input element
            the.input = mUtil.get(the.options.inputTarget);

            // close icon
            the.iconClose = mUtil.get(the.options.iconCloseTarget);

            if (the.options.mode == 'default') {
                // search icon
                the.iconSearch = mUtil.get(the.options.iconSearchTarget);

                // cancel icon
                the.iconCancel = mUtil.get(the.options.iconCancelTarget);
            }

            // dropdown
            the.dropdown = new mDropdown(element, {
                mobileOverlay: false
            });

            // cancel search timeout
            the.cancelTimeout;

            // ajax processing state
            the.processing = false;

            // ajax request fire timeout
            the.requestTimeout = false;
        },

        /**
         * Build plugin
         */
        build: function() {
            // attach input keyup handler
            mUtil.addEvent(the.input, 'keyup', Plugin.search);

            if (the.options.mode == 'default') {
                mUtil.addEvent(the.input, 'focus', Plugin.showDropdown);
                mUtil.addEvent(the.iconCancel, 'click', Plugin.handleCancel);

                mUtil.addEvent(the.iconSearch, 'click', function() {
                    if (mUtil.isInResponsiveRange('tablet-and-mobile')) {
                        mUtil.addClass(body, 'm-header-search--mobile-expanded');
                        the.input.focus();
                    }
                });

                mUtil.addEvent(the.iconClose, 'click', function() {
                    if (mUtil.isInResponsiveRange('tablet-and-mobile')) {
                        mUtil.removeClass(body, 'm-header-search--mobile-expanded');
                        Plugin.closeDropdown();
                    }
                });
            } else if (the.options.mode == 'dropdown') {
                the.dropdown.on('afterShow', function() {
                    the.input.focus();
                });

                mUtil.addEvent(the.iconClose, 'click', Plugin.closeDropdown);
            }
        },

        showProgress: function() {
            the.processing = true;
            mUtil.addClass(the.form, the.options.spinnerClass);
            Plugin.handleCancelIconVisibility('off');

            return the;
        },

        hideProgress: function() {
            the.processing = false;
            mUtil.removeClass(the.form, the.options.spinnerClass);
            Plugin.handleCancelIconVisibility('on');
            mUtil.addClass(element, the.options.hasResultClass);

            return the;
        },

        /**
         * Search handler
         */
        search: function(e) {
            the.query = the.input.value;

            if (the.query.length === 0) {
                Plugin.handleCancelIconVisibility('on');
                mUtil.removeClass(element, the.options.hasResultClass);
                mUtil.removeClass(the.form, the.options.spinnerClass);
            }

            if (the.query.length < the.options.minLength || the.processing == true) {
                return;
            }

            if (the.requestTimeout) {
                clearTimeout(the.requestTimeout);
            }

            the.requestTimeout = false;

            the.requestTimeout = setTimeout(function() {
                Plugin.eventTrigger('search');
            }, the.options.requestTimeout);            

            return the;
        },

        /**
         * Handle cancel icon visibility
         */
        handleCancelIconVisibility: function(status) {
            if (status == 'on') {
                if (the.input.value.length === 0) {
                    if (the.iconCancel) mUtil.css(the.iconCancel, 'visibility', 'hidden');
                    if (the.iconClose) mUtil.css(the.iconClose, 'visibility', 'visible');
                } else {
                    clearTimeout(the.cancelTimeout);
                    the.cancelTimeout = setTimeout(function() {
                        if (the.iconCancel) mUtil.css(the.iconCancel, 'visibility', 'visible');
                        if (the.iconClose) mUtil.css(the.iconClose, 'visibility', 'visible');
                    }, 500);
                }
            } else {
                if (the.iconCancel) mUtil.css(the.iconCancel, 'visibility', 'hidden');
                if (the.iconClose) mUtil.css(the.iconClose, 'visibility', 'hidden');
            }
        },

        /**
         * Cancel handler
         */
        handleCancel: function(e) {
            the.input.value = '';
            mUtil.css(the.iconCancel, 'visibility', 'hidden');
            mUtil.removeClass(element, the.options.hasResultClass);

            Plugin.closeDropdown();
        },

        /**
         * Cancel handler
         */
        closeDropdown: function() {
            the.dropdown.hide();
        },

        /**
         * Show dropdown
         */
        showDropdown: function(e) {
            if (the.dropdown.isShown() == false && the.input.value.length > the.options.minLength && the.processing == false) {
                console.log('show!!!');
                the.dropdown.show();
                if (e) {
                    e.preventDefault();
                    e.stopPropagation();
                }                
            }
        },

        /**
         * Trigger events
         */
        eventTrigger: function(name) {
            //mUtil.triggerCustomEvent(name);
            for (i = 0; i < the.events.length; i++) {
                var event = the.events[i];
                if (event.name == name) {
                    if (event.one == true) {
                        if (event.fired == false) {
                            the.events[i].fired = true;
                            event.handler.call(this, the);
                        }
                    } else {
                        event.handler.call(this, the);
                    }
                }
            }
        },

        addEvent: function(name, handler, one) {
            the.events.push({
                name: name,
                handler: handler,
                one: one,
                fired: false
            });

            return the;
        }
    };

    //////////////////////////
    // ** Public Methods ** //
    //////////////////////////

    /**
     * Set default options 
     */

    the.setDefaults = function(options) {
        defaultOptions = options;
    };

    /**
     * quicksearch off 
     */
    the.search = function() {
        return Plugin.handleSearch();
    };

    the.showResult = function(res) {
        the.dropdown.setContent(res);
        Plugin.showDropdown();

        return the;
    };

    the.showError = function(text) {
        var msg = the.options.templates.error.replace('{{message}}', text);
        the.dropdown.setContent(msg);
        Plugin.showDropdown();

        return the;
    };

    /**
     *  
     */
    the.showProgress = function() {
        return Plugin.showProgress();
    };

    the.hideProgress = function() {
        return Plugin.hideProgress();
    };

    /**
     * quicksearch off 
     */
    the.search = function() {
        return Plugin.search();
    };

    /**
     * Attach event
     * @returns {mQuicksearch}
     */
    the.on = function(name, handler) {
        return Plugin.addEvent(name, handler);
    };

    /**
     * Attach event that will be fired once
     * @returns {mQuicksearch}
     */
    the.one = function(name, handler) {
        return Plugin.addEvent(name, handler, true);
    };

    //== Construct plugin
    Plugin.construct.apply(the, [options]);

    return the;
};