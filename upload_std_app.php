<?php
include('admin/database_connection.php');
$connect->beginTransaction();

$response = array();
if (empty($_FILES) || $_FILES['file']['error']) {
    $response["code"] = 2;
    $response["message"] = "Failed to move uploaded file";
    echo json_encode($response);
    exit();
}

$chunk = isset($_REQUEST["chunk"]) ? intval($_REQUEST["chunk"]) : 0;
$chunks = isset($_REQUEST["chunks"]) ? intval($_REQUEST["chunks"]) : 0;

$fileName = isset($_REQUEST["name"]) ? $_REQUEST["name"] : $_FILES["file"]["name"];

// Get the student ID and name to create a unique file name
$stdid_latest = getLatestStdID();
$stud_name = getStd_username($stdid_latest);

// Get the current count of images for the student
$img_count = countimagesStd($stdid_latest);

// Increment the image count
$img_count++;

// Append the count to the student's name in the file name
$fileName = "{$stud_name}{$img_count}.jpg";

// Create the file path with the student's name and ID
$filePath = "C:/Users/User/Desktop/facedetect/people/{$fileName}";


// Debugging: Display variables
error_log ("Chunk: " . $chunk . "\n");
error_log ("Chunks: " . $chunks . "\n");
error_log ("FileName: " . $fileName . "\n");
error_log ("Student ID: " . $stdid_latest . "\n");
error_log ("Student Name: " . $stud_name . "\n");
error_log ("File Path: " . $filePath . "\n");

$query = "SELECT MAX(imgpath_id) AS max_id FROM t_imgpath";
$statement = $connect->prepare($query);
$statement->execute();
$result = $statement->fetch(PDO::FETCH_ASSOC);
if ($result === NULL) {
    $result = 0;
    $next_imgid = $result['max_id'] + 1;
} else {
    $next_imgid = $result['max_id'] + 1;
}

// Debugging: Display variable
error_log ("Next Image ID: " . $next_imgid . "\n");

// Insert the image path into the database
$query = "INSERT INTO t_imgpath (imgpath_id, imgp_stdid, imagepath) VALUES (?, ?, ?)";
$statement = $connect->prepare($query);
$statement->execute([$next_imgid, $stdid_latest, $filePath]);

// Open temp file
$out = @fopen("{$filePath}.part", $chunk == 0 ? "wb" : "ab");

// Debugging: Display file path
echo "Temporary File Path: " . "{$filePath}.part" . "\n";


  if ($out) {
    // Read binary input stream and append it to temp file
    $in = @fopen($_FILES['file']['tmp_name'], "rb");

    if ($in) {
        while ($buff = fread($in, 4096))
        fwrite($out, $buff);

        @fclose($in);
        @fclose($out);

        @unlink($_FILES['file']['tmp_name']);

        
    } else {
        $response["code"] = 3;
        $response["message"] = "Oops! Failed to open input Stream error occurred.";
        echo json_encode($response);
    }
} else {
    $response["code"] = 4;
    $response["message"] = "Oops! Failed to open output error occurred.";
    echo json_encode($response);
}

// Check if file has been uploaded
if (!$chunks || $chunk == $chunks - 1) {
// Strip the temp .part suffix off
rename("{$filePath}.part", $filePath);
}// Commit the transaction after the image file is saved
$connect->commit();

// File is saved, now call the Python script
$python_path = 'C:\Users\User\Desktop\facedetect\venv\Scripts\python.exe';
$python_script_path = "C:/Users/User/Desktop/facedetect/face_train.py";
$stud_id = escapeshellarg($stdid_latest);
$stud_name = escapeshellarg($stud_name); // Use the student's name here
$img_path = escapeshellarg($filePath);

$command = "$python_path $python_script_path $stdid_latest $stud_name $img_path";

// Debugging: Display command
echo "Python Command: " . $command . "\n";
error_log("Python Command: " . $command . "\n");

$output = [];
$return_value = 0;
exec($command, $output, $return_value);
error_log("Command: " . $command . "\n, Output: " . implode("\n", $output) . "\n, Return value: " . $return_value);

$response["code"] = 5;
$response["message"] = "successfully uploaded";
echo json_encode($response);

?>