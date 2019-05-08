//Maddie Louis, Merissa Tan

package huffman;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Huffman instances provide reusable Huffman Encoding Maps for
 * compressing and decompressing text corpi with comparable
 * distributions of characters.
 */
public class Huffman {
    
    // -----------------------------------------------
    // Construction
    // -----------------------------------------------

    private HuffNode trieRoot;
    private Map<Character, String> encodingMap;
    
    /**
     * Creates the Huffman Trie and Encoding Map using the character
     * distributions in the given text corpus
     * @param corpus A String representing a message / document corpus
     *        with distributions over characters that are implicitly used
     *        throughout the methods that follow. Note: this corpus ONLY
     *        establishes the Encoding Map; later compressed corpi may
     *        differ.
     */
    Huffman (String corpus) {
    	//Creates hashmap of characters with their frequencies
    	HashMap<Character, Integer> frequencies = new HashMap<Character, Integer>();
    	for (int i = 0; i < corpus.length(); i++) {
    		char currentCharacter = corpus.charAt(i);
    		if ( ! frequencies.containsKey(currentCharacter)) {
    			frequencies.put(currentCharacter, 1);
    		} else {
    			frequencies.put(currentCharacter, frequencies.get(currentCharacter) + 1);
    		}
    	}
    	
    	//Adds each character from the corpus to a priority queue
    	PriorityQueue<HuffNode> huffmanQueue = new PriorityQueue<HuffNode>();
    	for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
    	    HuffNode leaf = new HuffNode(entry.getKey(), entry.getValue());
    	    huffmanQueue.add(leaf);
    	}
    	
    	//Constructs the Huffman Trie
    	while (huffmanQueue.size() > 1) {
    		HuffNode nodeA = huffmanQueue.poll();
    		HuffNode nodeB = huffmanQueue.poll();
    		HuffNode parent = new HuffNode('\0', nodeA.count + nodeB.count);
    		parent.left = nodeA;
    		parent.right = nodeB;
    		huffmanQueue.add(parent);
    	}
    	trieRoot = huffmanQueue.poll();
    	
    	encodingMap = new HashMap<Character, String>();
    	createEncodingMap(trieRoot, "");
    }
    
    /**
     * Creates the encoding map using depth-first traversal on the Huffman Trie
     * @param current The current node to perform the traversal on
     * @param path The path that has been taken to get to the current node
     */
    public void createEncodingMap(HuffNode current, String path) {
    	if (current.isLeaf()) {
    		encodingMap.put(current.character, path);
    		return;
    	}
    	if (current.left != null) {
    		createEncodingMap(current.left, path + "0");
    	}
    	if (current.right != null) {
    		createEncodingMap(current.right, path + "1");
    	}
    }
    
    
    // -----------------------------------------------
    // Compression
    // -----------------------------------------------
    
    /**
     * Compresses the given String message / text corpus into its Huffman coded
     * bitstring, as represented by an array of bytes. Uses the encodingMap
     * field generated during construction for this purpose.
     * @param message String representing the corpus to compress.
     * @return {@code byte[]} representing the compressed corpus with the
     *         Huffman coded bytecode. Formatted as 3 components: (1) the
     *         first byte contains the number of characters in the message,
     *         (2) the bitstring containing the message itself, (3) possible
     *         0-padding on the final byte.
     */
    public byte[] compress (String message) {
        String fullBinaryString = "";

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        byteArrayOS.write(message.length());
        
        for (int i = 0; i < message.length(); i++) {
            String encoding = encodingMap.get(message.charAt(i));
            fullBinaryString += encoding;
        }
        
        // performs modulo operation (instead of remainder)
        int numOfZeroesNeeded = ((8 - fullBinaryString.length()) % 8 + 8 ) % 8;
                 
        for (int i = 0; i < fullBinaryString.length() + numOfZeroesNeeded - 8; i += 8) {        
            int tempInt = Integer.parseInt(fullBinaryString.substring(i, i + 8), 2);
            byteArrayOS.write(tempInt);
        }
        
        String tempString = fullBinaryString.substring(fullBinaryString.length() - (8 - numOfZeroesNeeded));        
        int tempInt = Integer.parseInt(tempString, 2);
        tempInt = tempInt << numOfZeroesNeeded;
        byteArrayOS.write(tempInt);
  
        return byteArrayOS.toByteArray();
    }
    
    
    // -----------------------------------------------
    // Decompression
    // -----------------------------------------------
    
    /**
     * Decompresses the given compressed array of bytes into their original,
     * String representation. Uses the trieRoot field (the Huffman Trie) that
     * generated the compressed message during decoding.
     * @param compressedMsg {@code byte[]} representing the compressed corpus with the
     *        Huffman coded bytecode. Formatted as 3 components: (1) the
     *        first byte contains the number of characters in the message,
     *        (2) the bitstring containing the message itself, (3) possible
     *        0-padding on the final byte.
     * @return Decompressed String representation of the compressed bytecode message.
     */
    public String decompress (byte[] compressedMsg) {
    	int lengthOfMessage = compressedMsg[0];
    	String fullBinaryString = "";
    	for (int i = 1; i < compressedMsg.length; i++) {
    		int valueOfByte = compressedMsg[i];
    		String binaryString = Integer.toBinaryString(valueOfByte);
    		if (binaryString.length() > 8) {
    			binaryString = binaryString.substring(binaryString.length() - 8);
    		}
    		while (binaryString.length() < 8) {
    			binaryString = "0" + binaryString;
    		}
    		fullBinaryString += binaryString;
    	}
    	String message = "";
    	int currentIndex = 0;
    	while (message.length() != lengthOfMessage) {
    		char nextLetter = retrieveLetter(trieRoot, fullBinaryString.substring(currentIndex, fullBinaryString.length())); 
    		message += nextLetter;
    		currentIndex += encodingMap.get(nextLetter).length();
    	}
    	return message;
    }
    
    /**
     * Retrieves the next letter from the binary string
     * @param currentNode The current node we are at while trying to retrieve next letter
     * @param binaryString The binary string that tells us where to look in the tree for the next letter
     * @return The next letter that was in the original corpus
     */
    public char retrieveLetter(HuffNode currentNode, String binaryString) {
    	if (currentNode.isLeaf()) {
    		return currentNode.character;
    	}
    	return retrieveLetter((binaryString.charAt(0) == '0') ? currentNode.left : currentNode.right, binaryString.substring(1));
    }
    
    // -----------------------------------------------
    // Huffman Trie
    // -----------------------------------------------
    
    /**
     * Huffman Trie Node class used in construction of the Huffman Trie.
     * Each node is a binary (having at most a left and right child), contains
     * a character field that it represents (in the case of a leaf, otherwise
     * the null character \0), and a count field that holds the number of times
     * the node's character (or those in its subtrees) appear in the corpus.
     */
    private static class HuffNode implements Comparable<HuffNode> {
        
        HuffNode left, right;
        char character;
        int count;
        
        HuffNode (char character, int count) {
            this.count = count;
            this.character = character;
        }
        
        public boolean isLeaf () {
            return left == null && right == null;
        }
        
        public int compareTo (HuffNode other) {
            return this.count - other.count;
        }
        
    }

}
