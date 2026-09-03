CREATE TABLE boxes (
    id UUID PRIMARY KEY,
    txref VARCHAR(20) NOT NULL UNIQUE,
    weight_limit_grams INTEGER NOT NULL,
    battery_percentage INTEGER NOT NULL,
    state VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT boxes_weight_limit_check
        CHECK (weight_limit_grams > 0 AND weight_limit_grams <= 500),

    CONSTRAINT boxes_battery_percentage_check
        CHECK (battery_percentage >= 0 AND battery_percentage <= 100),

    CONSTRAINT boxes_state_check
        CHECK (
            state IN (
                'IDLE',
                'LOADING',
                'LOADED',
                'DELIVERING',
                'DELIVERED',
                'RETURNING'
            )
        )
);

CREATE TABLE items (
    id UUID PRIMARY KEY,
    box_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    weight_grams INTEGER NOT NULL,
    code VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT items_weight_check
        CHECK (weight_grams > 0),

    CONSTRAINT items_box_fk
        FOREIGN KEY (box_id)
        REFERENCES boxes(id)
        ON DELETE CASCADE,

    CONSTRAINT items_box_code_unique
        UNIQUE (box_id, code)
);

CREATE INDEX idx_items_box_id
    ON items(box_id);

CREATE INDEX idx_boxes_state_battery
    ON boxes(state, battery_percentage);