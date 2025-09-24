FROM openjdk:17-jdk-slim


RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .


RUN javac -cp ".:Java-WebSocket-1.5.4.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar" *.java


EXPOSE 8080


CMD ["sh", "-c", "java -cp \".:Java-WebSocket-1.5.4.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar\" Realgenesiss & while true; do echo -e 'HTTP/1.1 200 OK\\r\\nContent-Length: 2\\r\\n\\r\\nOK' | nc -l -p 8080 -q 1; done"]