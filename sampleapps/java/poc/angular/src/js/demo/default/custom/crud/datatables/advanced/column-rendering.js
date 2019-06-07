var DatatablesAdvancedColumnRendering = function() {

	var initTable1 = function() {
		var table = $('#m_table_1');

		// begin first table
		table.DataTable({
			responsive: true,
			paging: true,
			columnDefs: [
				{
					targets: 0,
					title: 'Agent',
					render: function(data, type, full, meta) {
						var number = mUtil.getRandomInt(1, 14);
						var user_img = '100_' + number + '.jpg';

						var output;
						if (number > 8) {
							output = `
                                <div class="m-card-user m-card-user--sm">
                                    <div class="m-card-user__pic">
                                        <img src="./assets/app/media/img/users/` + user_img + `" class="m--img-rounded m--marginless" alt="photo">
                                    </div>
                                    <div class="m-card-user__details">
                                        <span class="m-card-user__name">` + full[2] + `</span>
                                        <a href="" class="m-card-user__email m-link">` + full[3] + `</a>
                                    </div>
                                </div>`;
						}
						else {
							var stateNo = mUtil.getRandomInt(0, 7);
							var states = [
								'success',
								'brand',
								'danger',
								'accent',
								'warning',
								'metal',
								'primary',
								'info'];
							var state = states[stateNo];

							output = `
                                <div class="m-card-user m-card-user--sm">
                                    <div class="m-card-user__pic">
                                        <div class="m-card-user__no-photo m--bg-fill-` + state + `"><span>` + full[2].substring(0, 1) + `</span></div>
                                    </div>
                                    <div class="m-card-user__details">
                                        <span class="m-card-user__name">` + full[2] + `</span>
                                        <a href="" class="m-card-user__email m-link">` + full[3] + `</a>
                                    </div>
                                </div>`;
						}

						return output;
					},
				},
				{
					targets: 1,
					render: function(data, type, full, meta) {
						return '<a class="m-link" href="mailto:' + data + '">' + data + '</a>';
					},
				},
				{
					targets: -1,
					title: 'Actions',
					orderable: false,
					render: function(data, type, full, meta) {
						return `
                        <span class="dropdown">
                            <a href="#" class="btn m-btn m-btn--hover-brand m-btn--icon m-btn--icon-only m-btn--pill" data-toggle="dropdown" aria-expanded="true">
                              <i class="la la-ellipsis-h"></i>
                            </a>
                            <div class="dropdown-menu dropdown-menu-right">
                                <a class="dropdown-item" href="#"><i class="la la-edit"></i> Edit Details</a>
                                <a class="dropdown-item" href="#"><i class="la la-leaf"></i> Update Status</a>
                                <a class="dropdown-item" href="#"><i class="la la-print"></i> Generate Report</a>
                            </div>
                        </span>
                        <a href="#" class="m-portlet__nav-link btn m-btn m-btn--hover-brand m-btn--icon m-btn--icon-only m-btn--pill" title="View">
                          <i class="la la-edit"></i>
                        </a>`;
					},
				},
				{
					targets: 4,
					render: function(data, type, full, meta) {
						var status = {
							1: {'title': 'Pending', 'class': 'm-badge--brand'},
							2: {'title': 'Delivered', 'class': ' m-badge--metal'},
							3: {'title': 'Canceled', 'class': ' m-badge--primary'},
							4: {'title': 'Success', 'class': ' m-badge--success'},
							5: {'title': 'Info', 'class': ' m-badge--info'},
							6: {'title': 'Danger', 'class': ' m-badge--danger'},
							7: {'title': 'Warning', 'class': ' m-badge--warning'},
						};
						if (typeof status[data] === 'undefined') {
							return data;
						}
						return '<span class="m-badge ' + status[data].class + ' m-badge--wide">' + status[data].title + '</span>';
					},
				},
				{
					targets: 5,
					render: function(data, type, full, meta) {
						var status = {
							1: {'title': 'Online', 'state': 'danger'},
							2: {'title': 'Retail', 'state': 'primary'},
							3: {'title': 'Direct', 'state': 'accent'},
						};
						if (typeof status[data] === 'undefined') {
							return data;
						}
						return '<span class="m-badge m-badge--' + status[data].state + ' m-badge--dot"></span>&nbsp;' +
							'<span class="m--font-bold m--font-' + status[data].state + '">' + status[data].title + '</span>';
					},
				},
			],
		});
	};

	return {

		//main function to initiate the module
		init: function() {
			initTable1();
		},

	};

}();

jQuery(document).ready(function() {
	DatatablesAdvancedColumnRendering.init();
});