-- Lược đồ cơ bản để triển khai trên máy chủ khác (MySQL/PostgreSQL hãy điều chỉnh cú pháp tương ứng)
CREATE TABLE IF NOT EXISTS departments (
    code TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    queue_prefix TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS doctors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    department_code TEXT NOT NULL REFERENCES departments(code)
);

CREATE TABLE IF NOT EXISTS patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    age INTEGER NOT NULL,
    symptoms TEXT NOT NULL,
    department_code TEXT NOT NULL REFERENCES departments(code),
    queue_number TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL REFERENCES patients(id),
    doctor_id INTEGER NOT NULL REFERENCES doctors(id),
    department_code TEXT NOT NULL,
    queue_number TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS symptom_rules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    keyword TEXT UNIQUE NOT NULL,
    department_code TEXT NOT NULL REFERENCES departments(code),
    explanation TEXT
);

INSERT OR IGNORE INTO departments(code, name, queue_prefix) VALUES
 ('TMH','Tai mũi họng','TMH'),
 ('HH','Hô hấp','HH'),
 ('NOI','Nội tổng quát','NT'),
 ('DA','Da liễu','DAL');
