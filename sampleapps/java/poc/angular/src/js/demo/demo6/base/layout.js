var mLayout = function() {
    var header;
    var horMenu;
    var asideMenu;
    var asideMenuOffcanvas;
    var horMenuOffcanvas;
    var asideLeftToggle;
    var scrollTop;
    var quicksearch;

    //== Header
    var initStickyHeader = function() {
        var tmp;
        var headerEl = mUtil.get('m_header');
        var options = {
            offset: {},
            minimize:{}
        };

        if (mUtil.attr(headerEl, 'm-minimize-mobile') == 'minimize') {
            options.minimize.mobile = {};
            options.minimize.mobile.on = 'm-header--minimize-on';
            options.minimize.mobile.off = 'm-header--minimize-off';
        } else {
            options.minimize.mobile = false;
        }

        if (mUtil.attr(headerEl, 'm-minimize') == 'minimize') {
            options.minimize.desktop = {};
            options.minimize.desktop.on = 'm-header--minimize-on';
            options.minimize.desktop.off = 'm-header--minimize-off';
        } else {
            options.minimize.desktop = false;
        }

        if (tmp = mUtil.attr(headerEl, 'm-minimize-offset')) {
            options.offset.desktop = tmp;
        }

        if (tmp = mUtil.attr(headerEl, 'm-minimize-mobile-offset')) {
            options.offset.mobile = tmp;
        }

        header = new mHeader('m_header', options);
    };

    //== Hor menu
    var initHorMenu = function() {
        // init aside left offcanvas
        horMenuOffcanvas = new mOffcanvas('m_header_menu', {
            overlay: true,
            baseClass: 'm-aside-header-menu-mobile',
            closeBy: 'm_aside_header_menu_mobile_close_btn',
            toggleBy: {
                target: 'm_aside_header_menu_mobile_toggle',
                state: 'm-brand__toggler--active'
            }
        });

        horMenu = new mMenu('m_header_menu', {
            submenu: {
                desktop: 'dropdown',
                tablet: 'accordion',
                mobile: 'accordion'
            },
            accordion: {
                slideSpeed: 200,  // accordion toggle slide speed in milliseconds
                expandAll: false   // allow having multiple expanded accordions in the menu
            }
        });
    }

    //== Aside menu
    var initLeftAsideMenu = function() {
        //== Init aside menu
        var menu = mUtil.get('m_ver_menu');
        var menuDesktopMode = (mUtil.attr(menu, 'm-menu-dropdown') === '1' ? 'dropdown' : 'accordion');

        var scroll;
        if ( mUtil.attr(menu, 'm-menu-scrollable') === '1' ) {
            scroll = {
                height: function() {
                    return mUtil.getViewPort().height - parseInt(mUtil.css('m_header', 'height'));
                }
            };
        }

        asideMenu = new mMenu('m_ver_menu', {
            // vertical scroll
            scroll: scroll,

            // submenu setup
            submenu: {
                desktop: {
                    // by default the menu mode set to accordion in desktop mode
                    default: menuDesktopMode,
                    // whenever body has this class switch the menu mode to dropdown
                    state: {
                        body: 'm-aside-left--minimize',
                        mode: 'dropdown'
                    }
                },
                tablet: 'accordion', // menu set to accordion in tablet mode
                mobile: 'accordion'  // menu set to accordion in mobile mode
            },

            //accordion setup
            accordion: {
                autoScroll: false, // enable auto scrolling(focus) to the clicked menu item
                expandAll: true   // allow having multiple expanded accordions in the menu
            }
        });
    }

    //== Aside
    var initLeftAside = function() {
        // init aside left offcanvas
        var body = mUtil.get('body');
        var asideLeft = mUtil.get('m_aside_left');
        var asideOffcanvasClass = mUtil.hasClass(asideLeft, 'm-aside-left--offcanvas-default') ? 'm-aside-left--offcanvas-default' : 'm-aside-left';

        asideMenuOffcanvas = new mOffcanvas('m_aside_left', {
            baseClass: asideOffcanvasClass,
            overlay: true,
            closeBy: 'm_aside_left_close_btn',
            toggleBy: {
                target: 'm_aside_left_offcanvas_toggle',
                state: 'm-brand__toggler--active'
            }
        });

        //== Handle minimzied aside hover
        if (mUtil.hasClass(body, 'm-aside-left--fixed')) {
            var insideTm;
            var outsideTm;

            if (mUtil.hasClass(body, 'm-aside-left--minimize') && mUtil.isInResponsiveRange('desktop')) {
                mUtil.removeClass(body, 'm-aside-left--minimize');
                // mUtil.addClass(body, 'm-aside-left--minimize-hover');
                // asideMenu.scrollerUpdate();
                // asideMenu.scrollerTop();
            }

            // mUtil.addEvent(asideLeft, 'mouseenter', function() {
            //     if (outsideTm) {
            //         clearTimeout(outsideTm);
            //         outsideTm = null;
            //     }
            //
            //     insideTm = setTimeout(function() {
            //         if (mUtil.hasClass(body, 'm-aside-left--minimize') && mUtil.isInResponsiveRange('desktop')) {
            //             mUtil.removeClass(body, 'm-aside-left--minimize');
            //             mUtil.addClass(body, 'm-aside-left--minimize-hover');
            //             asideMenu.scrollerUpdate();
            //             asideMenu.scrollerTop();
            //         }
            //     }, 300);
            // });

            // mUtil.addEvent(asideLeft, 'mouseleave', function() {
            //     if (insideTm) {
            //         clearTimeout(insideTm);
            //         insideTm = null;
            //     }
            //
            //     outsideTm = setTimeout(function() {
            //         if (mUtil.hasClass(body, 'm-aside-left--minimize-hover') && mUtil.isInResponsiveRange('desktop')) {
            //             mUtil.removeClass(body, 'm-aside-left--minimize-hover');
            //             mUtil.addClass(body, 'm-aside-left--minimize');
            //             asideMenu.scrollerUpdate();
            //             asideMenu.scrollerTop();
            //         }
            //     }, 500);
            // });
        }
    }

    //== Sidebar toggle
    var initLeftAsideToggle = function() {
        if ($('#m_aside_left_minimize_toggle').length === 0 ) {
            return;
        }

        asideLeftToggle = new mToggle('m_aside_left_minimize_toggle', {
            target: 'body',
            targetState: 'm-brand--minimize m-aside-left--minimize',
            togglerState: 'm-brand__toggler--active'
        });

        asideLeftToggle.on('toggle', function(toggle) {
            horMenu.pauseDropdownHover(800);
            asideMenu.pauseDropdownHover(800);

            //== Remember state in cookie
            Cookies.set('sidebar_toggle_state', toggle.getState());
            // to set default minimized left aside use this cookie value in your
            // server side code and add "m-brand--minimize m-aside-left--minimize" classes to
            // the body tag in order to initialize the minimized left aside mode during page loading.
        });
    }

    //== Topbar
    var initTopbar = function() {
        $('#m_aside_header_topbar_mobile_toggle').click(function() {
            $('body').toggleClass('m-topbar--on');
        });
    }

    //== Quicksearch
    var initQuicksearch = function() {
        if ($('#m_quicksearch').length === 0 ) {
            return;
        }

        quicksearch = new mQuicksearch('m_quicksearch', {
            mode: mUtil.attr( 'm_quicksearch', 'm-quicksearch-mode' ), // quick search type
            minLength: 1
        });

        //<div class="m-search-results m-search-results--skin-light"><span class="m-search-result__message">Something went wrong</div></div>

        quicksearch.on('search', function(the) {
            the.showProgress();

            $.ajax({
                url: 'inc/api/quick_search.php',
                data: {query: the.query},
                dataType: 'html',
                success: function(res) {
                    the.hideProgress();
                    the.showResult(res);
                },
                error: function(res) {
                    the.hideProgress();
                    the.showError('Connection error. Pleae try again later.');
                }
            });
        });
    }

    //== Scrolltop
    var initScrollTop = function() {
        var scrollTop = new mScrollTop('m_scroll_top', {
            offset: 300,
            speed: 600
        });
    }

    return {
        init: function() {
            this.initHeader();
            this.initAside();
        },

        initHeader: function() {
            initStickyHeader();
            initHorMenu();
            initTopbar();
            initQuicksearch();
            initScrollTop();
        },

        initAside: function() {
            initLeftAside();
            initLeftAsideMenu();
            initLeftAsideToggle();

            this.onLeftSidebarToggle(function(e) {
              var datatables = $('.m-datatable');

                $(datatables).each(function() {
                    $(this).mDatatable('redraw');
                });
            });
        },

        getAsideMenu: function() {
            return asideMenu;
        },

        onLeftSidebarToggle: function(func) {
            if (asideLeftToggle) {
                asideLeftToggle.on('toggle', func);
            }
        },

        closeMobileAsideMenuOffcanvas: function() {
            // if (mUtil.isMobileDevice()) {
            //     asideMenuOffcanvas.hide();
            // }
        },

        closeMobileHorMenuOffcanvas: function() {
            if (mUtil.isMobileDevice()) {
                horMenuOffcanvas.hide();
            }
        }
    };
}();

$(document).ready(function() {
    if (mUtil.isAngularVersion() === false) {
        mLayout.init();
    }
});
