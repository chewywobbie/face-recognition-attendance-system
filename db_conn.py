import mysql.connector


def db_connect():
    mydb = mysql.connector.connect(
        host="localhost",
        user="root",
        password="",
        database="attendance"
    )

    # Check if the connection is successful
    if mydb.is_connected():
        print("Connected to the database!")

    return mydb


def db_close(mydb):
    # Close the database connection
    mydb.close()
    print("Database connection closed.")
