<?php

// Include the database connection file
include('admin/database_connection.php');

// Fetch the classes data from the database
$query = "SELECT course_id, coursename FROM t_course";
$statement = $connect->prepare($query);
$statement->execute();
$courses = $statement->fetchAll(PDO::FETCH_ASSOC);

// Return the classes data as JSON
header('Content-Type: application/json');
echo json_encode($courses);
