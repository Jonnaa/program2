import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


class test{
  public static void main(String[] args) {
    File aFile = new File(".Test/Test.html");
    FileInputStream inFile = null;
    try {
      inFile = new FileInputStream(aFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace(System.err);
    }
    FileChannel inChannel = inFile.getChannel();
    final int PRIMECOUNT = 6;
    ByteBuffer buf = ByteBuffer.allocate(8 * PRIMECOUNT);
    long[] primes = new long[PRIMECOUNT];
    try {
      while (inChannel.read(buf) != -1) {
        ((ByteBuffer) (buf.flip())).asLongBuffer().get(primes);
        for (long prime : primes) {
          System.out.printf("%10d", prime);
        }
        buf.clear();
      }
      inFile.close();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
  }
}