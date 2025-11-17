import random
from faker import Faker

# ---------------------------------------------------------
# INITIAL SETUP
# ---------------------------------------------------------
fake = Faker()
Faker.seed(1)
fake.unique.clear()

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

# ---------------------------------------------------------
# COURSE CODE GENERATOR
# ---------------------------------------------------------
def course_code():
    return fake.unique.bothify("??####").upper()


# ---------------------------------------------------------
# PERSON
# ---------------------------------------------------------
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
    persons.append(p)
    print(
        f"INSERT INTO person (personal_number, first_name, last_name, phone_number, adress) "
        f"VALUES ('{p['personal_number']}', '{p['first_name']}', '{p['last_name']}', "
        f"'{p['phone_number']}', '{p['address']}');"
    )


# ---------------------------------------------------------
# JOB TITLES
# ---------------------------------------------------------
print("\n-- JOB TITLES")
job_titles = []
for i in range(1, NUM_JOB_TITLES + 1):
    jt = fake.job().replace("'", "")
    job_titles.append(jt)
    print(f"INSERT INTO job_title (job_title) VALUES ('{jt}');")


# ---------------------------------------------------------
# SKILL SET
# ---------------------------------------------------------
print("\n-- SKILL SET")
skills = []
for i in range(1, NUM_SKILLS + 1):
    skill = fake.unique.word().capitalize()
    skills.append(skill)
    print(f"INSERT INTO skill_set (skill_set) VALUES ('{skill}');")


# ---------------------------------------------------------
# EMPLOYEES (select persons)
# ---------------------------------------------------------
print("\n-- EMPLOYEES")

# randomly choose persons
employee_persons = random.sample(persons, NUM_EMPLOYEES)

employees = []
for idx, p in enumerate(employee_persons, start=1):
    emp = {
        "id": idx,
        "person_id": p["id"],
        "employment_id": fake.unique.bothify("EMP#####"),
    }
    employees.append(emp)

# Insert employees
for emp in employees:
    dept = random.randint(1, NUM_DEPARTMENTS)
    job = random.randint(1, NUM_JOB_TITLES)
    salary = random.randint(30000, 90000)
    max_courses = random.randint(1, 6)

    # Assign a manager by employment_id (string)
    manager = random.choice([e for e in employees if e != emp])
    manager_id = manager["employment_id"]

    print(
        "INSERT INTO employee (employment_id, salary, manager_id, person_id, department_id, max_allowed_courses, job_title_id) "
        f"VALUES ('{emp['employment_id']}', {salary}, '{manager_id}', {emp['person_id']}, {dept}, {max_courses}, {job});"
    )


# ---------------------------------------------------------
# EMPLOYEE SKILL SET
# ---------------------------------------------------------
print("\n-- EMPLOYEE SKILL SET")

for emp in employees:
    chosen_skills = random.sample(range(1, NUM_SKILLS + 1), random.randint(1, 5))
    for s in chosen_skills:
        print(
            f"INSERT INTO employee_skill_set (skill_set_id, id) "
            f"VALUES ({s}, {emp['id']});"
        )


# ---------------------------------------------------------
# DEPARTMENTS
# ---------------------------------------------------------
print("\n-- DEPARTMENT")

departments = []
for i in range(1, NUM_DEPARTMENTS + 1):
    manager = random.choice(employees)
    manager_empid = manager["employment_id"]

    d = {
        "id": i,
        "name": fake.word().capitalize() + " Department",
        "manager_id": manager_empid
    }
    departments.append(d)

    print(
        f"INSERT INTO department (department_name, manager_id) "
        f"VALUES ('{d['name']}', '{d['manager_id']}');"
    )


# ---------------------------------------------------------
# COURSES
# ---------------------------------------------------------
print("\n-- COURSES")
course_codes = []
for i in range(NUM_COURSES):
    code = course_code()
    name = fake.sentence(nb_words=3).replace("'", "")
    course_codes.append(code)

    print(
        f"INSERT INTO course (course_code, course_name) "
        f"VALUES ('{code}', '{name}');"
    )


