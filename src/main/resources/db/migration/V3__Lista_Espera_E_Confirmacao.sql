-- Migration V3: Lista de Espera e Sistema de Confirmação

-- 1. Atualizar tabela consultas: adicionar campos para confirmação e lista de espera
ALTER TABLE consultas 
    ADD COLUMN confirmada_em TIMESTAMP NULL,
    ADD COLUMN data_limite_confirmacao TIMESTAMP NULL,
    ADD COLUMN vaga_oferecida_para_lista_espera_id INT NULL,
    ADD COLUMN vaga_oferecida_em TIMESTAMP NULL;

-- 2. Atualizar constraint de status para incluir novos status
ALTER TABLE consultas 
    DROP CHECK chk_status;

ALTER TABLE consultas 
    ADD CONSTRAINT chk_status CHECK (status IN ('AGENDADA', 'PENDENTE_CONFIRMACAO', 'LIBERADA', 'CANCELADA', 'DESISTENCIA', 'REALIZADA'));

-- 3. Criar tabela lista_espera
CREATE TABLE lista_espera (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    especialidade_id INT NOT NULL,
    medico_id INT NULL,
    unidade_id INT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    prioridade INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ATIVA',
    consulta_oferecida_id INT NULL,
    data_oferta TIMESTAMP NULL,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    FOREIGN KEY (especialidade_id) REFERENCES especialidades(id),
    FOREIGN KEY (medico_id) REFERENCES medicos(id),
    FOREIGN KEY (unidade_id) REFERENCES unidades_saude(id),
    FOREIGN KEY (consulta_oferecida_id) REFERENCES consultas(id),
    CONSTRAINT chk_status_lista_espera CHECK (status IN ('ATIVA', 'ATENDIDA', 'CANCELADA', 'AGUARDANDO_RESPOSTA'))
);

-- 4. Adicionar foreign key para lista_espera na tabela consultas
ALTER TABLE consultas 
    ADD CONSTRAINT fk_consulta_lista_espera 
    FOREIGN KEY (vaga_oferecida_para_lista_espera_id) REFERENCES lista_espera(id);

-- 5. Índices para performance
CREATE INDEX idx_lista_espera_status ON lista_espera(status);
CREATE INDEX idx_lista_espera_especialidade ON lista_espera(especialidade_id);
CREATE INDEX idx_lista_espera_medico ON lista_espera(medico_id);
CREATE INDEX idx_lista_espera_unidade ON lista_espera(unidade_id);
CREATE INDEX idx_lista_espera_data_cadastro ON lista_espera(data_cadastro);
CREATE INDEX idx_lista_espera_paciente ON lista_espera(paciente_id);
CREATE INDEX idx_consultas_confirmada_em ON consultas(confirmada_em);
CREATE INDEX idx_consultas_data_limite_confirmacao ON consultas(data_limite_confirmacao);
CREATE INDEX idx_consultas_status_data_limite ON consultas(status, data_limite_confirmacao);
