<?php
// upload_picture.php
include('admin/database_connection.php');
include('stdheader.php');
session_start();

if (!isset($_SESSION["logininfo_id"])) {
  header("Location: login.php");
  exit();
}

if (isset($_POST["upload"])) {
  // Process the uploaded picture
  if (isset($_FILES['picture'])) {
    $file_name = $_FILES['picture']['name'];
    $file_tmp = $_FILES['picture']['tmp_name'];

    // Extract the file extension from the original file name
    $file_extension = pathinfo($file_name, PATHINFO_EXTENSION);

    $_SESSION['image_path'] = $file_path;
    
    // Get the student ID based on the logininfo_id session
    $logininfo_id = $_SESSION["logininfo_id"];
    $query = "SELECT std_id, stdname FROM t_std WHERE std_loginid = :logininfo_id";
    $statement = $connect->prepare($query);
    $statement->bindValue(':logininfo_id', $logininfo_id, PDO::PARAM_INT);
    $statement->execute();
    $result = $statement->fetch(PDO::FETCH_ASSOC);
    if (!$result) {
      // Handle the case when the student ID is not found for the given logininfo_id
      echo "Error: Student ID not found for the current session.";
      exit();
    }

    $student_id = $result['std_id'];
    $stdname = $result['stdname'];

    $img_count = countimagesStd($student_id);

    // Increment the image count
    $img_count++;

    // Append the count to the student's name in the file name
    $fileName = "{$std_name}{$img_count}.jpg";

    $image_upload_path = "C:/Users/User/Desktop/facedetect/people/".$file_name;
    // move file to path
    if (move_uploaded_file($file_tmp, $image_upload_path)) {
      echo "<h3>  Image uploaded successfully!</h3>";
    } else {
      echo "<h3>  Failed to upload image!</h3>";
    }

    // Create the file path with the student's name and ID
    $file_path = "C:/Users/User/Desktop/facedetect/people/{$fileName}";


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

    $query = "INSERT INTO t_imgpath (imgpath_id, imgp_stdid, imagepath) VALUES ('$next_imgid','$student_id', '$file_path')";
    $statement = $connect->prepare($query);
    $statement->execute();

    $python_path = 'C:\Users\User\Desktop\facedetect\venv\Scripts\python.exe';
    $python_script_path = "C:/Users/User/Desktop/facedetect/face_train.py";
    $stud_id = escapeshellarg($student_id);
    $stud_name = escapeshellarg($stdname);
    $img_path = escapeshellarg($file_path);
    $command = "$python_path $python_script_path $stud_name $img_path";

    echo "command: " .$command;

    // Execute the Python script using exec() and capture the output
    $output = [];
    $return_value = 0;
    exec($command, $output, $return_value);

    // Display the output and return value
    echo "Python Script Output: " . implode("\n", $output) . "\n";
    echo "Return value: " . $return_value;  

    header('Location: stdprofile.php');
    exit();
  }
}
?>

<div class="container" style="margin-top: 30px;">
  <div class="card">
    <div class="card-header">
      <h5 class="card-title">Upload Profile Picture</h5>
    </div>
    <div class="card-body">
      
      <form method="post" enctype="multipart/form-data">
        <div class="form-group">
          <label>Select Picture:</label>
          <input type="file" name="picture" class="form-control-file" accept="image/*" required>
        </div>
        <div class="form-group">
          <button type="submit" name="upload" class="btn btn-primary">Upload</button>
        </div>
      </form>

    </div>
  </div>
</div>
</body>
</html>
