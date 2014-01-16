package com.CloudDisk;

//package Galois;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author cg
 * 
 * @update Chenzheng
 * @date 12.12.2013
 * 
 */
public class FileCoder {
	int w = 8; // 有限域CF的大小，这里是w=8；
	int m = 3; // 矩阵的行数,暂定为3
	int n = 5; // 矩阵的列数
	int[][] encodeMatrix; // 编码矩阵
	int[][] decodeMatrix; // 译码矩阵
	int blockSize = 2048; // 块大小 2048直接
	int blocks; // 分块数
	Galois galois = new Galois();

	public FileCoder() {

	}

	// 还是要个构造方法方便些，设置一些基本的参数
	public FileCoder(int w, int m, int n, int blockSize) {
		this.w = w;
		this.m = m;
		this.n = n;
		this.blockSize = blockSize;
	}

	// 初始化编码矩阵
	public void iniEncodeMatrix() {
		if (encodeMatrix == null) {
			/*
			 * 构造范德蒙矩阵 如下： * 1 1 1 1 1 1 2 3 4 5 1 4 9 16 25 1 8 27 64 125
			 * ....................... ......................
			 */
			encodeMatrix = new int[m][n];
			for (int i = 0; i < n; i++)
				for (int j = 0; j < m; j++) {
					if (j == 0)
						encodeMatrix[j][i] = 1;
					else
						encodeMatrix[j][i] = galois.galois_single_multiply(
								encodeMatrix[j - 1][i], i + 1, 8);
				}
		}
	}

	// 打印编码矩阵
	private void printEncodeMatrix() {
		if (encodeMatrix == null) {
			System.out.println("initial encode matrix first!");
		} else {
			for (int i = 0; i < encodeMatrix.length; i++) {
				for (int j = 0; j < encodeMatrix[0].length; j++) {
					System.out.print(encodeMatrix[i][j] + " ");
				}
				System.out.println();
			}
		}
	}

	// 打印译码矩阵
	private void printDecodeMatrix() {
		if (decodeMatrix == null) {
			System.out.println("initial encode matrix first!");
		} else {
			for (int i = 0; i < decodeMatrix.length; i++) {
				for (int j = 0; j < decodeMatrix[0].length; j++) {
					System.out.print(decodeMatrix[i][j] + " ");
				}
				System.out.println();
			}
		}
	}

	/**
	 * 编码，将输入的文件进行编码后得到一系列（n个）的输出文件
	 * 
	 * @param inputFile
	 *            输入需要编码的文件
	 * @return 编码后输出的文件数组，一共有n个
	 */
	public File[] Encode(File inputFile) throws FileNotFoundException,
			IOException {

		// 初始化编码矩阵
		iniEncodeMatrix();

		/*
		 * 读取inputFile内容到m个byte[]
		 */
		RandomAccessFile file = new RandomAccessFile(inputFile, "r");
		// FileInputStream file = new FileInputStream(inputFile);
		long size = file.length(); // 确定文件的大小
		size += 64;
		System.out.println("size: " + size);

		blocks = (int) (Math.ceil(size / (blockSize * 1.0))); // 确定M的大小
		System.out.println("blocks: " + blocks);

		byte[][] inputBuffer = new byte[m][]; // 保存读取内容
		byte[][] outputBuffer = new byte[n][]; // 保存读取内容

		int iteration = (int) Math.ceil(blocks / (1.0 * m));
		System.out.println("iteration: " + iteration);

		// 输出的files
		File[] outFiles = new File[n];
		for (int i = 0; i < n; i++) {
			outFiles[i] = new File(inputFile.getName() + "." + i);
			if (outFiles[i].exists())
				outFiles[i].delete();
		}

		for (int i = 0; i < iteration; i++) {
			// 每次迭代，把m块编码成n块，并写入n个文件

			// 读取m个块
			for (int j = 0; j < m; j++) {
				inputBuffer[j] = new byte[blockSize];
				file.seek((j * iteration + i) * blockSize);
				// System.out.println("reading" + (j * iteration + i));
				int readLength = (file.read(inputBuffer[j]));
				// System.out.println("read" + readLength);
				if (readLength == -1)
					readLength = 0;
				if (readLength < blockSize) {
					for (int k = readLength; k < blockSize; k++)
						inputBuffer[j][k] = 0;
				}
				// System.out.println("printing");
				// for (int k = 0; k < blockSize; k++)
				// System.out.print(inputBuffer[j][k] + " ");
				// System.out.println();

				// 最后一块的最后8个字节中写入文件的长度
				if (i == iteration - 1 && j == m - 1) {
					System.out.println("read the last block");
					writeSize(inputBuffer[j], size);
				}
			}

			// 编码n个块
			for (int j = 0; j < n; j++) {
				outputBuffer[j] = new byte[blockSize];
				for (int k = 0; k < m; k++) {
					galois.galois_w08_region_multiply(inputBuffer[k],
							encodeMatrix[k][j], inputBuffer[k].length,
							outputBuffer[j], true);
				}
			}

			// 写入n个文件
			for (int j = 0; j < n; j++) {
				RandomAccessFile outFile = null;
				try {
					outFile = new RandomAccessFile(outFiles[j], "rw");
					outFile.seek(outFile.length());
					outFile.write(outputBuffer[j]);
					outFile.close();

				} catch (FileNotFoundException e) {
					System.out.println("FileNotFoundException");
				} finally {
				}
			}

		}

		file.close();
		return outFiles;

	}

