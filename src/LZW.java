import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ning on 6/23/2015.
 */

public class LZW {
    static int limit = 256;

    public int compress(String from, String to) {
        FileInputStream fin;
        FileOutputStream fout;

        try {
            fin = new FileInputStream(from);
            fout = new FileOutputStream(to);
        } catch (FileNotFoundException e) {
            System.out.println("Can\'t Find the Specified File!");
            e.printStackTrace();
            return -1;
        }

        HashMap<String, Integer> dict = new HashMap<>();
        ArrayList<Integer> compressedStream = new ArrayList<>();

        // init dict
        int sizeOfDict = limit;
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

        try {
            for (int t : compressedStream) {
                fout.write(t >> 8);
                fout.write(t % 256);
            }
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    public int decompress(String from, String to) {
        FileInputStream fin;
        FileOutputStream fout;

        try {
            fin = new FileInputStream(from);
            fout = new FileOutputStream(to);
        } catch (FileNotFoundException e) {
            System.out.println("Can\'t Find the Specified File!");
            e.printStackTrace();
            return -1;
        }

        int sizeOfDict = limit;
        HashMap<Integer, String> dict = new HashMap<>();
        ArrayList<String> decompressedStream = new ArrayList<>();

        // init dict
        for (int i = 0; i < 256; i++) {
            dict.put(i, String.valueOf((char) i));
        }

        byte inputCharBuffer[] = new byte[2];
        int compressedCode = 0;
        String inputQueue = "";

        try {
            fin.read(inputCharBuffer);
            compressedCode = toInt(inputCharBuffer);
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

            compressedCode = toInt(inputCharBuffer);

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

        System.out.println(dict.size());

        try {
            for (String s:decompressedStream) {
                for (int i = 0; i < s.length(); i++) {
                    fout.write(s.charAt(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public static void main(String[] args) {
        LZW lzw = new LZW();
        lzw.compress("emailheaders.csv", "output.txt");
        lzw.decompress("output.txt", "decompress.txt");
    }
}
