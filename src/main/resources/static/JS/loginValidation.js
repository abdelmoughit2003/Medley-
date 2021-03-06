/**
 * Created by Nick on 1/22/17.
 */
$().ready(function(){

    $('#loginForm').validate({
        rules: {
            username: {
                required: true,
                minlength: 5
            },
            password: {
                required: true,
                minlength: 5
            }
        },
        messages:{
            username:{
                required: "Enter your username",
                minlength: "A username is at least 5 characters"
            },
            password:{
                required: "Please enter your password",
                minlength: "A password is at least 5 characters"
            }
        },
        errorElement: 'div',
        errorPlacement: function (error, element) {
            var placement = $(element).data('error');
            if(placement){
                $(placement).append(error)
            }else{
                error.insertAfter(element);
            }
        }
    });
});
