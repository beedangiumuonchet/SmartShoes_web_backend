import psycopg2
from psycopg2.extensions import register_adapter, AsIs
import numpy as np

# ---------------------------------------------------------------------
# Numpy → PostgreSQL (pgvector format)
# ---------------------------------------------------------------------
# Float32 adapter
def adapt_numpy_float32(numpy_float32):
    return AsIs(float(numpy_float32))

# Array adapter → convert numpy array to '{0.12,0.34,...}' string for pgvector
def adapt_numpy_array(numpy_array):
    # Ensure numbers are float and join them
    vector_str = ",".join(str(float(x)) for x in numpy_array)
    return AsIs("'" + "{" + vector_str + "}" + "'")

# Register adapters
register_adapter(np.float32, adapt_numpy_float32)
register_adapter(np.ndarray, adapt_numpy_array)


# ---------------------------------------------------------------------
# PostgreSQL Connection
# ---------------------------------------------------------------------
def get_connection():
    return psycopg2.connect(
        host="localhost",
        port=5432,
        dbname="data",
        user="postgres",
        password="123456",
    )


# ---------------------------------------------------------------------
# CREATE TABLE + EXTENSION + HNSW INDEX
# ---------------------------------------------------------------------
def setup_database():
    conn = get_connection()
    cur = conn.cursor()

    # Enable pgvector
    cur.execute("CREATE EXTENSION IF NOT EXISTS vector;")

    # Create table
    cur.execute("""
        CREATE TABLE IF NOT EXISTS product_embeddings (
            id TEXT PRIMARY KEY,
            description TEXT,
            embedding vector(768)
        );
    """)

    # Create HNSW index if missing
    cur.execute("""
        DO $$
        BEGIN
            IF NOT EXISTS (
                SELECT 1 FROM pg_indexes WHERE indexname = 'idx_embedding_hnsw'
            ) THEN
                CREATE INDEX idx_embedding_hnsw
                ON product_embeddings USING hnsw (embedding vector_l2_ops)
                WITH (m=16, ef_construction=64);
            END IF;
        END$$;
    """)

    conn.commit()
    conn.close()
