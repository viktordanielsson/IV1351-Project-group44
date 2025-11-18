import random
from faker import Faker
from faker.providers import barcode

fake = Faker()
fake.add_provider(barcode)

# --------------------------
# CONFIG
# --------------------------
NUM_PERSONS = 100
NUM_EMPLOYEES = 80
NUM_DEPARTMENTS = 5
NUM_JOB_TITLES = 10
NUM_SKILLS = 20

NUM_COURSES = 50
MAX_LAYOUTS_PER_COURSE = 3
MAX_INSTANCES_PER_LAYOUT = 2
STUDY_PERIODS = ["P1", "P2", "P3", "P4"]

TEACHING_ACTIVITIES = [
    ("Lecture", 3.6),
    ("Seminar", 1.8),
    ("Tutorial", 2.4),
    ("Lab", 2.4),
    ("Workshop", 0.9)
]

# --------------------------
# PERSONS
# --------------------------
print("-- PERSON")
persons = []
for i in range(1, NUM_PERSONS + 1):
    p = {
        "id": i,
        "personal_number": fake.unique.numerify("##########"),
        "first_name": fake.first_name(),
        "last_name": fake.last_name(),
        "phone_number": fake.msisdn()[:12],
        "address": fake.address().replace("\n", ", ")
    }
    persons.append(p) # removed ID from person because it should auto generate
    print(f"INSERT INTO person(personal_number, first_name, last_name, phone_number, address) "
          f"VALUES ("'{p['personal_number']}', '{p['first_name']}', "
          f"'{p['last_name']}', '{p['phone_number']}', '{p['address']}');")


# --------------------------
# JOB TITLES
# --------------------------
print("\n-- JOB TITLES")
job_titles = []
for i in range(1, NUM_JOB_TITLES + 1):
    jt = fake.job()
    job_titles.append((i, jt))
    print(f"INSERT INTO job_title(job_title) VALUES ('{jt}');")


# --------------------------
# SKILL SETS
# --------------------------
print("\n-- SKILL SET")
skill_sets = []
for i in range(1, NUM_SKILLS + 1):
    skill = fake.unique.word().capitalize()
    skill_sets.append((i, skill))
    print(f"INSERT INTO skill_set(skill_set) VALUES ('{skill}');")


# --------------------------
# EMPLOYEES
# --------------------------
print("\n-- EMPLOYEES")

employees = random.sample(persons, NUM_EMPLOYEES)

# Assign IDs and departments
for idx, e in enumerate(employees, start=1):
    e['id'] = idx
    e['employment_id'] = fake.ean(length=8)
    e['department_id'] = random.randint(1, NUM_DEPARTMENTS)
    e['manager_id'] = None  # to be assigned later

# --------------------------
# DEPARTMENTS
# --------------------------
print("\n-- DEPARTMENT")
department_managers = {}
departments = []
for dept_id in range(1, NUM_DEPARTMENTS + 1):
    # Pick employees in department
    dept_emps = [e for e in employees if e['department_id'] == dept_id]
    if not dept_emps:
        # if empty, assign random employee to this department
        e = random.choice(employees)
        e['department_id'] = dept_id
        dept_emps = [e]
    manager = random.choice(dept_emps)
    department_managers[dept_id] = manager['id']
    manager['manager_id'] = None
    dept_name = fake.word().capitalize() + " Department"
    departments.append((dept_id, dept_name, manager['id']))
    print(f"INSERT INTO department(department_name, manager_id) VALUES ('{dept_name}', {manager['id']});")

# Assign remaining employees to managers
for e in employees:
    dept = e['department_id']
    if e['id'] != department_managers[dept]:
        e['manager_id'] = department_managers[dept]

# Print employee inserts
for e in employees:
    salary = random.randint(30000, 90000)
    max_courses = random.randint(1, 6)
    job_id = random.randint(1, NUM_JOB_TITLES)
    manager_id_value = "NULL" if e['manager_id'] is None else f"{e['manager_id']}"
    print(f"INSERT INTO employee(employment_id, salary, manager_id, person_id, department_id, max_allowed_courses, job_title_id) "
          f"VALUES ({'{e['employment_id']}', {salary}, {manager_id_value}, "
          f"{e['id']}, {e['department_id']}, {max_courses}, {job_id});")


