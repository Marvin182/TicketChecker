# inserttickets
 
# --- !Ups
 
insert into tickets (orderNumber, code, forename, surname, tableNumber) values
	(20, 'JUg5Gi9e', 'prename', 'surname', 1),
	(20, 'AONx2bXm', 'prename', 'surname', 1),
	(20, 'PvZQdC2h', 'prename', 'surname', 1),
	(20, 'uW8JZtJa', 'prename', 'surname', 2),
	(20, 'GsIFFPAQ', 'prename', 'surname', 2),
	(20, 'x2nrGtdz', 'prename', 'surname', 2),
	(20, 'o5Q57oX4', 'prename', 'surname', 3),
	(20, 'dMiFPEKj', 'prename', 'surname', 3);

# --- !Downs
 
truncate table tickets;
