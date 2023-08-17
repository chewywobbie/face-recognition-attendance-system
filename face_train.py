import cv2
import numpy as np
from PIL import Image
import os
import db_conn
import sys


def retrieve_list(file_path, mode="int"):
    my_list = []

    if not os.path.exists(file_path):
        return my_list

    if os.path.getsize(file_path) == 0:
        return my_list

    with open(file_path, 'r') as file:
        if mode == "int":
            my_list = [int(line.strip()) for line in file]
    return my_list

def retrieve_std_classes(std_id):
    student_id = (std_id,)
    mydb = db_conn.db_connect()
    query = """SELECT t_class.class_id
            FROM t_std
            JOIN t_std_class ON t_std.std_id = t_std_class.join_stdid
            JOIN t_class ON t_std_class.join_classid = t_class.class_id
            WHERE t_std.std_id = %s"""

    cursor = mydb.cursor()
    cursor.execute(query, student_id)
    result = cursor.fetchall()
    student_classes = [row[0] for row in result]
    mydb.close()
    return student_classes


def face_train(std_id, student_name, image_path):
    detector = cv2.CascadeClassifier(r"C:\Users\User\Desktop\facedetect\haarcascade_frontalface_default.xml")
    imgp_folder = r'C:\Users\User\Desktop\facedetect\img_preprocess'

    new_filename = 'final_' + os.path.basename(image_path)
    new_filename_gray = 'finalgray_' + os.path.basename(image_path)
    new_filename_resize = 'finalresize_' + os.path.basename(image_path)

    input_img = cv2.imread(image_path)
    grayscale = cv2.cvtColor(input_img, cv2.COLOR_BGR2GRAY)

    pil_img = Image.open(image_path).convert('L')
    img_numpy = np.array(pil_img, 'uint8')

    face = detector.detectMultiScale(grayscale ,minNeighbors=3)
    # print(face[0])
    (x, y, w, h) = face[0]
    std_id = int(std_id)

    face_image = img_numpy[y:y + h, x:x + w]
    face_image_resized = cv2.resize(face_image, (300, 300))
    # cv2.imshow('face',face_image)
    imgp_path = os.path.join(imgp_folder,new_filename)
    imgp_path_gray = os.path.join(imgp_folder,new_filename_gray)
    imgp_path_resize = os.path.join(imgp_folder,new_filename_resize)

    cv2.imwrite(imgp_path, face_image)
    cv2.imwrite(imgp_path_gray, grayscale)
    cv2.imwrite(imgp_path_resize, face_image_resized)
    cv2.waitKey(2)
    
    student_classes = retrieve_std_classes(std_id)
    class_folder = r'C:\Users\User\Desktop\facedetect\classes'

    recognizer = {}
    for class_id in student_classes:
        recognizer[class_id] = cv2.face.LBPHFaceRecognizer_create(radius=1, neighbors=8, grid_x=8, grid_y=8)
        class_path = os.path.join(class_folder, str(class_id))
        os.makedirs(class_path, exist_ok=True)

        yaml_file_path = os.path.join(class_path, f"{class_id}.yml")
        if os.path.exists(yaml_file_path):
            recognizer[class_id].read(yaml_file_path)
            recognizer[class_id].update([face_image_resized], np.array([std_id]))
        else:
            recognizer[class_id].train([face_image_resized], np.array([std_id]))

        recognizer[class_id].save(yaml_file_path)
        ids = retrieve_list(os.path.join(class_path, 'ids.txt'))

        if std_id not in ids:
            with open(os.path.join(class_path, 'ids.txt'), 'a') as id_file:
                id_file.write(f'{std_id}\n')

            with open(os.path.join(class_path, 'names.txt'), 'a') as name_file:
                name_file.write(f'{student_name}\n')

    # Print the number of faces trained and end the program
    print("\n [INFO] {0} faces trained. Exiting Program".format(len(np.unique(student_classes))))


if __name__ == "__main__":
    std_id = sys.argv[1]
    student_name = sys.argv[2]
    image_path = sys.argv[3]

    face_train(std_id, student_name, image_path)
