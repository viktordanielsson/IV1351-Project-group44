import random
from faker import Faker
from faker.providers import barcode

fake = Faker()
fake.add_provider(barcode)

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

print("-- PERSON")
persons = []
for i in range(1, NUM_PERSONS + 1):
    p = {
        "personal_number": fake.unique.numerify("##########"),
        "first_name": fake.first_name(),
        "last_name": fake.last_name(),
        "phone_number": fake.msisdn()[:12],
        "address": fake.address().replace("\n", ", ")
    }
    persons.append(p)
    print(f"INSERT INTO person VALUES ('{p['personal_number']}', '{p['first_name']}', "
          f"'{p['last_name']}', '{p['phone_number']}', '{p['address']}');")


print("\n-- JOB TITLES")
job_titles = []
for i in range(1, NUM_JOB_TITLES + 1):
    jt = fake.job()
    job_titles.append(jt)
    print(f"INSERT INTO job_title VALUES ('{jt}');")


print("\n-- SKILL SET")
for i in range(1, NUM_SKILLS + 1):
    skill = fake.unique.word().capitalize()
    print(f"INSERT INTO skill_set VALUES ('{skill}');")


print("\n-- EMPLOYEES")

employees = random.sample(persons, NUM_EMPLOYEES)

for p in employees:
    p["employment_id"] = fake.ean(length=8)
    p["department_id"] = random.randint(1, NUM_DEPARTMENTS)
    p["manager_id"] = None  # assigned later

# STEP 1: Choose a manager for each department
department_managers = {}

for dept in range(1, NUM_DEPARTMENTS + 1):
    dept_emps = [e for e in employees if e["department_id"] == dept]
    manager = random.choice(dept_emps)
    department_managers[dept] = manager["employment_id"]
    manager["manager_id"] = None  # manager has no manager

# STEP 2: Assign manager_id to all non-manager employees
for e in employees:
    dept = e["department_id"]
    if e["employment_id"] != department_managers[dept]:
        e["manager_id"] = department_managers[dept]

# STEP 3: Print employees with correct manager structure
for p in employees:
    salary = random.randint(30000, 90000)
    job = random.randint(1, NUM_JOB_TITLES)
    max_courses = random.randint(1, 6)

    manager_value = "NULL" if p["manager_id"] is None else f"'{p['manager_id']}'"

    print(
        "INSERT INTO employee "
        "(employment_id, salary, manager_id, person_id, department_id, max_allowed_courses, job_title_id) "
        f"VALUES ('{p['employment_id']}', {salary}, {manager_value}, "
        f"'{p['personal_number']}', {p['department_id']}, {max_courses}, {job});"
    )


print("\n-- EMPLOYEE SKILL SET")
for p in employees:
    assigned = random.sample(range(1, NUM_SKILLS + 1), random.randint(1, 5))
    for s in assigned:
        print(
            "INSERT INTO employee_skill_set(person_id, department_id, skill_set_id) "
            f"VALUES ('{p['personal_number']}', {random.randint(1, NUM_DEPARTMENTS)}, {s});"
        )


print("\n-- DEPARTMENT")
for dept_id, manager_id in department_managers.items():
    dept_name = fake.word().capitalize() + " Department"
    print(f"INSERT INTO department VALUES ('{dept_name}', '{manager_id}');")


# -----------------------
# COURSES + LAYOUTS + INSTANCES
# -----------------------

# Define a provider function for course codes
def course_code():
    # Two uppercase letters + four digits
    return fake.unique.bothify(text='??####').upper()

print("\n-- COURSES")
course_codes = [course_code() for _ in range(NUM_COURSES)]
for code in course_codes:
    name = fake.sentence(nb_words=3).replace("'", "")
    print(f"INSERT INTO course VALUES ('{code}', '{name}');")

