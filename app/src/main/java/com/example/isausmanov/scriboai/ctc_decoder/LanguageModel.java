package com.example.isausmanov.scriboai.ctc_decoder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageModel {

    public PrefixTree prefixTree;
    public int wordsCount;
    public int uniqueWordsCount;
    public ArrayList<Character> allChars;
    public ArrayList<Character> wordChars;
    public ArrayList<Character> nonWordChars;
    public HashMap<String, Integer> wordToInteger;

    public int ngram_usage;
    public static final int NGRAM_NONE = 0;
    public static final int NGRAM_UNIGRAM = 1;
    public static final int NGRAM_BIGRAM = 2;

    /**
     *
     * @param corpus file of format <word> <frequency>\n
     * @param chars list of all possible characters
     * @param wordChars list of all possible **word** characters
     */
    public LanguageModel(FileInputStream corpus, String chars, String wordChars, int ngram_usage) {
        this.ngram_usage = ngram_usage;

        this.wordsCount = 0;
        this.uniqueWordsCount = 0;

        this.prefixTree = new PrefixTree();

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(corpus)).lines())
        {
            stream.forEach(s -> {
                this.wordsCount += 1;
                this.uniqueWordsCount += 1;

                String[] values = s.split(" ");
                Double unigramProb = Double.valueOf(values[1]);
                this.prefixTree.addWord(values[0], unigramProb);
            });
        }

        // Create char arrays
        Arrays.asList(chars.toCharArray());
        this.allChars = (ArrayList<Character>) chars.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        this.wordChars = (ArrayList<Character>) wordChars.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        this.nonWordChars = new ArrayList<>(this.allChars);
        this.nonWordChars.removeAll(this.wordChars);
    }

    /**
     *
     * @param corpus_unigram file of format <word> <frequency>\n
     * @param corpus_bigram file of format <word1> <word2> <frequency>\n
     * @param chars list of all possible characters
     * @param wordChars list of all possible **word** characters
     */
    public LanguageModel(FileInputStream corpus_unigram, FileInputStream corpus_bigram, String chars, String wordChars, int ngram_usage) {
        this.ngram_usage = ngram_usage;

        this.wordsCount = 0;
        this.uniqueWordsCount = 0;

        this.prefixTree = new PrefixTree();

        this.wordToInteger = new HashMap<>();

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(corpus_unigram)).lines())
        {
            stream.forEach(s -> {
                this.wordsCount += 1;
                this.uniqueWordsCount += 1;

                String[] values = s.split(" ");
                Double unigramProb = Double.valueOf(values[1]);
                this.prefixTree.addWord(values[0], unigramProb);

                wordToInteger.put(values[0], wordToInteger.size());
            });
        }

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(corpus_bigram)).lines())
        {
            stream.forEach(s -> {
                String[] values = s.split(" ");
                Double bigramProb = Double.valueOf(values[2]);
                this.prefixTree.addBigram(wordToInteger.get(values[0]), values[1], bigramProb);
            });
        }

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

    public Double getUnigramProb(String text) {
        return this.prefixTree.getUnigramProb(text);
    }

    public Double getNextWordsUnigramProb(String text) {
        return this.prefixTree.getNextWordsUnigramProb(text);
    }

    public Double getBigramProb(String word, String text) {
        return this.prefixTree.getBigramProb(wordToInteger.get(word), word, text, 10930416, this.wordsCount);
    }

    public Double getNextWordsBigramProb(String word, String text) {
        return this.prefixTree.getNextWordsBigramProb(wordToInteger.get(word), word, text, 10930416, this.wordsCount);
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
}
