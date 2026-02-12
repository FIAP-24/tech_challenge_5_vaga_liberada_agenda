-- ==================================================================================
-- SCRIPT DE DEMONSTRAÇÃO - CENÁRIO "VAGA LIBERADA POR GEOLOCALIZAÇÃO"
-- ==================================================================================

-- 1. Limpeza Garantida (Ordem correta para não dar erro de chave estrangeira)
DELETE FROM notificacao_historico; -- Se existir tabela de logs de notificação
DELETE FROM consulta;
DELETE FROM lista_espera;
DELETE FROM medico;
DELETE FROM paciente;
DELETE FROM unidade_saude;
DELETE FROM especialidade;

-- 2. Configuração do Cenário
-- Especialidade: Cardiologia (Onde teremos a fila)
INSERT INTO especialidade (id, nome) VALUES (1, 'Cardiologia');

-- Unidade: UBS Paulista (No centro de SP)
INSERT INTO unidade_saude (id, nome, endereco, latitude, longitude)
VALUES (1, 'UBS Paulista', 'Av. Paulista, 1000', -23.561684, -46.655981);

-- Médico: Dr. Drauzio (Cardiologista na UBS Paulista)
INSERT INTO medico (id, nome, crm, especialidade_id)
VALUES (1, 'Dr. Drauzio', 'CRM12345', 1);

-- 3. Os Atores (Pacientes)

-- Paciente 1 (JOÃO): Vai confirmar a consulta de AMANHÃ.
INSERT INTO paciente (id, nome, cpf, cartao_sus, telefone, email, latitude, longitude)
VALUES (1, 'João Confirmado', '111.111.111-11', 'SUS001', '11999999991', 'joao@teste.com', -23.500000, -46.600000);

-- Paciente 2 (MARIA): Vai CANCELAR a consulta de HOJE (Gerando a vaga).
INSERT INTO paciente (id, nome, cpf, cartao_sus, telefone, email, latitude, longitude)
VALUES (2, 'Maria Desistente', '222.222.222-22', 'SUS002', '11999999992', 'maria@teste.com', -23.510000, -46.610000);

-- Paciente 3 (CARLOS VIZINHO): O "Escolhido". Mora a 100 metros da UBS.
-- Truque: Inserimos ele na lista de espera.
INSERT INTO paciente (id, nome, cpf, cartao_sus, telefone, email, latitude, longitude)
VALUES (3, 'Carlos Vizinho', '333.333.333-33', 'SUS003', '11999999993', 'carlos@teste.com', -23.561700, -46.656000);

-- 4. Agendamentos (O Estado Inicial)

-- Consulta 1 (João): Amanhã às 10h. O Scheduler vai pegar essa e mandar confirmar.
INSERT INTO consulta (paciente_id, medico_id, unidade_saude_id, data_hora, status, lembrete_enviado)
VALUES (1, 1, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), 'AGENDADA', false);

-- Consulta 2 (Maria): HOJE, daqui a 1 hora. Ela vai entrar no app e cancelar.
INSERT INTO consulta (paciente_id, medico_id, unidade_saude_id, data_hora, status, lembrete_enviado)
VALUES (2, 1, 1, DATE_ADD(NOW(), INTERVAL 1 HOUR), 'AGENDADA', true);

-- 5. Lista de Espera (A Mágica)
-- Inserimos APENAS o Carlos Vizinho compatível com o Dr. Drauzio na UBS Paulista.
-- Assim, seu código (findFirstByFiltros) só terá ele como opção e vai "acertar" o alvo.
INSERT INTO lista_espera (paciente_id, especialidade_id, unidade_saude_id, medico_id, data_solicitacao, status)
VALUES (3, 1, 1, 1, NOW(), 'ATIVA');