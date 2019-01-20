package com.example.isausmanov.scriboai.ctc_decoder;

import android.util.Log;
import android.util.SparseArray;
import java.util.*;

public class PrefixTree {
    public Node root;

    public PrefixTree() {
        root = new Node();
    }

    public void addWord(String text, Double unigramProb) {
        Node node = root;

        root.unigramChildrenSum += unigramProb;
        root.childrenNum += 1;

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (!node.children.containsKey(c)) {
                node.children.put(c, new Node());
            }
            node = node.children.get(c);

            node.unigramChildrenSum += unigramProb;
            node.childrenNum += 1;

            if (i + 1 == text.length()) {
                node.isWord = true;
                node.unigramProb = unigramProb;
            }
        }
    }

    public void addBigram(Integer word_encoded, String word2, Double prob) {
        Node node = root;

        if (root.bigramChildrenCount.get(word_encoded, null) != null) {
            root.bigramChildrenCount.put(word_encoded, root.bigramChildrenCount.get(word_encoded) + 1);
        }
        else {
            root.bigramChildrenCount.put(word_encoded, 1);
        }

        if (root.bigramChildrenSum.get(word_encoded, null) != null) {
            root.bigramChildrenSum.put(word_encoded, root.bigramChildrenSum.get(word_encoded) + prob);
        }
        else {
            root.bigramChildrenSum.put(word_encoded, prob);
        }

        for (int i = 0; i < word2.length(); ++i) {
            char c = word2.charAt(i);
            node = node.children.get(c);

            if (node.bigramChildrenSum.get(word_encoded, null) != null) {
                node.bigramChildrenSum.put(word_encoded, node.bigramChildrenSum.get(word_encoded) + prob);
            }
            else {
                node.bigramChildrenSum.put(word_encoded, prob);
            }

            if (node.bigramChildrenCount.get(word_encoded, null) != null) {
                node.bigramChildrenCount.put(word_encoded, node.bigramChildrenCount.get(word_encoded) + 1);
            }
            else {
                node.bigramChildrenCount.put(word_encoded, 1);
            }

            if (i+1 == word2.length()) {
                node.bigramProb.put(word_encoded, prob);
            }
        }
    }

    public Node getNode(String text) {
        Node node = root;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (!node.children.containsKey(c)) {
                return null;
            }
            node = node.children.get(c);
        }
        return node;
    }

    public List<Character> getNextChars(String text) {
        Node node = getNode(text);
        if (node != null) {
            return new LinkedList<>(node.children.keySet());
        }
        return new LinkedList<>();
    }

    public List<String> getNextWords(String text) {
        Node node = getNode(text);
        if (node != null) {
            LinkedList<String> words = new LinkedList<>();
            LinkedList<String> prefixes = new LinkedList<>();
            prefixes.add(text);
            LinkedList<Node> nodes = new LinkedList<>();
            nodes.add(node);

            while (!nodes.isEmpty()) {
                node = nodes.pollFirst();
                if (node.isWord) {
                    words.add(prefixes.getFirst());
                }

                for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
                    nodes.add(entry.getValue());
                    prefixes.add(prefixes.getFirst() + entry.getKey());
                }

                prefixes.pollFirst();
            }

            return words;
        }

        return new LinkedList<>();
    }

    public Double getUnigramProb(String text) {
        Node node = getNode(text);
        if (node != null) {
            return node.unigramProb;
        }

        return 0.;
    }

    public Double getNextWordsUnigramProb(String text) {
        Node node = getNode(text);
        if (node != null) {
            return node.unigramChildrenSum;
        }

        return 0.;
    }

    public Double getBigramProb(Integer prev_word_encoded, String prev_word, String text, Integer totalWordsCount, Integer vocabularySize) {
        Node node = getNode(text);
        if (node != null) {
            return node.bigramProb.get(prev_word_encoded,
                    1.0 / (getUnigramProb(prev_word) * totalWordsCount + vocabularySize));
        }

        return null;
    }

    public Double getNextWordsBigramProb(Integer prev_word_encoded, String prev_word, String text, Integer totalWordsCount, Integer vocabularySize) {
        Node node = getNode(text);
        if (node != null) {
            double word_count = getUnigramProb(prev_word) * (totalWordsCount + vocabularySize) - 1;

            return node.bigramChildrenSum.get(prev_word_encoded, 0.) +
                    (node.childrenNum - node.bigramChildrenCount.get(prev_word_encoded, 0)) * 1.0 / (word_count + vocabularySize);
        }

        return null;
    }

    public void dump() {
        LinkedList<Node> nodes = new LinkedList<>();
        nodes.add(root);

        while (!nodes.isEmpty()) {
            Node node = nodes.pollFirst();
            System.out.println(node);

            nodes.addAll(node.children.values());
        }
    }

    public boolean isWord(String text) {
        Node node = getNode(text);
        if (node != null) {
            return node.isWord;
        }
        return false;
    }

    public class Node {
        public HashMap<Character, Node> children;
        public boolean isWord;
        public double unigramProb;
        public double unigramChildrenSum;
        public SparseArray<Double> bigramProb;
        public SparseArray<Double> bigramChildrenSum;
        public SparseArray<Integer> bigramChildrenCount;
        public int childrenNum;

        public Node() {
            this.childrenNum = 0;

            this.children = new HashMap<>();
            this.isWord = false;

            this.unigramProb = 0;
            this.unigramChildrenSum = 0;

            this.bigramProb = new SparseArray<>();
            this.bigramChildrenSum = new SparseArray<>();
            this.bigramChildrenCount = new SparseArray<>();
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            for (Character child : children.keySet()) {
                s.append(child);
            }
            return "IsWord: " + String.valueOf(isWord) +
                    ", Children: " + s.toString() +
                    ", UnigramChildrenSum: " + String.valueOf(unigramChildrenSum);
        }
    }
}
