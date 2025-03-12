<h1 align="center">
  <br>
  <img src="/resources/landscape.png" alt="Audio and Image Compression" width="400">
  <img src="/resources/canyon.jpg" alt="Audio and Image Compression" width="400" >
  <br>
  Audio and Image Compression
  <br>
</h1>

<h4 align="center">A project demonstrating lossless audio and lossy image compression techniques.</h4>

<p align="center">
  <a href="#description">Description</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#technologies">Technologies</a> •
  <a href="#organization">Organization</a> •
  <a href="#how-to-use">How To Use</a>
</p>


## Description

* Lossless Audio Compression: the program compresses wav files using DPCM and mid/side encoding and passes it further to Java's Deflater library.
* Lossy Image Compression: compresses a BMP image by transforming it into the DCT frequency domain and quantizing using a quantization table.

## Key Features

* UI .bmp and .wav selectors
* Display compression ratios of .wav files in a window
* Display .bmp files in a window

## Technologies

* Maven
* Java
* JavaFX GUI
* Java Sound API

## Organization

* The project consists of two sub-programs, and you will need to compile and execute them separately

## How to use

To clone and run both applications, you will need [Git](https://git-scm.com), [Maven](https://maven.apache.org/install.html), and [Java](https://www.java.com/en/download) installed on your computer.

```bash
# Clone this repository
$ git clone https://github.com/abay-kulamkadyr/audio_and_image_compression.git

# Go into the repository
$ cd audio_and_image_compression

# Go into the sub-program directory
$ cd audio_compression #or image_compression

# Compile and build a jar executable
$ mvn clean compile assembly:single

# Go to the target directory
$ cd target

# Run the program
$ java -jar program.jar

```
