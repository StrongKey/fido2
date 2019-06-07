var mQuickSidebar = function() {
    var topbarAside = $('#m_quick_sidebar');
    var topbarAsideTabs = $('#m_quick_sidebar_tabs');    
    var topbarAsideContent = topbarAside.find('.m-quick-sidebar__content');

    var initMessages = function() {
        var messages = mUtil.find( mUtil.get('m_quick_sidebar_tabs_messenger'),  '.m-messenger__messages'); 
        var form = $('#m_quick_sidebar_tabs_messenger .m-messenger__form');

        mUtil.scrollerInit(messages, {
            disableForMobile: true, 
            resetHeightOnDestroy: false, 
            handleWindowResize: true, 
            height: function() {
                var height = topbarAside.outerHeight(true) - 
                    topbarAsideTabs.outerHeight(true) - 
                    form.outerHeight(true) - 120;

                return height;                    
            }
        });
    }

    var initSettings = function() { 
        var settings = mUtil.find( mUtil.get('m_quick_sidebar_tabs_settings'),  '.m-list-settings'); 

        if (!settings) {
            return;
        }

        mUtil.scrollerInit(settings, {
            disableForMobile: true, 
            resetHeightOnDestroy: false, 
            handleWindowResize: true, 
            height: function() {
                return mUtil.getViewPort().height - topbarAsideTabs.outerHeight(true) - 60;            
            }
        });
    }

    var initLogs = function() {
        var logs = mUtil.find( mUtil.get('m_quick_sidebar_tabs_logs'),  '.m-list-timeline'); 

        if (!logs) {
            return;
        }

        mUtil.scrollerInit(logs, {
            disableForMobile: true, 
            resetHeightOnDestroy: false, 
            handleWindowResize: true, 
            height: function() {
                return mUtil.getViewPort().height - topbarAsideTabs.outerHeight(true) - 60;            
            }
        });
    }

    var initOffcanvasTabs = function() {
        initMessages();
        initSettings();
        initLogs();
    }

    var initOffcanvas = function() {
        var topbarAsideObj = new mOffcanvas('m_quick_sidebar', {
            overlay: true,  
            baseClass: 'm-quick-sidebar',
            closeBy: 'm_quick_sidebar_close',
            toggleBy: 'm_quick_sidebar_toggle'
        });   

        // run once on first time dropdown shown
        topbarAsideObj.one('afterShow', function() {
            mApp.block(topbarAside);

            setTimeout(function() {
                mApp.unblock(topbarAside);
                
                topbarAsideContent.removeClass('m--hide');

                initOffcanvasTabs();
            }, 1000);                         
        });
    }

    return {     
        init: function() {  
            if (topbarAside.length === 0) {
                return;
            }

            initOffcanvas(); 
        }
    };
}();

$(document).ready(function() {
    mQuickSidebar.init();
});