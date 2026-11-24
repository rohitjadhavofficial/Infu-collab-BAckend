CREATE TABLE IF NOT EXISTS Influ_login (
	email VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	name VARCHAR(255) NOT NULL,
	followers BIGINT NULL,
	categories JSON NULL,
	averageCollaborationPriceInr DECIMAL(12,2) NULL,
	location VARCHAR(255) NULL,
	countryCode VARCHAR(10) NULL,
	mobile VARCHAR(20) NULL,
	password VARCHAR(255) NULL,
	submittedAt TIMESTAMP(3) NULL,
	PRIMARY KEY (email)
);

CREATE TABLE IF NOT EXISTS business_login (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	business_name VARCHAR(255) NOT NULL,
	category VARCHAR(100) NOT NULL,
	city VARCHAR(100) NOT NULL,
	email VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	phone VARCHAR(20) NOT NULL,
	password VARCHAR(255) NOT NULL,
	UNIQUE KEY uk_business_login_email (email)
);

CREATE TABLE IF NOT EXISTS chat_conversations (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	source_email VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	target_email VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	user_low VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	user_high VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	created_at TIMESTAMP(3) NOT NULL,
	updated_at TIMESTAMP(3) NOT NULL,
	UNIQUE KEY uk_chat_conversation_pair (user_low, user_high),
	INDEX idx_chat_conversations_updated_at (updated_at)
);

CREATE TABLE IF NOT EXISTS chat_messages (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	conversation_id BIGINT NOT NULL,
	source_email VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	target_email VARCHAR(255) NOT NULL COLLATE utf8mb4_unicode_ci,
	message TEXT NOT NULL,
	sent_at TIMESTAMP(3) NOT NULL,
	is_read BOOLEAN NOT NULL DEFAULT FALSE,
	CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversations (id),
	INDEX idx_chat_messages_conversation_id (conversation_id),
	INDEX idx_chat_messages_target_email (target_email),
	INDEX idx_chat_messages_sent_at (sent_at)
);

CREATE TABLE IF NOT EXISTS ads (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	ad_title VARCHAR(255) NOT NULL,
	business_name VARCHAR(255) NOT NULL,
	business_email VARCHAR(255) NOT NULL,
	category VARCHAR(100) NOT NULL,
	budget DECIMAL(12,2) NOT NULL,
	ad_type VARCHAR(100) NOT NULL,
	description TEXT NULL,
	created_at TIMESTAMP(3) NOT NULL
);
