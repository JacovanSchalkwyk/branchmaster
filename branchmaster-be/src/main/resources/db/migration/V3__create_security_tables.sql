CREATE TABLE IF NOT EXISTS branch_master.staff_user (
                                                        id SERIAL PRIMARY KEY,
                                                        email TEXT NOT NULL UNIQUE,
                                                        password_hash TEXT NOT NULL,
                                                        active BOOLEAN NOT NULL DEFAULT true,
                                                        created_at TIMESTAMP DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS branch_master.staff_user_role (
                                                             staff_user_id INT NOT NULL,
                                                             role TEXT NOT NULL,
                                                             PRIMARY KEY (staff_user_id, role),
    CONSTRAINT fk_staff_user_role_user
    FOREIGN KEY (staff_user_id) REFERENCES branch_master.staff_user(id) ON DELETE CASCADE
    );

WITH ins_staff AS (
INSERT INTO branch_master.staff_user (email, password_hash, active)
SELECT
    'admin@local',
    '$2a$10$OJklcG6mEe/BFbBPTHh4sOkPly/fn4POkFZs6ZE16LVqFSygWiCiG',
    true
    WHERE NOT EXISTS (
    SELECT 1
    FROM branch_master.staff_user
    WHERE email = 'admin@local'
  )
  RETURNING id
),
staff_user_id AS (
SELECT id FROM ins_staff
UNION ALL
SELECT id FROM branch_master.staff_user WHERE email = 'admin@local'
    LIMIT 1
    )
INSERT INTO branch_master.staff_user_role (staff_user_id, role)
SELECT (SELECT id FROM staff_user_id), 'ADMIN'
    WHERE NOT EXISTS (
  SELECT 1
  FROM branch_master.staff_user_role
  WHERE staff_user_id = (SELECT id FROM staff_user_id)
    AND role = 'ADMIN'
);
