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

    public static void main(String[] args) throws InterruptedException, IOException {
        BlockingQueue<String> queue1 = new ArrayBlockingQueue<>(SIZE_BLOCKING_QUEUE);
        BlockingQueue<String> queue2 = new ArrayBlockingQueue<>(SIZE_BLOCKING_QUEUE);
        BlockingQueue<String> queue3 = new ArrayBlockingQueue<>(SIZE_BLOCKING_QUEUE);

        CopyOnWriteArrayList<Map.Entry<String, Long>> list = new CopyOnWriteArrayList<>();
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
                    long countSymbol = text.chars()
                            .filter(x -> x == 'a')
                            .count();
                    if(list.get(0).getValue() < countSymbol) {
                        list.set(0, new AbstractMap.SimpleEntry<>(text, countSymbol));
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread analysQueue2Thread = new Thread(()->{
            for (int i=0; i<COUNT_WORDS; i++) {
                try {
                    String text = queue2.take();
                    long countSymbol = text.chars()
                            .filter(x -> x == 'b')
                            .count();
                    if(list.get(1).getValue() < countSymbol) {
                        list.set(1, new AbstractMap.SimpleEntry<>(text, countSymbol));
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread analysQueue3Thread = new Thread(()->{
            for (int i=0; i<COUNT_WORDS; i++) {
                try {
                    String text = queue3.take();
                    long countSymbol = text.chars()
                            .filter(x -> x == 'c')
                            .count();
                    if(list.get(2).getValue() < countSymbol) {
                        list.set(2, new AbstractMap.SimpleEntry<>(text, countSymbol));
                    }
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
}