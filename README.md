# hp_album_service
Album lookup service + thymeleaf templates for HP assessment.

Please find the code_review sample in the resources folder.

You can run the service from IntelliJ or alternatively, from the root project folder - `.\mvnw install`

And then `java -jar` + path of the installed JAR.

You can access the landing page of album search at `http://localhost:8080`.
To query REST variant of album search, you can send a GET Request to `http://localhost:8080/album/search?artist=<artistName>`.
