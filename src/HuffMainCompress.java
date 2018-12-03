import java.io.*;
import java.util.PriorityQueue;

public class HuffMainCompress extends HuffProcessor {
	public static void main(String[] args) {
		
		System.out.println("Huffman Compress Main");
		File inf = FileSelector.selectFile();
		File outf = FileSelector.saveFile();
		if (inf == null || outf == null) {
			System.err.println("input or output file cancelled");
			return;
		}
		BitInputStream bis = new BitInputStream(inf);
		BitOutputStream bos = new BitOutputStream(outf);
		HuffProcessor hp = new HuffProcessor();
		hp.compress(bis, bos);
		System.out.printf("compress from %s to %s\n", 
		                   inf.getName(),outf.getName());
		System.out.printf("file: %d bits to %d bits\n",inf.length()*8,outf.length()*8);
		System.out.printf("read %d bits, wrote %d bits\n", 
				           bis.bitsRead(),bos.bitsWritten());
		long diff = bis.bitsRead() - bos.bitsWritten();
		System.out.printf("bits saved = %d\n",diff);
	}
	
	public void compress(BitInputStream in, BitOutputStream out) {
		//step1 : create the frequency array
		int[] freq = new int[ALPH_SIZE];
		freq[PSEUDO_EOF] = 1;
		while(true){
			int character = in.readBits(BITS_PER_WORD);
			if(character == -1)
				break;
			freq[character]++;
		}
		// step2: create the tree
		PriorityQueue<HuffNode> pq = new PriorityQueue<HuffNode>();
		for(int i = 0; i < ALPH_SIZE; i++){
			if(freq[i] != 0){
				pq.add(new HuffNode(i, freq[i], null, null));
			}
		}
		pq.add(new HuffNode(PSEUDO_EOF, 0));
		while(pq.size() > 1){
			HuffNode sub1 = pq.remove();
			HuffNode sub2 = pq.remove();
			pq.add(new HuffNode(-1, sub1.getWeight()+sub2.getWeight(), sub1, sub2));
		}
		HuffNode root = pq.remove();
		
		//step3: create the encodings - use a helper function
		String[] codings = new String[ALPH_SIZE+1];
		encoding(root, "", codings);
		
		//step4: write the header - use a helper function
		out.writeBits(BITS_PER_INT, HUFF_NUMBER);
		writeHeader(root, out);
		
		//step5: compress 
		while(true){
			int character = in.readBits(BITS_PER_WORD);
			if(character == -1)
				break;
			String code = codings[character];
			out.writeBits(code.length(), Integer.parseInt(code, 2));
		}
		String code = codings[PSEUDO_EOF];
		out.writeBits(code.length(), Integer.parseInt(code, 2));		
	}
	
	// helper methods 
	private void encoding(HuffNode current, String path, String[] codes) {
		if(current.getLeft() == null && current.getRight() == null){
			codes[current.getValue()] = path;
			return;
		}
		encoding(current.getLeft(), path + 0, codes);
		encoding(current.getRight(), path + 1, codes);
	}
	
	private void writeHeader(HuffNode current, BitOutputStream out){
		if(current.getLeft() == null && current.getRight() == null){
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD + 1, current.getValue());
			return;
		}
		out.writeBits(1, 0);
		writeHeader(current.getLeft(), out);
		writeHeader(current.getRight(), out);
	}
	
}