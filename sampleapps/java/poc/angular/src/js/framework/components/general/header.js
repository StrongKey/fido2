var mHeader = function(elementId, options) {
    //== Main object
    var the = this;
    var init = false;

    //== Get element object
    var element = mUtil.get(elementId);
    var body = mUtil.get('body');

    if (element === undefined) {
        return;
    }

    //== Default options
    var defaultOptions = {
        classic: false,
        offset: {
            mobile: 150,
            desktop: 200
        },
        minimize: {
            mobile: false,
            desktop: false
        }
    };

    ////////////////////////////
    // ** Private Methods  ** //
    ////////////////////////////

    var Plugin = {
        /**
         * Run plugin
         * @returns {mHeader}
         */
        construct: function(options) {
            if (mUtil.data(element).has('header')) {
                the = mUtil.data(element).get('header');
            } else {
                // reset header
                Plugin.init(options);

                // build header
                Plugin.build();

                mUtil.data(element).set('header', the);
            }

            return the;
        },

        /**
         * Handles subheader click toggle
         * @returns {mHeader}
         */
        init: function(options) {
            the.events = [];

            // merge default and user defined options
            the.options = mUtil.deepExtend({}, defaultOptions, options);
        },

        /**
         * Reset header
         * @returns {mHeader}
         */
        build: function() {
            var lastScrollTop = 0;

            if (the.options.minimize.mobile === false && the.options.minimize.desktop === false) {
                return;
            }

            window.addEventListener('scroll', function() {
                var offset = 0, on, off, st;

                if (mUtil.isInResponsiveRange('desktop')) {
                    offset = the.options.offset.desktop;
                    on = the.options.minimize.desktop.on;
                    off = the.options.minimize.desktop.off;
                } else if (mUtil.isInResponsiveRange('tablet-and-mobile')) {
                    offset = the.options.offset.mobile;
                    on = the.options.minimize.mobile.on;
                    off = the.options.minimize.mobile.off;
                }

                st = window.pageYOffset;

                if (
                    (mUtil.isInResponsiveRange('tablet-and-mobile') && the.options.classic && the.options.classic.mobile) ||
                    (mUtil.isInResponsiveRange('desktop') && the.options.classic && the.options.classic.desktop)

                ) {
                    if (st > offset) { // down scroll mode
                        mUtil.addClass(body, on);
                        mUtil.removeClass(body, off);
                    } else { // back scroll mode
                        mUtil.addClass(body, off);
                        mUtil.removeClass(body, on);
                    }
                } else {
                    if (st > offset && lastScrollTop < st) { // down scroll mode
                        mUtil.addClass(body, on);
                        mUtil.removeClass(body, off);
                    } else { // back scroll mode
                        mUtil.addClass(body, off);
                        mUtil.removeClass(body, on);
                    }

                    lastScrollTop = st;
                }
            });
        },

        /**
         * Trigger events
         */
        eventTrigger: function(name, args) {
            for (var i = 0; i < the.events.length; i++) {
                var event = the.events[i];
                if (event.name == name) {
                    if (event.one == true) {
                        if (event.fired == false) {
                            the.events[i].fired = true;
                            event.handler.call(this, the, args);
                        }
                    } else {
                        event.handler.call(this, the, args);
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
     * Register event
     */
    the.on = function(name, handler) {
        return Plugin.addEvent(name, handler);
    };

    ///////////////////////////////
    // ** Plugin Construction ** //
    ///////////////////////////////

    //== Run plugin
    Plugin.construct.apply(the, [options]);

    //== Init done
    init = true;

    // Return plugin instance
    return the;
};