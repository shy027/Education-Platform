-- =============================================
-- 测试数据SQL脚本
-- 用于前台基础API测试
-- =============================================

USE education_platform;

-- =============================================
-- 1. 角色数据 (如果不存在则插入)
-- =============================================
INSERT IGNORE INTO user_role (role_code, role_name, description, sort_order, status, created_time, updated_time, is_deleted) VALUES
('ADMIN', '管理员', '系统管理员', 1, 1, NOW(), NOW(), 0),
('TEACHER', '教师', '教师角色', 2, 1, NOW(), NOW(), 0),
('STUDENT', '学生', '学生角色', 3, 1, NOW(), NOW(), 0);

-- =============================================
-- 2. 用户数据 (如果不存在则插入)
-- 密码统一为: 123456 (BCrypt加密后)
-- =============================================
INSERT IGNORE INTO user_account (username, password, real_name, email, phone, avatar_url, gender, status, created_time, updated_time, is_deleted) VALUES
-- 管理员
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', '13800000000', 'https://via.placeholder.com/150', 1, 1, NOW(), NOW(), 0),
-- 教师
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李老师', 'teacher1@example.com', '13800000001', 'https://via.placeholder.com/150', 1, 1, NOW(), NOW(), 0),
('teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王老师', 'teacher2@example.com', '13800000002', 'https://via.placeholder.com/150', 2, 1, NOW(), NOW(), 0),
-- 学生
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'student1@example.com', '13800000003', 'https://via.placeholder.com/150', 1, 1, NOW(), NOW(), 0),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 'student2@example.com', '13800000004', 'https://via.placeholder.com/150', 2, 1, NOW(), NOW(), 0),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 'student3@example.com', '13800000005', 'https://via.placeholder.com/150', 1, 1, NOW(), NOW(), 0);

-- =============================================
-- 3. 用户角色关联 (如果不存在则插入)
-- =============================================
INSERT IGNORE INTO user_rel_role (user_id, role_id, school_id, created_time) VALUES
-- 管理员
(1, 1, NULL, NOW()),
-- 教师
(2, 2, 1, NOW()),
(3, 2, 1, NOW()),
-- 学生
(4, 3, 1, NOW()),
(5, 3, 1, NOW()),
(6, 3, 1, NOW());

-- =============================================
-- 4. 学校数据 (如果不存在则插入)
-- =============================================
INSERT IGNORE INTO user_school (school_code, school_name, province, city, address, logo_url, description, contact_person, contact_phone, status, created_by, created_time, updated_time, is_deleted) VALUES
('NJPI', '南京工业职业技术大学', '江苏省', '南京市', '仙林大学城羊山北路1号', 'https://via.placeholder.com/200', '南京工业职业技术大学是一所全日制公办普通高校,是国家示范性高职院校。', '张老师', '025-85864000', 1, 1, NOW(), NOW(), 0),
('NJU', '南京大学', '江苏省', '南京市', '鼓楼区汉口路22号', 'https://via.placeholder.com/200', '南京大学是中华人民共和国教育部直属的全国重点大学。', '李老师', '025-83593186', 1, 1, NOW(), NOW(), 0),
('SEU', '东南大学', '江苏省', '南京市', '玄武区四牌楼2号', 'https://via.placeholder.com/200', '东南大学是中央直管、教育部直属的全国重点大学。', '王老师', '025-52090000', 1, 1, NOW(), NOW(), 0),
('NUAA', '南京航空航天大学', '江苏省', '南京市', '秦淮区御道街29号', 'https://via.placeholder.com/200', '南京航空航天大学是工业和信息化部直属的全国重点大学。', '赵老师', '025-84892899', 1, 1, NOW(), NOW(), 0),
('NJUST', '南京理工大学', '江苏省', '南京市', '玄武区孝陵卫200号', 'https://via.placeholder.com/200', '南京理工大学是工业和信息化部直属的全国重点大学。', '钱老师', '025-84315114', 1, 1, NOW(), NOW(), 0);

-- =============================================
-- 5. 学校成员数据 (如果不存在则插入)
-- =============================================
INSERT IGNORE INTO user_school_member (school_id, user_id, member_type, department, job_number, join_time, status, created_time, updated_time, is_deleted) VALUES
-- 南京工业职业技术大学
(1, 2, 2, '计算机与软件学院', 'T2021001', NOW(), 1, NOW(), NOW(), 0),
(1, 3, 2, '机电工程学院', 'T2021002', NOW(), 1, NOW(), NOW(), 0),
(1, 4, 3, '计算机与软件学院', '2205221127', NOW(), 1, NOW(), NOW(), 0),
(1, 5, 3, '计算机与软件学院', '2205221128', NOW(), 1, NOW(), NOW(), 0),
(1, 6, 3, '机电工程学院', '2205221129', NOW(), 1, NOW(), NOW(), 0);

