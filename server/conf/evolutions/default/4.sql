# insert users
 
# --- !Ups
insert into users (username, password, isAdmin) values
	('Admin', 'b1c1d8736f20db3fb6c1c66bb1455ed43909f0d8', 1),
	('Max', 'da39a3ee5e6b4b0d3255bfef95601890afd80709', 0),
	('Peter', 'da39a3ee5e6b4b0d3255bfef95601890afd80709', 0),

# --- !Downs
 
delete from users;
