<?php

// Include the database connection file
include('admin/database_connection.php');

if (isset($_POST['username'])) {
    $username = $_POST['username'];
    // Fetch the classes data for the given lecturer username from the database
    $query = "SELECT t_class.classname
    FROM t_logininfo
    JOIN t_lect ON t_logininfo.logininfo_id = t_lect.lect_loginid
    JOIN t_lect_class ON t_lect.lect_id = t_lect_class.join_lectid
    JOIN t_class ON t_lect_class.join_Lclassid = t_class.class_id
    WHERE t_logininfo.username = :username";

    $statement = $connect->prepare($query);
    $statement->bindParam(':username', $username, PDO::PARAM_STR);
    $statement->execute();
    $classes = $statement->fetchAll(PDO::FETCH_ASSOC);

    // // Log the query and the fetched data
    // error_log("Fetched classes: " . json_encode($classes));


    // Return the classes data as JSON
    header('Content-Type: application/json');
    echo json_encode($classes);
} else {
    // If username parameter is not provided, return an error response
    header('Content-Type: application/json');
    echo json_encode(array('error' => 'Username parameter is missing.'));
}