	// write size to last 8 byte of input byte array
	private void writeSize(byte[] bs, long s) {
		byte[] buf = new byte[8];
		// System.out.println(s);

		for (int i = buf.length - 1; i >= 0; i--) {
			buf[i] = (byte) (s & 0x00000000000000ff);
			s >>= 8;
		}
		// for (int i = 0; i < buf.length; i++) {
		// System.out.print(Integer.toBinaryString(buf[i]) + " ");
		// }

		for (int i = 0; i < buf.length; i++) {
			bs[bs.length - i - 1] = buf[i];
		}
	}

	// read file size from last 8 byte of input byte array
	private long readSize(byte[] bs) {
		long s = 0;

		byte[] buf = new byte[8];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = bs[bs.length - i - 1];
		}
		// System.out.println();

		// for (int i = 0; i < buf.length; i++) {
		// System.out.print(Integer.toBinaryString(buf[i]) + " ");
		// }
		// System.out.println();

		for (int i = 0; i < buf.length; i++) {
			// System.out.print(buf[i] + " ");
			long temp = buf[i];
			if (temp < 0)
				temp += 256;
			s += (long) (temp << (buf.length - i - 1) * 8);
		}
		// System.out.println(s);

		return s;
	}

	public File Decode(File[] inputFile) throws IOException {

		String fileName = inputFile[0].getName();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		File outFile = new File(fileName);

		int[] fileSN = new int[m];
		for (int i = 0; i < m; i++) {
			String fileNamei = inputFile[i].getName();
			int SN = Integer.parseInt(fileNamei.substring(fileNamei
					.lastIndexOf(".") + 1));
			fileSN[i] = SN;

			// System.out.print(SN + " ");
		}
		// System.out.println();

		int[][] tempMatrix = new int[m][m];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
				tempMatrix[i][j] = encodeMatrix[i][fileSN[j]];
		decodeMatrix = this.reverseMatrix(tempMatrix);
		// this.printDecodeMatrix();

		byte[][] inputBuffer = new byte[m][blockSize]; // 保存读取内容
		byte[] outputBuffer = new byte[blockSize]; // 保存写入内容

		long size = inputFile[0].length();
		long fileSize = 0;
		System.out.println("size: " + size);

		blocks = (int) (Math.ceil(size / (blockSize * 1.0))); // 确定M的大小
		System.out.println("blocks: " + blocks);

		int iteration = blocks;
		System.out.println("iteration: " + iteration);

		RandomAccessFile[] files = new RandomAccessFile[m];
		for (int i = 0; i < m; i++)
			files[i] = new RandomAccessFile(inputFile[i], "r");

		RandomAccessFile outfile = new RandomAccessFile(outFile, "rw");

		for (int i = 0; i < iteration; i++) {

			for (int j = 0; j < m; j++) {
				// System.out.println(files[j].length());
				files[j].read(inputBuffer[j]);

				// System.out.println(files[j].getFilePointer());
				// System.out.println("printing");
				// for (int k = 0; k < blockSize; k++)
				// System.out.print(inputBuffer[j][k] + " ");
				// System.out.println();
				files[j].close();
			}

			if (i == iteration - 1) {
				for (int j = 0; j < m; j++) {

					for (int k = 0; k < m; k++) {
						galois.galois_w08_region_multiply(inputBuffer[k],
								decodeMatrix[k][j], inputBuffer[k].length,
								outputBuffer, true);
					}
					fileSize = this.readSize(outputBuffer);
					outputBuffer = new byte[blockSize];
					fileSize -= 64;
				}
				System.out.println("fileSize: " + fileSize);
			}

			for (int j = 0; j < m; j++) {

				for (int k = 0; k < m; k++) {
					galois.galois_w08_region_multiply(inputBuffer[k],
							decodeMatrix[k][j], inputBuffer[k].length,
							outputBuffer, true);
				}

				// System.out.println("printing" + " i:" + i + " j:" + j);
				// for (int k = 0; k < blockSize; k++)
				// System.out.print(outputBuffer[k] + " ");
				// System.out.println();

				outfile.seek((iteration * j + i) * blockSize);
				outfile.write(outputBuffer);
				outputBuffer = new byte[blockSize];

			}

		}

		System.out.print(fileSize);
		outfile.seek(fileSize);
		outfile.close();

		cutFile(outFile, fileSize);
		

		return outFile;

	}

	private void cutFile(File inFile, long fileSize) throws IOException {
		// TODO Auto-generated method stub
		// Construct the new file that will later be renamed to the original
		// filename.
		File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

		RandomAccessFile fileIn = new RandomAccessFile(inFile, "r");
		RandomAccessFile fileOut = new RandomAccessFile(tempFile, "rw");

		// BufferedReader br = new BufferedReader(new FileReader(inFile));
		// BufferedWriter bw=new BufferedWriter (new FileWriter(tempFile) );
		// PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

		String line = null;

		byte[] tempChar = new byte[1024];
		fileIn.read(tempChar);
		fileOut.write(tempChar);

		while (tempFile.length() < fileSize - tempChar.length / 2) {
			fileIn.read(tempChar);
			fileOut.write(tempChar);
		}
		// System.out.println("fileLength:" + tempFile.length() + " "
		// + ((int) fileSize - (int) tempFile.length()));

		fileIn.read(tempChar);

		// for (int k = 0; k < blockSize; k++)
		// System.out.print(tempChar[k] + " ");
		// System.out.println();

		long tempFileLength = tempFile.length();
		for (int i = 0; i < fileSize - tempFileLength; i++) {

			fileOut.writeByte(tempChar[i]);
			// fileOut.writeByte(tempChar[2 * i + 1]);
		}

		fileIn.close();
		fileOut.close();
		// System.out.println("fileLength:" + tempFile.length());

		// Delete the original file
		if (!inFile.delete()) {
			System.out.println("Could not delete file");
			return;
		}

		// Rename the new file to the filename the original file had.
		if (!tempFile.renameTo(inFile))
			System.out.println("Could not rename file");

	}

	private int[][] reverseMatrix(int[][] inMaxtrix) {

		// Galois galois = new Galois();
		/*
		 * 构造译码矩阵 根据m的大小以及范德蒙矩阵m×n， 只需要构造一个m×m的逆矩阵，即可完成解码文件 怎么得到可逆矩阵呢？？？？？？
		 * 利用高斯消元法
		 */
		m = inMaxtrix[0].length;
		// System.out.println(m);
		int[][] temp;
		// 创建n*（2n-1）行列式，用来求逆矩阵，原矩阵和单位矩阵
		temp = new int[m][2 * m];
		// 创建返回的矩阵,初始化
		// 将原矩阵的值赋给 temp矩阵，并添加单位矩阵的值
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < temp[i].length; j++) {
				if (j > m - 1) {
					if (i == (j - m))
						temp[i][j] = 1;
					else
						temp[i][j] = 0;
				} else {
					temp[i][j] = inMaxtrix[i][j];
				}
			}
		}
		// 求逆矩阵
		for (int i = 0; i < temp.length; i++) {
			int var = galois.galois_inverse(temp[i][i], w);
			// 判断对角线上元素是否为0，是的话与后面的行进行交换行，如没有满足条件的
			// 则可认为原矩阵没有逆矩阵。然后取值要化为0的列的值
			for (int j = i; j < temp[i].length; j++) {

				temp[i][j] = galois.galois_single_multiply(temp[i][j], var, w);
			}

			for (int k = 0; k < temp.length; k++) {
				if (k == i)
					continue;

				for (int l = temp[i].length - 1; l >= i; l--)
					temp[k][l] = temp[k][l]
							^ galois.galois_single_multiply(temp[i][l],
									temp[k][i], w);
			}
			// 将第x列的元素出对角线上的元素外都化为0，即构建单位矩阵

		}
		// 取逆矩阵的值
		int[][] buff = new int[temp.length][temp.length]; // 存放译码矩阵，作返回值
		for (int i = 0; i < temp.length; i++) {
			for (int j = 0; j < temp.length; j++) {
				buff[i][j] = temp[i][j + temp.length];
			}
		}

		return buff;
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		// FileCoder testfileCoder = new FileCoder(8, 5, 7, 1024);
		// testfileCoder.iniEncodeMatrix();
		// String testFileName = "123.jpg";
		// File encodeIn = new File(testFileName);
		// testfileCoder.Encode(encodeIn);
		//
		// File[] decodeIn = new File[5];
		// decodeIn[0] = new File(testFileName + ".0");
		// decodeIn[1] = new File(testFileName + ".1");
		// decodeIn[2] = new File(testFileName + ".3");
		// decodeIn[3] = new File(testFileName + ".5");
		// decodeIn[4] = new File(testFileName + ".4");
		// testfileCoder.Decode(decodeIn);
		//
		// System.exit(1);

		if (args.length > 0) {

			if (args[0].equals("encode")) {

				int m = Integer.parseInt(args[1]);
				int n = Integer.parseInt(args[2]);
				int blockSize = Integer.parseInt(args[3]);
				String file = args[4];

				FileCoder fileCoder = new FileCoder(8, m, n, blockSize);
				fileCoder.iniEncodeMatrix();

				File in = new File(file);
				File[] out = fileCoder.Encode(in);

				System.out.print(file + "been encoded into ");
				for (int i = 0; i < out.length; i++)
					System.out.print(out[i].getName() + " ");
				System.out.println();
				System.exit(1);
			}

			if (args[0].equals("decode")) {
				int m = Integer.parseInt(args[1]);
				int n = Integer.parseInt(args[2]);
				int blockSize = Integer.parseInt(args[3]);
				String[] files = new String[m];
				for (int i = 0; i < m; i++)
					files[i] = args[i + 4];

				FileCoder fileCoder = new FileCoder(8, m, n, blockSize);
				fileCoder.iniEncodeMatrix();

				File[] decodeIn = new File[m];
				for (int i = 0; i < m; i++)
					decodeIn[i] = new File(files[i]);
				File outFile = fileCoder.Decode(decodeIn);

				System.out.print(outFile.getName() + " has been decoded! ");
				System.out.println();
				System.exit(1);
			}
		}
		System.out.println("Usage:");
		System.out.println("encode m n blockSize filename");
		System.out.println("    or    ");
		System.out
				.println("decode m n blockSize filename0 filename1 ... filenamem");

	}
}
