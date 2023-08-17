<?php
// check_logininfo_app.php

include('admin/database_connection.php');

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

        // Prepare the response array
        $response = array(
          'success' => true,
          'accesslvl' => $result["accesslvl"]
        );

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
  $response = array(
    'success' => false,
    'error_username'=> $error_username,
    'error_pw' => $error_pw
  );
} else {
  $response = array(
    'success' => false
  );
}

// Send the JSON response
echo json_encode($response);
?>
