CREATE TABLE IF NOT EXISTS regulatory_documents (
    id UUID PRIMARY KEY,
    title VARCHAR(240) NOT NULL,
    jurisdiction VARCHAR(120) NOT NULL,
    regulation_code VARCHAR(80) NOT NULL,
    article_reference VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    embedding vector(384) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_regulatory_documents_embedding
    ON regulatory_documents USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_regulatory_documents_jurisdiction
    ON regulatory_documents (jurisdiction);
