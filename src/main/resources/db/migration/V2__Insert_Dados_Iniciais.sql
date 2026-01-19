-- Inserir Especialidades
INSERT INTO especialidades (nome) VALUES
('Cardiologia'),
('Pediatria'),
('Clínica Geral'),
('Ortopedia'),
('Dermatologia');

-- Inserir Unidades de Saúde
INSERT INTO unidades_saude (nome, logradouro, numero, bairro, cidade, cep, latitude, longitude, criado_em) VALUES
('UBS Centro', 'Av. Paulista', '1000', 'Centro', 'São Paulo', '01310100', -23.563210, -46.654439, NOW()),
('UBS Vila Mariana', 'Rua Vergueiro', '1500', 'Vila Mariana', 'São Paulo', '04101000', -23.587976, -46.630788, NOW()),
('UBS Mooca', 'Rua da Mooca', '2100', 'Mooca', 'São Paulo', '03102000', -23.548943, -46.595758, NOW()),
('UBS Pinheiros', 'Rua dos Pinheiros', '500', 'Pinheiros', 'São Paulo', '05422000', -23.561477, -46.691804, NOW()),
('UBS Tatuapé', 'Av. Celso Garcia', '2000', 'Tatuapé', 'São Paulo', '03015000', -23.537375, -46.574581, NOW());

-- Inserir Médicos
INSERT INTO medicos (nome, crm, especialidade_id, unidade_id, ativo) VALUES
('Dr. João Silva', 'CRM123456', 1, 1, TRUE),
('Dra. Maria Santos', 'CRM234567', 2, 2, TRUE),
('Dr. Carlos Oliveira', 'CRM345678', 3, 1, TRUE),
('Dra. Ana Costa', 'CRM456789', 4, 3, TRUE),
('Dr. Pedro Almeida', 'CRM567890', 5, 4, TRUE);

-- Inserir Pacientes
INSERT INTO pacientes (nome, cpf, cartao_sus, email, telefone, logradouro, numero, bairro, cidade, cep, latitude, longitude, criado_em) VALUES
('Roberto Ferreira', '12345678901', '123456789012345', 'roberto.ferreira@email.com', '11987654321', 'Rua das Flores', '100', 'Jardim Paulista', 'São Paulo', '01410000', -23.567421, -46.672983, NOW()),
('Juliana Rocha', '23456789012', '234567890123456', 'juliana.rocha@email.com', '11976543210', 'Av. Rebouças', '200', 'Pinheiros', 'São Paulo', '05402000', -23.561477, -46.691804, NOW()),
('Fernando Souza', '34567890123', '345678901234567', 'fernando.souza@email.com', '11965432109', 'Rua Tupi', '300', 'Vila Mariana', 'São Paulo', '04105000', -23.587976, -46.630788, NOW()),
('Patrícia Lima', '45678901234', '456789012345678', 'patricia.lima@email.com', '11954321098', 'Rua Conselheiro Nébias', '400', 'Centro', 'São Paulo', '01303000', -23.563210, -46.654439, NOW()),
('Lucas Martins', '56789012345', '567890123456789', 'lucas.martins@email.com', '11943210987', 'Av. Brás Leme', '500', 'Santana', 'São Paulo', '02020000', -23.497859, -46.626618, NOW());

-- Inserir Consultas (algumas agendadas para o futuro, uma cancelada, uma desistência)
INSERT INTO consultas (paciente_id, medico_id, unidade_id, data_hora, status, observacoes) VALUES
(1, 1, 1, DATE_ADD(NOW(), INTERVAL 5 DAY), 'AGENDADA', 'Primeira consulta de rotina'),
(2, 2, 2, DATE_ADD(NOW(), INTERVAL 3 DAY), 'AGENDADA', 'Retorno de consulta'),
(3, 3, 1, DATE_ADD(NOW(), INTERVAL 7 DAY), 'AGENDADA', NULL),
(4, 4, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), 'CANCELADA', 'Paciente solicitou cancelamento'),
(5, 5, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), 'DESISTENCIA', 'Paciente não compareceu');

-- Inserir Notificações (exemplo de notificações de vagas liberadas)
INSERT INTO notificacoes (paciente_id, consulta_id, mensagem, tipo, enviada_em, lida) VALUES
(1, 4, 'Vaga disponível em Cardiologia para amanhã às 14h. Deseja agendar?', 'EMAIL', NOW(), FALSE),
(2, 5, 'Nova vaga disponível em Dermatologia. Acesse o sistema para agendar.', 'SMS', NOW(), FALSE),
(3, 4, 'Vaga liberada! Especialidade: Ortopedia. Data: Hoje às 15h.', 'EMAIL', DATE_SUB(NOW(), INTERVAL 1 HOUR), TRUE);
