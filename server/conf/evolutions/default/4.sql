# insert users
 
# --- !Ups
insert into users (username, password, isAdmin) values
	('Marvin', '', 1),
	('Max', '', 0),
	('Peter', '', 0),

# --- !Downs
 
delete from users;
