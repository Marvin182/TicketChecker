# tickets schema
 
# --- !Ups

create table tickets (
	id int unsigned auto_increment,
	orderNumber int unsigned not null,
	code varchar(16) not null,
	forename varchar(45) not null,
	surname varchar(45) not null,
	tableNumber int unsigned not null default 0,
	checkedIn boolean not null default 0,
	checkedInById int unsigned,
	checkInTime int unsigned,
	primary key (id)
	-- constraint foreign key (checkedInById) references users (id) on delete set null
);

# --- !Downs
 
drop table if exists tickets;
