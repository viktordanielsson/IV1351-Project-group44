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
 allocated_hours DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
 PRIMARY KEY (teaching_activity_id,course_instance_id,employee_id),

 FOREIGN KEY (teaching_activity_id, course_instance_id) REFERENCES planned_activity (teaching_activity_id,course_instance_id) ON DELETE CASCADE,
 FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE NO ACTION
);

DROP TABLE university_constants CASCADE;
CREATE TABLE university_constants (
 constant_name VARCHAR(40) NOT NULL UNIQUE,
 constant_value DOUBLE PRECISION, 

 PRIMARY KEY (constant_name)
);

-----
--TRIGGER and FUNCTION ensuring the max_allowed_courses for an employee is followed when adding data
-----


CREATE OR REPLACE FUNCTION check_max_courses() RETURNS TRIGGER AS $$
DECLARE
    course_count INT;
    max_allowed INT;
    emp_study_year CHAR(4);
    emp_study_period VARCHAR(200);
BEGIN 
    SELECT ci.study_year, ci.study_period
    INTO emp_study_year, emp_study_period
    FROM course_instance ci
    WHERE ci.id = NEW.course_instance_id;

    SELECT CAST(constant_value AS INT)
    INTO max_allowed
    FROM university_constants
    WHERE constant_name = 'Max allowed courses';

    IF max_allowed IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT COUNT(DISTINCT ci.id)
    INTO course_count
    FROM employee_load_allocation ela
    JOIN planned_activity pa ON ela.teaching_activity_id = pa.teaching_activity_id
        AND ela.course_instance_id = pa.course_instance_id
    JOIN course_instance ci ON pa.course_instance_id = ci.id
    WHERE ela.employee_id = NEW.employee_id
    AND ci.study_year = emp_study_year
    AND ci.study_period = emp_study_period
    AND ci.id != NEW.course_instance_id;

    IF course_count >= max_allowed THEN
        RAISE EXCEPTION 'Employee % already has % courses in % %. Cannot exceed % courses.',
            NEW.employee_id, course_count, emp_study_year, emp_study_period, max_allowed;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER limit_allowed_courses
BEFORE INSERT ON employee_load_allocation
FOR EACH ROW
EXECUTE FUNCTION check_max_courses();

-----
--TRIGGER for deriving the planned_hours for a given course_intance when it is created 
--for the exmaination and administration teaching_activity
-----

CREATE OR REPLACE FUNCTION derive_hours() RETURNS TRIGGER AS $$
DECLARE
    course_hp DOUBLE PRECISION; 
    examination_constant INT;  --('Examination constant', 32),
    examination_num_student_constant DOUBLE PRECISION; --('Examination num_student constant', 0.725),
    admin_constant INT; --('Admin constant', 28),
    admin_num_student_constant DOUBLE PRECISION;  --('Admin num_student constant', 0.2),
    admin_hp_constant INT;    --('Admin HP constant', 2)
    examination_id INT;
    admin_id INT;
BEGIN
    SELECT cl.hp INTO course_hp
    FROM course_layout cl
    WHERE cl.id = NEW.course_layout_id;

    SELECT constant_value
    INTO  examination_constant
    FROM university_constants u
    WHERE u.constant_name = 'Examination constant';
    
    SELECT constant_value
    INTO  examination_num_student_constant
    FROM university_constants u
    WHERE u.constant_name = 'Examination num_student constant';
    
    SELECT constant_value
    INTO  admin_constant
    FROM university_constants u
    WHERE u.constant_name = 'Admin constant';
    
    SELECT constant_value
    INTO  admin_num_student_constant
    FROM university_constants u
    WHERE u.constant_name ='Admin num_student constant';

    SELECT constant_value
    INTO admin_hp_constant
    FROM university_constants u 
    WHERE u.constant_name = 'Admin HP constant';
    
    SELECT id
    INTO examination_id
    FROM teaching_activity t 
    WHERE t.activity_name = 'Examination';

    SELECT id
    INTO admin_id
    FROM teaching_activity t 
    WHERE t.activity_name = 'Administration';

    INSERT INTO planned_activity(course_instance_id, teaching_activity_id, planned_hours)
    VALUES
    (NEW.id, examination_id, examination_constant + examination_num_student_constant * NEW.num_students),
    (NEW.id, admin_id, admin_hp_constant*course_hp + admin_constant + admin_num_student_constant*NEW.num_students);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER derive_course_instance_hours 
AFTER INSERT ON course_instance
FOR ROW
EXECUTE FUNCTION derive_hours();