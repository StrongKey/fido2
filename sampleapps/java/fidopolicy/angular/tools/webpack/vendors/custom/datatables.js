"use strict";

/**
 * Define the output of this file. The output of CSS and JS file will be auto detected.
 *
 * @output plugins/custom/datatables/datatables.bundle
 */

// Datatables.net
require("datatables.net")(window, window.$);
require("datatables.net-bs4")(window, window.$);
require("datatables.net-autofill")(window, window.$);
require("datatables.net-autofill-bs4")(window, window.$);
require("datatables.net-buttons")(window, window.$);
require("datatables.net-buttons-bs4")(window, window.$);
require("datatables.net-buttons/js/buttons.print.js")(window, window.$);
require("datatables.net-buttons/js/buttons.html5.js")(window, window.$);
require("datatables.net-buttons/js/buttons.flash.js")(window, window.$);
require("datatables.net-buttons/js/buttons.colVis.js")(window, window.$);
require("datatables.net-colreorder-bs4")(window, window.$);
require("datatables.net-fixedcolumns-bs4")(window, window.$);
require("datatables.net-fixedheader-bs4")(window, window.$);
require("datatables.net-keytable-bs4")(window, window.$);
require("datatables.net-responsive-bs4")(window, window.$);
require("datatables.net-rowgroup-bs4")(window, window.$);
require("datatables.net-rowreorder-bs4")(window, window.$);
require("datatables.net-scroller-bs4")(window, window.$);
require("datatables.net-select-bs4")(window, window.$);
require("../../../../src/assets/js/global/integration/plugins/datatables.init.js");

window.JSZip = require("jszip/dist/jszip.js");
var pdfMake = require("pdfmake/build/pdfmake.js");
var pdfFonts = require("pdfmake/build/vfs_fonts.js");
pdfMake.vfs = pdfFonts.pdfMake.vfs;

require("datatables.net-bs4/css/dataTables.bootstrap4.css");
require("datatables.net-buttons-bs4/css/buttons.bootstrap4.min.css");
require("datatables.net-autofill-bs4/css/autoFill.bootstrap4.min.css");
require("datatables.net-colreorder-bs4/css/colReorder.bootstrap4.min.css");
require("datatables.net-fixedcolumns-bs4/css/fixedColumns.bootstrap4.min.css");
require("datatables.net-fixedheader-bs4/css/fixedHeader.bootstrap4.min.css");
require("datatables.net-keytable-bs4/css/keyTable.bootstrap4.min.css");
require("datatables.net-responsive-bs4/css/responsive.bootstrap4.min.css");
require("datatables.net-rowgroup-bs4/css/rowGroup.bootstrap4.min.css");
require("datatables.net-rowreorder-bs4/css/rowReorder.bootstrap4.min.css");
require("datatables.net-scroller-bs4/css/scroller.bootstrap4.min.css");
require("datatables.net-select-bs4/css/select.bootstrap4.min.css");
