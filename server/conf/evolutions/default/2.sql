# users schema
 
# --- !Ups

create table users (
	id int unsigned auto_increment,
	name varchar_ignorecase(45) not null,
	password varchar(45) not null,
	isAdmin boolean not null default 0,
	primary key (id)
);

alter table tickets add constraint fk_checkedInById foreign key (checkedInById) references users(id);

# --- !Downs

alter table tickets drop constraint fk_checkedInById;
drop table if exists users;
