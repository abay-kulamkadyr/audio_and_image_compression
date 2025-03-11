<h1 align="center">
  <br>
  <img src="/images/landscape.png" alt="Audio and Image Compression" width="400" hieght="300">
  <img src="/images/canyon.jpg" alt="Audio and Image Compression" width="400" height="300">
  <br>
  Audio and Image Compression
  <br>
</h1>

<h4 align="center">A project demonstrating lossless audio and lossy image compression techniques.</h4>

<p align="center">
  <a href="#description-of-the-project">Description</a> •
  <a href="#technologies-used">Technologies</a> •
  <a href="#project-organization">Organization</a> •
  <a href="#image-compression">Image Compression</a> •
  <a href="#audio-compression">Audio Compression</a>
</p>


## Description of the project

* Lossless Audio Compression: the program compresses wav files using DPCM and mid/side encoding and passes it further to Java's Deflater library.

* Lossy Image Compression: compresses a BMP image by transforming it into the DCT frequency domain and quantizing using a quantization table.

## Tech Stack

* Maven build automation
* Java programming language
* JavaFX GUI library
* Java Sound API

## Project Organization

* The project consists of two sub-programs, and you will need to compile and execute them separately

## How to use

To clone and run both applications, you will need [Git](https://git-scm.com), [https://maven.apache.org/install.html], and [Java][https://www.java.com/en/download] installed on your computer.

```bash
# Clone this repository
$ git clone https://github.com/abay-kulamkadyr/audio_and_image_compression.git

# Go into the repository
$ cd audio_compression #or cd image_compression

# Compile and build a jar executable
$ mvn clean compile assembly:single

# Go to the target directory
$ cd target

# Run the program
$ java -jar program.jar

```
