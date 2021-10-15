$(document).ready(function() {

  // var $body = $('body');
  // var isActive = $(".hamburger").hasClass("toggler_active");
  // var $sidebar = $body.find('.sidebar__menu');
  // var takePosition = isActive ? "-200px" : "0px";
  var $window = $(window);
  var timeout = false;
  var delay = 250;
  var windowsize;

  /*function scrollTo(target) {
    if ($(target).length) {
      $('html, body').stop().animate({
        scrollTop: $(target).offset().top}, 500);
    }
  }*/

  // sidebar in mobile mode
  $('.hamburger').click(function() {
    $(this).addClass('toggler_active');
    $('.sidebar__menu').animate({left: '0'}, 200);
    $('.wrapper').addClass('screen');
    $('.cover-layout').
        css({visibility: 'visible'}).
        animate({opacity: '1'}, 250);
  });

  // $(".wrapper").click(function () {
  // 	$(".sidebar__menu").removeClass("active");
  // 	$(this).removeClass("screen");
  // });

  // $(document).mouseup(function(e) {
  //   var $sidebar = $('.sidebar__menu');
  //
  //   if (!$sidebar.is(e.target) && $sidebar.has(e.target).length === 0) {
  //     $sidebar.animate({left: '-200'}, 200);
  //     $('.hamburger').removeClass('toggler_active');
  //     $('.cover-layout').
  //         css({visibility: 'hidden'}).
  //         animate({opacity: '0'}, 250);
  //   }
  // });

  /* tabs switcher in faq page */
  $('.contents__sidebar_list > li > a').first().trigger('click');

  function checkWidth() {
    windowsize = $window.width();

    if (windowsize < 1200 && windowsize > 768) {
      $('.contents__sidebar').css('padding-top', '70px');
    }
    else if (windowsize < 768) {
      $('.contents__sidebar').removeClass('fixedsticky');
      $('.contents__sidebar').css('padding-top', '0');
    } else {
      $('.contents__sidebar').addClass('fixedsticky');
    }
  }

  // window.resize event listener
  window.addEventListener('resize', function() {
    clearTimeout(timeout);
    timeout = setTimeout(checkWidth, delay);
  });

  // Execute on load
  checkWidth();

  // $('.sidebar__navigation_icon').click(function() {
  //   $('.cover-layout').css({visibility: 'visible'}).animate({opacity: '1'}, 10);
  //   $('.sidebar__navigation__list').css({'right': '0'});
  // });
  //
  // $('.sidebar__navigation__list').click(function() {
  //   $(this).css({'right': '-250px'});
  //   $('.cover-layout').css({visibility: 'hidden'}).animate({opacity: '0'}, 50);
  // });

  // $('.sidebar__navigation__list .contents__sidebar_list_inset a').
  //     click(function() {
  //       $('.sidebar__navigation__list').css({'right': '-250px'});
  //       $('.cover-layout').
  //           css({visibility: 'hidden'}).
  //           animate({opacity: '0'}, 50);
  //     });

  // $('.cover-layout').click(function() {
  //   $(this).css({visibility: 'hidden'}).animate({opacity: '0'}, 450);
  //   $('.sidebar__navigation__list').css({'right': '-250px'});
  // });

  /* smooth scroll */
  $('#sidebarScroll').
      find('.contents__sidebar_list_inset li a[href^=\'#\']').
      on('click', function(e) {
        e.preventDefault();
        var link = this;
        // store hash
        var hash = this.hash;

        // animate
        $('html, body').animate({
          scrollTop: $(hash).offset().top - 10,
        }, 500, function() {
          // when done, add hash to url
          window.location.hash = hash;
        });
      });

});
