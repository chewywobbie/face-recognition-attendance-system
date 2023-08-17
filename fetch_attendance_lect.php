<?php

// Include the database connection file
include('admin/database_connection.php');

if (isset($_POST['classname'])) {
    $classname = $_POST['classname'];
    $classId = getClassIdByClassName($classname);

    $query = "SELECT t_std.stdname, t_std.stdmatricno, t_attendance.attend_date, t_attendance.attend_status
    FROM t_std
    INNER JOIN t_attendance ON t_std.std_id = t_attendance.attend_stdid
    WHERE t_attendance.attend_classid = :class_id";

    $statement = $connect->prepare($query);
    $statement->bindParam(':class_id', $classId, PDO::PARAM_STR);
    $statement->execute();
    $classes = $statement->fetchAll(PDO::FETCH_ASSOC);

    // // Log the query and the fetched data
    // error_log("Query: " . $query);
    // error_log("Fetched classes: " . json_encode($classes));

    // Initialize an array to store the rows
    $attendanceData = array();

    // Loop through the fetched data and add each row to the array
    foreach ($classes as $class) {
        $row = array(
            'stdname' => $class['stdname'],
            'stdmatricno' => $class['stdmatricno'],
            'attend_date' => $class['attend_date'],
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
