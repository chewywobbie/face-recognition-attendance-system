<?php
  // stdprofile.php
  include('admin/database_connection.php');
  include('stdheader.php');
  session_start();

  if (!isset($_SESSION["logininfo_id"])) {
    header("Location: login.php");
    exit();
  }

  $student_id = $_SESSION["logininfo_id"];
  error_log($student_id);
  $numImages = countImagesStdLogin($student_id);
  // Retrieve student's profile information
  $query = "SELECT t_std.stdname, t_std.stdmatricno, t_course.coursecode, t_course.coursename, t_class.classcode, t_class.classname
  FROM t_std
  INNER JOIN t_course ON t_std.std_courseid = t_course.course_id
  INNER JOIN t_std_class ON t_std.std_id = t_std_class.join_stdid
  INNER JOIN t_class ON t_std_class.join_classid = t_class.class_id
  INNER JOIN t_logininfo ON t_std.std_loginid = t_logininfo.logininfo_id
  WHERE t_logininfo.logininfo_id = :student_id";
  $statement = $connect->prepare($query);
  $statement->bindParam(':student_id', $student_id, PDO::PARAM_INT);
  $statement->execute();
  $profile = $statement->fetch(PDO::FETCH_ASSOC);

  // Retrieve classes enrolled by the student
  $query = "SELECT t_class.classcode, t_class.classname
  FROM t_class
  INNER JOIN t_std_class ON t_class.class_id = t_std_class.join_classid
  INNER JOIN t_std ON t_std.std_id = t_std_class.join_stdid
  INNER JOIN t_logininfo ON t_std.std_loginid = t_logininfo.logininfo_id
  WHERE t_logininfo.logininfo_id = :student_id";
  $statement = $connect->prepare($query);
  $statement->bindParam(':student_id', $student_id, PDO::PARAM_INT);
  $statement->execute();
  $classes = $statement->fetchAll(PDO::FETCH_ASSOC);

 
?>

<div class="container" style="margin-top:30px">
  <div class="card">
    <div class="card-header">
      <h5 class="card-title">Student Profile</h5>
    </div>
    <div class="card-body">
        <div class="row">
          <div class="col-md-6">
            <h6 class="card-subtitle">Name:</h6>
          </div>
          <div class="col-md-6 text-left">
            <h6 class="card-subtitle" style="font-weight: normal;"><?php echo $profile['stdname']; ?></h6>
          </div>
        </div>
        <div class="row" style="margin-top:20px">
          <div class="col-md-6">
            <h6 class="card-subtitle">Matric No:</h6>
          </div>
          <div class="col-md-6 text-left">
            <h6 class="card-subtitle" style="font-weight: normal;"><?php echo $profile['stdmatricno']; ?></h6>
          </div>
        </div>
        <div class="row" style="margin-top:20px">
          <div class="col-md-6">
            <h6 class="card-subtitle">Course:</h6>
          </div>
          <div class="col-md-6 text-left">
            <h6 class="card-subtitle" style="font-weight: normal;"><?php echo $profile['coursecode'] . ' - ' . $profile['coursename']; ?></h6>
          </div>
        </div>
        <div class="row" style="margin-top:20px">
          <div class="col-md-6">
            <h6 class="card-subtitle">Profile Images:</h6>
          </div>
          <div class="col-md-6 text-left">
          <?php

            if ($numImages > 0) {
              echo '<p>' . $numImages . ' image(s) available</p>';
            } else {
              echo '<p>No images available</p>';
            }
            ?>
          <!-- Add Picture Button -->
          <a href="upload_stdpic.php" class="btn btn-primary">Add Picture</a>
        </div>
      </div>
        <div class="row" style="margin-top:20px">
          <div class="col-md-6">
            <h6 class="card-subtitle">Classes:</h6>
          </div>
          <div class="col-md-6 text-left">
            <ul class="list-group" style="margin-bottom: 0;">
              <?php foreach ($classes as $class) { ?>
                <li class="list-group-item"><?php echo $class['classcode'] . ' - ' . $class['classname']; ?></li>
              <?php } ?>
            </ul>
          </div>
        </div>
    </div>
  </div>
</div>
</body>
</html>
