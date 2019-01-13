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
    public ArrayList<Character> allChars;
    public ArrayList<Character> wordChars;
    public ArrayList<Character> nonWordChars;
    public HashMap<String, Integer> wordToInteger;
    public HashMap<Integer, String> integerToWord;
    public HashMap<String, Integer> wordToPostag;
    public HashSet<Long> posTagSequenceDotHash;

    public int ngram_usage;
    public static final int NGRAM_NONE = 0;
    public static final int NGRAM_UNIGRAM = 1;
    public static final int NGRAM_BIGRAM = 2;

    /**
     *
     * @param corpus
     * @param chars list of all possible characters
     * @param wordChars list of all possible **word** characters
     */
    public LanguageModel(FileInputStream corpus, FileInputStream posTagDotData, String chars, String wordChars, int ngram_usage) {
        this.ngram_usage = ngram_usage;

        this.wordsCount = 0;

        this.prefixTree = new PrefixTree();

        this.wordToInteger = new HashMap<>();
        this.integerToWord = new HashMap<>();
        this.wordToPostag = new HashMap<>();

        this.posTagSequenceDotHash = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(corpus))) {
            int line_counter = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (line_counter == 0) {
                    this.wordsCount = Integer.valueOf(line);
                }
                else if (line_counter <= this.wordsCount) {
                    String[] values = line.split(" ");
                    Integer unigramProb = Integer.valueOf(values[1]);
                    Integer postagCode = Integer.valueOf(values[2]);
                    this.wordToPostag.put(values[0], postagCode);
                    this.prefixTree.addWord(values[0], unigramProb * 1e-9);
                    wordToInteger.put(values[0], wordToInteger.size());
                    integerToWord.put(integerToWord.size(), values[0]);
                }
                else if (line_counter == this.wordsCount + 1) {
                }
                else {
                    String[] values = line.split(" ");
                    Integer bigramProb = Integer.valueOf(values[2]);
                    this.prefixTree.addBigram(
                            Integer.valueOf(values[0]),
                            integerToWord.get(Integer.valueOf(values[1])),
                            bigramProb * 1e-9);
                }
                line_counter += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(posTagDotData))) {
            String line;
            while ((line = br.readLine()) != null) {
                this.posTagSequenceDotHash.add(Long.valueOf(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create char arrays
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
        return this.prefixTree.getBigramProb(wordToInteger.get(word), word, text, 98347850, this.wordsCount);
    }

    public Double getNextWordsBigramProb(String word, String text) {
        return this.prefixTree.getNextWordsBigramProb(wordToInteger.get(word), word, text, 98347850, this.wordsCount);
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
