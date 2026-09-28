CREATE DATABASE IF NOT EXISTS machine_risk_db;
USE machine_risk_db;

CREATE TABLE IF NOT EXISTS machine_fields (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  field_name VARCHAR(80) NOT NULL UNIQUE,
  field_type VARCHAR(20) NOT NULL,
  required BOOLEAN NOT NULL,
  dropdown_options TEXT,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS machines (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS machine_values (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  machine_id BIGINT NOT NULL,
  field_id BIGINT NOT NULL,
  value_text TEXT,
  CONSTRAINT uq_machine_field UNIQUE (machine_id, field_id),
  CONSTRAINT fk_values_machine FOREIGN KEY (machine_id) REFERENCES machines(id) ON DELETE CASCADE,
  CONSTRAINT fk_values_field FOREIGN KEY (field_id) REFERENCES machine_fields(id)
);
