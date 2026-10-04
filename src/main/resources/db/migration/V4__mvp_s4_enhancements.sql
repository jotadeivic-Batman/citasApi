-- V4: Tablas de Afiliaciones EPS, Recuperación de Contraseñas y Reprogramación de Citas

-- 1. Regímenes de Aseguramiento
CREATE TABLE insurance_regimes (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_regimes_code UNIQUE (code)
);

-- 2. Entidades Promotoras de Salud (EPS)
CREATE TABLE eps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_eps_code UNIQUE (code)
);

-- 3. Planes de EPS
CREATE TABLE eps_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eps_id BIGINT NOT NULL,
    regime_id SMALLINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_eps_plans_code UNIQUE (eps_id, code),
    FOREIGN KEY (eps_id) REFERENCES eps(id),
    FOREIGN KEY (regime_id) REFERENCES insurance_regimes(id)
);

-- 4. Afiliaciones del Usuario
CREATE TABLE user_insurance_affiliations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    membership_number VARCHAR(50) NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_users(id),
    FOREIGN KEY (plan_id) REFERENCES eps_plans(id)
);

-- 5. Tokens de Recuperación de Contraseña
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(120) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pwd_reset_token UNIQUE (token_hash),
    FOREIGN KEY (user_id) REFERENCES app_users(id)
);

-- 6. Estados de Solicitud de Reprogramación
CREATE TABLE reschedule_request_statuses (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_reschedule_status_code UNIQUE (code)
);

-- 7. Solicitudes de Reprogramación
CREATE TABLE reschedule_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    requested_by_user_id BIGINT NOT NULL,
    requested_location_id SMALLINT NOT NULL,
    status_id SMALLINT NOT NULL,
    requested_start_at DATETIME NOT NULL,
    requested_end_at DATETIME NOT NULL,
    rejection_reason VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (requested_by_user_id) REFERENCES app_users(id),
    FOREIGN KEY (requested_location_id) REFERENCES locations(id),
    FOREIGN KEY (status_id) REFERENCES reschedule_request_statuses(id)
);

-- Semillas de Regímenes
INSERT INTO insurance_regimes (id, code, name) VALUES
(1, 'CONTRIBUTIVO', 'Régimen Contributivo'),
(2, 'SUBSIDIADO', 'Régimen Subsidiado'),
(3, 'PARTICULAR', 'Particular');

-- Semillas de EPS
INSERT INTO eps (id, code, name, active) VALUES
(1, 'SANITAS', 'EPS Sanitas', TRUE),
(2, 'SURA', 'EPS SURA', TRUE),
(3, 'NUEVA_EPS', 'Nueva EPS', TRUE),
(4, 'SALUD_TOTAL', 'Salud Total EPS', TRUE);

-- Semillas de Planes de EPS
INSERT INTO eps_plans (id, eps_id, regime_id, code, name, active) VALUES
(1, 1, 1, 'SAN-POS-CONT', 'Sanitas PBS Contributivo', TRUE),
(2, 1, 1, 'SAN-PAC-PLUS', 'Sanitas Plan Premium / PAC', TRUE),
(3, 2, 1, 'SURA-POS-CONT', 'SURA PBS Contributivo', TRUE),
(4, 3, 2, 'NEPS-POS-SUBS', 'Nueva EPS Subsidiado', TRUE),
(5, 4, 3, 'PART-LIBRE', 'Particular Atención Directa', TRUE);

-- Semillas de Estados de Reprogramación
INSERT INTO reschedule_request_statuses (id, code, name) VALUES
(1, 'PENDING', 'Pendiente de Aprobación'),
(2, 'APPROVED', 'Aprobada'),
(3, 'REJECTED', 'Rechazada');
