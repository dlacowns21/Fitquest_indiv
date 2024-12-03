#drop schema fitquest;

CREATE SCHEMA IF NOT EXISTS fitquest;

use fitquest;

-- User table
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
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
    content VARCHAR(100) NOT NULL,
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
    writer VARCHAR(50) NOT NULL,
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    view_count INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (writer) REFERENCES user(name) ON DELETE CASCADE,
    INDEX idx_user_tag (user_id, tag)
);

-- Comment table
CREATE TABLE comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    user_id INT,
    writer VARCHAR(50) NOT NULL,
    content VARCHAR(500) NOT NULL,
    date DATE DEFAULT (CURRENT_DATE),
    parent_id INT,
    is_delete TINYINT DEFAULT 0,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE SET NULL,
    FOREIGN KEY (writer) REFERENCES user(name) ON DELETE CASCADE,
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

# user 가 새롭게 생성되면, 카테고리 1,카테고리 2,카테고리 3을 default로 생성함
DELIMITER //
CREATE TRIGGER create_default_categories
AFTER INSERT ON user
FOR EACH ROW
BEGIN
    -- 카테고리1 생성
    INSERT INTO category (user_id, title, is_public, color)
    VALUES (NEW.id, '어깨', 1, '#2857aa');
    
    -- 카테고리2 생성
    INSERT INTO category (user_id, title, is_public, color)
    VALUES (NEW.id, '등', 1, '#191919');
    
    -- 카테고리3 생성
    INSERT INTO category (user_id, title, is_public, color)
    VALUES (NEW.id, '가슴', 1, '#785cb4');
    
    -- 카테고리4 생성
    INSERT INTO category (user_id, title, is_public, color)
    VALUES (NEW.id, '하체', 1, '#1eae98');
    
    -- 카테고리5 생성
    INSERT INTO category (user_id, title, is_public, color)
    VALUES (NEW.id, '유산소', 1, '#cd295a');
END//
DELIMITER ;

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

# 11/18 추가
# todo 순서 컬럼 추가
ALTER TABLE todo ADD COLUMN todo_order INT;

# 기존 데이터에 대해 임시로 순서 부여
SET SQL_SAFE_UPDATES = 0;
UPDATE todo t1
JOIN (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY category_id ORDER BY id) as rn
    FROM todo
) t2 ON t1.id = t2.id
SET t1.todo_order = t2.rn;
SET SQL_SAFE_UPDATES = 1;

select * from activity;
select * from article;
select * from board;
select * from category;
select * from `comment`;
select * from hit;
select * from todo;
select * from token;
select * from `user`;
use fitquest;

INSERT INTO board (user_id, tag, writer, title, content, view_count) VALUES
(1, '헬스', '임채준', '벤치프레스 자세 질문', '벤치프레스 할 때 어깨가 아픈데 혹시 자세가 잘못된 걸까요? 팁 부탁드립니다.', 245),
(1, '러닝', '임채준', '초보 러너 5km 완주 후기', '처음으로 5km를 완주했습니다! 여러분의 응원 덕분입니다. 다음 목표는 10km!', 178),
(2, '식단', '배성훈', '벌크업 식단 공유합니다', '하루 3000kcal 벌크업 식단 공유드립니다. 영양소 배분 비율도 함께 올려요.', 421),
(1, '홈트', '임채준', '홈트 3개월 변화', '홈트레이닝 시작한지 3개월, 제 변화 과정을 공유합니다. 같이 힘내요!', 367),
(2, '크로스핏', '배성훈', '크로스핏 입문기', '처음 크로스핏을 시작하시는 분들을 위한 팁 공유드립니다.', 156),
(2, '헬스', '배성훈', '데드리프트 폼체크 부탁드려요', '데드리프트 1RM 120kg 도전 영상입니다. 폼 봐주세요!', 289),
(2, '요가', '배성훈', '요가 30일 챌린지 후기', '30일 동안 매일 요가한 결과, 놀라운 변화가 있었어요.', 198),
(1, '식단', '임채준', '다이어트 식단 꿀팁', '다이어트 하면서도 맛있게 먹는 방법 공유드려요.', 512),
(1, '러닝', '임채준', '러닝화 추천해주세요', '발볼이 넓은데 어떤 러닝화가 좋을까요?', 167),
(1, '헬스', '임채준', '3개월 벌크업 성공기', '3개월 동안 10kg 늘리는데 성공했습니다! 노하우 공유해요.', 445),
(1, '홈트', '임채준', '홈트용 운동기구 추천', '적은 예산으로 구매한 홈트 필수템들 추천드립니다.', 234),
(2, '크로스핏', '배성훈', '오늘의 와드 완료!', '오늘의 와드 기록 공유하고 싶어서 글 남깁니다.', 145),
(1, '식단', '임채준', '식단 일기 시작합니다', '오늘부터 식단 일기 쓰면서 다이어트 시작해요!', 167),
(1, '요가', '임채준', '요가 입문자 추천 동작', '요가 처음 시작하시는 분들을 위한 기본 동작 설명드려요.', 276),
(1, '헬스', '임채준', '프로틴 추천 부탁드려요', '입문자한테 좋은 프로틴 추천해주세요!', 198),
(2, '러닝', '배성훈', '처음 하프마라톤 완주!', '드디어 하프마라톤 완주했습니다! 준비 과정 공유할게요.', 334),
(1, '홈트', '임채준', '퇴근 후 30분 홈트 루틴', '바쁜 직장인들을 위한 효율적인 홈트 루틴 공유합니다.', 423),
(2, '크로스핏', '배성훈', '크로스핏 6개월 후기', '6개월 동안 크로스핏 하면서 느낀 점 공유드려요.', 267),
(2, '식단', '배성훈', '식단 고민 상담해주세요', '체중감량과 근력운동을 같이 하고 있는데 식단 조언 부탁드려요.', 189),
(2, '헬스', '배성훈', '1년 운동 변화 공유', '1년 동안의 제 변화 과정을 사진과 함께 공유합니다!', 678);

-- 1. user 테이블의 name 컬럼에 UNIQUE 인덱스 추가
ALTER TABLE user
ADD UNIQUE INDEX idx_user_name (name);

-- 기존 외래 키 제약조건 삭제
ALTER TABLE board
DROP FOREIGN KEY fk_board_writer;

-- writer 컬럼의 외래 키 제약조건 제거
ALTER TABLE board 
MODIFY COLUMN writer VARCHAR(50);

-- comment 테이블의 외래 키 제약조건 삭제
ALTER TABLE comment
DROP FOREIGN KEY fk_comment_writer;

-- writer 컬럼의 외래 키 제약조건 제거
ALTER TABLE comment 
MODIFY COLUMN writer VARCHAR(50);

-- board 테이블
ALTER TABLE board 
MODIFY COLUMN date DATETIME DEFAULT CURRENT_TIMESTAMP;

-- comment 테이블
ALTER TABLE comment 
MODIFY COLUMN date DATETIME DEFAULT CURRENT_TIMESTAMP;

delete from board where id > 0;
delete from comment where id > 0;
