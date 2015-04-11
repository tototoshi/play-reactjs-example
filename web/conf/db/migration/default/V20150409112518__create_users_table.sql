CREATE TABLE twitter_accounts (
  id bigint primary key,
  screen_name text not null unique,
  access_token text not null,
  access_token_secret text not null
);
