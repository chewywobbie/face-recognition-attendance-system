<?php

// std_register_app.php

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
    $stdName = isset($_POST['stdname']) ? $_POST['stdname'] : '';
    $stdMatricNo = isset($_POST['stdmatric']) ? $_POST['stdmatric'] : '';
    $selectedCourse = isset($_POST['course']) ? $_POST['course'] : '';
    $selectedClassesString = isset($_POST['classes']) ? $_POST['classes'] : '';

    // Perform basic validation on input data
    if (empty($username) || empty($password) || empty($stdName)  || empty($stdMatricNo) || empty($selectedCourse) || empty($selectedClassesString)) {
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
      
        $loginInfoQuery = "INSERT INTO t_logininfo (logininfo_id, username, pw, accesslvl) VALUES (?, ? , ?, 3)";
        $loginInfoStatement = $connect->prepare($loginInfoQuery);
        $loginInfoStatement->execute([$next_login_id, $username, $hashedPassword]);

        $query = "SELECT MAX(std_id) AS max_id FROM t_std";
        $statement = $connect->prepare($query);
        $statement->execute();
        $result = $statement->fetch(PDO::FETCH_ASSOC);
        $next_std_id = ($result['max_id'] === null) ? 1 : $result['max_id'] + 1;

        $courseId = getCourseIdByCourseName($selectedCourse);

        // Insert into t_std table
        $stdquery = "INSERT INTO t_std (std_id, stdname, stdmatricno, std_courseid, std_loginid) VALUES (?, ?, ?, ?, ?)";
        $stdStatement = $connect->prepare($stdquery);
        $stdStatement->execute([$next_std_id,$stdName,$stdMatricNo, $courseId, $next_login_id]);


        $selectedClassesArray = explode(",", $selectedClassesString); // Convert the comma-separated string to an array

        foreach ($selectedClassesArray as $className) {
            $className = trim($className);

            // Convert class name to class ID
            $classId = getClassIdByClassName($className);
            if (!is_numeric($classId)) {
                sendResponse(false, "Invalid class ID encountered.");
            }
            $query = "SELECT MAX(std_class_id) AS max_id FROM t_std_class";
            $statement = $connect->prepare($query);
            $statement->execute();
            $result = $statement->fetch(PDO::FETCH_ASSOC);
            $next_stdclass = ($result['max_id'] === null) ? 1 : $result['max_id'] + 1;

            // Insert the lecturer-class relationships into t_lect_class table
            $stdClassQuery = "INSERT INTO t_std_class (std_class_id,join_stdid, join_classid) VALUES (?, ?, ?)";
            $stdClassStatement = $connect->prepare($stdClassQuery);
            $stdClassStatement->execute([$next_stdclass, $next_std_id, $classId]);
        }

        $connect->commit();
        sendResponse(true, "Student registration successful.");

    } catch (PDOException $e) {
        // Rollback the transaction in case of any errors
        $connect->rollBack();
        error_log("Error during student registration: " . $e->getMessage());
        sendResponse(false, "An error occurred during student registration.");
    }

} else {
    // If the request method is not POST, return an error message
    sendResponse(false, "Invalid request method.");
}
