// plugin setup
var mWizard = function(elementId, options) {
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
        startStep: 1,
        manualStepForward: false
    };

    ////////////////////////////
    // ** Private Methods  ** //
    ////////////////////////////

    var Plugin = {
        /**
         * Construct
         */

        construct: function(options) {
            if (mUtil.data(element).has('wizard')) {
                the = mUtil.data(element).get('wizard');
            } else {
                // reset menu
                Plugin.init(options);

                // build menu
                Plugin.build();

                mUtil.data(element).set('wizard', the);
            }

            return the;
        },

        /**
         * Init wizard
         */
        init: function(options) {
            the.element = element;
            the.events = [];

            // merge default and user defined options
            the.options = mUtil.deepExtend({}, defaultOptions, options);

            //== Elements
            the.steps = mUtil.findAll(element, '.m-wizard__step');

            the.progress = mUtil.find(element, '.m-wizard__progress .progress-bar');
            the.btnSubmit = mUtil.find(element, '[data-wizard-action="submit"]');
            the.btnNext = mUtil.find(element, '[data-wizard-action="next"]');
            the.btnPrev = mUtil.find(element, '[data-wizard-action="prev"]');
            the.btnLast = mUtil.find(element, '[data-wizard-action="last"]');
            the.btnFirst = mUtil.find(element, '[data-wizard-action="first"]');

            //== Variables
            the.events = [];
            the.currentStep = 1;
            the.stop = false;
            the.totalSteps = the.steps.length;

            //== Init current step
            if (the.options.startStep > 1) {
                Plugin.goTo(the.options.startStep);
            }

            //== Init UI
            Plugin.updateUI();
        },

        /**
         * Build Form Wizard
         */
        build: function() {
            //== Next button event handler
            mUtil.addEvent(the.btnNext, 'click', function(e) {
                e.preventDefault();
                Plugin.goNext();
            });

            //== Prev button event handler
            mUtil.addEvent(the.btnPrev, 'click', function(e) {
                e.preventDefault();
                Plugin.goPrev();
            });

            //== First button event handler
            mUtil.addEvent(the.btnFirst, 'click', function(e) {
                e.preventDefault();
                Plugin.goFirst();
            });

            //== Last button event handler
            mUtil.addEvent(the.btnLast, 'click', function(e) {
                e.preventDefault();
                Plugin.goLast();
            });

            mUtil.on(element, '.m-wizard__step a.m-wizard__step-number', 'click', function() {
                var step = this.closest('.m-wizard__step');
                var steps = mUtil.parents(this, '.m-wizard__steps')
                var find = mUtil.findAll(steps, '.m-wizard__step');
                var num;

                for (var i = 0, j = find.length; i < j; i++) {
                    if (step === find[i]) {
                        num = (i + 1);
                        break;
                    }
                }

                if (num) {
                    if (the.options.manualStepForward === false) {
                        if (num < the.currentStep) {
                            Plugin.goTo(num);
                        }
                    } else {
                        Plugin.goTo(num);
                    }                    
                }
            });
        },

        /**
         * Handles wizard click wizard
         */
        goTo: function(number) {
            //== Skip if this step is already shown
            if (number === the.currentStep || number > the.totalSteps || number < 0) {
                return;
            }

            //== Validate step number
            if (number) {
                number = parseInt(number);
            } else {
                number = Plugin.getNextStep();
            }

            //== Before next and prev events
            var callback;

            if (number > the.currentStep) {
                callback = Plugin.eventTrigger('beforeNext');
            } else {
                callback = Plugin.eventTrigger('beforePrev');
            }
            
            //== Skip if stopped
            if (the.stop === true) {
                the.stop = false;
                return;
            }

            //== Continue if no exit
            if (callback !== false) {
                //== Before change
                Plugin.eventTrigger('beforeChange');

                //== Set current step 
                the.currentStep = number;

                //== Update UI
                Plugin.updateUI();

                //== Trigger change event
                Plugin.eventTrigger('change');
            }

            //== After next and prev events
            if (number > the.startStep) {
                Plugin.eventTrigger('afterNext');
            } else {
                Plugin.eventTrigger('afterPrev');
            }

            return the;
        },

        /**
         * Set step class
         */
        setStepClass: function() {
            if (Plugin.isLastStep()) {
                mUtil.addClass(element, 'm-wizard--step-last');
            } else {
                mUtil.removeClass(element, 'm-wizard--step-last');
            }

            if (Plugin.isFirstStep()) {
                mUtil.addClass(element, 'm-wizard--step-first');
            } else {
                mUtil.removeClass(element, 'm-wizard--step-first');
            }

            if (Plugin.isBetweenStep()) {
                mUtil.addClass(element, 'm-wizard--step-between');
            } else {
                mUtil.removeClass(element, 'm-wizard--step-between');
            }
        },

        updateUI: function(argument) {
            //== Update progress bar
            Plugin.updateProgress();

            //== Show current target content
            Plugin.handleTarget();

            //== Set classes
            Plugin.setStepClass();

            //== Apply nav step classes
            for (var i = 0, j = the.steps.length; i < j; i++) {
                mUtil.removeClass(the.steps[i], 'm-wizard__step--current m-wizard__step--done');
            }

            for (var i = 1; i < the.currentStep; i++) {
                mUtil.addClass(the.steps[i - 1], 'm-wizard__step--done');
            }
            
            mUtil.addClass(the.steps[the.currentStep - 1], 'm-wizard__step--current');
        },

        /**
         * Cancel
         */
        stop: function() {
            the.stop = true;
        },

        /**
         * Resume
         */
        start: function() {
            the.stop = false;
        },

        /**
         * Check last step
         */
        isLastStep: function() {
            return the.currentStep === the.totalSteps;
        },

        /**
         * Check first step
         */
        isFirstStep: function() {
            return the.currentStep === 1;
        },

        /**
         * Check between step
         */
        isBetweenStep: function() {
            return Plugin.isLastStep() === false && Plugin.isFirstStep() === false;
        },

        /**
         * Go to the next step
         */
        goNext: function() {
            return Plugin.goTo(Plugin.getNextStep());
        },

        /**
         * Go to the prev step
         */
        goPrev: function() {
            return Plugin.goTo(Plugin.getPrevStep());
        },

        /**
         * Go to the last step
         */
        goLast: function() {
            return Plugin.goTo(the.totalSteps);
        },

        /**
         * Go to the first step
         */
        goFirst: function() {
            return Plugin.goTo(1);
        },

        /**
         * Set progress
         */
        updateProgress: function() {
            //== Calculate progress position
            if (!the.progress) {
                return;
            }

            //== Update progress
            if (mUtil.hasClass(element, 'm-wizard--1')) {
                var width = 100 * ((the.currentStep) / (the.totalSteps));
                var number = mUtil.find(element, '.m-wizard__step-number');
                var offset = parseInt(mUtil.css(number, 'width'));
                mUtil.css(the.progress, 'width', 'calc(' + width + '% + ' + (offset / 2) + 'px)');
            } else if (mUtil.hasClass(element, 'm-wizard--2')) {
                if (the.currentStep === 1) {
                    //return;
                }

                var progress = (the.currentStep - 1) * (100 * (1 / (the.totalSteps - 1)));

                if (mUtil.isInResponsiveRange('minimal-desktop-and-below')) {
                    mUtil.css(the.progress, 'height', progress + '%');
                } else {
                    mUtil.css(the.progress, 'width', progress + '%');
                }
            } else {
                var width = 100 * ((the.currentStep) / (the.totalSteps));
                mUtil.css(the.progress, 'width', width + '%');
            }
        },

        /**
         * Show/hide target content
         */
        handleTarget: function() {
            var step = the.steps[the.currentStep - 1];
            var target = mUtil.get(mUtil.attr(step, 'm-wizard-target'));
            var current = mUtil.find(element, '.m-wizard__form-step--current');
            
            mUtil.removeClass(current, 'm-wizard__form-step--current');
            mUtil.addClass(target, 'm-wizard__form-step--current');
        },

        /**
         * Get next step
         */
        getNextStep: function() {
            if (the.totalSteps >= (the.currentStep + 1)) {
                return the.currentStep + 1;
            } else {
                return the.totalSteps;
            }
        },

        /**
         * Get prev step
         */
        getPrevStep: function() {
            if ((the.currentStep - 1) >= 1) {
                return the.currentStep - 1;
            } else {
                return 1;
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
     * Go to the next step 
     */
    the.goNext = function() {
        return Plugin.goNext();
    };

    /**
     * Go to the prev step 
     */
    the.goPrev = function() {
        return Plugin.goPrev();
    };

    /**
     * Go to the last step 
     */
    the.goLast = function() {
        return Plugin.goLast();
    };

    /**
     * Cancel step 
     */
    the.stop = function() {
        return Plugin.stop();
    };

    /**
     * Resume step 
     */
    the.start = function() {
        return Plugin.start();
    };

    /**
     * Go to the first step 
     */
    the.goFirst = function() {
        return Plugin.goFirst();
    };

    /**
     * Go to a step
     */
    the.goTo = function(number) {
        return Plugin.goTo(number);
    };

    /**
     * Get current step number 
     */
    the.getStep = function() {
        return the.currentStep;
    };

    /**
     * Check last step 
     */
    the.isLastStep = function() {
        return Plugin.isLastStep();
    };

    /**
     * Check first step 
     */
    the.isFirstStep = function() {
        return Plugin.isFirstStep();
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