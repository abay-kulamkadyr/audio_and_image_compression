package org.qulad;

import java.io.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import javax.swing.*;

public class BMP {

  private final Image imageOriginal;
  private WritableImage imageBW;
  private WritableImage dithered;
  private WritableImage autoLevel;
  private final int imageWidth;
  private final int imageHeight;
  private final int totalPixels;
  private int blockSize = 8;
  private static int numOfInvokations = 0;
  private WritableImage recoveredImage;

  // quantization table
  private int[][] quantizationTable;

  // Matrices to convert YCgCo values to frequency domain
  private double[][] DCTmatrix;
  private double[][] DCTmatrixInverse;

  // buffers for storing YCgCo values
  private double[][] YBuffer;
  private double[][] CoBuffer;
  private double[][] CgBuffer;

  // quantized coefficients holders
  private int[][] quantizedCoefsYChannel;
  private int[][] quantizedCoefsCgChannel;
  private int[][] quantizedCoefsCoChannel;

  //
  private int[][] YBufferConverted;
  private int[][] CgBufferConverted;
  private int[][] CoBufferConverted;

  public BMP(String path) throws IOException {
    File fileIn = new File(path);
    imageOriginal = new Image(fileIn.toURI().toString());
    imageWidth = (int) imageOriginal.getWidth();
    imageHeight = (int) imageOriginal.getHeight();
    totalPixels = imageHeight * imageWidth;

    DCTmatrix = new double[blockSize][blockSize];
    DCTmatrixInverse = new double[blockSize][blockSize];

    quantizedCoefsYChannel = new int[imageHeight][imageWidth];
    quantizedCoefsCgChannel = new int[imageHeight][imageWidth];
    quantizedCoefsCoChannel = new int[imageHeight][imageWidth];

    YBuffer = new double[imageHeight][imageWidth];
    CgBuffer = new double[imageHeight][imageWidth];
    CoBuffer = new double[imageHeight][imageWidth];

    YBufferConverted = new int[imageHeight][imageWidth];
    CgBufferConverted = new int[imageHeight][imageWidth];
    CoBufferConverted = new int[imageHeight][imageWidth];

    PixelReader pixelReader = imageOriginal.getPixelReader();
    recoveredImage = new WritableImage(imageWidth, imageHeight);

    for (int i = 0; i < imageHeight; i++) {
      for (int j = 0; j < imageWidth; j++) {
        Color color = pixelReader.getColor(j, i);
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        int R = (int) (255 * r);
        int G = (int) (255 * g);
        int B = (int) (255 * b);
        if (R > 255) {
          R = 255;
        }
        if (G > 255) {
          G = 255;
        }
        if (B > 255) {
          B = 255;
        }
        double[] CgYCo = liftingMatrixMult(R, G, B);
        CgBuffer[i][j] = CgYCo[0];
        YBuffer[i][j] = CgYCo[1];
        CoBuffer[i][j] = CgYCo[2];
      }
    }
    numOfInvokations++;
    constructDCTMatrix();
    constructDCTMatrixInverse();
    constructQuantizationTable();
    applyCompression();
    applyInverseCompression();
    convertYCgCoToRGB();
    readToFile();
    readToFile1();
  }

  private void readToFile1() throws IOException {
    File outputFile = new File("ResultantImage" + numOfInvokations + ".png");
    ImageIO.write(SwingFXUtils.fromFXImage(recoveredImage, null), "png", outputFile);
  }

