<?php
// std_register.php

include('admin/database_connection.php');

session_start();

$errors = 0; 
if (isset($_POST["register"])) {
  if (empty($_POST["username"])) {
    $errors_username = "Username is required";
    alert( $errors_username);
    $errors++;
  }
  if (empty($_POST["pw"])) {
    $errors_pw="Password is required";
    alert($errors_pw);
    $errors++;
  }
  if (empty($_POST["stdname"])) {
    $errors_stdname="Student name is required";
    alert($errors_stdname);
    $errors++;
  }
  if (empty($_POST["stdmatricno"])) {
    $errors_stdmatric = "Student Matric No. is required";
    alert($errors_stdmatric);
    $errors++;
  }
  if(empty($_POST["course"])) {
    $errors_course = "Please select your course";
    alert($errors_course);
    $errors++;
  }
  if (empty($_POST["classes"])) {
    $errors_class = "Please select at least one class";
    alert($errors_class);
    $errors++;
  }

  //if(isset($_POST["register"])) {
  $username = $_POST["username"];
  $password = password_hash($_POST["pw"], PASSWORD_DEFAULT);
  $stdname = addslashes($_POST["stdname"]);
  $stdmatricno = $_POST["stdmatricno"];
  $courseId = $_POST["course"];
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

  // Insert into t_logininfo table
  $query = "INSERT INTO t_logininfo (logininfo_id, username, pw, accesslvl) VALUES ('$next_login_id','$username', '$password', 3)";
  $statement = $connect->prepare($query);
  $statement->execute();
  $_SESSION['username'] = $username;

  $query = "SELECT MAX(std_id) AS max_id FROM t_std";
  $statement = $connect->prepare($query);
  $statement->execute();
  $result = $statement->fetch(PDO::FETCH_ASSOC);
  if($result === NULL)
  {
    $result = 0;
    $next_std_id = $result['max_id']+1;
  }
  else{
    $next_std_id = $result['max_id']+1;
  }


  // Insert into t_std table
  $query = "INSERT INTO t_std (std_id, stdname, stdmatricno, std_courseid, std_loginid) VALUES ('$next_std_id','$stdname', '$stdmatricno', '$courseId', '$next_login_id')";
  $statement = $connect->prepare($query);
  $statement->execute();

  $_SESSION['student_name'] = $stdname;

  // Insert into t_std_class table
  foreach($classes as $classId) {

    $query = "SELECT MAX(std_class_id) AS max_id FROM t_std_class";
    $statement = $connect->prepare($query);
    $statement->execute();
    $result = $statement->fetch(PDO::FETCH_ASSOC);
    if($result === NULL)
    {
      $result = 0;
      $next_stdclass = $result['max_id']+1;
    }
    else{
      $next_stdclass= $result['max_id']+1;
    }

    $query = "INSERT INTO t_std_class (std_class_id,join_stdid, join_classid) VALUES ('$next_stdclass','$next_std_id', '$classId')";
    $statement = $connect->prepare($query);
    $statement->execute();
  }

if (isset($_FILES['student_image'])) {
    $file_name = $_FILES['student_image']['name'];
    $file_tmp = $_FILES['student_image']['tmp_name'];

    // Extract the file extension from the original file name
    $file_extension = pathinfo($file_name, PATHINFO_EXTENSION);

    // Get the username of the student
    $username = $_SESSION['username'];

    // Generate a new file name using the student's username and the desired file extension
    $new_file_name = $username . '.' . $file_extension;

    $file_path = 'C:/Users/User/Desktop/facedetect/people/' . $new_file_name;
    $_SESSION['image_path'] = $file_path;

    // Move uploaded file to desired directory with the new file name
    move_uploaded_file($file_tmp, $file_path);

    $query = "SELECT MAX(imgpath_id) AS max_id FROM t_imgpath";
    $statement = $connect->prepare($query);
    $statement->execute();
    $result = $statement->fetch(PDO::FETCH_ASSOC);
    if($result === NULL)
    {
      $result = 0;
      $next_imgid = $result['max_id']+1;
    }
    else{
      $next_imgid = $result['max_id']+1;
    }

    $query = "INSERT INTO t_imgpath (imgpath_id, imgp_stdid, imagepath) VALUES ('$next_imgid','$next_std_id', '$file_path')";
    $statement = $connect->prepare($query);
    $statement->execute();
    
  }
  $student_name = $_SESSION['student_name'];
  $image_path = $_SESSION['image_path'];
  
  // Call the preprocess.py script and pass the image path
  $python_path = 'C:\Users\User\Desktop\facedetect\venv\Scripts\python.exe';
  $python_script_path = "C:/Users/User/Desktop/facedetect/face_train.py";
  $stud_id = escapeshellarg($next_std_id);
  $stud_name = escapeshellarg($username);
  $img_path = escapeshellarg($file_path);

  $command = "$python_path $python_script_path $stud_id $stud_name $img_path";
  error_log ("Python Command: " . $command . "\n");

  // $query = "INSERT INTO testing_commands (command) VALUES ('$command')";
  // $statement = $connect->prepare($query);
  // $statement->execute();

  // $_POST['command'] = $command;
  // echo "command: " .$command;
  
  // Execute the Python script using exec() and capture the output
  $output = [];
  $return_value = 0;
  exec($command, $output, $return_value);

  // Display the output and return value
  echo "Python Script Output: " . implode("\n", $output) . "\n";
  echo "Return value: " . $return_value;
  error_log ("Command: " . $command . "\n, Output: " . $output . "\n, Return value: ". $return_value );

  header('Location: login.php');
  session_destroy();  
  exit();
}


