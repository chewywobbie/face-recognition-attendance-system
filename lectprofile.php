<?php
  // lectprofile.php
  include('admin/database_connection.php');
  include('lectheader.php');
  session_start();

  if (!isset($_SESSION["logininfo_id"])) {
    header("Location: login.php");
    exit();
  }

  $lecturer_id = $_SESSION["logininfo_id"];

  // Retrieve lecturer's name
  $query = "SELECT lectname FROM t_lect WHERE lect_loginid = :lecturer_id";
  $statement = $connect->prepare($query);
  $statement->bindParam(':lecturer_id', $lecturer_id, PDO::PARAM_INT);
  $statement->execute();
  $lecturer = $statement->fetch(PDO::FETCH_ASSOC);

  // Retrieve classes taught by the lecturer
  $query = "SELECT t_class.classcode, t_class.classname
            FROM t_class
            INNER JOIN t_lect_class ON t_class.class_id = t_lect_class.join_Lclassid
            INNER JOIN t_lect ON t_lect.lect_id = t_lect_class.join_lectid
            WHERE t_lect.lect_loginid = :lecturer_id";
  $statement = $connect->prepare($query);
  $statement->bindParam(':lecturer_id', $lecturer_id, PDO::PARAM_INT);
  $statement->execute();
  $classes = $statement->fetchAll(PDO::FETCH_ASSOC);
?>

<div class="container" style="margin-top:30px">
  <div class="card">
    <div class="card-header">
      <h5 class="card-title">Lecturer Profile</h5>
    </div>
    <div class="card-body">
      <div class="row">
        <div class="col-md-6">
          <h6 class="card-subtitle">NAME:</h6>
        </div>
        <div class="col-md-3 text-left">
          <h6 class="card-subtitle" style="font-weight: normal;"style="margin-right:40px"><?php echo $lecturer['lectname']; ?></h6>
        </div>
      </div>
      <div class="row" style="margin-top:20px">
        <div class="col-md-6">
          <h6 class="card-subtitle">CLASSES:</h6>
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
