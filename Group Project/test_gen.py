print("\n-- COURSES")
course_rows = []
for course_id in range(1, NUM_COURSES + 1):
    code = fake.unique.bothify(text="??####").upper()
    name = fake.sentence(nb_words=3).replace("'", "")
    course_rows.append((course_id, code))
    print(f"INSERT INTO course(id, course_code, course_name) VALUES ({course_id}, '{code}', '{name}');")

