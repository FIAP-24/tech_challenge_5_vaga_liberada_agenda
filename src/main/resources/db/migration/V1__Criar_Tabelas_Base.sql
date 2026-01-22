-- 1. Especialidades
CREATE TABLE especialidades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Unidades de Saúde com Endereço Completo
CREATE TABLE unidades_saude (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    cep VARCHAR(8),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Médicos
CREATE TABLE medicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    crm VARCHAR(20) NOT NULL UNIQUE,
    especialidade_id INT,
    unidade_id INT,
    ativo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (especialidade_id) REFERENCES especialidades(id),
    FOREIGN KEY (unidade_id) REFERENCES unidades_saude(id)
);

-- 4. Pacientes com Endereço e Cartão SUS
CREATE TABLE pacientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    cartao_sus VARCHAR(15) UNIQUE,
    email VARCHAR(255),
    telefone VARCHAR(20),
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    cep VARCHAR(8),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Consultas
CREATE TABLE consultas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT,
    medico_id INT,
    unidade_id INT,
    data_hora TIMESTAMP NOT NULL,
    lembrete_enviado BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'AGENDADA',
    observacoes TEXT,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    FOREIGN KEY (medico_id) REFERENCES medicos(id),
    FOREIGN KEY (unidade_id) REFERENCES unidades_saude(id),
    CONSTRAINT chk_status CHECK (status IN ('AGENDADA', 'CANCELADA', 'DESISTENCIA', 'REALIZADA'))
);

-- 6. Notificações (Para avisos de vagas por desistência)
CREATE TABLE notificacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT,
    consulta_id INT,
    mensagem TEXT NOT NULL,
    tipo VARCHAR(10) DEFAULT 'EMAIL',
    enviada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lida BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    FOREIGN KEY (consulta_id) REFERENCES consultas(id),
    CONSTRAINT chk_tipo_notificacao CHECK (tipo IN ('EMAIL', 'SMS'))
);

-- Índices para performance
CREATE INDEX idx_pacientes_cpf ON pacientes(cpf);
CREATE INDEX idx_medicos_crm ON medicos(crm);
CREATE INDEX idx_consultas_data_hora ON consultas(data_hora);
CREATE INDEX idx_consultas_status ON consultas(status);
CREATE INDEX idx_consultas_paciente_id ON consultas(paciente_id);
CREATE INDEX idx_consultas_medico_id ON consultas(medico_id);
