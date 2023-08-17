
<?php

// Include the database connection file
include('admin/database_connection.php');

if (isset($_POST['username'])) {
    $username = $_POST['username'];
    error_log("Received username: " . $username);

    // Fetch student details by username, including all classes
    $query = "SELECT
        s.stdname,
        s.stdmatricno,
        c.coursecode,
        c.coursename,
        GROUP_CONCAT(DISTINCT cl.classcode ORDER BY cl.classcode ASC) AS classcodes,
        GROUP_CONCAT(DISTINCT cl.classname ORDER BY cl.classcode ASC) AS classnames
    FROM
        t_logininfo AS l
        JOIN t_std AS s ON l.logininfo_id = s.std_loginid
        JOIN t_course AS c ON s.std_courseid = c.course_id
        JOIN t_std_class AS sc ON s.std_id = sc.join_stdid
        JOIN t_class AS cl ON sc.join_classid = cl.class_id
    WHERE
        l.username = :username
    GROUP BY
        s.std_id";

    $statement = $connect->prepare($query);
    $statement->execute(['username' => $username]);
    $result = $statement->fetch(PDO::FETCH_ASSOC);

    // Prepare the response
    $response = array(
        'stdname' => $result['stdname'],
        'stdmatric' => $result['stdmatricno'],
        'stdcourse' => $result['coursecode'] . ' - ' . $result['coursename'],
        'stdclasses' => formatClasses($result['classcodes'], $result['classnames'])
    );

    // Send the response as JSON
    header('Content-Type: application/json');
    echo json_encode($response);
} else {
    // Send error response if 'username' parameter is missing
    header('Content-Type: application/json');
    echo json_encode(array('error' => 'username parameter is missing.'));
}

function formatClasses($classcodes, $classnames) {
    $classes = array();
    $classCodesArray = explode(',', $classcodes);
    $classNamesArray = explode(',', $classnames);

    for ($i = 0; $i < count($classCodesArray); $i++) {
        $classes[] = $classCodesArray[$i] . ' - ' . $classNamesArray[$i];
    }

    return implode(', ', $classes);
}
?>