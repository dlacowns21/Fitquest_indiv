USE fitquest_test;

-- User table
CREATE TABLE `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    profile_image VARCHAR(255) DEFAULT '/uploads/profiles/default_profile.png',
    is_admin TINYINT DEFAULT 0,
    description varchar(20),
    INDEX idx_email (email)
);

-- Category table
CREATE TABLE category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    is_public TINYINT DEFAULT 0,
    color VARCHAR(255) DEFAULT '#000000',
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- Todo table
CREATE TABLE todo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    is_done TINYINT DEFAULT 0,
    content VARCHAR(100) NOT NULL,
    date DATE DEFAULT (CURRENT_DATE),
    todo_order INT,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, date)
);

-- Board table
CREATE TABLE board (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    tag VARCHAR(20) NOT NULL,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    writer VARCHAR(50) NOT NULL,
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    view_count INT DEFAULT 0,
    post_image VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_user_tag (user_id, tag)
);

-- Article table
CREATE TABLE article (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) NOT NULL
    -- ... other columns
);

-- Triggers
DROP TRIGGER IF EXISTS create_default_categories;
CREATE TRIGGER create_default_categories
AFTER INSERT ON `user`
FOR EACH ROW 
INSERT INTO category (user_id, title, is_public, color) VALUES 
    (NEW.id, '어깨', 1, '#2857aa'),
    (NEW.id, '등', 1, '#191919'),
    (NEW.id, '가슴', 1, '#785cb4'),
    (NEW.id, '하체', 1, '#1eae98'),
    (NEW.id, '유산소', 1, '#cd295a');

DROP TRIGGER IF EXISTS before_board_insert;
CREATE TRIGGER before_board_insert 
BEFORE INSERT ON board
FOR EACH ROW 
SET NEW.title = IF(LENGTH(NEW.title) > 50, CONCAT(LEFT(NEW.title, 47), '...'), NEW.title);


-- Comment table
CREATE TABLE comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    user_id INT,
    writer VARCHAR(50) NOT NULL,
    content VARCHAR(500) NOT NULL,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    parent_id INT,
    is_delete TINYINT DEFAULT 0,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE SET NULL,
    INDEX idx_board_id (board_id)
);

-- Token table
CREATE TABLE token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token TEXT NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expiry (expiry_date)
);

-- Activity table
CREATE TABLE activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    date DATE NOT NULL,
    ratio DECIMAL(5,2) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_user_date (user_id, date)
);

-- Like table
CREATE TABLE hit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_board_user (board_id, user_id)
);

-- Search History table
CREATE TABLE search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(id),
    UNIQUE INDEX unique_user_content (user_id, content)
);

-- Board Choseong table
CREATE TABLE board_choseong (
    board_id INT PRIMARY KEY,
    title_choseong VARCHAR(200),
    content_choseong TEXT,
    writer_choseong VARCHAR(100),
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    INDEX idx_title_cho (title_choseong),
    INDEX idx_writer_cho (writer_choseong)
);

-- User Choseong table
CREATE TABLE user_choseong (
    user_id INT PRIMARY KEY,
    name_choseong VARCHAR(200),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_name_cho (name_choseong)
);

-- Follow table
CREATE TABLE follow (
    id INT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT NOT NULL,
    following_id INT NOT NULL,
    FOREIGN KEY (follower_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_follower_following (follower_id, following_id)
);

-- Guestbook table
CREATE TABLE guestbook (
    id INT AUTO_INCREMENT PRIMARY KEY,
    target_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    date DATETIME DEFAULT NOW(),
    FOREIGN KEY (target_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_target_user (target_id, user_id, date)
);

-- Todo 테이블 복합 인덱스
CREATE INDEX idx_todo_user_date_done ON todo(user_id, date, is_done);

-- Activity 테이블 복합 인덱스
CREATE INDEX idx_activity_user_date ON activity(user_id, date);