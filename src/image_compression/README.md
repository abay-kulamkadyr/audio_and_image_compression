## Image Compression
***Prerequisites:***

    Have maven installed on your machine
    If Maven is not installed, please refer to https://maven.apache.org/install.html
 ***INSTRUCTIONS TO BUILD AND RUN THE PROJECT***

    i) While in image_compression directory, using command prompt, execute the following command to compile and build a jar executable:

        mvn clean compile assembly:single

    ii) Target directory will be created. After target folder is built, go to the following directory to find the executable file:

        cd target

    iii) In the target directory execute the following command to run the program:

        java -jar program.jar
    iv) Output files will be created (quantized results along with resultant images) in the target directory
***INSTRUCTIONS TO DELETE COMPILED AND EXECUTABLE FILES***

    mvn clean

***GENERATED FILES BY THE PROGRAM***

    Results of quantizations represented as value1:value2:value3 at a particular index,
    where value1 is the result of quantization on the Y channel, value2 is the result of quantization on the Co channel, value3 is the result of quantization on the Cg channel.

    Resultant images are store in a .png format
