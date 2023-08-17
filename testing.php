<?php
// receive_data.php

// Check if the request is a POST request
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Check for the presence of the login parameter to identify it as a login request
    if (isset($_POST["login"])) {
        // Get the POST data
        $username = $_POST["username"];
        $password = $_POST["pw"];

        // Here, you can perform your login logic with the received $username and $password

        // For demonstration purposes, let's simply echo the login result back to the app
        if ($username === "nai" && $password === "nai") {
            echo "LOGIN SUCCESSFUL";
        } else {
            echo "LOGIN FAILED";
        }
    } else {
        echo "Error: Invalid request. 'login' parameter missing.";
    }
} else {
    echo "Error: Invalid request method. Please use POST.";
}
?>
