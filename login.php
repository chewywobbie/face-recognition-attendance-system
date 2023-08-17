<?php

//login.php

include('admin/database_connection.php');


session_start();

// if(!isempty($_POST['command'])){
//   echo $_POST['command'];
// }

if(isset($_POST["login"])){
  include('stdindex.php');

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
    <div class="col-md-4">

    </div>
    <div class="col-md-4" style="margin-top:20px;">
      <div class="card">
        <div class="card-header">Sign In</div>
        <div class="card-body">

          <form method="post" action="check_logininfo.php">
            <div class="form-group" >
              <label>Enter Username</label>
              <input type="text" name="username" id="username" class="form-control" />
              <span id="error_username" class="text-danger"></span>
            </div>
            <div class="form-group">
              <label>Enter Password</label>
              <input type="password" name="pw" id="pw" class="form-control" />
              <span id="error_pw" class="text-danger"></span>
            </div>
            <div class="form-group">
              <div class="d-flex justify-content-between">
                <input type="submit" name="login" id="login" class="btn btn-info" value="Login" />
                <a href="register.php" class="btn btn-link">Register</a>
              </div>
            </div>
          </form>

        </div>
      </div>
    </div>
    <div class="col-md-4">

    </div>
  </div>
</div>

</body>
</html>

<!-- <script>
$(document).ready(function(){
  $('#login').on('submit', function(event){
    event.preventDefault();
    $.ajax({
      url:"check_logininfo.php",
      method:"POST",
      data:$(this).serialize(),
      dataType:"json",
      beforeSend:function(){
        $('#login').val('Validating...');
        $('#login').attr('disabled','disabled');
      },
      success:function(data)
      {
        if(data.success)
        {
          location.href="index.php";
        }
        if(data.error)
        {
          $('#login').val('Login');
          $('#login').attr('disabled', false);
          if(data.error_username != '')
          {
            $('#error_username').text(data.error_username);
          }
          else
          {
            $('#error_username').text('');
          }
          if(data.error_pw != '')
          {
            $('#error_pw').text(data.error_pw);
          }
          else
          {
            $('#error_pw').text('');
          }
        }
      }
    })
  });
});
</script> -->
