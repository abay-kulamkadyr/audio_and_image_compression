## Audio Compression

***Prerequisites:***

    Have maven installed on your machine
    If Maven is not installed, please refer to https://maven.apache.org/install.html
***INSTRUCTIONS TO BUILD AND RUN THE PROJECT***

    i) While in audio_compression directory, using command prompt, execute the following command to compile and build a jar executable:

        mvn clean compile assembly:single

    ii) Target directory will be created. After target folder is built, go to the following directory to find the executable file:

        cd target

    iii) In the target directory execute the following command to run the program:
        java -jar program.jar

    iv) Compressed wav files will be created in the target directory