# ---------------------------------------------------------
# COURSE LAYOUTS
# ---------------------------------------------------------
print("\n-- COURSE LAYOUTS")
layout_id = 1
course_layouts = []  # stores (layout_id, course_id)

for course_id in range(1, NUM_COURSES + 1):
    for v in range(1, random.randint(1, MAX_LAYOUTS_PER_COURSE) + 1):

        min_s = random.randint(50, 99)
        max_s = min_s + random.randint(100, 250)
        hp = round(random.uniform(5, 20), 1)

        print(
            "INSERT INTO course_layout (course_id, min_students, max_students, hp, ver) "
            f"VALUES ({course_id}, {min_s}, {max_s}, {hp}, {v});"
        )

        course_layouts.append((layout_id, course_id))
        layout_id += 1


# ---------------------------------------------------------
# COURSE INSTANCES
# ---------------------------------------------------------
print("\n-- COURSE INSTANCES")
instance_id_seq = 1
course_instances = []

for layout_id, course_id in course_layouts:
    for _ in range(random.randint(1, MAX_INSTANCES_PER_LAYOUT)):
        year = random.choice(["2023", "2024", "2025"])
        num_students = random.randint(5, 100)
        inst_code = f"INST{instance_id_seq:04d}"

        print(
            "INSERT INTO course_instance (instance_id, num_students, study_year, course_layout_id, course_id) "
            f"VALUES ('{inst_code}', {num_students}, '{year}', {layout_id}, {course_id});"
        )

        course_instances.append((instance_id_seq, layout_id, course_id))
        instance_id_seq += 1


# ---------------------------------------------------------
# STUDY PERIODS
# ---------------------------------------------------------
print("\n-- STUDY PERIOD")
for i, sp in enumerate(STUDY_PERIODS, start=1):
    print(f"INSERT INTO study_period (value) VALUES ('{sp}');")


# ---------------------------------------------------------
# COURSE INSTANCE STUDY PERIOD
# ---------------------------------------------------------
print("\n-- COURSE INSTANCE STUDY PERIOD")
for inst_id, layout_id, course_id in course_instances:
    assigned = random.sample(range(1, len(STUDY_PERIODS) + 1), random.randint(1, 2))
    for sp in assigned:
        print(
            f"INSERT INTO course_instance_study_period (study_period_id, course_instance_id) "
            f"VALUES ({sp}, {inst_id});"
        )


# ---------------------------------------------------------
# TEACHING ACTIVITIES
# ---------------------------------------------------------
print("\n-- TEACHING ACTIVITIES")
for i, (name, factor) in enumerate(TEACHING_ACTIVITIES, start=1):
    print(
        f"INSERT INTO teaching_activity (activity_name, factor) "
        f"VALUES ('{name}', {factor});"
    )


# ---------------------------------------------------------
# PLANNED ACTIVITY
# ---------------------------------------------------------
print("\n-- PLANNED ACTIVITY")
for inst_id, layout_id, course_id in course_instances:
    for ta_id in range(1, len(TEACHING_ACTIVITIES) + 1):
        hours = random.randint(5, 40)
        print(
            f"INSERT INTO planned_activity (teaching_activity_id, course_instance_id, planned_hours) "
            f"VALUES ({ta_id}, {inst_id}, {hours});"
        )


# ---------------------------------------------------------
# EMPLOYEE LOAD ALLOCATION
# ---------------------------------------------------------
print("\n-- EMPLOYEE LOAD ALLOCATION")
for inst_id, layout_id, course_id in course_instances:
    allocated = random.sample(employees, random.randint(1, 3))
    for emp in allocated:
        ta_id = random.randint(1, len(TEACHING_ACTIVITIES))
        print(
            f"INSERT INTO employee_load_allocation (teaching_activity_id, course_instance_id, employee_id) "
            f"VALUES ({ta_id}, {inst_id}, {emp['id']});"
        )
