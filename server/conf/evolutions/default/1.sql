# tickets schema
 
# --- !Ups

create table tickets (
	id int unsigned auto_increment,
	orderNumber int unsigned not null,
	code varchar(16) not null,
	forename varchar(45) not null,
	surname varchar(45) not null,
	student boolean not null default 0,
	tableNumber int unsigned not null default 0,
	checkedIn boolean not null default 0,
	checkedInById int unsigned,
	checkInTime int unsigned,
	primary key (id)
);

# --- !Downs
 
drop table if exists tickets;
