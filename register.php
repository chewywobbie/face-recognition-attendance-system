<?php

//register.php

include('admin/database_connection.php');

session_start();

// Redirect if user is already logged in
if(isset($_SESSION["logininfo_id"])) {
  header('location:index.php');
}

// Check if the form is submitted
if(isset($_POST["register"])) {
  $userType = $_POST["userType"]; // Get the user type (accesslvl)

// Register as a lecturer
if($userType === "lecturer") {
    header('location:lect_register.php'); // Update the redirection path
  }
  // Register as a student
  elseif($userType === "student") {
    header('location:std_register.php'); // Update the redirection path
  }
  
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
        <div class="card-header">Registration</div>
        <div class="card-body">
          <form method="post" id="register">
            <div class="form-group">
              <label>Select User Type</label>
              <div class="custom-control custom-radio">
                <input type="radio" id="lecturerRadio" name="userType" value="lecturer" class="custom-control-input" required>
                <label class="custom-control-label" for="lecturerRadio">Lecturer</label>
              </div>
              <div class="custom-control custom-radio">
                <input type="radio" id="studentRadio" name="userType" value="student" class="custom-control-input" required>
                <label class="custom-control-label" for="studentRadio">Student</label>
              </div>
            </div>
            <div class="form-group">
              <input type="submit" name="register" id="register" class="btn btn-info" value="Register" />
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

<script>
$(document).ready(function(){
  $('#register').on('submit', function(event){
    event.preventDefault();
    $.ajax({
      url:"register.php",
      method:"POST",
      data:$(this).serialize(),
      dataType:"json",
      beforeSend:function(){
        $('#register').val('Registering...');
        $('#register').attr('disabled','disabled');
      },
      success:function(data)
      {
        if(data.success)
        {
          if(data.userType === "lecturer") {
            location.href = "lect_register.php"; // Redirect to lect_register.php
          } else if(data.userType === "student") {
            location.href = "std_register.php"; // Redirect to std_register.php
          }
        }
        if(data.error)
        {
          $('#register').val('Register');
          $('#register').attr('disabled', false);
          echo "Unable to!"
        }
      }
    })
  });
});
</script>

