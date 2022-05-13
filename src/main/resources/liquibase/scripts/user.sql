-- liquibase formatted sql

-- changeset ichernyy:1
create table notification_task
(
    id serial NOT NULL PRIMARY KEY,
    chat_id bigint NOT NULL,
    notification_date timestamp NOT NULL,
    notification_text text NOT NULL,
    executed boolean NOT NULL DEFAULT 'false'
);

-- changeset ichernyy:2
CREATE INDEX task_date_idx ON notification_task (notification_date) WHERE executed = 'false';