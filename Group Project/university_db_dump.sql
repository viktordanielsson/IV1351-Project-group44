DROP SCHEMA university_db_dump CASCADE;

CREATE TABLE course (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_code VARCHAR(200) NOT NULL UNIQUE,
 course_name VARCHAR(200) NOT NULL
);

ALTER TABLE course ADD CONSTRAINT PK_course PRIMARY KEY (id);


CREATE TABLE course_layout (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 min_students INT NOT NULL,
 max_students INT NOT NULL,
 hp DOUBLE PRECISION NOT NULL,
 version INT NOT NULL
);

ALTER TABLE course_layout ADD CONSTRAINT PK_course_layout PRIMARY KEY (id,course_id);


CREATE TABLE department (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 department_name VARCHAR(200) NOT NULL UNIQUE,
 manager_id VARCHAR(200) UNIQUE
);

ALTER TABLE department ADD CONSTRAINT PK_department PRIMARY KEY (id);


CREATE TABLE job_title (
 id CHAR(10) NOT NULL,
 job_title VARCHAR(200) NOT NULL UNIQUE
);

ALTER TABLE job_title ADD CONSTRAINT PK_job_title PRIMARY KEY (id);


CREATE TABLE person (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 personal_number CHAR(12) NOT NULL UNIQUE,
 first_name VARCHAR(200) NOT NULL,
 last_name VARCHAR(200) NOT NULL,
 phone_number VARCHAR(13) NOT NULL,
 adress VARCHAR(200) NOT NULL
);

ALTER TABLE person ADD CONSTRAINT PK_person PRIMARY KEY (id);


CREATE TABLE skill_set (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 skill_set VARCHAR(100) NOT NULL UNIQUE
);

ALTER TABLE skill_set ADD CONSTRAINT PK_skill_set PRIMARY KEY (id);


CREATE TABLE study_period (
 study_period_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 value CHAR(2) NOT NULL UNIQUE
);

ALTER TABLE study_period ADD CONSTRAINT PK_study_period PRIMARY KEY (study_period_id);


CREATE TABLE teaching_activity (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 activity_name VARCHAR(20) NOT NULL UNIQUE,
 factor DOUBLE PRECISION NOT NULL
);

ALTER TABLE teaching_activity ADD CONSTRAINT PK_teaching_activity PRIMARY KEY (id);


CREATE TABLE course_instance (
 course_layout_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 instance_id VARCHAR(200) NOT NULL UNIQUE,
 num_students INT NOT NULL,
 study_year CHAR(4) NOT NULL
);

ALTER TABLE course_instance ADD CONSTRAINT PK_course_instance PRIMARY KEY (course_layout_id,course_id);


CREATE TABLE course_instance_study_period (
 course_layout_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 study_period_id INT GENERATED ALWAYS AS IDENTITY NOT NULL
);

ALTER TABLE course_instance_study_period ADD CONSTRAINT PK_course_instance_study_period PRIMARY KEY (course_layout_id,course_id,study_period_id);


CREATE TABLE employee (
 person_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE NOT NULL,
 department_id INT GENERATED ALWAYS AS IDENTITY REFERENCES department(id) NOT NULL ON DELETE NO ACTION,
 job_title_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 employment_id VARCHAR(200) NOT NULL UNIQUE,
 salary INT NOT NULL,
 manager_id VARCHAR(200) REFERENCES employee(person_id),
 max_allowed_courses INT NOT NULL
);

ALTER TABLE employee ADD CONSTRAINT PK_employee PRIMARY KEY (person_id,department_id,job_title_id);


CREATE TABLE employee_skill_set (
 person_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 department_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 id_0 CHAR(10) NOT NULL
);

ALTER TABLE employee_skill_set ADD CONSTRAINT PK_employee_skill_set PRIMARY KEY (person_id,department_id,id,id_0);


CREATE TABLE planned_activity (
 course_layout_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 teaching_activity_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 planned_hours INT NOT NULL
);

ALTER TABLE planned_activity ADD CONSTRAINT PK_planned_activity PRIMARY KEY (course_layout_id,course_id,teaching_activity_id);


CREATE TABLE employee_load_allocation (
 person_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 department_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_layout_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 teaching_activity_id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 job_title_id CHAR(10) NOT NULL
);

ALTER TABLE employee_load_allocation ADD CONSTRAINT PK_employee_load_allocation PRIMARY KEY (person_id,department_id,course_layout_id,course_id,teaching_activity_id,job_title_id);


ALTER TABLE course_layout ADD CONSTRAINT FK_course_layout_0 FOREIGN KEY (course_id) REFERENCES course (id);


ALTER TABLE course_instance ADD CONSTRAINT FK_course_instance_0 FOREIGN KEY (course_layout_id,course_id) REFERENCES course_layout (id,course_id);


ALTER TABLE course_instance_study_period ADD CONSTRAINT FK_course_instance_study_period_0 FOREIGN KEY (course_layout_id,course_id) REFERENCES course_instance (course_layout_id,course_id);
ALTER TABLE course_instance_study_period ADD CONSTRAINT FK_course_instance_study_period_1 FOREIGN KEY (study_period_id) REFERENCES study_period (study_period_id);


ALTER TABLE employee ADD CONSTRAINT FK_employee_0 FOREIGN KEY (person_id) REFERENCES person (id);
ALTER TABLE employee ADD CONSTRAINT FK_employee_1 FOREIGN KEY (department_id) REFERENCES department (id);
ALTER TABLE employee ADD CONSTRAINT FK_employee_2 FOREIGN KEY (job_title_id) REFERENCES job_title (id);


ALTER TABLE employee_skill_set ADD CONSTRAINT FK_employee_skill_set_0 FOREIGN KEY (person_id,department_id,id_0) REFERENCES employee (person_id,department_id,job_title_id);
ALTER TABLE employee_skill_set ADD CONSTRAINT FK_employee_skill_set_1 FOREIGN KEY (id) REFERENCES skill_set (id);


ALTER TABLE planned_activity ADD CONSTRAINT FK_planned_activity_0 FOREIGN KEY (course_layout_id,course_id) REFERENCES course_instance (course_layout_id,course_id);
ALTER TABLE planned_activity ADD CONSTRAINT FK_planned_activity_1 FOREIGN KEY (teaching_activity_id) REFERENCES teaching_activity (id);


ALTER TABLE employee_load_allocation ADD CONSTRAINT FK_employee_load_allocation_0 FOREIGN KEY (person_id,department_id,job_title_id) REFERENCES employee (person_id,department_id,job_title_id);
ALTER TABLE employee_load_allocation ADD CONSTRAINT FK_employee_load_allocation_1 FOREIGN KEY (course_layout_id,course_id,teaching_activity_id) REFERENCES planned_activity (course_layout_id,course_id,teaching_activity_id);


