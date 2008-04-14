package dr.util;

import java.io.*;

/**
 * @author joseph
 *         Date: 15/04/2008
 */
public class FileHelpers {

    /**
     *
     * @param file
     * @return Number of lines in file
     * @throws IOException
     */
    public static int numberOfLines(File file) throws IOException {
        RandomAccessFile randFile = new RandomAccessFile(file,"r");
        long lastRec=randFile.length();
        randFile.close();
        FileReader fileRead = new FileReader(file);
        LineNumberReader lineRead = new LineNumberReader(fileRead);
        lineRead.skip(lastRec);
        int count = lineRead.getLineNumber()-1;
        fileRead.close();
        lineRead.close();
        return count;
    }
}