// Retrieve courses from t_course table
$query = "SELECT * FROM t_course";
$statement = $connect->prepare($query);
$statement->execute();
$courses = $statement->fetchAll();

// Retrieve classes from t_class table
$query = "SELECT * FROM t_class";
$statement = $connect->prepare($query);
$statement->execute();
$classes = $statement->fetchAll();

?>
<!DOCTYPE html>
<html lang="en">
<head>
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
        <div class="card-header">Student Registration</div>
        <div class="card-body">
          <form method="post" id="stdregister" enctype="multipart/form-data" >
            <div class="form-group">
              <label>Enter Username</label>
              <input type="text" name="username" id="username" class="form-control" required>
            </div>
            <div class="form-group">
              <label>Enter Password</label>
              <input type="password" name="pw" id="pw" class="form-control" required>
            </div>
            <div class="form-group">
              <label>Student Name</label>
              <input type="text" name="stdname" id="stdname" class="form-control" required>
            </div>
            <div class="form-group">
              <label>Student Matric No</label>
              <input type="text" name="stdmatricno" id="stdmatricno" class="form-control" required>
            </div>
            <div class="form-group">
              <label>Select Course</label>
              <select name="course" id="course" class="form-control" required>
                <option value="">Select Course</option>
                <?php foreach ($courses as $course) { ?>
                  <option value="<?php echo $course['course_id']; ?>"><?php echo $course['coursename']; ?></option>
                <?php } ?>
              </select>
            </div>
            <div class="form-group">
              <label>Select Classes</label>
              <?php foreach ($classes as $class) { ?>
                <div class="custom-control custom-checkbox">
                  <input type="checkbox" class="custom-control-input" id="class<?php echo $class['class_id']; ?>" name="classes[]" value="<?php echo $class['class_id']; ?>">
                  <label class="custom-control-label" for="class<?php echo $class['class_id']; ?>"><?php echo $class['classcode']; ?> - <?php echo $class['classname']; ?></label>
                </div>
              <?php } ?>
            </div>
            <div class="form-group">
              <label>Upload Student Image</label>
              <input type="file" name="student_image" id="student_image" class="form-control-file" accept="image/*">
            </div>
            <div class="form-group">
              <input type="submit" name="register" id="register" class="btn btn-info" value="Register">
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