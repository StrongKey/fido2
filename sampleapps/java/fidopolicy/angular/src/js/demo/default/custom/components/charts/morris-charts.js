//== Class definition
var MorrisChartsDemo = function() {

    //== Private functions
    
    var demo1 = function() {
        // LINE CHART
        new Morris.Line({
            // ID of the element in which to draw the chart.
            element: 'm_morris_1',
            // Chart data records -- each entry in this array corresponds to a point on
            // the chart.
            data: [{
                    y: '2006',
                    a: 100,
                    b: 90
                },
                {
                    y: '2007',
                    a: 75,
                    b: 65
                },
                {
                    y: '2008',
                    a: 50,
                    b: 40
                },
                {
                    y: '2009',
                    a: 75,
                    b: 65
                },
                {
                    y: '2010',
                    a: 50,
                    b: 40
                },
                {
                    y: '2011',
                    a: 75,
                    b: 65
                },
                {
                    y: '2012',
                    a: 100,
                    b: 90
                }
            ],
            // The name of the data record attribute that contains x-values.
            xkey: 'y',
            // A list of names of data record attributes that contain y-values.
            ykeys: ['a', 'b'],
            // Labels for the ykeys -- will be displayed when you hover over the
            // chart.
            labels: ['Values A', 'Values B']
        });
    }

    var demo2 = function() {
        // AREA CHART
        new Morris.Area({
            element: 'm_morris_2',
            data: [{
                    y: '2006',
                    a: 100,
                    b: 90
                },
                {
                    y: '2007',
                    a: 75,
                    b: 65
                },
                {
                    y: '2008',
                    a: 50,
                    b: 40
                },
                {
                    y: '2009',
                    a: 75,
                    b: 65
                },
                {
                    y: '2010',
                    a: 50,
                    b: 40
                },
                {
                    y: '2011',
                    a: 75,
                    b: 65
                },
                {
                    y: '2012',
                    a: 100,
                    b: 90
                }
            ],
            xkey: 'y',
            ykeys: ['a', 'b'],
            labels: ['Series A', 'Series B']
        });
    }

    var demo3 = function() {
        // BAR CHART
        new Morris.Bar({
            element: 'm_morris_3',
            data: [{
                    y: '2006',
                    a: 100,
                    b: 90
                },
                {
                    y: '2007',
                    a: 75,
                    b: 65
                },
                {
                    y: '2008',
                    a: 50,
                    b: 40
                },
                {
                    y: '2009',
                    a: 75,
                    b: 65
                },
                {
                    y: '2010',
                    a: 50,
                    b: 40
                },
                {
                    y: '2011',
                    a: 75,
                    b: 65
                },
                {
                    y: '2012',
                    a: 100,
                    b: 90
                }
            ],
            xkey: 'y',
            ykeys: ['a', 'b'],
            labels: ['Series A', 'Series B']
        });
    }

    var demo4 = function() {
        // PIE CHART
        new Morris.Donut({
            element: 'm_morris_4',
            data: [{
                    label: "Download Sales",
                    value: 12
                },
                {
                    label: "In-Store Sales",
                    value: 30
                },
                {
                    label: "Mail-Order Sales",
                    value: 20
                }
            ]
        });
    }

    return {
        // public functions
        init: function() {
            demo1();
            demo2();
            demo3();
            demo4();
        }
    };
}();

jQuery(document).ready(function() {
    MorrisChartsDemo.init();
});