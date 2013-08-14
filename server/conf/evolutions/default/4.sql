# insert users
 
# --- !Ups
insert into users (name, password, isAdmin) values
	('Marvin', '', 1),
	('Alex', '', 1),
	('Robin', '', 1),
	('Max', '', 0),
	('Peter', '', 0),

# --- !Downs
 
delete from users;