print("\n-- COURSE LAYOUTS")
layout_id = 1
course_layouts = []  # will hold tuples (layout_id, course_id)
for c_idx, course_code_str in enumerate(course_codes):
    # create between 1 and MAX_LAYOUTS_PER_COURSE layouts per course
    num_layouts = random.randint(1, MAX_LAYOUTS_PER_COURSE)
    for ver in range(1, num_layouts + 1):
        min_s = random.randint(10, 49)   # sensible min students
        max_s = min_s + random.randint(10, 200)
        hp = round(random.uniform(1, 20), 1)

        # I assume course_layout has columns like:
        # (course_layout_id, course_id, ver, min_students, max_students, hp)
        print(
            "INSERT INTO course_layout (course_layout_id, course_id, ver, min_students, max_students, hp) "
            f"VALUES ({layout_id}, '{course_code_str}', {ver}, {min_s}, {max_s}, {hp});"
        )

        course_layouts.append((layout_id, course_code_str))
        layout_id += 1

print("\n-- COURSE INSTANCES")
instance_id = 1
course_instances = []  # (instance_id, course_layout_id, course_id)
for layout_id_val, course_code_str in course_layouts:
    # create between 1 and MAX_INSTANCES_PER_LAYOUT instances for this layout
    for _ in range(random.randint(1, MAX_INSTANCES_PER_LAYOUT)):
        year = random.choice(["2023", "2024", "2025"])
        num_students = random.randint(1, 100)

        # I assume course_instance columns:
        # (instance_id, num_students, study_year, course_layout_id, course_id)
        print(
            "INSERT INTO course_instance (instance_id, num_students, study_year, course_layout_id, course_id) "
            f"VALUES ({instance_id}, {num_students}, '{year}', {layout_id_val}, '{course_code_str}');"
        )

        course_instances.append((instance_id, layout_id_val, course_code_str))
        instance_id += 1

print("\n-- STUDY PERIOD")
for i, sp in enumerate(STUDY_PERIODS, start=1):
    print(f"INSERT INTO study_period VALUES ({i}, '{sp}');")

print("\n-- COURSE INSTANCE STUDY PERIOD")
# Your original code used the 4-tuple (inst_id, layout_id, course_id, sp).
# Keep that format, but ensure types are correct (course_id quoted).
for inst_id, layout_id_val, course_code_str in course_instances:
    # assign 1 or 2 study periods to each instance
    assigned = random.sample(range(1, len(STUDY_PERIODS) + 1), random.randint(1, 2))
    for sp in assigned:
        print(
            "INSERT INTO course_instance_study_period (instance_id, course_layout_id, course_id, study_period_id) "
            f"VALUES ({inst_id}, {layout_id_val}, '{course_code_str}', {sp});"
        )

print("\n-- TEACHING ACTIVITIES")
for i, (name, factor) in enumerate(TEACHING_ACTIVITIES, start=1):
    # assume activity id is the auto-number (i) or (name,factor) tuple only
    print(f"INSERT INTO teaching_activity VALUES ('{name}', {factor});")

print("\n-- PLANNED ACTIVITY")

for inst_id, layout_id_val, course_code_str in course_instances:

    # Create planned hours for each teaching activity for this course instance
    for ta_id in range(1, len(TEACHING_ACTIVITIES) + 1):
        hours = random.randint(5, 40)

        print(
            "INSERT INTO planned_activity (teaching_activity_id, course_instance_id, planned_hours) "
            f"VALUES ({ta_id}, {inst_id}, {hours});"
        )


# -----------------------
# EMPLOYEE LOAD ALLOCATION
# -----------------------

print("\n-- EMPLOYEE LOAD ALLOCATION")

if employees:
    for inst_id, layout_id_val, course_code_str in course_instances:

        # pick 1–4 employees for this instance
        allocated = random.sample(employees, random.randint(1, min(4, len(employees))))

        for employee in allocated:
            ta_id = random.randint(1, len(TEACHING_ACTIVITIES))
            emp_id = employee["employment_id"]  # employees have employment_id

            print(
                "INSERT INTO employee_load_allocation (teaching_activity_id, course_instance_id, employee_id) "
                f"VALUES ({ta_id}, {inst_id}, '{emp_id}');"
            )

