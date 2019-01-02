package Structures;

/**
 * a node of the trie tree used for the stop words
 */

public class TrieNode {
    TrieNode[] arr;
    boolean isEnd;


    public TrieNode() {
        //each trie node only store letters A-Z (26)+ ' (1)
        this.arr = new TrieNode[27];
    }
}
