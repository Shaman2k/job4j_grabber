create table posts
(
    id           serial primary key,
    title        varchar(100),
    link         varchar(255) unique ,
    description  text,
    created_date timestamp
)