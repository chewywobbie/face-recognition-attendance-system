<?php
  //lectindex.php
  include('admin/database_connection.php');
  include('lectheader.php');
  session_start();

  if($_SESSION["logininfo_id"] != NULL || $_SESSION["logininfo_id"] != ""){
    $user_id = $_SESSION["logininfo_id"];
  }

  if (!isset($user_id)) {
    // Clear all session variables
    session_unset();

    // Destroy the session
    session_destroy();
    header("Location: login.php");
    // exit();
  }

  $class_id = 2; // Default class_id if no option is selected

  if (isset($_POST['class'])) {
    $class_id = $_POST['class'];
  }

  // Retrieve the classes for the lecturer
  $query = "SELECT t_class.class_id, t_class.classname
              FROM t_class
              INNER JOIN t_lect_class ON t_class.class_id = t_lect_class.join_Lclassid
              INNER JOIN t_lect ON t_lect.lect_id = t_lect_class.join_lectid
              INNER JOIN t_logininfo ON t_logininfo.logininfo_id = t_lect.lect_loginid
              WHERE t_logininfo.logininfo_id = :lect_login_id";
  $statement = $connect->prepare($query);
  $statement->bindParam(':lect_login_id',$lect_login_id, PDO::PARAM_INT);
  $lect_login_id = $_SESSION["logininfo_id"];
  $statement->execute();
  $classes = $statement->fetchAll(PDO::FETCH_ASSOC);
?>

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
      <div class="col-md-9" style="margin-top:5px"><strong>Overall Student Attendance Status</strong></div>
        <div class="col-md-3" align="right">
          <form method="POST" action="">
            <select class="form-control" id="classDropdown" name="class" onchange="this.form.submit()">
              <option value="">SELECT CLASS</option>
              <?php
                // Generate the dropdown options
                foreach ($classes as $class) {
                  $selected = ($class_id == $class['class_id']) ? 'selected' : '';
                  echo '<option value="'.$class['class_id'].'" '.$selected.'>'.$class['classname'].'</option>';
                }
              ?>
            </select>
          </form>
        </div>
      </div>
    </div>
    <div class="card-body">
      <div class="table-responsive">
        <table class="table table-striped table-bordered" id="lecttable">
          <thead>
            <tr>
              <th>Student Name</th>
              <th>Matric No.</th>
              <th>Date</th>
              <th>Attendance Status</th>
              <!--<th>Report</th>-->
            </tr>
          </thead style="font-weight: normal">
          <tbody>
          <?php
            $query = "SELECT t_std.stdname, t_std.stdmatricno, t_attendance.attend_date, t_attendance.attend_status
                      FROM t_std
                      INNER JOIN t_attendance ON t_std.std_id = t_attendance.attend_stdid
                      WHERE t_attendance.attend_classid = :class_id";

            $statement = $connect->prepare($query);
            $statement->bindParam(':class_id', $class_id, PDO::PARAM_INT);

            $statement->execute();
            $attendances = $statement->fetchAll(PDO::FETCH_ASSOC);

            // Generate the table rows for attendances
            foreach ($attendances as $attendance) {
              echo '<tr> 
                      <td>'.$attendance['stdname'].'</td>
                      <td>'.$attendance['stdmatricno'].'</td>
                      <td>'.$attendance['attend_date'].'</td>
                      <td>'.$attendance['attend_status'].'</td>
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
