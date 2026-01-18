CREATE TABLE veiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    ano INTEGER NOT NULL,
    cor VARCHAR(30) NOT NULL,
    preco_usd DECIMAL(12, 2) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_veiculo_marca ON veiculo(marca);
CREATE INDEX idx_veiculo_ano ON veiculo(ano);
CREATE INDEX idx_veiculo_ativo ON veiculo(ativo);
