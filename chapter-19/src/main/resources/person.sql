-- jpa에서 person 테이블을 생성할 것이므로 중복생성 방지를 위해 주석처리함
-- create table PERSON (
--     id bigint primary key auto_increment,
--     name varchar(255),
--     age varchar(255),
--     address varchar(255)
-- );

insert into person(name, age, address) values('이경원',32,'인천');
insert into person(name, age, address) values('홍길동',30,'서울');
insert into person(name, age, address) values('아무개',25,'강원');