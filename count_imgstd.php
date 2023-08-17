<?php

// Include the database connection file
include('admin/database_connection.php');

if (isset($_POST['username'])) {
    $username = $_POST['username'];
    error_log("Received username: " . $username);

    $std_id = getStd_id($username);
    error_log("Student ID: " . $std_id);

    $query = "SELECT COUNT(*) AS num_imgpath
              FROM t_imgpath
              WHERE imgp_stdid = :std_id";

    $statement = $connect->prepare($query);
    $statement->execute(['std_id' => $std_id]); 
    $result = $statement->fetch(PDO::FETCH_ASSOC);

    $count = $result['num_imgpath'];
    error_log("Image count: " . $count);

    // Prepare the response
    $response = array('num_imgpath' => $count);

    // Send the response as JSON
    header('Content-Type: application/json');
    echo json_encode($response);
} else {
    // Send error response if 'username' parameter is missing
    header('Content-Type: application/json');
    echo json_encode(array('error' => 'username parameter is missing.'));
}
