// plugin setup
var mToggle = function(elementId, options) {
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
        togglerState: '',
        targetState: ''
    };    

    ////////////////////////////
    // ** Private Methods  ** //
    ////////////////////////////

    var Plugin = {
        /**
         * Construct
         */

        construct: function(options) {
            if (mUtil.data(element).has('toggle')) {
                the = mUtil.data(element).get('toggle');
            } else {
                // reset menu
                Plugin.init(options);

                // build menu
                Plugin.build();

                mUtil.data(element).set('toggle', the);
            }

            return the;
        },

        /**
         * Handles subtoggle click toggle
         */
        init: function(options) {
            the.element = element;
            the.events = [];

            // merge default and user defined options
            the.options = mUtil.deepExtend({}, defaultOptions, options);

            the.target = mUtil.get(the.options.target);
            the.targetState = the.options.targetState;
            the.togglerState = the.options.togglerState;

            the.state = mUtil.hasClasses(the.target, the.targetState) ? 'on' : 'off';
        },

        /**
         * Setup toggle
         */
        build: function() {
            mUtil.addEvent(element, 'mouseup', Plugin.toggle);
        },
        
        /**
         * Handles offcanvas click toggle
         */
        toggle: function() {
            if (the.state == 'off') {
                Plugin.toggleOn();
            } else {
                Plugin.toggleOff();
            }

            return the;
        },

        /**
         * Handles toggle click toggle
         */
        toggleOn: function() {
            Plugin.eventTrigger('beforeOn');

            mUtil.addClass(the.target, the.targetState);

            if (the.togglerState) {
                mUtil.addClass(element, the.togglerState);
            }

            the.state = 'on';

            Plugin.eventTrigger('afterOn');

            Plugin.eventTrigger('toggle');

            return the;
        },

        /**
         * Handles toggle click toggle
         */
        toggleOff: function() {
            Plugin.eventTrigger('beforeOff');

            mUtil.removeClass(the.target, the.targetState);

            if (the.togglerState) {
                mUtil.removeClass(element, the.togglerState);
            }

            the.state = 'off';

            Plugin.eventTrigger('afterOff');

            Plugin.eventTrigger('toggle');

            return the;
        },

        /**
         * Trigger events
         */
        eventTrigger: function(name) {
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
     * Get toggle state 
     */
    the.getState = function() {
        return the.state;
    };

    /**
     * Toggle 
     */
    the.toggle = function() {
        return Plugin.toggle();
    };

    /**
     * Toggle on 
     */
    the.toggleOn = function() {
        return Plugin.toggleOn();
    };

    /**
     * Toggle off 
     */
    the.toggle = function() {
        return Plugin.toggleOff();
    };

    /**
     * Attach event
     * @returns {mToggle}
     */
    the.on = function(name, handler) {
        return Plugin.addEvent(name, handler);
    };

    /**
     * Attach event that will be fired once
     * @returns {mToggle}
     */
    the.one = function(name, handler) {
        return Plugin.addEvent(name, handler, true);
    };

    //== Construct plugin
    Plugin.construct.apply(the, [options]);

    return the;
};