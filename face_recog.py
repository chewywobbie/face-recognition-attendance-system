import cv2
import os
import time
import sys
import db_conn


def get_array_face_recog(file_path):
    my_list = [0]
    if not os.path.exists(file_path):
        return my_list

    if os.path.getsize(file_path) == 0:
        return my_list

    with open(file_path, 'r') as file:
        for line in file:
            my_list.append(line.strip())
    return my_list


def retrieve_std_id(student_name):
    student_name = (student_name,)
    mydb = db_conn.db_connect()
    query = """SELECT t_std.std_id
    FROM t_std
    INNER JOIN t_logininfo ON t_std.std_loginid = t_logininfo.logininfo_id
    WHERE t_logininfo.username = %s"""

    cursor = mydb.cursor()
    cursor.execute(query, student_name)
    result = cursor.fetchall()
    student_id = [row[0] for row in result]
    mydb.close()
    return student_id


if __name__ == "__main__":
    class_id = sys.argv[1]
    image_path = sys.argv[2]

recognizer = cv2.face.LBPHFaceRecognizer_create(radius=1, neighbors=8, grid_x=8, grid_y=8)
recognizer.read(fr'C:\Users\User\Desktop\facedetect\classes\{class_id}\{class_id}.yml')
print(f'./classes/{class_id}/{class_id}.yml')

cascadePath = r"C:\Users\User\Desktop\facedetect\haarcascade_frontalface_default.xml"
faceCascade = cv2.CascadeClassifier(cascadePath)

font = cv2.FONT_HERSHEY_SIMPLEX
name_var_path = fr'C:/Users/User/Desktop/facedetect/classes/{class_id}/names.txt'
id_var_path = fr'C:/Users/User/Desktop/facedetect/classes/{class_id}/ids.txt'

# initiate id counter
id = 0
names = get_array_face_recog(name_var_path)
id = get_array_face_recog(id_var_path)
print(id)  # Print the id list to check its content and data type
time.sleep(1)
input_img = cv2.imread(image_path)
# input_img = cv2.GaussianBlur(input_img, (5,5), 0)
# Define min window size to be recognized as a face
height, width = input_img.shape[:2]
minW = 0.01 * width
minH = 0.01 * height

gray = cv2.cvtColor(input_img, cv2.COLOR_BGR2GRAY)
faces = faceCascade.detectMultiScale(
    gray,
    # scaleFactor=1.5,
    minNeighbors=7,
    # minSize=(int(minW), int(minH)),
)
recognized_students = []
face = {}

result_list = dict()
temp_comp = 0
ids = 1
face_counter = 0
for (x, y, w, h) in faces:
    cv2.rectangle(input_img, (x, y), (x + w, y + h), (0, 255, 0), 4)
    student_id, confidence = recognizer.predict(cv2.resize(gray[y:y + h, x:x + w], (500, 500)))
        # gray[y:y + h, x:x + w])
        # cv2.resize(gray[y:y + h, x:x + w], (500, 500)))
    face_counter += 1
    print(student_id, confidence)
    print(id)
    if 30 > confidence:
        # if confidence <= result_list[student_id]:
        # if str(student_id) not in id:

        std_index = id.index(str(student_id))
        name = names[std_index]
        print(name)
        confidence = "  {0}%".format(round(100 - confidence))
        recognized_students.append(name)  # Add the recognized student name to the list
    else:
        name = ""
        confidence = "  {0}%".format(round(100 - confidence))
    cv2.putText(input_img, str(name), (x + 5, y - 5), font, 1.5, (255, 255, 255), 3)
    cv2.putText(input_img, str(confidence), (x + 5, y + h - 5), font, 1, (255, 255, 0), 3)

final = cv2.resize(input_img, (640, 480))
# cv2.imshow('camera', final)
print(face_counter)
attendance_folder = r'C:\Users\User\Desktop\facedetect\attendance_supervised'
class_path = os.path.join(attendance_folder, str(class_id))
os.makedirs(class_path, exist_ok=True)

new_filename = 'final_' + os.path.basename(image_path)
output_path = os.path.join(class_path, new_filename)
cv2.imwrite(output_path, final)

mydb = db_conn.db_connect()
for name in recognized_students:
    student_id = retrieve_std_id(name)[0]
    attendance_query = f"INSERT INTO t_attendance (attend_stdid, attend_classid, attend_date, attend_status, attendimgpath)" \
                       f" VALUES ({student_id}, {class_id}, NOW(), 'PRESENT', '{output_path}')"
    cursor = mydb.cursor()
    cursor.execute(attendance_query)
    mydb.commit()
mydb.close()

# clean datas for every run through
print("Attendance recorded successfully!")
print("\n [INFO] Exiting Program and cleanup stuff")
