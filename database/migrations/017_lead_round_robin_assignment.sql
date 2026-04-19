CREATE TABLE round_robin_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counselor_id BIGINT NOT NULL UNIQUE,
    last_assigned_at DATETIME,
    assignment_count INT NOT NULL DEFAULT 0,
    FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE lead_assignment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    from_counselor_id BIGINT,
    to_counselor_id BIGINT,
    assigned_by_id BIGINT,
    reason VARCHAR(500),
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE,
    FOREIGN KEY (from_counselor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (to_counselor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_by_id) REFERENCES users(id) ON DELETE SET NULL
);
