import random
from faker import Faker

fake = Faker()

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
for i in range(1, NUM_PERSONS+1):
    p = {
        "personal_number": fake.unique.ssn()[:10],
        "first_name": fake.first_name(),
        "last_name": fake.last_name(),
        "phone_number": fake.msisdn()[:13],
        "address": fake.address().replace("\n", ", ")
    }
    persons.append(p)
    print(f"INSERT INTO person VALUES ({i}, '{p['personal_number']}', '{p['first_name']}', "
          f"'{p['last_name']}', '{p['phone_number']}', '{p['address']}');")

print("\n-- DEPARTMENT")
departments = []
for i in range(1, NUM_DEPARTMENTS+1):
    d = {
        "name": fake.word().capitalize() + " Department",
        "manager_id": random.randint(1, NUM_PERSONS)
    }
    departments.append(d)
    print(f"INSERT INTO department VALUES({i}, '{d['name']}', {d['manager_id']});")

print("\n-- JOB TITLES")
job_titles = []
for i in range(1, NUM_JOB_TITLES+1):
    j = fake.job()
    job_titles.append(j)
    print(f"INSERT INTO job_title VALUES ({i}, '{j}');")

print("\n-- SKILL SET")
for i in range(1, NUM_SKILLS+1):
    skill = fake.word().capitalize()
    print(f"INSERT INTO skill_set VALUES ({i}, '{skill}');")

print("\n-- EMPLOYEES")
employees = random.sample(persons, NUM_EMPLOYEES)
for idx, p in enumerate(employees, start=1):
    dept = random.randint(1, NUM_DEPARTMENTS)
    job = random.randint(1, NUM_JOB_TITLES)

    salary = random.randint(30000, 90000)
    manager_id = random.choice(employees)["id"]

    max_courses = random.randint(1, 6)

    print(f"INSERT INTO employee(person_id, department_id, job_title_id, salary, manager_id, max_allowed_courses) "
          f"VALUES ({p['id']}, {dept}, {job}, {salary}, {manager_id}, {max_courses});")

print("\n-- EMPLOYEE SKILL SET")
for p in employees:
    assigned = random.sample(range(1, NUM_SKILLS+1), random.randint(1, 5))
    for s in assigned:
        print(f"INSERT INTO employee_skill_set(person_id, department_id, skill_set_id) "
              f"VALUES ({p['id']}, {random.randint(1, NUM_DEPARTMENTS)}, {s});")

print("\n-- COURSES")
for i in range(1, NUM_COURSES+1):
    code = "C" + str(i).zfill(4)
    name = fake.sentence(nb_words=3).replace("'", "")
    print(f"INSERT INTO course VALUES ({i}, '{code}', '{name}');")

print("\n-- COURSE LAYOUTS")
layout_id = 1
course_layouts = []
for c in range(1, NUM_COURSES+1):
    for v in range(1, random.randint(1, MAX_LAYOUTS_PER_COURSE)+1):
        min_s = random.randint(5, 20)
        max_s = min_s + random.randint(10, 60)
        hp = round(random.uniform(5, 20), 1)

        print(f"INSERT INTO course_layout VALUES ({layout_id}, {c}, {min_s}, {max_s}, {hp}, {v});")

        course_layouts.append((layout_id, c))
        layout_id += 1

print("\n-- COURSE INSTANCES")
instance_id = 1
course_instances = []
for layout_id, course_id in course_layouts:
    for _ in range(random.randint(1, MAX_INSTANCES_PER_LAYOUT)):
        year = random.choice(["2023","2024","2025"])
        num_students = random.randint(5, 100)

        print(f"INSERT INTO course_instance VALUES ({instance_id}, {layout_id}, {course_id}, '{instance_id}', "
              f"{num_students}, '{year}');")

        course_instances.append((instance_id, layout_id, course_id))
        instance_id += 1

print("\n-- STUDY PERIOD")
for i, sp in enumerate(STUDY_PERIODS, start=1):
    print(f"INSERT INTO study_period VALUES ({i}, '{sp}');")

print("\n-- COURSE INSTANCE STUDY PERIOD")
for inst_id, layout_id, course_id in course_instances:
    assigned = random.sample(range(1, len(STUDY_PERIODS)+1), random.randint(1,2))
    for sp in assigned:
        print(f"INSERT INTO course_instance_study_period VALUES ({inst_id}, {layout_id}, {course_id}, {sp});")

print("\n-- TEACHING ACTIVITIES")
for i, (name, factor) in enumerate(TEACHING_ACTIVITIES, start=1):
    print(f"INSERT INTO teaching_activity VALUES ({i}, '{name}', {factor});")

print("\n-- PLANNED ACTIVITY")
for layout_id, course_id in course_layouts:
    for ta_id in range(1, len(TEACHING_ACTIVITIES)+1):
        hours = random.randint(5, 40)
        print(f"INSERT INTO planned_activity VALUES ({layout_id}, {course_id}, {ta_id}, {hours});")

print("\n-- EMPLOYEE LOAD ALLOCATION")
for (inst_id, layout_id, course_id) in course_instances:
    allocated = random.sample(employees, random.randint(1, 3))
    for employee in allocated:
        dept = random.randint(1, NUM_DEPARTMENTS)
        ta = random.randint(1, len(TEACHING_ACTIVITIES))
        print(f"INSERT INTO employee_load_allocation(person_id, department_id, course_id, teaching_activity_id) "
              f"VALUES ({employee['id']}, {dept}, {course_id}, {layout_id}, {ta});")
