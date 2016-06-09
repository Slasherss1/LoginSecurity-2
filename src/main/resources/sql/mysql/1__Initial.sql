-- apply changes
create table ls_actions (
  id                            integer auto_increment not null,
  timestamp                     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  unique_user_id                varchar(255),
  type                          varchar(255),
  service                       varchar(255),
  provider                      varchar(255),
  constraint pk_ls_actions primary key (id)
);

create table ls_upgrades (
  id                            integer auto_increment not null,
  version                       varchar(255),
  description                   varchar(255),
  applied_at                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  constraint uq_ls_upgrades_version unique (version),
  constraint pk_ls_upgrades primary key (id)
);

create table ls_locations (
  id                            integer auto_increment not null,
  world                         varchar(255),
  x                             double,
  y                             double,
  z                             double,
  yaw                           integer,
  pitch                         integer,
  constraint pk_ls_locations primary key (id)
);

create table ls_players (
  id                            integer auto_increment not null,
  unique_user_id                varchar(128),
  last_name                     varchar(16),
  ip_address                    varchar(255),
  password                      varchar(512),
  hashing_algorithm             integer,
  last_login                    datetime(6),
  location_id                   integer,
  registration_date             date DEFAULT CURRENT_DATE,
  optlock                       bigint not null,
  constraint uq_ls_players_unique_user_id unique (unique_user_id),
  constraint uq_ls_players_location_id unique (location_id),
  constraint pk_ls_players primary key (id)
);

alter table ls_players add constraint fk_ls_players_location_id foreign key (location_id) references ls_locations (id) on delete restrict on update restrict;

