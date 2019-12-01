create table NEWS(
id int not null primary key auto_increment,
url varchar(1000),
title varchar(1000),
content text,
create_time datetime,
modify_time datetime
);
create table LINKS_ALREADY_PROCESSED(url varchar(1000));
create table LINKS_TOBE_PROCESSED(url varchar(1000));