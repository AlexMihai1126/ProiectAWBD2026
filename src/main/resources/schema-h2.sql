CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE authority (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE user_authority (
    user_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, authority_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (authority_id) REFERENCES authority(id)
);

CREATE TABLE client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(200),
    phone VARCHAR(30),
    notes VARCHAR(1000),
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    county VARCHAR(255),
    country VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE
);

CREATE TABLE gear_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    notes VARCHAR(1000),
    owner_user_id BIGINT NOT NULL,
    FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE TABLE shoot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(2000),
    owner_user_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (owner_user_id) REFERENCES users(id),
    FOREIGN KEY (location_id) REFERENCES location(id),
    FOREIGN KEY (client_id) REFERENCES client(id),
    CONSTRAINT chk_shoot_dates CHECK (end_at IS NULL OR end_at >= start_at)
);

CREATE TABLE shoot_gear (
    shoot_id BIGINT NOT NULL,
    gear_item_id BIGINT NOT NULL,
    PRIMARY KEY (shoot_id, gear_item_id),
    FOREIGN KEY (shoot_id) REFERENCES shoot(id),
    FOREIGN KEY (gear_item_id) REFERENCES gear_item(id)
);

CREATE TABLE media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shoot_id BIGINT NOT NULL,
    media_type VARCHAR(30) NOT NULL,
    file_ref VARCHAR(500) NOT NULL,
    taken_at TIMESTAMP WITH TIME ZONE,
    iso INTEGER,
    aperture DOUBLE,
    shutter_speed VARCHAR(255),
    focal_length INTEGER,
    focal_length35mm INTEGER,
    width_px INTEGER,
    height_px INTEGER,
    rating INTEGER,
    notes VARCHAR(1000),
    duration_seconds INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (shoot_id) REFERENCES shoot(id)
);

CREATE TABLE invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shoot_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    details VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT unique_invoice_shoot UNIQUE (shoot_id),
    FOREIGN KEY (shoot_id) REFERENCES shoot(id),
    FOREIGN KEY (client_id) REFERENCES client(id)
);
