import java.io.File;
import java.io.*;

public class HuffMainDecompress extends HuffProcessor {
	
	public static void main(String[] args) {
		
		System.out.println("Huffman Decompress Main");
		
		File inf = FileSelector.selectFile();
		File outf = FileSelector.saveFile();
		if (inf == null || outf == null) {
			System.err.println("input or output file cancelled");
			return;
		}
		BitInputStream bis = new BitInputStream(inf);
		BitOutputStream bos = new BitOutputStream(outf);
		HuffProcessor hp = new HuffProcessor();
		hp.decompress(bis, bos);
		System.out.printf("uncompress from %s to %s\n", 
				           inf.getName(),outf.getName());		
		
		System.out.printf("file: %d bits to %d bits\n",inf.length()*8,outf.length()*8);
		System.out.printf("read %d bits, wrote %d bits\n", 
				           bis.bitsRead(),bos.bitsWritten());
		long diff = 8*(outf.length() - inf.length());
		long diff2 = bos.bitsWritten() - bis.bitsRead();
		System.out.printf("%d compared to %d\n",diff,diff2);
	}
	
	public void decompress(BitInputStream in, BitOutputStream out) {
		if(in.readBits(BITS_PER_INT) != HUFF_NUMBER)
			throw new HuffException("Illegal header");
		HuffNode root = readHeader(in);
		HuffNode current = root;
		while(true){
			int bits = in.readBits(1);
			if(bits == -1) {
				 throw new HuffException("bad input, no PSEUDO_EOF");
			}
			else { 
				if (bits ==0) {
					current = current.getLeft();
				}
				else {
					current = current.getRight();
				}
				if(current.getLeft() == null && current.getRight() == null){
					if(current.getValue() == PSEUDO_EOF) {
						break;
					}
					else { 
						out.writeBits(BITS_PER_WORD, current.getValue());
						current = root;
				    }
			}
		}
	}
}
	
	private HuffNode readHeader(BitInputStream in){
		if(in.readBits(1) == -1) {
			throw new HuffException("Wrong file");
		}
		if(in.readBits(1) == 0){
			HuffNode left = readHeader(in);
			HuffNode right = readHeader(in);
			return new HuffNode(-1, 0, left, right);
		} else {
			int value = in.readBits(BITS_PER_WORD+1);
			return new HuffNode(value, 0);
		}
}
}
