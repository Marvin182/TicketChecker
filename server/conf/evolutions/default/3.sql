# sessions schema
 
# --- !Ups
 
create table sessions (
	id varchar(32),
	userId int unsigned not null,
	timestamp int not null,
	primary key(id),
);

alter table sessions add constraint fk_userId foreign key (userId) references users(id) on delete cascade;

# --- !Downs

drop table if exists sessions;
