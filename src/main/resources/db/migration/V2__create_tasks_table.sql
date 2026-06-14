  CREATE TABLE tasks
  (
      id         BIGSERIAL    PRIMARY KEY,
      user_id BIGINT NOT NULL,
      title      VARCHAR(50)  NOT NULL,
      description   Text,
      due_date DATE,
      status VARCHAR(20)  NOT NULL DEFAULT 'TODO',
      created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id)
  );