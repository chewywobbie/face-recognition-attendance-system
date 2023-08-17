<?php

//stdindex.php
  include('admin/database_connection.php');
  include('stdheader.php');

session_start();


if($_SESSION["logininfo_id"] == NULL || $_SESSION["logininfo_id"] == ""){
    // Clear all session variables
    session_unset();
    // Destroy the session
    session_destroy();
    header("Location: login.php");
    // exit();
}

// if($_SESSION["logininfo_id"] != NULL || $_SESSION["logininfo_id"] != ""){
//   $user_id = $_SESSION["logininfo_id"];
// }

// if (!isset($user_id)) {
//   // Clear all session variables
//   session_unset();

//   // Destroy the session
//   session_destroy();
//   header("Location: login.php");
//   // exit();
// }

// $class_id = 2;

// if (isset($_POST['class'])) {
//   $class_id = $_POST['class'];
// }

// // Retrieve the classes for the student
// $query = "SELECT t_class.class_id, t_class.classname
// FROM t_class
// INNER JOIN t_std_class ON t_class.class_id = t_std_class.join_classid
// INNER JOIN t_std ON t_std.std_id = t_std_class.join_stdid
// INNER JOIN t_logininfo ON t_logininfo.logininfo_id = t_std.std_loginid
// WHERE t_logininfo.logininfo_id = :std_login_id";
// $statement = $connect->prepare($query);
// $statement->bindParam(':std_login_id',$std_login_id, PDO::PARAM_INT);
// $lect_login_id = $_SESSION["logininfo_id"];
// $statement->execute();
// $classes = $statement->fetchAll(PDO::FETCH_ASSOC);
// ?>

<div class="container" style="margin-top:20px">
<form method="test" action="" align="center" style="margin-bottom:10px">
<?php

            $logininfo_id = $_SESSION["logininfo_id"];
            $query = "SELECT username
                      FROM t_logininfo
                      WHERE logininfo_id = :logininfo_id";
                    
            $statement = $connect->prepare($query);
            $statement->bindParam(':logininfo_id', $logininfo_id, PDO::PARAM_INT);
            $statement->execute();
            $result = $statement->fetch(PDO::FETCH_ASSOC);
            $username = $result['username'];
                    
            echo 'Welcome, ' . $username . '!';
          ?>
</form>
  <div class="card">
    <div class="card-header">
      <div class="row">
        <div class="col-md-9">Student Attendance Status</div>
        <div class="col-md-3" align="right">
        </div>
      </div>
    </div>
    <div class="card-body">
      <div class="table-responsive">
        <table class="table table-striped table-bordered" id="stdtable">
          <thead>
            <tr>
              <th>Date</th>
              <th>Class Code</th>
              <th>Class Name</th>
              <th>Attendance Status</th>
              <!--<th>Report</th>-->
            </tr>
          </thead>
          <tbody>
            <?php
            $student_id = $_SESSION["logininfo_id"];

            $query = "SELECT t_attendance.attend_date, t_class.classcode, t_class.classname, t_attendance.attend_status
            FROM t_attendance
            INNER JOIN t_class ON t_attendance.attend_classid = t_class.class_id
            INNER JOIN t_std ON t_attendance.attend_stdid = t_std.std_id
            INNER JOIN t_logininfo ON t_std.std_loginid = t_logininfo.logininfo_id
            WHERE t_logininfo.logininfo_id = :student_id;
            ";

            $statement = $connect->prepare($query);
            $statement->bindParam(':student_id', $student_id, PDO::PARAM_INT);

            $statement->execute();
            $attendances = $statement->fetchAll(PDO::FETCH_ASSOC);

            // Generate the table rows for attendances
            foreach ($attendances as $attendance) {
              echo '<tr> 
                      <td>' . $attendance['attend_date'] . '</td>
                      <td>' . $attendance['classcode'] . '</td>
                      <td>' . $attendance['classname'] . '</td>
                      <td>' . $attendance['attend_status'] . '</td>
                    </tr>';
            }
            ?>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</body>
</html>
