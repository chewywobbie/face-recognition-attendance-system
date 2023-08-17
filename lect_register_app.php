<?php

// lect_register_app.php

// Include the database connection file
include('admin/database_connection.php');

// Function to handle the response and send JSON data
function sendResponse($success, $message) {
    $response = array("success" => $success, "message" => $message);
    ob_clean(); // Clear the output buffer to remove any unwanted data (e.g., <br> tag)
    header('Content-Type: application/json');
    echo json_encode($response);
    exit;
}

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // Retrieve data from the POST request
    $username = isset($_POST['username']) ? $_POST['username'] : '';
    $password = isset($_POST['pw']) ? $_POST['pw'] : '';
    $lectName = isset($_POST['lectname']) ? $_POST['lectname'] : '';
    $selectedClasses = isset($_POST['classes']) ? $_POST['classes'] : array();

    // Perform basic validation on input data
    if (empty($username) || empty($password) || empty($lectName) || empty($selectedClasses)) {
        sendResponse(false, "Missing required fields.");
    }

    // Hash the password for security
    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

    try {
        // Begin the database transaction
        $connect->beginTransaction();

        // Insert the lecturer into t_logininfo table
        $query = "SELECT MAX(logininfo_id) AS max_id FROM t_logininfo";
        $statement = $connect->prepare($query);
        $statement->execute();
      
        $result = $statement->fetch(PDO::FETCH_ASSOC);
        if($result === NULL)
        {
          $result = 0;
          $next_login_id = $result['max_id']+1;
        }
        else{
          $next_login_id = $result['max_id']+1;
        }
      
        $loginInfoQuery = "INSERT INTO t_logininfo (logininfo_id, username, pw, accesslvl) VALUES (?, ? , ?, 2)";
        $loginInfoStatement = $connect->prepare($loginInfoQuery);
        $loginInfoStatement->execute([$next_login_id, $username, $hashedPassword]);

        $query = "SELECT MAX(lect_id) AS max_id FROM t_lect";
        $statement = $connect->prepare($query);
        $statement->execute();
        $result = $statement->fetch(PDO::FETCH_ASSOC);
        if($result === NULL)
        {
          $result = 0;
          $next_lect_id = $result['max_id']+1;
        }
        else{
          $next_lect_id = $result['max_id']+1;
        }

        // Insert the lecturer into t_lect table
        $lecturerQuery = "INSERT INTO t_lect (lect_id, lectname, lect_loginid) VALUES (?, ?, ?)";
        $lecturerStatement = $connect->prepare($lecturerQuery);
        $lecturerStatement->execute([$next_lect_id, $lectName, $next_login_id]);

        $selectedClassesArray = explode(",", $selectedClasses); // Convert the comma-separated string to an array

        foreach ($selectedClassesArray as $className) {
            $className = trim($className);

            // Convert class name to class ID
            $classId = getClassIdByClassName($className);
            if (!is_numeric($classId)) {
                sendResponse(false, "Invalid class ID encountered.");
            }

            $query = "SELECT MAX(lect_class_id) AS max_id FROM t_lect_class";
            $statement = $connect->prepare($query);
            $statement->execute();
            $result = $statement->fetch(PDO::FETCH_ASSOC);
            if($result === NULL)
            {
              $result = 0;
              $next_lectclass = $result['max_id']+1;
            }
            else{
              $next_lectclass= $result['max_id']+1;
            }
            // Insert the lecturer-class relationships into t_lect_class table
            $lecturerClassQuery = "INSERT INTO t_lect_class (lect_class_id, join_lectid, join_Lclassid) VALUES (?, ?, ?)";
            $lecturerClassStatement = $connect->prepare($lecturerClassQuery);
            $lecturerClassStatement->execute([$next_lectclass, $next_lect_id, $classId]);
        }

        // Commit the transaction if everything is successful
        $connect->commit();

        sendResponse(true, "Lecturer registration successful.");

    } catch (PDOException $e) {
        // Rollback the transaction in case of any errors
        $connect->rollBack();

        sendResponse(false, "An error occurred during lecturer registration.");
    }

} else {
    // If the request method is not POST, return an error message
    sendResponse(false, "Invalid request method.");
}