# --------------------------
# EMPLOYEE SKILL SET
# --------------------------
print("\n-- EMPLOYEE SKILL SET")
for e in employees:
    num_skills = random.randint(1, 5)
    assigned = random.sample(range(1, NUM_SKILLS + 1), num_skills)
    for skill_id in assigned:
        print(f"INSERT INTO employee_skill_set(skill_set_id, employee_id) VALUES ({skill_id}, {e['id']});")


# --------------------------
# COURSES
# --------------------------
print("\n-- COURSES")
course_rows = []
for course_id in range(1, NUM_COURSES + 1):
    code = fake.unique.bothify(text="??####").upper()
    name = fake.sentence(nb_words=3).replace("'", "")
    course_rows.append((course_id, code))
    print(f"INSERT INTO course(id, course_code, course_name) VALUES ({course_id}, '{code}', '{name}');")


# --------------------------
# COURSE LAYOUTS
# --------------------------
print("\n-- COURSE LAYOUTS")
layout_rows = []
layout_id_counter = 1
for course_id, code in course_rows:
    for ver in range(1, random.randint(1, MAX_LAYOUTS_PER_COURSE) + 1):
        min_s = random.randint(10, 49)
        max_s = min_s + random.randint(10, 200)
        hp = round(random.uniform(1, 20), 1)
        print(f"INSERT INTO course_layout(id, course_id, min_students, max_students, hp, version) "
              f"VALUES ({layout_id_counter}, {course_id}, {min_s}, {max_s}, {hp}, {ver});")
        layout_rows.append((layout_id_counter, course_id))
        layout_id_counter += 1


# --------------------------
# COURSE INSTANCES
# --------------------------
print("\n-- COURSE INSTANCES")
instance_rows = []
instance_pk = 1
instance_business_id = 1
for layout_id_val, course_id in layout_rows:
    for _ in range(random.randint(1, MAX_INSTANCES_PER_LAYOUT)):
        year = random.choice(["2023", "2024", "2025"])
        num_students = random.randint(1, 100)
        print(f"INSERT INTO course_instance(id, instance_id, num_students, study_year, course_layout_id, course_id) "
              f"VALUES ({instance_pk}, '{instance_business_id}', {num_students}, '{year}', {layout_id_val}, {course_id});")
        instance_rows.append((instance_pk, layout_id_val, course_id))
        instance_pk += 1
        instance_business_id += 1


# --------------------------
# STUDY PERIODS
# --------------------------
print("\n-- STUDY PERIOD")
for i, sp in enumerate(STUDY_PERIODS, start=1):
    print(f"INSERT INTO study_period(id, value) VALUES ({i}, '{sp}');")


# --------------------------
# COURSE INSTANCE STUDY PERIOD
# --------------------------
print("\n-- COURSE INSTANCE STUDY PERIOD")
for inst_id, layout_id_val, course_id in instance_rows:
    assigned = random.sample(range(1, len(STUDY_PERIODS)+1), random.randint(1, 2))
    for sp_id in assigned:
        print(f"INSERT INTO course_instance_study_period(study_period_id, course_instance_id) "
              f"VALUES ({sp_id}, {inst_id});")


# --------------------------
# TEACHING ACTIVITIES
# --------------------------
print("\n-- TEACHING ACTIVITIES")
for ta_id, (name, factor) in enumerate(TEACHING_ACTIVITIES, start=1):
    print(f"INSERT INTO teaching_activity(id, activity_name, factor) VALUES ({ta_id}, '{name}', {factor});")


# --------------------------
# PLANNED ACTIVITY
# --------------------------
print("\n-- PLANNED ACTIVITY")
for inst_id, layout_id_val, course_id in instance_rows:
    for ta_id in range(1, len(TEACHING_ACTIVITIES) + 1):
        hours = random.randint(5, 40)
        print(f"INSERT INTO planned_activity(teaching_activity_id, course_instance_id, planned_hours) "
              f"VALUES ({ta_id}, {inst_id}, {hours});")


# --------------------------
# EMPLOYEE LOAD ALLOCATION
# --------------------------
print("\n-- EMPLOYEE LOAD ALLOCATION")
for inst_id, layout_id_val, course_id in instance_rows:
    allocated = random.sample(employees, random.randint(1, min(4, len(employees))))
    for e in allocated:
        ta_id = random.randint(1, len(TEACHING_ACTIVITIES))
        print(f"INSERT INTO employee_load_allocation(teaching_activity_id, course_instance_id, employee_id) "
              f"VALUES ({ta_id}, {inst_id}, {e['id']});")
