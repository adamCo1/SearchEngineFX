package Structures;


import java.io.*;
import java.util.ArrayList;

public class TrieTree {
    private TrieNode root;

    public TrieTree() {
        root = new TrieNode();
    }



    public void insertFromTextFile(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        while((line = br.readLine()) != null){
            //System.out.println("inserting to the trie tree-> " + line);
            this.insert(line);
        }

    }

    // Inserts a word into the trie.
    public void insert(String word) {
        TrieNode p = root;
        for(int i=0; i < word.length(); i++){
            char c = word.charAt(i);
            int index = 0;
            if(c == '\'')
                index = 26;
//            else if (c-38 < 53)
//                index = c-38;
            else
                index = c-'a';
            if(p.arr[index]==null){
                TrieNode temp = new TrieNode();
                p.arr[index]=temp;
                p = temp;
            }else{
                p=p.arr[index];
            }
        }
        p.isEnd=true;
    }

    // Returns if the word is in the trie.
    public boolean search(String word) {
        try {
            TrieNode p = searchNode(word);
            if (p == null) {
                return false;
            } else {
                if (p.isEnd)
                    return true;
            }

            return false;
        }catch (IndexOutOfBoundsException e){
            return false;
        }
    }
    // Returns if there is any word in the trie
    // that starts with the given prefix.
    public boolean startsWith(String prefix) {
        TrieNode p = searchNode(prefix);
        if(p==null){
            return false;
        }else{
            return true;
        }
    }

    public TrieNode searchNode(String s) throws IndexOutOfBoundsException{

        TrieNode p = root;
        for(int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            int index = 0;
            if (c == '\'')
                index = 26;
//            else if (c-38 < 53)
//                index = c-38;
            else
                index = c - 'a';

            if (index < 0)
                return null;

            if (p.arr[index] != null) {
                p = p.arr[index];
            } else {
                return null;
            }
        }


        if(p==root)
            return null;

        return p;
    }

}
