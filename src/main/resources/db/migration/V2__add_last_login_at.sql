alter table users add column if not exists last_login_at timestamptz;

create index if not exists idx_users_username on users(username);
