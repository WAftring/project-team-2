--https://spring.io/guides/gs/accessing-data-mysql/
--https://stackoverflow.com/questions/6720050/foreign-key-constraints-when-to-use-on-update-and-on-delete

--src/main/resources/application.properties
-- spring.jpa.hibernate.ddl-auto=update
-- spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/solosavings
-- spring.datasource.username=team2
-- spring.datasource.password=cs673
-- spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
-- #spring.jpa.show-sql: true

--CREATE DATABASE AND USER
CREATE DATABASE IF NOT EXISTS solosavings;
CREATE USER 'team2'@'localhost' IDENTIFIED BY 'cs673';
GRANT ALL PRIVILEGES ON * . * TO 'team2'@'localhost';
FLUSH PRIVILEGES;

--CREATE TABLES

CREATE TABLE IF NOT EXISTS users (
 user_id INT NOT NULL AUTO_INCREMENT,
 username VARCHAR(255) NOT NULL,
 email VARCHAR(255) NOT NULL,
 password_hash VARCHAR(255) NOT NULL,
 registration_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 balance_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
 last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (user_id)
 ) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS income (
 income_id INT NOT NULL AUTO_INCREMENT,
 user_id INT NOT NULL,
 source VARCHAR(255) NOT NULL,
 amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
 income_date DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (income_id),
 FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
 ) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS expenses (
 expense_id INT NOT NULL AUTO_INCREMENT,
 user_id INT NOT NULL,
 category VARCHAR(255) NOT NULL,
 amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
 expense_date DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (expense_id),
 FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
 )ENGINE=INNODB;

 --SELECT STATEMENTS
-- SELECT * FROM `users`
-- SELECT `user_id`, `username`, `email`, `password_hash`, `registration_date`, `balance_amount`, `last_updated` FROM `users`

-- SELECT * FROM `income`
-- SELECT `income_id`, `user_id`, `source`, `amount`, `income_date` FROM `income`

-- SELECT * FROM `expenses` 
-- SELECT `expense_id`, `user_id`, `category`, `amount`, `expense_date` FROM `expenses`

--INSERT STATEMENTS
-- INSERT INTO `users`(`user_id`, `username`, `email`, `password_hash`, `registration_date`, `balance_amount`, `last_updated`) VALUES ('[value-1]','[value-2]','[value-3]','[value-4]','[value-5]','[value-6]','[value-7]')
-- INSERT INTO `income`(`income_id`, `user_id`, `source`, `amount`, `income_date`) VALUES ('[value-1]','[value-2]','[value-3]','[value-4]','[value-5]')
-- INSERT INTO `expenses`(`expense_id`, `user_id`, `category`, `amount`, `expense_date`) VALUES ('[value-1]','[value-2]','[value-3]','[value-4]','[value-5]')