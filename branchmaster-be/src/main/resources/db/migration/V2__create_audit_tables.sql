CREATE TABLE branch_master.admin_audit_log (
                                               id SERIAL PRIMARY KEY,
                                               staff_id INT NOT NULL,
                                               action_type TEXT NOT NULL,
                                               params JSONB NOT NULL,
                                               created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_admin_audit_staff_id
    ON branch_master.admin_audit_log (staff_id);

CREATE INDEX idx_admin_audit_action_type
    ON branch_master.admin_audit_log (action_type);

CREATE INDEX idx_admin_audit_created_at
    ON branch_master.admin_audit_log (created_at);