<?php
include('admin/database_connection.php');

// Check if the POST parameter 'username' is set
if (isset($_POST['username'])) {
    $username = $_POST['username'];

    $query = "SELECT lectname FROM t_lect INNER JOIN t_logininfo ON t_lect.lect_loginid = t_logininfo.logininfo_id WHERE t_logininfo.username = :username";

    $statement = $connect->prepare($query);
    $statement->bindParam(':username', $username, PDO::PARAM_STR);
    $statement->execute();
    $lectname = $statement->fetchAll(PDO::FETCH_ASSOC);
    
    // Log the query and the fetched data
    error_log("Query: " . $query);
    error_log("Fetched lecturer names: " . json_encode($lectname));
    
    // Prepare the JSON response with the lecturer's name
    $response = array();
    if ($lectname) {
        // Assuming the query will only return one row for the lecturer name
        $lecturerName = $lectname[0]['lectname'];
        $response['lecturer_name'] = $lecturerName;
    } else {
        // Handle the case when no data is found or an error occurs
        $response['error'] = "Failed to fetch lecturer's name.";
    }
    
    // Return the JSON response
    echo json_encode($response);
}
?>
