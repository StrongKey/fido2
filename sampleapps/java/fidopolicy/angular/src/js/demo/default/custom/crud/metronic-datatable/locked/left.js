//== Class definition

var DefaultDatatableDemo = function () {
	//== Private functions

	// basic demo
	var demo = function () {

		var datatable = $('.m_datatable').mDatatable({
			data: {
				type: 'remote',
				source: {
					read: {
						url: 'inc/api/datatables/demos/default.php'
					}
				},
				pageSize: 20,
				serverPaging: true,
				serverFiltering: true,
				serverSorting: true
			},

			layout: {
				theme: 'default',
				class: '',
				scroll: true,
				height: 550,
				footer: false
			},

			sortable: true,

			filterable: false,

			pagination: true,

			search: {
				input: $('#generalSearch')
			},

			columns: [{
				field: "RecordID",
				title: "#",
				locked: {left: 'xl'},
				sortable: false,	
				width: 40,
				selector: {class: 'm-checkbox--solid m-checkbox--brand'}
			}, {
				field: "Actions",
				width: 110,
				title: "Actions",
				sortable: false,
				locked: {left: 'xl'},
				overflow: 'visible',
				template: function (row, index, datatable) {
					var dropup = (datatable.getPageSize() - index) <= 4 ? 'dropup' : '';
					return '\
						<div class="dropdown '+ dropup +'">\
							<a href="#" class="btn m-btn m-btn--hover-accent m-btn--icon m-btn--icon-only m-btn--pill" data-toggle="dropdown">\
                                <i class="la la-ellipsis-h"></i>\
                            </a>\
						  	<div class="dropdown-menu dropdown-menu-left">\
						    	<a class="dropdown-item" href="#"><i class="la la-edit"></i> Edit Details</a>\
						    	<a class="dropdown-item" href="#"><i class="la la-leaf"></i> Update Status</a>\
						    	<a class="dropdown-item" href="#"><i class="la la-print"></i> Generate Report</a>\
						  	</div>\
						</div>\
						<a href="#" class="m-portlet__nav-link btn m-btn m-btn--hover-accent m-btn--icon m-btn--icon-only m-btn--pill" title="Edit details">\
							<i class="la la-edit"></i>\
						</a>\
						<a href="#" class="m-portlet__nav-link btn m-btn m-btn--hover-danger m-btn--icon m-btn--icon-only m-btn--pill" title="Delete">\
							<i class="la la-trash"></i>\
						</a>\
					';
				}
			}, {
				field: "ShipCountry",
				title: "Ship Country",
				width: 150,
				template: function (row) {
					return row.ShipCountry + ' - ' + row.ShipCity;
				}
			}, {
				field: "ShipCity",
				title: "Ship City",
				sortable: false
			}, {
				field: "ShipName",
				title: "Ship Name",
				width: 150,
				responsive: {visible: 'lg'}
			}, {
				field: "ShipAddress",
				title: "Ship Address",
				width: 200,
				responsive: {visible: 'lg'}
			}, {
				field: "CompanyEmail",
				title: "Email",
				width: 150,
				responsive: {visible: 'lg'}
			}, {
				field: "CompanyAgent",
				title: "Agent",
				responsive: {visible: 'lg'}
			}, {
				field: "Notes",
				title: "Notes",
				width: 350
			}, {
				field: "Website",
				title: "Website",
				width: 200
			}, {
				field: "Currency",
				title: "Currency",
				width: 100
			}, {
				field: "Department",
				title: "Department"
			}, {
				field: "ShipDate",
				title: "Ship Date"
			}, {
				field: "TimeZone",
				title: "Time Zone",
				width: 150
			}, {
				field: "Status",
				title: "Status",
				locked: {left: 'xl'},
				width: 100,
				// callback function support for column rendering
				template: function (row) {
					var status = {
						1: {'title': 'Pending', 'class': 'm-badge--brand'},
						2: {'title': 'Delivered', 'class': ' m-badge--metal'},
						3: {'title': 'Canceled', 'class': ' m-badge--primary'},
						4: {'title': 'Success', 'class': ' m-badge--success'},
						5: {'title': 'Info', 'class': ' m-badge--info'},
						6: {'title': 'Danger', 'class': ' m-badge--danger'},
						7: {'title': 'Warning', 'class': ' m-badge--warning'}
					};
					return '<span class="m-badge ' + status[row.Status].class + ' m-badge--wide">' + status[row.Status].title + '</span>';
				}
			}, {
				field: "Type",
				title: "Type",
				width: 150,
				// callback function support for column rendering
				template: function (row) {
					var status = {
						1: {'title': 'Online', 'state': 'danger'},
						2: {'title': 'Retail', 'state': 'primary'},
						3: {'title': 'Direct', 'state': 'accent'}
					};
					return '<span class="m-badge m-badge--' + status[row.Type].state + ' m-badge--dot"></span>&nbsp;<span class="m--font-bold m--font-' + status[row.Type].state +'">' + status[row.Type].title + '</span>';
				}
			}]
		});
	};

	return {
		// public functions
		init: function () {
			demo();
		}
	};
}();

jQuery(document).ready(function () {
	DefaultDatatableDemo.init();
});