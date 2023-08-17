
<?php

//lect_register.php

include('admin/database_connection.php');

session_start();

$errors = 0; // Initialize error count

if (isset($_POST["lectregister"])) {
  if (empty($_POST["username"])) {
    $errors++;
  }
  if (empty($_POST["pw"])) {
    $errors++;
  }
  if (empty($_POST["lectname"])) {
    $errors++;
  }
  if (empty($_POST["classes"])) {
    $errors++;
  }


// if (isset($_POST["register"]) && ($errors == 0)) {
  $username = $_POST["username"];
  $password = password_hash($_POST["pw"], PASSWORD_DEFAULT);
  $lectname = addslashes($_POST["lectname"]);
  $classes = $_POST["classes"];

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

  $query = "INSERT INTO t_logininfo (logininfo_id, username, pw, accesslvl) VALUES ('$next_login_id','$username', '$password', 2)";
  $statement = $connect->prepare($query);
  $statement->execute();

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
  $query = "INSERT INTO t_lect (lect_id, lectname, lect_loginid )VALUES ('$next_lect_id','$lectname','$next_login_id')";
  $statement = $connect->prepare($query);
  $statement->execute();

  // Insert the lecturer-class relationships into t_lect_class table
  foreach($classes as $classId) {

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

    $query = "INSERT INTO t_lect_class (lect_class_id,join_lectid, join_Lclassid) VALUES ('$next_lectclass','$next_lect_id', '$classId')";
    $statement = $connect->prepare($query);
    $statement->execute();
  }
  header('Location: login.php');
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
  <!-- <title>Student Attendance System in PHP using Ajax</title> -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.0/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
</head>
<body>

<div class="jumbotron text-center" style="margin-bottom:0">
  <h1>UniSnapAttend</h1>
</div>


<div class="container">
  <div class="row">
    <div class="col-md-4"></div>
    <div class="col-md-4" style="margin-top: 20px;">
      <div class="card">
        <div class="card-header">Lecturers' Registration</div>
        <div class="card-body">
          <form method="post" action="lect_register.php">
            <div class="form-group">
              <label>Enter Username</label>
              <input type="text" name="username" id="username" class="form-control" required />
            </div>
            <div class="form-group">
              <label>Enter Password</label>
              <input type="password" name="pw" id="pw" class="form-control" required />
            </div>
            <div class="form-group">
              <label>Lecturer Name</label>
              <input type="text" name="lectname" id="lectname" class="form-control" required />
            </div>
            <div class="form-group">
              <label>Select Classes</label>
              <?php
              // Retrieve classes from t_class table
              $query = "SELECT * FROM t_class";
              $statement = $connect->prepare($query);
              $statement->execute();
              $classes = $statement->fetchAll();

              foreach ($classes as $class) {
                $classId = $class['class_id'];
                $classCode = $class['classcode'];
                $className = $class['classname'];

                echo '<div class="custom-control custom-checkbox">';
                echo '<input type="checkbox" class="custom-control-input" id="class' . $classId . '" name="classes[]" value="' . $classId . '">';
                echo '<label class="custom-control-label" for="class' . $classId . '">' . $classCode . ' - ' . $className . '</label>';
                echo '</div>';
              }
              ?>
            </div>
            <div class="form-group">
            </div>
            <div class="form-group">
              <input type="submit" name="lectregister" id="lectregister" class="btn btn-info" value="Register" />
              <a href="login.php" class="btn btn-secondary">Cancel</a>
            </div>
          </form>
        </div>
      </div>
    </div>
    <div class="col-md-4"></div>
  </div>
</div>

</body>
</html>