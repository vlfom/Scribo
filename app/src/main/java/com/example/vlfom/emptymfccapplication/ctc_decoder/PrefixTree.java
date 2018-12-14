package com.example.vlfom.emptymfccapplication.ctc_decoder;

import java.util.*;

public class PrefixTree {
    public Node root;

    public PrefixTree() {
        root = new Node();
    }

    public void addWord(String text) {
        Node node = root;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (!node.children.containsKey(c)) {
                node.children.put(c, new Node());
            }
            node = node.children.get(c);
            if (i + 1 == text.length()) {
                node.isWord = true;
            }
        }
    }

    public void addWords(List<String> words) {
        for (String word : words) {
            addWord(word);
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

        public Node() {
            this.children = new HashMap<>();
            this.isWord = false;
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            for (Character child : children.keySet()) {
                s.append(child);
            }
            return "IsWord: " + String.valueOf(isWord) + ", Children: " + s.toString();
        }
    }
}
