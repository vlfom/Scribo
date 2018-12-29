package com.example.isausmanov.scriboai.ctc_decoder;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageModel {

    public PrefixTree prefixTree;
    public HashMap<String, Double> unigrams;
    public HashMap<String, HashMap<String, Double>> bigrams;
    public int wordsCount;
    public int uniqueWordsCount;
    public ArrayList<Character> allChars;
    public ArrayList<Character> wordChars;
    public ArrayList<Character> nonWordChars;

    /**
     *
     * @param corpus vocabulary with lower-case words separated by spaces
     * @param chars list of all possible characters
     * @param wordChars list of all possible **word** characters
     */
    public LanguageModel(String corpus, String chars, String wordChars) {
        String[] words = corpus.split(" ");

        this.wordsCount = words.length;
        this.uniqueWordsCount = new HashSet<>(Arrays.asList(words)).size();

        this.unigrams = new HashMap<>();
        this.bigrams = new HashMap<>();

        // TODO: remove data processing here, rather pass pre-processed
        // TODO: unigrams and bigrams to constructor

        // Calculate unigrams
        for (String word : words) {
            if (!unigrams.containsKey(word)) {
                unigrams.put(word, 1.0 / this.wordsCount);
            }
            else {
                unigrams.put(word, unigrams.get(word) + 1.0 / this.wordsCount);
            }
        }

        // Calculate bigrams
        for (int i = 0; i < words.length - 1; ++i) {
            String word1 = words[i];
            String word2 = words[i + 1];
            if (!bigrams.containsKey(word1)) {
                bigrams.put(word1, new HashMap<>());
            }
            if (!bigrams.get(word1).containsKey(word2)) {
                bigrams.get(word1).put(word2, 2.0);
            }
            else {
                bigrams.get(word1).put(word2, bigrams.get(word1).get(word2) + 1.0);
            }
        }

        // Normalize bigrams
        for (String word1 : bigrams.keySet()) {
            double valueSum = this.uniqueWordsCount;
            for (Double word2value : bigrams.get(word1).values()) {
                valueSum += word2value;
            }
            for (String word2 : bigrams.get(word1).keySet()) {
                bigrams.get(word1).put(word2, bigrams.get(word1).get(word2) / valueSum);
            }
        }

        // Create char arrays
        Arrays.asList(chars.toCharArray());
        this.allChars = (ArrayList<Character>) chars.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        this.wordChars = (ArrayList<Character>) wordChars.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        this.nonWordChars = new ArrayList<>(this.allChars);
        this.nonWordChars.removeAll(this.wordChars);

        // Create prefix tree
        this.prefixTree = new PrefixTree();
        this.prefixTree.addWords(Arrays.asList(words), this);
    }

    /**
     *
     * @param corpus file of format <word> <frequency>\n
     * @param chars list of all possible characters
     * @param wordChars list of all possible **word** characters
     */
    public LanguageModel(FileInputStream corpus, String chars, String wordChars) {

        this.wordsCount = 0;
        this.uniqueWordsCount = 0;

        this.unigrams = new HashMap<>();
        this.bigrams = new HashMap<>();

        this.prefixTree = new PrefixTree();

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(corpus)).lines())
        {
            stream.forEach(s -> {
                this.wordsCount += 1;
                this.uniqueWordsCount += 1;

                String[] values = s.split(" ");
                Double unigramProb = Double.valueOf(values[1]);
                unigrams.put(values[0], unigramProb);
                this.prefixTree.addWord(values[0], unigramProb);
            });
        }

        Log.d("CoolModel", String.valueOf(this.wordsCount));

        // Create char arrays
        Arrays.asList(chars.toCharArray());
        this.allChars = (ArrayList<Character>) chars.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        this.wordChars = (ArrayList<Character>) wordChars.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        this.nonWordChars = new ArrayList<>(this.allChars);
        this.nonWordChars.removeAll(this.wordChars);
    }

    public List<String> getNextWords(String text) {
        return this.prefixTree.getNextWords(text);
    }

    public Double getNextWordsUnigramProb(String text) {
        return this.prefixTree.getNextWordsUnigramProb(text);
    }

    public List<Character> getNextChars(String text) {
        List<Character> nextChars = this.prefixTree.getNextChars(text);

        if (text.equals("") || this.isWord(text)) {
            nextChars.addAll(this.nonWordChars);
        }

        return nextChars;
    }

    public boolean isWord(String text) {
        return this.prefixTree.isWord(text);
    }

    public double getUnigramProb(String w1) {
        w1 = w1.toLowerCase();
        if (this.unigrams.containsKey(w1)) {
            return this.unigrams.get(w1);
        }
        return 0;
    }

    public double getBigramProb(String w1, String w2) {
        w1 = w1.toLowerCase();
        w2 = w2.toLowerCase();
        if (this.bigrams.containsKey(w1)) {
            if (this.bigrams.get(w1).containsKey(w2)) {
                return this.bigrams.get(w1).get(w2);
            }
            return 1.0 / (this.getUnigramProb(w1) * this.uniqueWordsCount + this.uniqueWordsCount);
        }
        return 0;
    }
}
