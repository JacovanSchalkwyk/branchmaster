DO
$$
BEGIN
    IF '${seed_demo_data}' = 'true' THEN
        INSERT INTO branch_master.branch (id, name, timeslot_length, active, address,
            suburb, city, province, postal_code, country, latitude, longitude)
        VALUES (1, 'Stellenbosch', 30, true, '45 Andringa Street', NULL, 'Stellenbosch', 'Western Cape',
                '7600', 'South Africa', '-33.9346', '18.8610'),
                (2, 'Cape Town CBD', 30, true, '12 Long Street', 'CBD', 'Cape Town', 'Western Cape',
                '8001', 'South Africa', '-33.9258', '18.4232');

    PERFORM setval(
               pg_get_serial_sequence('branch_master.branch', 'id'),
               (SELECT COALESCE(MAX(id), 0) FROM branch_master.branch) + 1,
               false
       );

        INSERT INTO branch_master.resource_availability (branch_id, day_of_week, start_time, end_time, start_date, end_date, name)
        VALUES
            (1, 0, '08:00:00', '19:00:00', '2026-01-01', '2026-12-31', 'Jaco'),
            (1, 1, '08:00:00', '17:00:00', '2026-01-01', '2026-12-31', 'Jaco'),
            (1, 2, '09:00:00', '14:00:00', '2026-01-01', '2026-12-31', 'Jaco'),
            (1, 3, '08:00:00', '12:00:00', '2026-01-01', '2026-12-31', 'Jaco'),
            (1, 4, '09:00:00', '12:00:00', '2026-01-01', '2026-12-31', 'Jaco'),
            (1, 5, '08:00:00', '12:00:00', '2026-01-01', '2026-12-31', 'Jaco'),
            (1, 5, '08:00:00', '18:00:00', '2026-01-01', '2026-12-31', 'Jaco 2'),
            (1, 6, '09:00:00', '13:00:00', '2026-01-01', '2026-12-31', 'Jaco 2');
        INSERT INTO branch_master.branch_operating_hours (branch_id, opening_time, closing_time, day_of_week)
        VALUES
            (1, '08:00:00', '18:00:00', 0),
            (1, '08:00:00', '18:00:00', 1),
            (1, '08:00:00', '18:00:00', 2),
            (1, '08:00:00', '18:00:00', 3),
            (1, '08:00:00', '18:00:00', 4),
            (1, '08:00:00', '17:00:00', 5),
            (1, '08:00:00', '12:00:00', 6);
    END IF;
END
$$;