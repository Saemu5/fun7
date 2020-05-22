This solution uses the Spring Framework for serving the API endpoint and Apache Derby for persistent storage.
Build with Gradle or make sure Spring and Derby are on the classpath.

Run "Fun7demoApplication.java" to start the application, it will run a server on port 8080 to accept requests.
Type 'exit' in command line to safely terminate app and db.

I assumed that the country code of the user is provided in ISO 3166 alpha-2 standard.
I found the "timezone" parameter useless and left it unchecked. It is still required in the request, due to the specification of the task.
I assumed the "userID" parameter is at most 40 characters in length.
