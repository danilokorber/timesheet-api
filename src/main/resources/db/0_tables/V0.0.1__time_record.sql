CREATE TABLE time_record (
  id VARCHAR(36) NOT NULL UNIQUE,
  employee_id VARCHAR(36) NOT NULL,
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  PRIMARY KEY (id)
);
