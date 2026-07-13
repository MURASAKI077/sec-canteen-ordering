# Backend load test

Run the non-mutating dish-list load test while Tomcat and MySQL are running:

```cmd
cd /d D:\sec-canteen-ordering-github\server-servlet\tools
"D:\Eclipse Adoptium\jdk-17.0.19.10\bin\javac.exe" LoadTest.java
"D:\Eclipse Adoptium\jdk-17.0.19.10\bin\java.exe" LoadTest http://localhost:8080/SEC_Servlet/DishServlet 100 20
```

The final two numbers are total requests and concurrent workers. Increase them gradually rather than jumping directly to a large value.
