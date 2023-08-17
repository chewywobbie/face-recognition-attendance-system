<?php

// Include the database connection file
include('admin/database_connection.php');

// Fetch the classes data from the database
$query = "SELECT class_id, classname FROM t_class";
$statement = $connect->prepare($query);
$statement->execute();
$classes = $statement->fetchAll(PDO::FETCH_ASSOC);

// Return the classes data as JSON
header('Content-Type: application/json');
echo json_encode($classes);
