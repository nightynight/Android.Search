package com.brokepal.searchdemo.entity;

/**
 * Created by Administrator on 2016/8/19.
 */
public class Vocabulary {
    private String word;
    private String definition;

    public Vocabulary() {

    }
    public Vocabulary(String word, String definition) {
        this.word = word;
        this.definition = definition;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "Vocabulary{" +
                "word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}
