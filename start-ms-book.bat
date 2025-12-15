@echo off
cd /d "C:\Users\Ruddy\Documents\SIS-313 Desarrollo de software\Union front y MS\SpringBoot-LibraryProject-main\ms-book"
echo Waiting 10 seconds for Config Server to start...
timeout /t 10 /nobreak
echo Starting ms-book...
java -jar target/ms-book-0.0.1-SNAPSHOT.jar
pause