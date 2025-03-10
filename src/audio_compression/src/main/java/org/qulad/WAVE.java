package org.qulad;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WAVE {
  private final int bytesPerFrame;
  private final int totalFrameLength;
  private AudioInputStream audioInputStream;
  private final int numChannels;
  private byte[] rawData;

  private final int bytesPerSample;

  private final long totalSizeInBits;
  private static int numOfInvokations = 0;
  AudioFormat decoded;
  // private static WAVE theWave=null;
  private double compressionRatio;
  // buffers to store left and right channels
  private byte[] leftChannelBytes;
  private byte[] rightChannelBytes;

  private short[] leftChannelShort;
  private short[] rightChannelShort;
  private short[] monoChannelShort;

  public WAVE(String path) {
    File fileIn = new File(path);
    try {
      audioInputStream = AudioSystem.getAudioInputStream(fileIn);
    } catch (UnsupportedAudioFileException | IOException e) {
      e.printStackTrace();
    }
    AudioFormat audioFormat = audioInputStream.getFormat();
    numChannels = audioInputStream.getFormat().getChannels();
    bytesPerSample = audioInputStream.getFormat().getSampleSizeInBits() / 8;
    float frameRate = audioFormat.getFrameRate();
    bytesPerFrame = audioFormat.getFrameSize();
    totalFrameLength = (int) audioInputStream.getFrameLength();
    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_UNSIGNED;
    decoded =
        new AudioFormat(
            encoding,
            audioInputStream.getFormat().getSampleRate(),
            audioInputStream.getFormat().getSampleSizeInBits(),
            numChannels,
            bytesPerFrame,
            frameRate,
            false);

    int totalNumberOfSamples;
    if (numChannels > 1) {
      totalNumberOfSamples = totalFrameLength * 2;

    } else {
      totalNumberOfSamples = totalFrameLength;
    }
    totalSizeInBits =
        (long) totalNumberOfSamples * audioInputStream.getFormat().getSampleSizeInBits();
    numOfInvokations++;
  }

  public void read() throws IOException, DataFormatException {

    try (AudioInputStream audioInputStreamDecoded =
        AudioSystem.getAudioInputStream(decoded, audioInputStream)) {

      rawData = new byte[bytesPerFrame * totalFrameLength];
      if (numChannels == 1) {
        if (bytesPerSample == 2) {
          monoChannelShort = new short[totalFrameLength];

        } else if (bytesPerSample == 1) {

        } else {
          System.out.println(
              "The number of bytes per sample is greater than 2, terminating the program...");
          System.exit(1);
        }
      } else {
        if (bytesPerSample == 1) {
          leftChannelBytes = new byte[totalFrameLength * (bytesPerFrame / 2)];
          rightChannelBytes = new byte[totalFrameLength * (bytesPerFrame / 2)];
        } else if (bytesPerSample == 2) {
          leftChannelShort = new short[totalFrameLength];
          rightChannelShort = new short[totalFrameLength];
        } else {
          System.out.println(
              "The number of bytes per sample is greater than 2, terminating the program...");
          System.exit(1);
        }
      }
      while (audioInputStreamDecoded.read(rawData) != -1) {
        long value;
        int start = 0;
        int end = bytesPerFrame;
        byte[] leftChannelBytesSlice;
        byte[] rightChannelBytesSlice;
        long leftChannelValue;
        long rightChannelValue;
        int pitStop = 0;
        for (int i = 0; i < totalFrameLength; i++) {
          value = 0;
          byte[] arrayFrameSlice = Arrays.copyOfRange(rawData, start, end);

          if (numChannels > 1) {
            leftChannelValue = 0;
            rightChannelValue = 0;
            leftChannelBytesSlice = Arrays.copyOfRange(arrayFrameSlice, 0, (bytesPerFrame / 2));
            rightChannelBytesSlice =
                Arrays.copyOfRange(arrayFrameSlice, bytesPerFrame / 2, arrayFrameSlice.length);
            if (bytesPerSample == 1) {
              for (int k = 0; k < leftChannelBytesSlice.length; k++) {
                leftChannelBytes[pitStop] = leftChannelBytesSlice[k];
                rightChannelBytes[pitStop] = rightChannelBytesSlice[k];
                pitStop++;
              }
            }
            // convert the bytes to their corresponding values
            else {
              for (int j = 0; j < leftChannelBytesSlice.length; j++) {
                leftChannelValue += ((long) leftChannelBytesSlice[j] & 0xffL) << (8 * j);
                rightChannelValue += ((long) rightChannelBytesSlice[j] & 0xffL) << (8 * j);
              }
              if (bytesPerSample == 2) {
                leftChannelShort[i] = (short) leftChannelValue;
                rightChannelShort[i] = (short) rightChannelValue;
              } else {
                System.out.println(
                    "The number of bytes per sample is greater than 2, terminating the program...");
                System.exit(1);
              }
            }
          } else if (numChannels == 1 && bytesPerSample == 2) {
            // Since little endian, interpret least array pos as least sig byte
            for (int j = 0; j < arrayFrameSlice.length; j++) {
              value += ((long) arrayFrameSlice[j] & 0xffL) << (8 * j);
            }

            monoChannelShort[i] = (short) value;

          } else if (numChannels == 1 && bytesPerSample == 1) {
            break;
          } else {
            System.out.println(
                "The number of bytes per sample is greater than 2 bytes, terminating the"
                    + " program...");
            System.exit(1);
          }
          start += bytesPerFrame;
          end += bytesPerFrame;
        }
      }
    }
    if (numChannels > 1) {
      compressionStereo();
    } else {
      compressionMono();
    }
  }

  private void compressionMono() throws IOException, DataFormatException {
    // Calculate First Order Prediction
    if (bytesPerSample == 1) {
      // use rawData
      byte[] predicted = new byte[rawData.length];
      predicted[0] = rawData[0];
      for (int i = 1; i < predicted.length; i++) {
        predicted[i] = (byte) (rawData[i] - rawData[i - 1]);
      }
      byte[] compressed = compress(predicted);
      long size = (long) compressed.length * 8;
      Path path = Paths.get("compressedAudioFile" + numOfInvokations + ".txt");
      Files.write(path, compressed);

      compressionRatio = (totalSizeInBits * 1.0 / size);
      byte[] decompressed = decompress(compressed);
      byte[] revertToChannel = revertPredictionToChannel(decompressed);
      // revert

      System.out.println("Compression ratio: " + (totalSizeInBits * 1.0 / size));
      System.out.println("The first 20 elements of the original data:");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", rawData[i]);
      }
      System.out.println();
      System.out.println(
          "The first 20 elements of the uncompressed data (after transforming the uncompressed data"
              + " to original input): ");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", revertToChannel[i]);
      }
      System.out.println();
    } else if (bytesPerSample == 2) {
      // use monoShort
      short[] predicted = new short[monoChannelShort.length];
      predicted[0] = monoChannelShort[0];
      for (int i = 1; i < predicted.length; i++) {
        predicted[i] = (short) (monoChannelShort[i] - monoChannelShort[i - 1]);
      }
      ByteBuffer byteBuffer = ByteBuffer.allocate(predicted.length * 2);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
      shortBuffer.put(predicted);
      byte[] array = byteBuffer.array();
      byte[] compressed = compress(array);
      byte[] decompressed = decompress(compressed);
      Path path = Paths.get("compressedAudioFile" + numOfInvokations + ".txt");
      Files.write(path, compressed);
      long size = (long) compressed.length * 8;
      compressionRatio = (totalSizeInBits * 1.0 / size);
      System.out.println("Compression ratio: " + (totalSizeInBits * 1.0 / size));

      short[] convertBytesToShorts = convertByteStreamToShort(decompressed);
      short[] predictionToOrginal = revertPredictionToChannel(convertBytesToShorts);
      ByteBuffer recovered = ByteBuffer.allocate(rawData.length);
      recovered.order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < convertBytesToShorts.length; i++) {
        recovered.putShort(predictionToOrginal[i]);
      }
      byte[] bytes = recovered.array();
      System.out.println("The first 20 elements of the original data:");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", rawData[i]);
      }
      System.out.println();
      System.out.println(
          "The first 20 elements of the uncompressed data (after transforming the uncompressed data"
              + " to original input): ");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", bytes[i]);
      }
      System.out.println();

    } else {
      System.out.println(
          "The number of bytes per sample is greater than 2, terminating the program...");
      System.exit(1);
    }
  }

  private void compressionStereo() throws IOException, DataFormatException {
    if (bytesPerSample == 1) {
      int length = leftChannelBytes.length;
      byte[] midChannel = new byte[length];
      byte[] sideChannel = new byte[length];
      for (int i = 0; i < length; i++) {
        midChannel[i] = (byte) (0.5 * (leftChannelBytes[i] + rightChannelBytes[i]));
        sideChannel[i] = (byte) (0.5 * (leftChannelBytes[i] - rightChannelBytes[i]));
      }
      byte[] compressedMid = compress(midChannel);
      byte[] decompressedMid = decompress(compressedMid);

      byte[] compressedSide = compress(sideChannel);
      byte[] decompressedSide = decompress(compressedSide);
      long sizeMid = compressedMid.length * 8L;
      long sizeSide = compressedSide.length * 8L;
      long combineSizes = sizeMid + sizeSide;
      compressionRatio = ((totalSizeInBits * 1.0) / combineSizes);
      System.out.println("Compression ratio is: " + compressionRatio);
      // Converting compressed data to original data byte stream

      byte[] originalLeftChannel = convertToLeftChannel(decompressedMid, decompressedSide);
      byte[] originalRightChannel = convertToRightChannel(decompressedMid, decompressedSide);
      ByteBuffer recovered = ByteBuffer.allocate(rawData.length);
      recovered.order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < originalLeftChannel.length; i++) {
        recovered.put(originalLeftChannel[i]);
        recovered.put(originalRightChannel[i]);
      }
      byte[] bytes = recovered.array();
      System.out.println("The first 20 elements of the original data:");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", rawData[i]);
      }
      System.out.println();
      System.out.println(
          "The first 20 elements of the uncompressed data (after transforming the uncompressed data"
              + " to original input): ");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", bytes[i]);
      }
      System.out.println();
      File file = new File("compressedAudioFile" + numOfInvokations + ".txt");
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(compressedMid);
      fos.write(compressedSide);

    } else if (bytesPerSample == 2) {
      int length = leftChannelShort.length;
      short[] midChannel = new short[length];
      short[] sideChannel = new short[length];

      for (int i = 0; i < length; i++) {
        midChannel[i] = (short) (0.5 * (leftChannelShort[i] + rightChannelShort[i]));
        sideChannel[i] = (short) (0.5 * (leftChannelShort[i] - rightChannelShort[i]));
      }

      // convert to byte streams
      ByteBuffer byteBuffer = ByteBuffer.allocate(midChannel.length * 2);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
      shortBuffer.put(midChannel);
      byte[] array1 = byteBuffer.array();
      byte[] compressedMid = compress(array1);
      byte[] decompressedMid = decompress(compressedMid);
      short[] midConvertedFromBytesToShorts = convertByteStreamToShort(decompressedMid);

      ByteBuffer byteBuffer2 = ByteBuffer.allocate(sideChannel.length * 2);
      byteBuffer2.order(ByteOrder.LITTLE_ENDIAN);
      ShortBuffer shortBuffer2 = byteBuffer2.asShortBuffer();
      shortBuffer2.put(sideChannel);
      byte[] array2 = byteBuffer2.array();
      byte[] compressedSide = compress(array2);
      byte[] decompressedSide = decompress(compressedSide);
      short[] sideConvertedFromBytesToShorts = convertByteStreamToShort(decompressedSide);

      short[] originalLeftChannel =
          convertToLeftChannel(midConvertedFromBytesToShorts, sideConvertedFromBytesToShorts);
      short[] originalRightChannel =
          convertToRightChannel(midConvertedFromBytesToShorts, sideConvertedFromBytesToShorts);

      ByteBuffer recovered = ByteBuffer.allocate(rawData.length);
      recovered.order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < originalLeftChannel.length; i++) {
        recovered.putShort(originalLeftChannel[i]);
        recovered.putShort(originalRightChannel[i]);
      }

      byte[] bytes = recovered.array();
      long sizeMid = compressedMid.length * 8L;
      long sizeSide = compressedSide.length * 8L;
      long combineSizes = sizeMid + sizeSide;
      compressionRatio = ((totalSizeInBits * 1.0) / combineSizes);
      System.out.println("Compression ratio is: " + compressionRatio);
      System.out.println("The first 20 elements of the original data:");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", rawData[i]);
      }
      System.out.println();
      System.out.println(
          "The first 20 elements of the uncompressed data (after transforming the uncompressed data"
              + " to original input): ");
      for (int i = 0; i < 20; i++) {
        System.out.printf("%d ", bytes[i]);
      }
      System.out.println();
      File file = new File("compressedAudioFile" + numOfInvokations + ".txt");
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(compressedMid);
      fos.write(compressedSide);
    } else {
      System.out.println(
          "The number of bytes per sample is greater than 2, terminating the program...");
      System.exit(1);
    }
  }

  private byte[] compress(byte[] predicted) throws IOException {
    Deflater deflate = new Deflater();
    deflate.setInput(predicted);
    ByteArrayOutputStream out = new ByteArrayOutputStream(predicted.length);
    deflate.finish();
    byte[] buffer = new byte[1024];
    while (!deflate.finished()) {
      int count = deflate.deflate(buffer);
      out.write(buffer, 0, count);
    }
    out.close();
    return out.toByteArray();
  }

  private byte[] decompress(byte[] compressed) throws DataFormatException, IOException {

    Inflater inflater = new Inflater();
    inflater.setInput(compressed);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressed.length);
    byte[] buffer = new byte[1024];
    while (!inflater.finished()) {
      int count = inflater.inflate(buffer);
      outputStream.write(buffer, 0, count);
    }
    outputStream.close();
    return outputStream.toByteArray();
  }

  private byte[] revertPredictionToChannel(byte[] decompressed) {
    byte[] result = new byte[decompressed.length];
    result[0] = decompressed[0];
    for (int i = 1; i < result.length; i++) {
      result[i] = (byte) (decompressed[i] + result[i - 1]);
    }
    return result;
  }

  private short[] revertPredictionToChannel(short[] decompressed) {
    short[] result = new short[decompressed.length];
    result[0] = decompressed[0];
    for (int i = 1; i < result.length; i++) {
      result[i] = (short) (decompressed[i] + result[i - 1]);
    }
    return result;
  }

  private byte[] convertToLeftChannel(byte[] midChannel, byte[] sideChannel) {
    byte[] result = new byte[midChannel.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = (byte) ((midChannel[i] + sideChannel[i]));
    }
    return result;
  }

  private byte[] convertToRightChannel(byte[] midChannel, byte[] sideChannel) {
    byte[] result = new byte[midChannel.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = (byte) ((midChannel[i] - sideChannel[i]));
    }
    return result;
  }

  private short[] convertToLeftChannel(short[] midChannel, short[] sideChannel) {
    short[] result = new short[midChannel.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = (short) ((midChannel[i] + sideChannel[i]));
    }
    return result;
  }

  private short[] convertToRightChannel(short[] midChannel, short[] sideChannel) {
    short[] result = new short[midChannel.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = (short) ((midChannel[i] - sideChannel[i]));
    }
    return result;
  }

  private short[] convertByteStreamToShort(byte[] decompressed) {
    short[] result = new short[totalFrameLength];
    int value;
    int start = 0;
    int end = 2;
    for (int i = 0; i < result.length; i++) {
      value = 0;
      byte[] arrayFrameSlice = Arrays.copyOfRange(decompressed, start, end);

      for (int j = 0; j < arrayFrameSlice.length; j++) {
        value += ((long) arrayFrameSlice[j] & 0xffL) << (8 * j);
      }
      result[i] = (short) value;
      start += 2;
      end += 2;
    }
    return result;
  }

  public double getCompressionRatio() {
    return compressionRatio;
  }
}
