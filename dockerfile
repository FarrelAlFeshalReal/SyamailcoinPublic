FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . .
RUN javac *.java
EXPOSE 8080
CMD ["sh", "-c", "java Realgenesiss & while true; do echo -e 'HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK' | nc -l -p 8080 -q 1; done"]
