"use strict";

/**
 * Define the output of this file. The output of CSS and JS file will be auto detected.
 *
 * @output plugins/custom/fullcalendar/fullcalendar.bundle
 */

// Fullcalendar
require("@fullcalendar/core/main.css");
require("@fullcalendar/daygrid/main.css");
require("@fullcalendar/list/main.css");
require("@fullcalendar/timegrid/main.css");

window.FullCalendar = require("@fullcalendar/core");
window.FullCalendarDayGrid = require("@fullcalendar/daygrid");
window.FullCalendarGoogleCalendar = require("@fullcalendar/google-calendar");
window.FullCalendarInteraction = require("@fullcalendar/interaction");
window.FullCalendarList = require("@fullcalendar/list");
window.FullCalendarTimeGrid = require("@fullcalendar/timegrid");
