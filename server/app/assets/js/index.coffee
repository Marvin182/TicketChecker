$ ->
	alert = new Alert('#alerts')
	api = new Api(ticketApiUrl, alert)

	$('.hide').hide().removeClass('hide') # hide class has an !important so changing element style does not work
	$('#logout').click ->
		$.get '/logout', -> window.location.reload()

	$('#tickets').tablesorter({sortList:[[1, 0], [5, 0]]})
	$('.check-in-time').each ->
		text = $(this).text()
		if (text.length) then $(this).text(new Date(1000 * text).format('HH:MM:ss'))
	$('.action-check-in').click ->
		row = $(this).parents('tr')
		api.checkInTicket(row.find('.order').text(), row.find('.code').text())
	$('.action-decline').click ->
		id = $(this).parents('tr').attr('id').substr(7)
		api.declineTicket(id)

class Api
	constructor: (url, alert) ->
		@ws = new WebSocket(url)
		@ws.onopen = ->
			alert.info "Connected!", 5
		@ws.onclose = ->
			alert.error "Connection lost! Reloading page in 10 seconds.", 15
			window.setTimeout((-> window.location.reload()), 10000)
		@ws.onerror = (error)->
			alert.error "API Error! See JavaScript console."
			console.log error
		@ws.onmessage = (e) ->
			msg = JSON.parse(e.data)
			console.log "api receive", msg
			switch msg.typ
				when "EventStats"
					p = if (msg.ticketsTotal > 0) then (msg.ticketsCheckedIn / msg.ticketsTotal) else 0
					$('#checkInProgress').css('width', 100 * p + '%').text(if msg.ticketsCheckedIn > 0 then "#{msg.ticketsCheckedIn} / #{msg.ticketsTotal}" else "")
				when "CheckInTicketSuccess"
					t = msg.details
					alert.success t.forename + " " + t.surname + " checked in.", 3
					row = $('#ticket-' + t.id)
					row.find('.check-in-by').text(t.checkedInBy)
					row.find('.check-in-time').text(new Date(1000 * t.checkInTime).format('HH:MM:ss'))
					row.find('.action-check-in').hide()
					row.find('.action-decline').show()
				when "DeclineTicketSuccess"
					row = $('#ticket-' + msg.details.id)
					row.find('.check-in-by, .check-in-time').text("")
					row.find('.action-check-in').show()
					row.find('.action-decline').hide()
				when "ApiError"
					alert.error(msg.msg)

	send: (obj) ->
		console.log "api send", obj
		@ws.send JSON.stringify obj

	checkInTicket: (order, code) -> @send
			typ: "CheckInTicket"
			order: parseInt(order)
			code: code
	declineTicket: (id) -> @send
			typ: "DeclineTicket"
			id: parseInt(id)

class Alert
	constructor: (@root) ->
		@c = 0
	inc: -> @c++
	dec: -> if (--@c == 0) then $(@root).empty()
	newAlert: (text, type = 'info', time = 10) ->
		pos = text.indexOf('!')
		if (~pos)
			text = '<strong>' + text.substr(0, pos + 1) + '</strong>' + text.substr(pos + 1)
		e = $('<div class="alert alert-' + type + '">' + text + '</div>').appendTo(@root)
		at = @
		@.inc()
		e.animate({opacity:0.001}, 1000 * time, -> at.dec())
		e
	info: (text, time) -> @newAlert(text, 'info', time)
	success: (text, time) -> @newAlert(text, 'success', time)
	error: (text, time) -> @newAlert(text, 'danger', time)
	warn: (text, time) -> @newAlert(text, 'warn', time)
