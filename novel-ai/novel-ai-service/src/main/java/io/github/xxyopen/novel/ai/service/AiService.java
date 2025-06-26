package io.github.xxyopen.novel.ai.service;

public interface AiService {
    String continueText(String text, Double length);
    String expandText(String text, Double ratio);
    String condenseText(String text, Double ratio);
    String polishText(String text);
}
