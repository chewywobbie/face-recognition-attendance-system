<?php
include('admin/database_connection.php');

$classname = isset($_POST['classname']) ? $_POST['classname'] : '';
$fileName = isset($_FILES["file"]["name"]) ? $_FILES["file"]["name"] : '';
error_log($classname);
error_log($fileName);

if (empty($classname)) {
    $response["code"] = 7;
    $response["message"] = "Error: Classname is empty.";
    echo json_encode($response);
    exit();
}

$classID = getClassIdByClassName($classname);

if (empty($classID)) {
    $response["code"] = 8;
    $response["message"] = "Error: Class ID not found for the given classname.";
    echo json_encode($response);
    exit();
}

// Check if a file is uploaded
if (isset($_FILES["file"]["tmp_name"]) && is_uploaded_file($_FILES["file"]["tmp_name"])) {
    $path = 'C:/Users/User/Desktop/facedetect/attendance/';
    $filePath = $path . $classID . "_" . $fileName;
    error_log("filePath:".($filePath));

    if (move_uploaded_file($_FILES["file"]["tmp_name"], $filePath)) {
        // File is saved, now call the Python script
        $python_path = 'C:\Users\User\Desktop\facedetect\venv\Scripts\python.exe';
        $python_script_path = "C:/Users/User/Desktop/facedetect/face_recog.py";
        $classid = escapeshellarg($classID);
        $img_path = escapeshellarg($filePath);
        error_log($classid);
        error_log($img_path);

        $command = "$python_path $python_script_path $classid $img_path";

        $output = [];
        $return_value = 0;
        exec($command, $output, $return_value);

        $response["code"] = 5;
        $response["message"] = "Image uploaded and processed successfully.";
        echo json_encode($response);
        exit();
    } else {
        $response["code"] = 6;
        $response["message"] = "Error: Failed to move uploaded file.";
        echo json_encode($response);
        exit();
    }
} else {
    $response["code"] = 9;
    $response["message"] = "Error: No file uploaded.";
    echo json_encode($response);
    exit();
}
