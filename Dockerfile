FROM scratch
ADD sonatype-iq-app-licenses/target/sonatype-iq-app-licenses-*-jar-with-dependencies.jar /scanme/
CMD echo "Hello world! This is my first Docker image."
