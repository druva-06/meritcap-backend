CREATE TABLE IF NOT EXISTS campaigns (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    source      VARCHAR(100),
    description TEXT,
    qr_code     VARCHAR(512),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by  VARCHAR(255),
    created_at  DATE NOT NULL
);
