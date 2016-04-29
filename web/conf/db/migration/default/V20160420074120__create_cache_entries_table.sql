CREATE TABLE cache_entries (
  key varchar(191) PRIMARY KEY,
  value bytea NOT NULL,
  expired_at timestamp,
  created_at timestamp NOT NULL
);

create index on cache_entries(expired_at);
create index on cache_entries(created_at);