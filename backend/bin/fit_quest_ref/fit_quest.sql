-- drop schema fit_quest;

CREATE SCHEMA IF NOT EXISTS fit_quest;

use fit_quest;

-- User table
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    is_admin TINYINT DEFAULT 0,
    INDEX idx_email (email)
);

-- Category table
CREATE TABLE category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    is_public TINYINT DEFAULT 0,
    color VARCHAR(10) DEFAULT '#000000',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- Todo table
CREATE TABLE todo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    is_done TINYINT DEFAULT 0,
    date DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, date)
);

-- Board table
CREATE TABLE board (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    tag VARCHAR(20) NOT NULL,
    date DATE DEFAULT (CURRENT_DATE),
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_tag (user_id, tag)
);

-- Comment table
CREATE TABLE comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    user_id INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    date DATE DEFAULT (CURRENT_DATE),
    parent_id INT,
    is_delete TINYINT DEFAULT 0,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE SET NULL,
    INDEX idx_board_id (board_id)
);

-- Article table
CREATE TABLE article (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    link TEXT NOT NULL,
    description TEXT NOT NULL,
    date DATE NOT NULL,
    INDEX idx_date (date)
);

-- Token table
CREATE TABLE token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token TEXT NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expiry (expiry_date)
);

-- Activity table
CREATE TABLE activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    date DATE NOT NULL,
    ratio DECIMAL(5,2) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_user_date (user_id, date)
);

-- Like table
CREATE TABLE hit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_board_user (board_id, user_id)
);

-- Title truncate trigger for article
DELIMITER //
CREATE TRIGGER before_article_insert 
BEFORE INSERT ON article
FOR EACH ROW
BEGIN
    IF LENGTH(NEW.title) > 50 THEN
        SET NEW.title = CONCAT(LEFT(NEW.title, 47), '...');
    END IF;
END//
DELIMITER ;

-- Title truncate trigger for board
DELIMITER //
CREATE TRIGGER before_board_insert 
BEFORE INSERT ON board
FOR EACH ROW
BEGIN
    IF LENGTH(NEW.title) > 50 THEN
        SET NEW.title = CONCAT(LEFT(NEW.title, 47), '...');
    END IF;
END//
DELIMITER ;

select * from activity;
select * from article;
select * from board;
select * from category;
select * from `comment`;
select * from hit;
select * from todo;
select * from token;
select * from `user`;