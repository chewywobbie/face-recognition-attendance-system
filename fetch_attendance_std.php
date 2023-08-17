<?php

// Include the database connection file
include('admin/database_connection.php');
if (isset($_POST['username'])) {
    $username = $_POST['username'];
    $std_id = getStd_id($username);
    error_log("Student ID: " . $std_id);
    $query = "SELECT t_attendance.attend_date, t_class.classcode, t_class.classname, t_attendance.attend_status
    FROM t_attendance
    INNER JOIN t_class ON t_attendance.attend_classid = t_class.class_id
    WHERE t_attendance.attend_stdid = :std_id";

    $statement = $connect->prepare($query);
    $statement->execute(['std_id' => $std_id]); // Pass parameters as an associative array
    $attendance = $statement->fetchAll(PDO::FETCH_ASSOC);

    error_log("Fetched Attendance Data: " . json_encode($attendance));

    // error_log("Fetched classes: " . json_encode($classes));

    // Initialize an array to store the rows
    $attendanceData = array();

    // Loop through the fetched data and add each row to the array
    foreach ($attendance as $class) {
        $row = array(
            'attend_date' => $class['attend_date'],
            'classcode' => $class['classcode'],
            'classname' => $class['classname'],
            'attend_status' => $class['attend_status']
        );
        $attendanceData[] = $row;
    }

    // Return the attendance data as JSON
    header('Content-Type: application/json');
    echo json_encode($attendanceData);
} else {
    // If classname parameter is not provided, return an error response
    header('Content-Type: application/json');
    echo json_encode(array('error' => 'classname parameter is missing.'));
}
