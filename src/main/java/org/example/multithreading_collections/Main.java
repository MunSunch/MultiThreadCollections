package org.example.multithreading_collections;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    private static final int COUNT_WORDS = 10_000;
    private static final int LENGTH_WORD = 100_000;
    private static final int SIZE_BLOCKING_QUEUE = 100;
    private static final CopyOnWriteArrayList<Map.Entry<String, Long>> list = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws InterruptedException, IOException {
        BlockingQueue<String> queue1 = new ArrayBlockingQueue<>(SIZE_BLOCKING_QUEUE);
        BlockingQueue<String> queue2 = new ArrayBlockingQueue<>(SIZE_BLOCKING_QUEUE);
        BlockingQueue<String> queue3 = new ArrayBlockingQueue<>(SIZE_BLOCKING_QUEUE);

        list.add(new AbstractMap.SimpleEntry<>("", 1L));
        list.add(new AbstractMap.SimpleEntry<>("", 1L));
        list.add(new AbstractMap.SimpleEntry<>("", 1L));

        Thread addThread = new Thread(()->{
            for (int i=0; i<COUNT_WORDS; i++) {
                var text = generateText("abc", LENGTH_WORD);
                try {
                    queue1.put(text);
                    queue2.put(text);
                    queue3.put(text);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread analysQueue1Thread = new Thread(()->{
            for (int i=0; i<COUNT_WORDS; i++) {
                try {
                    String text = queue1.take();
                    isMaxCountSymbolText(text, 'a');
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread analysQueue2Thread = new Thread(()->{
            for (int i=0; i<COUNT_WORDS; i++) {
                try {
                    String text = queue2.take();
                    isMaxCountSymbolText(text, 'b');
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread analysQueue3Thread = new Thread(()->{
            for (int i=0; i<COUNT_WORDS; i++) {
                try {
                    String text = queue3.take();
                    isMaxCountSymbolText(text, 'c');
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        addThread.start();
        analysQueue1Thread.start();
        analysQueue2Thread.start();
        analysQueue3Thread.start();

        addThread.join();
        analysQueue1Thread.join();
        analysQueue2Thread.join();
        analysQueue3Thread.join();

        for (int i=0; i<3; i++) {
            String currentSymbol = Character.toString('a'+i);
            File file = new File("./" + "analys_" + currentSymbol);
            file.createNewFile();
            try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file)))
            {
                out.write(list.get(i).getKey().getBytes());
                out.flush();
            }
            System.out.println(currentSymbol + ": maxCount=" + list.get(i).getValue() + " text=" + file.getPath());
        }
    }

    private static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    private static boolean isMaxCountSymbolText(String text, char symbol) {
        long countSymbol = getCountSymbolText(text, symbol);
        int indexSymbolList = getIndexSymbolsList(symbol);
        return replaceEntryListIfMax(text,indexSymbolList, countSymbol);
    }

    private static long getCountSymbolText(String text, char symbol) {
        return text.chars()
                .filter(x -> x == symbol)
                .count();
    }

    private static int getIndexSymbolsList(char symbol) {
        switch (symbol) {
            case 'a' -> {
                return 0;
            }
            case 'b' -> {
                return 1;
            }
            case 'c' -> {
                return 2;
            }
            default -> throw new RuntimeException();
        }
    }

    private static boolean replaceEntryListIfMax(String newText, int indexSymbolList, long countSymbol) {
        if(list.get(indexSymbolList).getValue() < countSymbol) {
            list.set(indexSymbolList, new AbstractMap.SimpleEntry<>(newText, countSymbol));
            return true;
        }
        return false;
    }
}