import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ning on 6/23/2015.
 */

public class LZW {
    static int initSize = 256;

    public int compress(String from, String to) {
        FileInputStream fin;
        FileOutputStream fout;
        int numberOfBytesPerCode;

        try {
            fin = new FileInputStream(from);
            fout = new FileOutputStream(to);
        } catch (FileNotFoundException e) {
            System.out.println("Can\'t Open the Specified File!");
            e.printStackTrace();
            return -1;
        }

        HashMap<String, Integer> dict = new HashMap<>();
        ArrayList<Integer> compressedStream = new ArrayList<>();

        // init dict
        int sizeOfDict = initSize;
        for (int i = 0; i < 256; i++) {
            dict.put(String.valueOf((char) i), i);
        }

        String inputQueue = "";
        String currentLongestString = "";
        char currentChar;
        int currentByte = 0;
        while (true) {
            try {
                currentByte = fin.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (currentByte == -1) {
                compressedStream.add(dict.get(inputQueue));
                break;
            }
            currentChar = (char) currentByte;
            currentLongestString = inputQueue + currentChar;
            if (dict.containsKey(currentLongestString)) {
                inputQueue = currentLongestString;
            } else {
                compressedStream.add(dict.get(inputQueue));
                dict.put(currentLongestString, sizeOfDict++);
                inputQueue = "" + currentChar;
            }
        }

        numberOfBytesPerCode = (int) Math.ceil((Math.log(dict.size()) / Math.log(2) / 8));

        try {
            fout.write(numberOfBytesPerCode);
//            write(fout, numberOfBytesPerCode, 4);   // first of the output file indicate the numberOfBytesPerCode
            for (int t : compressedStream) {
                write(fout, t, numberOfBytesPerCode);
            }
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(dict.size());
        System.out.println(numberOfBytesPerCode);

        try {
            fin.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int decompress(String from, String to) {
        FileInputStream fin;
        FileOutputStream fout;
        int numberOfBytesPerCode;

        byte tempBuffer[] = new byte[4];
        try {
            fin = new FileInputStream(from);
            fout = new FileOutputStream(to);
            numberOfBytesPerCode = fin.read();
//            fin.read(tempBuffer);  // read in the metadata that indicate the numberOfBytesPerCode
//            numberOfBytesPerCode = toInt(tempBuffer, 4);
        } catch (IOException e) {
            System.out.println("Can\'t Open the Specified File!");
            e.printStackTrace();
            return -1;
        }

        int sizeOfDict = initSize;
        HashMap<Integer, String> dict = new HashMap<>();
        ArrayList<String> decompressedStream = new ArrayList<>();

        // init dict
        for (int i = 0; i < 256; i++) {
            dict.put(i, String.valueOf((char) i));
        }

        byte inputCharBuffer[] = new byte[numberOfBytesPerCode];
        int compressedCode = 0;
        String inputQueue = "";

        try {
            fin.read(inputCharBuffer);
            compressedCode = toInt(inputCharBuffer, numberOfBytesPerCode);
            inputQueue = String.valueOf((char)compressedCode);
            decompressedStream.add(inputQueue);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String decompressedString = null;
        while (true) {
            try {
                if (fin.read(inputCharBuffer) == -1) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            compressedCode = toInt(inputCharBuffer, numberOfBytesPerCode);

            if (dict.containsKey(compressedCode)) {
                decompressedString = dict.get(compressedCode);
            } else if (compressedCode == sizeOfDict) {
                // handle situation like "dddddddddddd"
                decompressedString = inputQueue + inputQueue.charAt(0);
            }

            decompressedStream.add(decompressedString);

            dict.put(sizeOfDict++, inputQueue + decompressedString.charAt(0));
            inputQueue = decompressedString;
        }

        try {
            for (String s:decompressedStream) {
                for (int i = 0; i < s.length(); i++) {
                    fout.write(s.charAt(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(dict.size());
        System.out.println(numberOfBytesPerCode);

        try {
            fin.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int compressInPlace(String from, String to) {    // fixed length (2 bytes per code) of compressed data
        FileInputStream fin;
        FileOutputStream fout;

        try {
            fin = new FileInputStream(from);
            fout = new FileOutputStream(to);
        } catch (FileNotFoundException e) {
            System.out.println("Can\'t Open the Specified File!");
            e.printStackTrace();
            return -1;
        }

        HashMap<String, Integer> dict = new HashMap<>();

        // init dict
        int sizeOfDict = initSize;
        for (int i = 0; i < 256; i++) {
            dict.put(String.valueOf((char) i), i);
        }

        String inputQueue = "";
        String currentLongestString = "";
        char currentChar;
        int currentByte, currentCompressedCode;
        while (true) {
            try {
                currentByte = fin.read();
                if (currentByte == -1) {    // EOF
                    currentCompressedCode = dict.get(inputQueue);   // write current compressed code
                    fout.write(currentCompressedCode >> 8); // write high 8 bits of all 16 bits
                    fout.write(currentCompressedCode % 256);    // write low
                    break;
                }
                currentChar = (char) currentByte;   // cast into Char for String cat
                currentLongestString = inputQueue + currentChar;    // String cat
                if (dict.containsKey(currentLongestString)) {   // if the dictionary contains the String
                    inputQueue = currentLongestString;  // extend the inputQueue
                } else {    // new String for our dictionary
                    currentCompressedCode = dict.get(inputQueue);   // write out the corresponding compressed code
                    fout.write(currentCompressedCode >> 8); // high
                    fout.write(currentCompressedCode % 256); // low
                    dict.put(currentLongestString, sizeOfDict++);   // put the new String into dictionary
                    inputQueue = "" + currentChar;  // clear the inputQueue and put the char read in this time into inputQueue
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fin.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int decompressInPlace(String from, String to) {
        FileInputStream fin;
        FileOutputStream fout;

        try {
            fin = new FileInputStream(from);
            fout = new FileOutputStream(to);
        } catch (FileNotFoundException e) {
            System.out.println("Can\'t Open the Specified File!");
            e.printStackTrace();
            return -1;
        }

        int sizeOfDict = initSize;
        HashMap<Integer, String> dict = new HashMap<>();

        // init dict
        for (int i = 0; i < 256; i++) {
            dict.put(i, String.valueOf((char) i));
        }

        byte inputCharBuffer[] = new byte[2];   // buffer to read 2 bytes
        int compressedCode; // the compressed code that converted from inputCharBuffer
        String inputQueue = "";

        try {
            fin.read(inputCharBuffer);
            compressedCode = toInt(inputCharBuffer);    // the first 2 compressed bytes of the compressed file must be the same as the original file
            inputQueue = String.valueOf((char)compressedCode);
            for (int i = 0; i < inputQueue.length(); i++) { // write out decompressed String
                fout.write(inputQueue.charAt(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String decompressedString = null;
        while (true) {
            try {
                if (fin.read(inputCharBuffer) == -1) {  // EOF
                    break;
                }

                compressedCode = toInt(inputCharBuffer);    // convert 2 bytes data into a 'unsigned' int

                if (dict.containsKey(compressedCode)) {
                    decompressedString = dict.get(compressedCode);  // find the corresponding String for the input code
                } else if (compressedCode == sizeOfDict) {
                    // handle situation like "dddddddddddd"
                    decompressedString = inputQueue + inputQueue.charAt(0);
                }

                for (int i = 0; i < decompressedString.length(); i++) { // write out data
                    fout.write(decompressedString.charAt(i));
                }

                dict.put(sizeOfDict++, inputQueue + decompressedString.charAt(0));
                inputQueue = decompressedString;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(dict.size());

        try {
            fin.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int toInt(byte [] buffer) {
        int result = (buffer[0] < 0 ? 256 : 0) + buffer[0];
        return (buffer[1] < 0 ? 256 : 0) + buffer[1] + (result << 8);
    }

    public int toInt(byte [] buffer, int length) {
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = (result << 8) + (buffer[i] < 0 ? 256 : 0) + buffer[i];
        }
        return result;
    }

    public int write(FileOutputStream fout, int data, int numberOfBytes) throws IOException {
        for (int i = 1; i <= numberOfBytes; i++) {
            fout.write((data >>> ((numberOfBytes - i) << 3)) % 256);
        }
        return 0;
    }

    public static void main(String[] args) {
        LZW lzw = new LZW();
        lzw.compress("image.jpg", "output.txt");
        lzw.decompress("output.txt", "decompress.jpg");
    }
}
