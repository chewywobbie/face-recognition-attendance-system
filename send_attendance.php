<?php
// send_attendance.php

// Check if the image file is uploaded and exists
if (isset($_FILES['attendance_image']) && $_FILES['attendance_image']['error'] === UPLOAD_ERR_OK) {
    $file_name = $_FILES['attendance_image']['name'];
    $file_tmp = $_FILES['attendance_image']['tmp_name'];

    // Directory where you want to store the image temporarily
    $upload_directory = 'C:/Users/User/Desktop/facedetect/testattendance/';

    // Move the uploaded image to the temporary directory
    $file_path = $upload_directory . $file_name;
    move_uploaded_file($file_tmp, $file_path);

    // Path to the Python script and the image
    $python_path = 'C:\Users\User\Desktop\facedetect\venv\Scripts\python.exe';
    $python_script_path = 'C:/Users/User/Desktop/facedetect/facedetrec.py';

    // Assuming the class ID is stored as $class_id when the form is submitted
    $class_id = $_POST['class_id'];

    // Command to execute the Python script with the image file and class ID as arguments
    $command = "$python_path $python_script_path $file_path $class_id";


    // Execute the Python script
    $output = [];
    $return_value = 0;
    exec($command, $output, $return_value);

    // Display the output and return value from the Python script
    echo "Python Script Output: " . implode("\n", $output) . "\n";
    echo "Return value: " . $return_value;
} else {
    echo "Error: No image uploaded or invalid file.";
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Attendance Upload</title>
</head>
<body>
    <h1>Attendance Upload</h1>
    <form action="send_attendance.php" method="post" enctype="multipart/form-data">
        <label for="attendance_image">Select Attendance Image:</label>
        <input type="file" name="attendance_image" id="attendance_image" required>
        
        <!-- Add the class ID selection dropdown -->
        <label for="class_id">Select Class:</label>
        <select name="class_id" id="class_id" required>
            <option value="1">Class 1</option>
            <option value="2">Class 2</option>
            <option value="3">Class 3</option>
            <option value="4">Class 4</option>
        </select>
        
        <input type="submit" value="Upload">
    </form>
</body>
</html>