  private void readToFile() throws IOException {

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < imageHeight; i++) {
      for (int j = 0; j < imageWidth; j++) {
        builder.append(
            "At index("
                + i
                + ","
                + j
                + ")="
                + quantizedCoefsYChannel[i][j]
                + ":"
                + quantizedCoefsCoChannel[i][j]
                + ":"
                + quantizedCoefsCgChannel[i][j]
                + "\n");
      }
    }
    BufferedWriter writer =
        new BufferedWriter(new FileWriter("QuantizedResult" + numOfInvokations + ".txt"));
    writer.write(builder.toString());
    writer.close();
  }

  private void applyCompression() {
    int numberOfBlocksInX = imageWidth / 8;
    int numberOfBlocksInY = imageHeight / 8;
    int offsetInX;
    int offsetInY = 0;
    double[][] dummy = new double[blockSize][blockSize];
    // Y plane

    for (int i = 0; i < numberOfBlocksInY; i++) {
      offsetInX = 0;
      for (int j = 0; j < numberOfBlocksInX; j++) {
        double[][] getBlockY = getBlock(YBuffer, offsetInY, offsetInX);
        double[][] getBlockCg = getBlock(CgBuffer, offsetInY, offsetInX);
        double[][] getBlockCo = getBlock(CoBuffer, offsetInY, offsetInX);
        getBlockY = applyDCTTransformOnBlock(getBlockY);
        getBlockCg = applyDCTTransformOnBlock(getBlockCg);
        getBlockCo = applyDCTTransformOnBlock(getBlockCo);

        getBlockY = applyQuantizationOnBlock(getBlockY);
        getBlockCg = applyQuantizationOnBlock(getBlockCg);
        getBlockCo = applyQuantizationOnBlock(getBlockCo);
        copyBlock(quantizedCoefsYChannel, getBlockY, offsetInY, offsetInX);
        copyBlock(quantizedCoefsCgChannel, getBlockCg, offsetInY, offsetInX);
        copyBlock(quantizedCoefsCoChannel, getBlockCo, offsetInY, offsetInX);
        offsetInX += blockSize;
      }
      offsetInY += blockSize;
    }
  }

  private void applyInverseCompression() {
    int numberOfBlocksInX = imageWidth / 8;
    int numberOfBlocksInY = imageHeight / 8;

    int offsetInX;
    int offsetInY = 0;
    // Y plane
    for (int i = 0; i < numberOfBlocksInY; i++) {
      offsetInX = 0;
      for (int j = 0; j < numberOfBlocksInX; j++) {
        double[][] getQuantizedY = getBlock(quantizedCoefsYChannel, offsetInY, offsetInX);
        double[][] getQuantizedCg = getBlock(quantizedCoefsCgChannel, offsetInY, offsetInX);
        double[][] getQuantizedCo = getBlock(quantizedCoefsCoChannel, offsetInY, offsetInX);
        getQuantizedY = applyDequantizationOnBlock(getQuantizedY);
        getQuantizedCg = applyDequantizationOnBlock(getQuantizedCg);
        getQuantizedCo = applyDequantizationOnBlock(getQuantizedCo);
        getQuantizedY = applyInverseDCTTransformOnBlock(getQuantizedY);
        getQuantizedCg = applyInverseDCTTransformOnBlock(getQuantizedCg);
        getQuantizedCo = applyInverseDCTTransformOnBlock(getQuantizedCo);
        copyBlock(YBufferConverted, getQuantizedY, offsetInY, offsetInX);
        copyBlock(CgBufferConverted, getQuantizedCg, offsetInY, offsetInX);
        copyBlock(CoBufferConverted, getQuantizedCo, offsetInY, offsetInX);
        offsetInX += blockSize;
      }
      offsetInY += blockSize;
    }
  }

  private void convertYCgCoToRGB() {
    PixelWriter pixelWriter = recoveredImage.getPixelWriter();
    for (int i = 0; i < imageHeight; i++) {
      for (int j = 0; j < imageWidth; j++) {
        int[] rgb =
            convertBackToRGB(
                CgBufferConverted[i][j], YBufferConverted[i][j], CoBufferConverted[i][j]);
        Color color = Color.rgb(rgb[0], rgb[1], rgb[2]);
        pixelWriter.setColor(j, i, color);
      }
    }
  }

  public WritableImage getRecoveredImage() {
    return recoveredImage;
  }

  private void copyBlock(int[][] target, double[][] transformedBlock, int startX, int startY) {
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        target[startX + i][startY + j] = (int) transformedBlock[i][j];
      }
    }
  }

  //    public static BMP instance(String path) throws IOException {
  //        if (theBMP == null) {
  //            theBMP = new BMP(path);
  //        }
  //        return theBMP;
  //
  //    }

  public Image getOriginalImage() {
    return imageOriginal;
  }

  public int getImageWidth() {
    return imageWidth;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  private int[] convertBackToRGB(double Cg, double Y, double Co) {
    int[] RGB = new int[3];
    double temp = Y - Cg;
    RGB[0] = (int) (temp + Co);
    RGB[1] = (int) (Y + Cg);
    RGB[2] = (int) (temp - Co);
    if (RGB[0] < 0) {
      RGB[0] = 0;
    }
    if (RGB[1] < 0) {
      RGB[1] = 0;
    }
    if (RGB[2] < 0) {
      RGB[2] = 0;
    }

    if (RGB[0] > 255) {
      RGB[0] = 255;
    }
    if (RGB[1] > 255) {
      RGB[1] = 255;
    }
    if (RGB[2] > 255) {
      RGB[2] = 255;
    }
    return RGB;
  }

  private double[] liftingMatrixMult(double R, double G, double B) {
    double[] CgYCo = new double[3];

    CgYCo[2] = R - B;
    double temp = B + CgYCo[2] / 2;
    CgYCo[0] = G - temp;
    CgYCo[1] = temp + CgYCo[0] / 2;

    return CgYCo;
  }

  private void constructDCTMatrix() {
    // working with the first row when i=0
    double a = Math.sqrt(1.0 / blockSize);
    int denominator = 2 * blockSize;
    for (int j = 0; j < blockSize; j++) {
      DCTmatrix[0][j] = a;
    }
    a = Math.sqrt(2.0 / blockSize);
    for (int i = 1; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        DCTmatrix[i][j] = a * Math.cos(((2 * j + 1) * i * Math.PI) / denominator);
      }
    }
  }

  private void constructDCTMatrixInverse() {
    // calculating the transpose of the DCT matrix
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        DCTmatrixInverse[i][j] = DCTmatrix[j][i];
      }
    }
  }

  private void constructQuantizationTable() {
    quantizationTable =
        new int[][] {
          {1, 1, 2, 4, 8, 16, 32, 64},
          {1, 1, 2, 4, 8, 16, 32, 64},
          {2, 2, 2, 4, 8, 16, 32, 64},
          {4, 4, 4, 4, 8, 16, 32, 64},
          {8, 8, 8, 8, 8, 16, 32, 64},
          {16, 16, 16, 16, 16, 16, 32, 64},
          {32, 32, 32, 32, 32, 32, 32, 64},
          {64, 64, 64, 64, 64, 64, 64, 64}
        };
  }

  private double[][] getBlock(double[][] matrix, int startX, int startY) {
    double[][] result = new double[blockSize][blockSize];
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        result[i][j] = matrix[startX + i][startY + j];
      }
    }
    return result;
  }

  private double[][] getBlock(int[][] matrix, int startX, int startY) {
    double[][] result = new double[blockSize][blockSize];
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        result[i][j] = matrix[startX + i][startY + j];
      }
    }
    return result;
  }

  private double[][] applyQuantizationOnBlock(double[][] matrix1) {
    double[][] result = new double[blockSize][blockSize];
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        result[i][j] = (int) (matrix1[i][j] / quantizationTable[i][j]);
      }
    }
    return result;
  }

  private double[][] applyDequantizationOnBlock(double[][] matrix1) {
    {
      double[][] result = new double[blockSize][blockSize];
      for (int i = 0; i < blockSize; i++) {
        for (int j = 0; j < blockSize; j++) {
          result[i][j] = matrix1[i][j] * quantizationTable[i][j];
        }
      }
      return result;
    }
  }

  private double[][] applyDCTTransformOnBlock(double[][] block) {
    // rows first then columns
    double[][] result = new double[blockSize][blockSize];

    for (int i = 0; i < blockSize; i++) {
      result[i] = vectorMatrixMult(DCTmatrix, block[i]);
    }
    double[] vector;
    for (int j = 0; j < blockSize; j++) {
      vector = vectorMatrixMult(DCTmatrix, getColumn(result, j));
      for (int i = 0; i < blockSize; i++) {
        result[i][j] = vector[i];
      }
    }
    return result;
  }

  private double[][] applyInverseDCTTransformOnBlock(double[][] block) {
    double[][] result = new double[blockSize][blockSize];
    double[] vector;
    for (int j = 0; j < blockSize; j++) {
      vector = vectorMatrixMult(DCTmatrixInverse, getColumn(block, j));
      for (int i = 0; i < blockSize; i++) {
        block[i][j] = vector[i];
      }
    }
    // multiplying rows of the input and putting the results in the rows
    for (int i = 0; i < blockSize; i++) {
      block[i] = vectorMatrixMult(DCTmatrixInverse, block[i]);
    }
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        result[i][j] = block[i][j];
      }
    }
    return result;
  }

  private double[] getColumn(double[][] matrix, int index1) {
    double[] result = new double[blockSize];
    for (int i = 0; i < blockSize; i++) {
      result[i] = matrix[i][index1];
    }
    return result;
  }

  private double[] vectorMatrixMult(double[][] matrix, double[] vector) {
    double[] result = new double[blockSize];
    double iterator = 0;
    for (int i = 0; i < blockSize; i++) {
      iterator = 0;
      for (int j = 0; j < blockSize; j++) {
        iterator += matrix[i][j] * vector[j];
      }
      result[i] = iterator;
    }
    return result;
  }
}
