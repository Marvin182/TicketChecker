signIn = ->
	# hide sign in button and login failed message (if shown) and display the loading button instead
	$('#signIn').hide()
	$('#loginFailed').css('opacity', 0)
	$('#loading').show()

	signInFailed = ->
		# display error sign in button and error message again and shake both shortly
		$('#loading').hide()
		$('#loginFailed').css('opacity', 1)
		$('#signIn').show()
		$('.buttons').effect('shake'
			distance: 8
			times: 2
			500
		)
	console.log('name=' + $('#name').val() + '&password=' + $('#password').val())
	# query the server for login, if the login data is correct the server saves this in the session and answers 1, otherwise 0
	$.ajax
		url: 'login'
		type: 'GET'
		data: 'name=' + $('#name').val() + '&password=' + $('#password').val()
		success: (response) ->
			if response == '1'
				# login successful, reload to display the interface
				window.location.reload()
			else
				signInFailed()
		error: signInFailed

$ ->
	$('.hide').hide().removeClass('hide')
	$('.buttons').css('box-sizing', 'content-box') # changing box-sizing fixes a jquery ui bug where the shake effect will resize the elements
	$('#signIn').parent().click signIn
	$('input').keypress (e) ->
		if e.which == 13
			signIn()
