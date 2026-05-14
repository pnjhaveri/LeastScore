create table if not exists users (
  id bigserial primary key,
  username text not null,
  created_at timestamptz not null default now()
);

create table if not exists rooms (
  id bigserial primary key,
  room_code text not null unique,
  status text not null,
  created_at timestamptz not null default now()
);

create table if not exists room_players (
  room_id bigint not null references rooms(id) on delete cascade,
  user_id bigint not null references users(id) on delete cascade,
  seat_index int not null,
  joined_at timestamptz not null default now(),
  disconnected_at timestamptz null,
  primary key (room_id, user_id),
  unique (room_id, seat_index)
);

create table if not exists games (
  id bigserial primary key,
  room_id bigint not null references rooms(id) on delete cascade,
  state_json text not null,
  current_turn_user_id bigint null references users(id),
  started_at timestamptz not null default now(),
  ended_at timestamptz null
);

create table if not exists moves (
  id bigserial primary key,
  game_id bigint not null references games(id) on delete cascade,
  user_id bigint not null references users(id) on delete cascade,
  move_type text not null,
  payload_json text not null,
  created_at timestamptz not null default now()
);

create index if not exists idx_moves_game_created_at on moves(game_id, created_at desc);

create table if not exists scores (
  game_id bigint not null references games(id) on delete cascade,
  user_id bigint not null references users(id) on delete cascade,
  total_score int not null,
  primary key (game_id, user_id)
);
