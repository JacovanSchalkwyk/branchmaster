CREATE TABLE branch_master.branch (
    id SERIAL PRIMARY KEY NOT NULL,
    name varchar(50) NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    timeslot_length INT NOT NULL,
    active boolean NOT NULL,
    address VARCHAR(120) NOT NULL,
    suburb VARCHAR(80),
    city VARCHAR(80) NOT NULL,
    province VARCHAR(80),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(80) NOT NULL DEFAULT 'South Africa',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    CONSTRAINT chk_timeslot_length_positive CHECK (timeslot_length > 0)
);

create index branch_id_active on branch_master.branch(
    id ASC,
    active ASC
);

CREATE TABLE branch_master.branch_operating_hours (
    id SERIAL PRIMARY KEY,
    branch_id INT NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    day_of_week INT NOT NULL, -- 0 - Monday, 6 - Sunday
    closed boolean NOT NULL DEFAULT false,

    CONSTRAINT chk_operating_hours_day CHECK (day_of_week BETWEEN 0 AND 6),
    CONSTRAINT chk_operating_hours_interval CHECK (closed = true OR opening_time < closing_time),

    CONSTRAINT fk_operating_hours_branch
        FOREIGN KEY (branch_id)
            REFERENCES branch_master.branch(id)
            ON DELETE CASCADE
);

create index operating_hours_branch_id on branch_master.branch_operating_hours(
    branch_id ASC
);

CREATE UNIQUE INDEX uq_operating_hours_branch_day
    ON branch_master.branch_operating_hours (branch_id, day_of_week);

CREATE TABLE branch_master.resource_availability (
    id SERIAL PRIMARY KEY,
    branch_id INT NOT NULL,
    day_of_week INT NOT NULL, -- 0 - Monday, 6 - Sunday
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    start_date DATE DEFAULT NULL,
    end_date DATE DEFAULT NULL,
    name VARCHAR(120) NOT NULL,
    CONSTRAINT valid_interval CHECK (start_time < end_time),

    CONSTRAINT chk_resource_avail_day CHECK (day_of_week BETWEEN 0 AND 6),
    CONSTRAINT chk_resource_avail_time CHECK (start_time < end_time),
    CONSTRAINT chk_resource_avail_dates CHECK (start_date <= end_date),

    CONSTRAINT fk_resource_availability_branch
        FOREIGN KEY (branch_id)
            REFERENCES branch_master.branch(id)
            ON DELETE CASCADE
);

create index resource_availability_branch_id on branch_master.resource_availability(
    branch_id ASC
);

CREATE TABLE branch_master.resource_unavailability (
    id SERIAL PRIMARY KEY,
    branch_id INT NOT NULL,  -- no FK
    available_resource_id INT NOT NULL,
    date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    reason TEXT,

    CONSTRAINT fk_resource_unavailability_branch
        FOREIGN KEY (branch_id)
            REFERENCES branch_master.branch(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_resource_unavailability_availability_same_branch
        FOREIGN KEY (available_resource_id)
            REFERENCES branch_master.resource_availability(id)
            ON DELETE CASCADE
);

create index resource_unavailability_branch_id on branch_master.resource_unavailability(
    branch_id ASC,
    date ASC
);

CREATE TABLE branch_master.appointment (
    id SERIAL PRIMARY KEY,
    branch_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status TEXT DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT now(),
    reason TEXT,
    name TEXT,
    email TEXT,
    phone_number TEXT,
    resource_availability_id INT NOT NULL,

    CONSTRAINT chk_appointment_time CHECK (start_time < end_time),

    CONSTRAINT fk_appointment_branch
        FOREIGN KEY (branch_id)
            REFERENCES branch_master.branch(id)
            ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uq_appt_resource_slot
    ON branch_master.appointment (resource_availability_id, appointment_date, start_time, end_time)
    WHERE status IN ('BOOKED');