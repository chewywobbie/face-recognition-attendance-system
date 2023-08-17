<?php
// check_logininfo.php

include('admin/database_connection.php');

session_start();

$username = '';
$pw = '';
$error_username = '';
$error_pw = '';
$error = 0;

if (empty($_POST["username"])) {
  $error_username = 'Username is required';
  $error++;
} else {
  $username = $_POST["username"];
}


if (empty($_POST["pw"])) {
  $error_pw = 'Password is required';
  $error++;
} else {
  $pw = $_POST["pw"];
}

if ($error == 0) {
  $query = "
    SELECT * FROM t_logininfo 
    WHERE username = '".$username."'
  ";

  $statement = $connect->prepare($query);
  if ($statement->execute()) {
    $total_row = $statement->rowCount();
    if ($total_row > 0) {
      $result = $statement->fetch(); // Use fetch() as we expect a single row.
      if (password_verify($pw, $result["pw"])) {
        $_SESSION["logininfo_id"] = $result["logininfo_id"];
        $_SESSION["accesslvl"] = $result["accesslvl"];
        $_SESSION["std_id"] = $result["logininfo_id"];

        // Prepare the response array
        $response = array();

        if ($result["accesslvl"] == 2) {
          header('location: lectindex.php');
          $response['redirect'] = 'lectindex.php';

        } elseif ($result["accesslvl"] == 3) {

          header('location: stdindex.php');
          $response['redirect'] = 'stdindex.php';
        } else {

          echo "Page not found";
          session_destroy();
          header('location: login.php');
          $response['error'] = "Unknown access level";
        }

        // Send the JSON response
        echo json_encode($response);
        exit;
      } else {
        $error_pw = "Incorrect Password";
        $error++;
      }
    } else {
      $error_username = "Username does not exist";
      $error++;
    }
  }
}

if ($error > 0) {
  $output = array(
    'error'         => true,
    'error_username'=> $error_username,
    'error_pw'      => $error_pw
  );
} else {
   // Include the 'success' key when login is successful
   $output = array(
    'success'       => true
  );
  
  // Add the 'accesslvl' key when login is successful
  $output['accesslvl'] = $_SESSION["accesslvl"];
}

// Send the JSON response
echo json_encode($output);
?>
