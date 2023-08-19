package net.osandman.rzdmonitoring.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Beeper {
    public static void Beep() {
        try {
            // Получение аудио формата для генерации звука
            AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);

            // Получение Line для аудио вывода
            SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
            line.open(audioFormat);
            line.start();

            // Генерация звукового сигнала
            int frequency = 1000; // Частота в герцах
            int durationMs = 500; // Продолжительность сигнала в миллисекундах
            int numSamples = durationMs * 44100 / 1000;
            byte[] audioData = new byte[numSamples * 2];

            for (int i = 0; i < numSamples; i++) {
                double angle = i / (44100.0 / frequency) * 2.0 * Math.PI;
                short sample = (short) (Short.MAX_VALUE * Math.sin(angle));
                audioData[2 * i] = (byte) (sample & 0xFF);
                audioData[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            // Запись звуковых данных в динамик
            line.write(audioData, 0, audioData.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