-- =============================================
-- 6. 思政标签数据 (如果不存在则插入)
-- =============================================
INSERT IGNORE INTO resource_tag (tag_name, tag_category, description, use_count, sort_order, status, created_time, updated_time, is_deleted) VALUES
-- 核心价值观
('爱国', '核心价值观', '培养爱国主义精神', 150, 1, 1, NOW(), NOW(), 0),
('敬业', '核心价值观', '培养敬业精神', 120, 2, 1, NOW(), NOW(), 0),
('诚信', '核心价值观', '培养诚信意识', 110, 3, 1, NOW(), NOW(), 0),
('友善', '核心价值观', '培养友善品质', 100, 4, 1, NOW(), NOW(), 0),
-- 思想品德
('奉献', '思想品德', '培养奉献精神', 130, 5, 1, NOW(), NOW(), 0),
('责任', '思想品德', '培养责任意识', 125, 6, 1, NOW(), NOW(), 0),
('担当', '思想品德', '培养担当精神', 115, 7, 1, NOW(), NOW(), 0),
-- 科学精神
('创新', '科学精神', '培养创新精神', 140, 8, 1, NOW(), NOW(), 0),
('求真', '科学精神', '培养求真精神', 105, 9, 1, NOW(), NOW(), 0),
('严谨', '科学精神', '培养严谨态度', 95, 10, 1, NOW(), NOW(), 0);

-- =============================================
-- 7. 智能案例数据 (如果不存在则插入)
-- =============================================
INSERT IGNORE INTO resource_case (case_title, case_content, case_summary, subject_area, ideological_elements, keywords, difficulty_level, suitable_grade, source_type, view_count, use_count, like_count, audit_status, status, creator_id, created_time, updated_time, is_deleted) VALUES
('袁隆平的科研精神', 
'袁隆平院士一生致力于杂交水稻研究,为解决中国人民的温饱问题和保障国家粮食安全做出了卓越贡献。他的科研精神体现了爱国、奉献、创新的思政元素...', 
'通过袁隆平院士的事迹,培养学生的科研精神和爱国情怀', 
'生物学', '爱国,奉献,创新', '袁隆平,科研,杂交水稻', 2, '大一,大二', 1, 1200, 85, 320, 1, 1, 2, NOW(), NOW(), 0),

('钱学森的归国之路', 
'钱学森先生放弃美国优厚待遇,冲破重重阻碍回到祖国,为中国航天事业奠定基础。他的选择体现了深厚的爱国情怀和强烈的责任担当...', 
'学习钱学森先生的爱国精神和科学态度', 
'物理学', '爱国,责任,担当', '钱学森,航天,爱国', 2, '大一,大二,大三', 1, 980, 72, 280, 1, 1, 2, NOW(), NOW(), 0),

('屠呦呦与青蒿素', 
'屠呦呦研究员从中医古籍中获得灵感,经过数百次实验,成功提取青蒿素,挽救了全球数百万人的生命。她的研究体现了创新精神和严谨态度...', 
'了解屠呦呦的科研历程,培养创新精神', 
'医学', '创新,严谨,奉献', '屠呦呦,青蒿素,诺贝尔奖', 3, '大二,大三', 1, 850, 65, 245, 1, 1, 2, NOW(), NOW(), 0);

-- =============================================
-- 8. 案例标签关联 (如果不存在则插入)
-- =============================================
INSERT IGNORE INTO resource_case_rel_tag (case_id, tag_id, created_time) VALUES
-- 袁隆平案例
(1, 1, NOW()), -- 爱国
(1, 5, NOW()), -- 奉献
(1, 8, NOW()), -- 创新
-- 钱学森案例
(2, 1, NOW()), -- 爱国
(2, 6, NOW()), -- 责任
(2, 7, NOW()), -- 担当
-- 屠呦呦案例
(3, 8, NOW()), -- 创新
(3, 10, NOW()), -- 严谨
(3, 5, NOW()); -- 奉献

-- =============================================
-- 查询验证
-- =============================================
-- 验证用户数据
SELECT u.id, u.username, u.real_name, r.role_name 
FROM user_account u 
LEFT JOIN user_rel_role urr ON u.id = urr.user_id 
LEFT JOIN user_role r ON urr.role_id = r.id 
WHERE u.is_deleted = 0;

-- 验证学校数据
SELECT id, school_code, school_name, province, city 
FROM user_school 
WHERE is_deleted = 0;

-- 验证案例数据
SELECT c.id, c.case_title, c.subject_area, GROUP_CONCAT(t.tag_name) as tags
FROM resource_case c
LEFT JOIN resource_case_rel_tag crt ON c.id = crt.case_id
LEFT JOIN resource_tag t ON crt.tag_id = t.id
WHERE c.is_deleted = 0
GROUP BY c.id;
