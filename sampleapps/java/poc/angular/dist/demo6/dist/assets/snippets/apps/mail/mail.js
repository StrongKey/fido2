var mMail = function() {
    var asideOffcanvas;
    var asideMenu;

    //== Aside
    var initAside = function() {
        //== Init offcanvas aside for mobile mode    
        asideOffcanvas = new mOffcanvas('m_mail_aside', {
            overlay: true,
            baseClass: 'm-mail__aside',
            closeBy: 'm_mail_aside_close_btn',
            toggleBy: {
                target: 'm_mail_aside_toggle_btn',
                state: 'm-mail-aside-toggle--active'
            }               
        });        
    }

    //== Aside Menu
    var initAsideMenu = function() {
        asideMenu = new mMenu('m_mail_aside_menu', {
            submenu: {
                desktop: 'dropdown',
                tablet: 'accordion',
                mobile: 'accordion'
            },
            accordion: {   
                slideSpeed: 200,  // accordion toggle slide speed in milliseconds
                autoScroll: true, // enable auto scrolling(focus) to the clicked menu item
                expandAll: false   // allow having multiple expanded accordions in the menu
            }
        });
    }

    return {
        init: function() {  
            //== Init components
            initAside();
            initAsideMenu();
        }
    };
}();

$(document).ready(function() {
    if (mUtil.isAngularVersion() === false) {
        mMail.init();
    }
});
