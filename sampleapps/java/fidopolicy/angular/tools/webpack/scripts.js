"use strict";

/**
 * Define the output of this file. The output of CSS and JS file will be auto detected.
 *
 * @output js/scripts.bundle
 */

// Core Plugins
window.KTUtil = require("../../src/assets/js/global/components/base/util");
window.KTApp = require("../../src/assets/js/global/components/base/app");
window.KTAvatar = require("../../src/assets/js/global/components/base/avatar");
window.KTDialog = require("../../src/assets/js/global/components/base/dialog");
window.KTHeader = require("../../src/assets/js/global/components/base/header");
window.KTMenu = require("../../src/assets/js/global/components/base/menu");
window.KTOffcanvas = require("../../src/assets/js/global/components/base/offcanvas");
window.KTPortlet = require("../../src/assets/js/global/components/base/portlet");
window.KTScrolltop = require("../../src/assets/js/global/components/base/scrolltop");
window.KTToggle = require("../../src/assets/js/global/components/base/toggle");
window.KTWizard = require("../../src/assets/js/global/components/base/wizard");
require("../../src/assets/js/global/components/base/datatable/core.datatable");
require("../../src/assets/js/global/components/base/datatable/datatable.checkbox");
require("../../src/assets/js/global/components/base/datatable/datatable.rtl");

// Layout Scripts
window.KTLayout = require("../../src/assets/js/global/layout/layout");
window.KTChat = require("../../src/assets/js/global/layout/chat");
require("../../src/assets/js/global/layout/demo-panel");
require("../../src/assets/js/global/layout/offcanvas-panel");
require("../../src/assets/js/global/layout/quick-panel");
require("../../src/assets/js/global/layout/quick-search");
