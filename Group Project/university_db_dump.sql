DROP TABLE course CASCADE;
CREATE TABLE course (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 course_code  VARCHAR(200) NOT NULL UNIQUE,
 course_name VARCHAR(200) NOT NULL
);


DROP TABLE course_layout CASCADE;
CREATE TABLE course_layout (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 course_id INT NOT NULL,
 min_students INT NOT NULL,
 max_students INT NOT NULL,
 hp DOUBLE PRECISION NOT NULL,
 version INT NOT NULL,

 FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);


DROP TABLE department CASCADE;
CREATE TABLE department (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 department_name  VARCHAR(200) NOT NULL UNIQUE,
 manager_id  VARCHAR(200) UNIQUE
);


DROP TABLE job_title CASCADE;
CREATE TABLE job_title (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 job_title  VARCHAR(200) NOT NULL UNIQUE
);


DROP TABLE person CASCADE;
CREATE TABLE person (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 personal_number  CHAR(12) NOT NULL UNIQUE,
 first_name VARCHAR(200) NOT NULL,
 last_name VARCHAR(200) NOT NULL,
 phone_number VARCHAR(13) NOT NULL,
 address VARCHAR(200) NOT NULL
);


DROP TABLE skill_set CASCADE;
CREATE TABLE skill_set (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 skill_set  VARCHAR(100) NOT NULL UNIQUE
);


DROP TABLE teaching_activity CASCADE;
CREATE TABLE teaching_activity (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 activity_name  VARCHAR(20) NOT NULL UNIQUE,
 factor DOUBLE PRECISION NOT NULL
);


DROP TABLE course_instance CASCADE;
CREATE TABLE course_instance (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 course_layout_id INT NOT NULL,
 instance_id  VARCHAR(200) NOT NULL UNIQUE,
 num_students INT NOT NULL,
 study_year CHAR(4) NOT NULL,
 study_period VARCHAR(200) NOT NULL,

 FOREIGN KEY (course_layout_id) REFERENCES course_layout (id) ON DELETE CASCADE
);


DROP TABLE employee CASCADE;
CREATE TABLE employee (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL PRIMARY KEY,
 employment_id  VARCHAR(200) NOT NULL UNIQUE,
 salary INT NOT NULL,
 person_id  INT NOT NULL,
 department_id INT NOT NULL,
 max_allowed_courses INT NOT NULL,
 job_title_id INT NOT NULL,
 manager_id VARCHAR(200),

 FOREIGN KEY (person_id ) REFERENCES person (id) ON DELETE CASCADE,
 FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE NO ACTION,
 FOREIGN KEY (job_title_id) REFERENCES job_title (id) ON DELETE NO ACTION
);


DROP TABLE employee_skill_set CASCADE;
CREATE TABLE employee_skill_set (
 skill_set_id INT NOT NULL,
 employee_id INT NOT NULL,

 PRIMARY KEY (skill_set_id,employee_id),

 FOREIGN KEY (skill_set_id) REFERENCES skill_set (id) ON DELETE CASCADE,
 FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE
);


DROP TABLE planned_activity CASCADE;
CREATE TABLE planned_activity (
 teaching_activity_id INT NOT NULL,
 course_instance_id INT NOT NULL,
 planned_hours INT NOT NULL,

 PRIMARY KEY (teaching_activity_id,course_instance_id),

 FOREIGN KEY (teaching_activity_id) REFERENCES teaching_activity (id),
 FOREIGN KEY (course_instance_id) REFERENCES course_instance (id)
);


DROP TABLE employee_load_allocation CASCADE;
CREATE TABLE employee_load_allocation (
 teaching_activity_id INT NOT NULL,
 course_instance_id INT NOT NULL,
 employee_id INT NOT NULL,

 PRIMARY KEY (teaching_activity_id,course_instance_id,employee_id),

 FOREIGN KEY (teaching_activity_id, course_instance_id) REFERENCES planned_activity (teaching_activity_id,course_instance_id) ON DELETE CASCADE,
 FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE NO ACTION
);