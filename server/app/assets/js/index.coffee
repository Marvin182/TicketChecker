$ ->
	alert = new Alert('#alerts')
	api = new Api(ticketApiUrl, alert)

	$('.hide').hide().removeClass('hide') # hide class has an !important so changing element style does not work
	$('#logout').click ->
		$.get '/logout', -> window.location.reload()

	$('#tickets').tablesorter({sortList:[[0, 0], [4, 0]]})
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
		at = @
		@connected = false
		@ws = new WebSocket(url)
		@ws.onopen = ->
			alert.info "Connected!", 5
			at.connected = true
		@ws.onclose = ->
			at.connected = false
			$e = alert.error "Connection lost! Trying to reconnect ...", -1
			at.reconnect.call at, $e
		@ws.onerror = (error)->
			alert.error "API Error! See JavaScript console."
			console.log "api websocket error", error
		@ws.onmessage = (e) ->
			msg = JSON.parse(e.data)
			# console.log "api receive", msg
			switch msg.typ
				when "EventStats"
					p = if (msg.ticketsTotal > 0) then (msg.ticketsCheckedIn / msg.ticketsTotal) else 0
					$('#progress-checked-in').css('width', 100 * p + '%')
					$('#progress-not-checked-in').css('width', 100 * (1-p) + '%')
					$('#progress-text').text("#{msg.ticketsCheckedIn} / #{msg.ticketsTotal}")
				when "Projection"
					predictedChechInDoneTime = if msg.predictedChechInDoneTime then new Date(1000 * msg.predictedChechInDoneTime).format('d.m.yy HH:MM:ss') else ''
					$('.predicted-check-in-done').text(predictedChechInDoneTime)
					$('.check-in-speed').text(msg.checkInsPerMin.toFixed(1))
				when "CheckInTicketSuccess"
					t = msg.details
					alert.success t.forename + " " + t.surname + " checked in.", 4
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
					alert.error msg.msg

	send: (obj) ->
		if (@connected)
			# console.log "api send", obj
			@ws.send JSON.stringify obj

	checkInTicket: (order, code) -> @send
			typ: "CheckInTicket"
			order: parseInt(order)
			code: code
	declineTicket: (id) -> @send
			typ: "DeclineTicket"
			id: parseInt(id)

	reconnect: ($e) ->
		retryEvery = 15 # seconds
		at = @
		$.ajax window.location.href,
			type: 'GET'
			timeout: 10000
			success: ->
				$e.text "Reconnecting ..."
				$e.removeClass 'alert-danger'
				$e.addClass 'alert-success'
				window.location.reload()
			error: ->
				nextRetry = new Date(new Date().getTime() + 1000 * retryEvery)
				$e.html '<strong>Not connected!</strong> Next try in <span class="seconds">' + retryEvery + '</span> seconds.'
				wait = ->
					diff = nextRetry - new Date()
					if (diff < 0)
						$e.html '<strong>Not connected!</strong> Trying to reconnect ...'
						at.reconnect.call at, $e
					else
						$e.find('.seconds').text Math.ceil(diff / 1000)
						window.setTimeout(wait, 1000)
				wait()

class Alert
	constructor: (@root) ->
		@c = 0
	inc: -> @c++
	dec: ($e) -> if (--@c == 0) then @autoRemoveAlerts().remove() else if (@autoRemoveAlerts().length > 10) then $e.remove()
	autoRemoveAlerts: -> $('.alert.auto-remove', @root)
	newAlert: (text, type = 'info', time = 10) ->
		pos = text.indexOf('!')
		if (~pos)
			text = '<strong>' + text.substr(0, pos + 1) + '</strong>' + text.substr(pos + 1)
		$e = $('<div class="alert alert-' + type + '">' + text + '</div>').appendTo(@root)
		if (time > 0)
			at = @
			at.inc()
			$e.addClass('auto-remove').animate({opacity: 0}, 1000 * time, -> at.dec($e))
		$e
	info: (text, time) -> @newAlert(text, 'info', time)
	success: (text, time) -> @newAlert(text, 'success', time)
	error: (text, time) -> @newAlert(text, 'danger', time)
	warn: (text, time) -> @newAlert(text, 'warn', time)






