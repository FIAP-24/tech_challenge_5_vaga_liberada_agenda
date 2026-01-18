USE vaga_liberada_db;

CREATE TABLE IF NOT EXISTS paciente (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(255),
    celular VARCHAR(20),
    data_nascimento DATE,
    ativo BOOLEAN DEFAULT TRUE
    );