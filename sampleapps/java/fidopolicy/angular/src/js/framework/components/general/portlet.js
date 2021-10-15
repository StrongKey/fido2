// plugin setup
var mPortlet = function(elementId, options) {
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
        bodyToggleSpeed: 400,
        tooltips: true,
        tools: {
            toggle: {
                collapse: 'Collapse',
                expand: 'Expand'
            },
            reload: 'Reload',
            remove: 'Remove',
            fullscreen: {
                on: 'Fullscreen',
                off: 'Exit Fullscreen'
            }
        },
        sticky: {
            offset: 300,
            zIndex: 101
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
            if (mUtil.data(element).has('portlet')) {
                the = mUtil.data(element).get('portlet');
            } else {
                // reset menu
                Plugin.init(options);

                // build menu
                Plugin.build();

                mUtil.data(element).set('portlet', the);
            }

            return the;
        },

        /**
         * Init portlet
         */
        init: function(options) {
            the.element = element;
            the.events = [];

            // merge default and user defined options
            the.options = mUtil.deepExtend({}, defaultOptions, options);
            the.head = mUtil.child(element, '.m-portlet__head');
            the.foot = mUtil.child(element, '.m-portlet__foot');

            if (mUtil.child(element, '.m-portlet__body')) {
                the.body = mUtil.child(element, '.m-portlet__body');
            } else if (mUtil.child(element, '.m-form').length !== 0) {
                the.body = mUtil.child(element, '.m-form');
            }
        },

        /**
         * Build Form Wizard
         */
        build: function() {
            //== Remove
            var remove = mUtil.find(the.head, '[m-portlet-tool=remove]');
            if (remove) {
                mUtil.addEvent(remove, 'click', function(e) {
                    e.preventDefault();
                    Plugin.remove();
                });
            }

            //== Reload
            var reload = mUtil.find(the.head, '[m-portlet-tool=reload]');
            if (reload) {
                mUtil.addEvent(reload, 'click', function(e) {
                    e.preventDefault();
                    Plugin.reload();
                });
            }

            //== Toggle
            var toggle = mUtil.find(the.head, '[m-portlet-tool=toggle]');
            if (toggle) {
                mUtil.addEvent(toggle, 'click', function(e) {
                    e.preventDefault();
                    Plugin.toggle();
                });
            }

            //== Fullscreen
            var fullscreen = mUtil.find(the.head, '[m-portlet-tool=fullscreen]');
            if (fullscreen) {
                mUtil.addEvent(fullscreen, 'click', function(e) {
                    e.preventDefault();
                    Plugin.fullscreen();
                });
            }

            Plugin.setupTooltips();
        },

        /**
         * Enable stickt mode
         */
        initSticky: function() {
            var lastScrollTop = 0;
            var offset = the.options.sticky.offset;

            if (!the.head) {
                return;
            }

            window.addEventListener('scroll', function() {
                var on, off, st;
                st = window.pageYOffset;

                if (st > offset) {
                    if (mUtil.hasClass(body, 'm-portlet--sticky') === false) {
                        Plugin.eventTrigger('stickyOn');

                        mUtil.addClass(body, 'm-portlet--sticky');
                        mUtil.addClass(element, 'm-portlet--sticky');

                        Plugin.updateSticky();
                    }
                } else { // back scroll mode
                    if (mUtil.hasClass(body, 'm-portlet--sticky')) {
                        Plugin.eventTrigger('stickyOff');

                        mUtil.removeClass(body, 'm-portlet--sticky');
                        mUtil.removeClass(element, 'm-portlet--sticky');

                        Plugin.resetSticky();
                    }
                }
            });
        },

        updateSticky: function() {
            if (!the.head) {
                return;
            }

            var top;

            if (mUtil.hasClass(body, 'm-portlet--sticky')) {
                if (the.options.sticky.position.top instanceof Function) {
                    top = parseInt(the.options.sticky.position.top.call());
                } else {
                    top = parseInt(the.options.sticky.position.top);
                }

                var left;
                if (the.options.sticky.position.left instanceof Function) {
                    left = parseInt(the.options.sticky.position.left.call());
                } else {
                    left = parseInt(the.options.sticky.position.left);
                }

                var right;
                if (the.options.sticky.position.right instanceof Function) {
                    right = parseInt(the.options.sticky.position.right.call());
                } else {
                    right = parseInt(the.options.sticky.position.right);
                }

                mUtil.css(the.head, 'z-index', the.options.sticky.zIndex);
                mUtil.css(the.head, 'top', top + 'px');
                mUtil.css(the.head, 'left', left + 'px');
                mUtil.css(the.head, 'right', right + 'px');
            }
        },

        resetSticky: function() {
            if (!the.head) {
                return;
            }

            if (mUtil.hasClass(body, 'm-portlet--sticky') === false) {
                mUtil.css(the.head, 'z-index', '');
                mUtil.css(the.head, 'top', '');
                mUtil.css(the.head, 'left', '');
                mUtil.css(the.head, 'right', '');
            }
        },

        /**
         * Remove portlet
         */
        remove: function() {
            if (Plugin.eventTrigger('beforeRemove') === false) {
                return;
            }

            if (mUtil.hasClass(body, 'm-portlet--fullscreen') && mUtil.hasClass(element, 'm-portlet--fullscreen')) {
                Plugin.fullscreen('off');
            }

            Plugin.removeTooltips();

            mUtil.remove(element);

            Plugin.eventTrigger('afterRemove');
        },

        /**
         * Set content
         */
        setContent: function(html) {
            if (html) {
                the.body.innerHTML = html;
            }
        },

        /**
         * Get body
         */
        getBody: function() {
            return the.body;
        },

        /**
         * Get self
         */
        getSelf: function() {
            return element;
        },

        /**
         * Setup tooltips
         */
        setupTooltips: function() {
            if (the.options.tooltips) {
                var collapsed = mUtil.hasClass(element, 'm-portlet--collapse') || mUtil.hasClass(element, 'm-portlet--collapsed');
                var fullscreenOn = mUtil.hasClass(body, 'm-portlet--fullscreen') && mUtil.hasClass(element, 'm-portlet--fullscreen');

                //== Remove
                var remove = mUtil.find(the.head, '[m-portlet-tool=remove]');
                if (remove) {
                    var placement = (fullscreenOn ? 'bottom' : 'top');
                    var tip = new Tooltip(remove, {
                        title: the.options.tools.remove,
                        placement: placement,
                        offset: (fullscreenOn ? '0,10px,0,0' : '0,5px'),
                        trigger: 'hover',
                        template: '<div class="m-tooltip m-tooltip--portlet tooltip bs-tooltip-' + placement + '" role="tooltip">\
                            <div class="tooltip-arrow arrow"></div>\
                            <div class="tooltip-inner"></div>\
                        </div>'
                    });

                    mUtil.data(remove).set('tooltip', tip);
                }

                //== Reload
                var reload = mUtil.find(the.head, '[m-portlet-tool=reload]');
                if (reload) {
                    var placement = (fullscreenOn ? 'bottom' : 'top');
                    var tip = new Tooltip(reload, {
                        title: the.options.tools.reload,
                        placement: placement,
                        offset: (fullscreenOn ? '0,10px,0,0' : '0,5px'),
                        trigger: 'hover',
                        template: '<div class="m-tooltip m-tooltip--portlet tooltip bs-tooltip-' + placement + '" role="tooltip">\
                            <div class="tooltip-arrow arrow"></div>\
                            <div class="tooltip-inner"></div>\
                        </div>'
                    });

                    mUtil.data(reload).set('tooltip', tip);
                }

                //== Toggle
                var toggle = mUtil.find(the.head, '[m-portlet-tool=toggle]');
                if (toggle) {
                    var placement = (fullscreenOn ? 'bottom' : 'top');
                    var tip = new Tooltip(toggle, {
                        title: (collapsed ? the.options.tools.toggle.expand : the.options.tools.toggle.collapse),
                        placement: placement,
                        offset: (fullscreenOn ? '0,10px,0,0' : '0,5px'),
                        trigger: 'hover',
                        template: '<div class="m-tooltip m-tooltip--portlet tooltip bs-tooltip-' + placement + '" role="tooltip">\
                            <div class="tooltip-arrow arrow"></div>\
                            <div class="tooltip-inner"></div>\
                        </div>'
                    });

                    mUtil.data(toggle).set('tooltip', tip);
                }

                //== Fullscreen
                var fullscreen = mUtil.find(the.head, '[m-portlet-tool=fullscreen]');
                if (fullscreen) {
                    var placement = (fullscreenOn ? 'bottom' : 'top');
                    var tip = new Tooltip(fullscreen, {
                        title: (fullscreenOn ? the.options.tools.fullscreen.off : the.options.tools.fullscreen.on),
                        placement: placement,
                        offset: (fullscreenOn ? '0,10px,0,0' : '0,5px'),
                        trigger: 'hover',
                        template: '<div class="m-tooltip m-tooltip--portlet tooltip bs-tooltip-' + placement + '" role="tooltip">\
                            <div class="tooltip-arrow arrow"></div>\
                            <div class="tooltip-inner"></div>\
                        </div>'
                    });

                    mUtil.data(fullscreen).set('tooltip', tip);
                }
            }
        },

        /**
         * Setup tooltips
         */
        removeTooltips: function() {
            if (the.options.tooltips) {
                //== Remove
                var remove = mUtil.find(the.head, '[m-portlet-tool=remove]');
                if (remove && mUtil.data(remove).has('tooltip')) {
                    mUtil.data(remove).get('tooltip').dispose();
                }

                //== Reload
                var reload = mUtil.find(the.head, '[m-portlet-tool=reload]');
                if (reload && mUtil.data(reload).has('tooltip')) {
                    mUtil.data(reload).get('tooltip').dispose();
                }

                //== Toggle
                var toggle = mUtil.find(the.head, '[m-portlet-tool=toggle]');
                if (toggle && mUtil.data(toggle).has('tooltip')) {
                    mUtil.data(toggle).get('tooltip').dispose();
                }

                //== Fullscreen
                var fullscreen = mUtil.find(the.head, '[m-portlet-tool=fullscreen]');
                if (fullscreen && mUtil.data(fullscreen).has('tooltip')) {
                    mUtil.data(fullscreen).get('tooltip').dispose();
                }
            }
        },

        /**
         * Reload
         */
        reload: function() {
            Plugin.eventTrigger('reload');
        },

        /**
         * Toggle
         */
        toggle: function() {
            if (mUtil.hasClass(element, 'm-portlet--collapse') || mUtil.hasClass(element, 'm-portlet--collapsed')) {
                Plugin.expand();
            } else {
                Plugin.collapse();
            }
        },

        /**
         * Collapse
         */
        collapse: function() {
            if (Plugin.eventTrigger('beforeCollapse') === false) {
                return;
            }

            mUtil.slideUp(the.body, the.options.bodyToggleSpeed, function() {
                Plugin.eventTrigger('afterCollapse');
            });

            mUtil.addClass(element, 'm-portlet--collapse');

            var toggle = mUtil.find(the.head, '[m-portlet-tool=toggle]');
            if (toggle && mUtil.data(toggle).has('tooltip')) {
                mUtil.data(toggle).get('tooltip').updateTitleContent(the.options.tools.toggle.expand);
            }
        },

        /**
         * Expand
         */
        expand: function() {
            if (Plugin.eventTrigger('beforeExpand') === false) {
                return;
            }

            mUtil.slideDown(the.body, the.options.bodyToggleSpeed, function() {
                Plugin.eventTrigger('afterExpand');
            });

            mUtil.removeClass(element, 'm-portlet--collapse');
            mUtil.removeClass(element, 'm-portlet--collapsed');

            var toggle = mUtil.find(the.head, '[m-portlet-tool=toggle]');
            if (toggle && mUtil.data(toggle).has('tooltip')) {
                mUtil.data(toggle).get('tooltip').updateTitleContent(the.options.tools.toggle.collapse);
            }
        },

        /**
         * Toggle
         */
        fullscreen: function(mode) {
            var d = {};
            var speed = 300;

            if (mode === 'off' || (mUtil.hasClass(body, 'm-portlet--fullscreen') && mUtil.hasClass(element, 'm-portlet--fullscreen'))) {
                Plugin.eventTrigger('beforeFullscreenOff');

                mUtil.removeClass(body, 'm-portlet--fullscreen');
                mUtil.removeClass(element, 'm-portlet--fullscreen');

                Plugin.removeTooltips();
                Plugin.setupTooltips();

                if (the.foot) {
                    mUtil.css(the.body, 'margin-bottom', '');
                    mUtil.css(the.foot, 'margin-top', '');
                }

                Plugin.eventTrigger('afterFullscreenOff');
            } else {
                Plugin.eventTrigger('beforeFullscreenOn');

                mUtil.addClass(element, 'm-portlet--fullscreen');
                mUtil.addClass(body, 'm-portlet--fullscreen');

                Plugin.removeTooltips();
                Plugin.setupTooltips();


                if (the.foot) {
                    var height1 = parseInt(mUtil.css(the.foot, 'height'));
                    var height2 = parseInt(mUtil.css(the.foot, 'height')) + parseInt(mUtil.css(the.head, 'height'));
                    mUtil.css(the.body, 'margin-bottom', height1 + 'px');
                    mUtil.css(the.foot, 'margin-top', '-' + height2 + 'px');
                }

                Plugin.eventTrigger('afterFullscreenOn');
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
     * Remove portlet
     * @returns {mPortlet}
     */
    the.remove = function() {
        return Plugin.remove(html);
    };

    /**
     * Remove portlet
     * @returns {mPortlet}
     */
    the.initSticky = function() {
        return Plugin.initSticky();
    };

    /**
     * Remove portlet
     * @returns {mPortlet}
     */
    the.updateSticky = function() {
        return Plugin.updateSticky();
    };

    /**
     * Remove portlet
     * @returns {mPortlet}
     */
    the.resetSticky = function() {
        return Plugin.resetSticky();
    };

    /**
     * Reload portlet
     * @returns {mPortlet}
     */
    the.reload = function() {
        return Plugin.reload();
    };

    /**
     * Set portlet content
     * @returns {mPortlet}
     */
    the.setContent = function(html) {
        return Plugin.setContent(html);
    };

    /**
     * Toggle portlet
     * @returns {mPortlet}
     */
    the.toggle = function() {
        return Plugin.toggle();
    };

    /**
     * Collapse portlet
     * @returns {mPortlet}
     */
    the.collapse = function() {
        return Plugin.collapse();
    };

    /**
     * Expand portlet
     * @returns {mPortlet}
     */
    the.expand = function() {
        return Plugin.expand();
    };

    /**
     * Fullscreen portlet
     * @returns {mPortlet}
     */
    the.fullscreen = function() {
        return Plugin.fullscreen('on');
    };

    /**
     * Fullscreen portlet
     * @returns {mPortlet}
     */
    the.unFullscreen = function() {
        return Plugin.fullscreen('off');
    };

    /**
     * Get portletbody 
     * @returns {jQuery}
     */
    the.getBody = function() {
        return Plugin.getBody();
    };

    /**
     * Get portletbody 
     * @returns {jQuery}
     */
    the.getSelf = function() {
        return Plugin.getSelf();
    };

    /**
     * Attach event
     */
    the.on = function(name, handler) {
        return Plugin.addEvent(name, handler);
    };

    /**
     * Attach event that will be fired once
     */
    the.one = function(name, handler) {
        return Plugin.addEvent(name, handler, true);
    };

    //== Construct plugin
    Plugin.construct.apply(the, [options]);

    return the;
};