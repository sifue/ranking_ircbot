create table "LOG_RECORD" ("LOG_RECORD_ID" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,"CHANNEL" VARCHAR NOT NULL,"NICKNAME" VARCHAR NOT NULL,"CONTENT_TYPE" VARCHAR NOT NULL,"CONTENT" VARCHAR NOT NULL,"UPDATED_AT" TIMESTAMP NOT NULL);
create index on "LOG_RECORD" ("CHANNEL", "UPDATED_AT");
create index on "LOG_RECORD" ("CHANNEL", "UPDATED_AT", "NICKNAME");
