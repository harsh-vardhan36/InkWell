CREATE DATABASE IF NOT EXISTS inkwell_auth;
CREATE DATABASE IF NOT EXISTS inkwell_posts;
CREATE DATABASE IF NOT EXISTS inkwell_comments;
CREATE DATABASE IF NOT EXISTS inkwell_categories;
CREATE DATABASE IF NOT EXISTS inkwell_media;
CREATE DATABASE IF NOT EXISTS inkwell_newsletter;
CREATE DATABASE IF NOT EXISTS inkwell_notifications;
CREATE DATABASE IF NOT EXISTS inkwell_admin;

-- Create admin user for all services
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'InkWellCloud1';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%';
FLUSH PRIVILEGES;
