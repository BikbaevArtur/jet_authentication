

create table if not exists users
(
    id                serial primary key,
    last_name         varchar(255) not null,
    first_name        varchar(255) not null,
    middle_name       varchar(255) not null,
    birthday          timestamp not null ,
    gender            char(1) check (gender in ( 'M','W')),
    email             varchar(255) not null unique,
    password          varchar(255) not null,
    phone             varchar(255),
    activate_email    boolean   default false,
    avatar            varchar(255),
    time_registration timestamp default now()
);


create table if not exists tokens
(
    id      uuid primary key,
    user_id  int not null ,
    refresh_token varchar(255) not null ,
    expires_in bigint not null ,
    creat_at timestamp not null default now(),
    constraint fk_user
    foreign key (user_id) references users(id) on delete  cascade
)













