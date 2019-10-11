create table sina_news (
id int not null primary key auto_increment,
url varchar(1000),
title varchar(1000),
content text,
create_time timestamp,
modify_time timestamp
);
create table LINKS_ALREADY_PROCESSED(url varchar(1000));
create table LINKS_TOBE_PROCESSED(url varchar(1000));