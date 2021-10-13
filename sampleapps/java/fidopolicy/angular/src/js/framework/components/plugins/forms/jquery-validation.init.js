jQuery.validator.setDefaults({
    errorElement: 'div', //default input error message container
    errorClass: 'form-control-feedback', // default input error message class
    focusInvalid: false, // do not focus the last invalid input
    ignore: "",  // validate all fields including form hidden input

    errorPlacement: function(error, element) { // render error placement for each input type
        var group = $(element).closest('.m-form__group-sub').length > 0 ? $(element).closest('.m-form__group-sub') : $(element).closest('.m-form__group');
        var help = group.find('.m-form__help');

        if (group.find('.form-control-feedback').length !== 0) {
            return;
        }

        if (help.length > 0) {
            help.before(error);
        } else {
            if ($(element).closest('.input-group').length > 0) {
                $(element).closest('.input-group').after(error);
            } else {
                if ($(element).is(':checkbox')) {
                    $(element).closest('.m-checkbox').find('>span').after(error);
                } else {
                    $(element).after(error);
                }
            }
        }
    },

    highlight: function(element) { // hightlight error inputs
        var group = $(element).closest('.m-form__group-sub').length > 0  ? $(element).closest('.m-form__group-sub') : $(element).closest('.m-form__group');
        group.addClass('has-danger'); // set error class to the control groupx
    },

    unhighlight: function(element) { // revert the change done by hightlight
        var group = $(element).closest('.m-form__group-sub').length > 0  ? $(element).closest('.m-form__group-sub') : $(element).closest('.m-form__group');

        group.removeClass('has-danger'); // set error class to the control group
    },

    success: function(label, element) {
        var group = $(label).closest('.m-form__group-sub').length > 0  ? $(label).closest('.m-form__group-sub') : $(label).closest('.m-form__group');

        //group.addClass('has-success').removeClass('has-danger'); // set success class and hide error class
        group.removeClass('has-danger'); // hide error class
        group.find('.form-control-feedback').remove();
    }
});

jQuery.validator.addMethod("email", function(value, element) {
    if (/^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/.test(value)) {
        return true;
    } else {
        return false;
    }
}, "Please enter a valid Email.");
