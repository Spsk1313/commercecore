CREATE TABLE merchants (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,

    CONSTRAINT merchants_name_not_blank
        CHECK (name ~ '[^[:space:]]'),

    CONSTRAINT merchants_name_no_edge_whitespace
        CHECK (name !~ '^[[:space:]]|[[:space:]]$'),

    CONSTRAINT merchants_name_no_control_chars
        CHECK (name !~ '[[:cntrl:]]')
);