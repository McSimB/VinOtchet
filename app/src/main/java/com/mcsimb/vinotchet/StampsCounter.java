package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

public class StampsCounter {

    private static final String VOL_5 = "0.5";
    private static final String VOL_7 = "0.7";
    private static final String LETTER_5 = "Ю";
    private static final String LETTER_7 = "Я";

    private FileUtils db;
    private Integer number5;
    private Integer number7;
    private Integer fullRange5;
    private Integer fullRange7;
    private Integer fullNumber5;
    private Integer fullNumber7;
    private Integer curPack5;
    private Integer curPack7;

    public StampsCounter() {
        db = FileUtils.getInstance();
    }

    @SuppressLint("DefaultLocale")
    public List<String[]> getDayStamps(String day) {
        ArrayList<String[]> list = new ArrayList<>();
        curPack5 = 0;
        curPack7 = 0;
        fullRange5 = toInt(db.stamps05.get(0), 0, 3);
        fullRange7 = toInt(db.stamps07.get(0), 0, 3);
        fullNumber5 = toInt(db.stamps05.get(0), 3);
        fullNumber7 = toInt(db.stamps07.get(0), 3);
        number5 = toInt(db.stamps05.get(0), 9) % 500;
        number7 = toInt(db.stamps07.get(0), 9) % 500;
        for (String[] data : db.dataBase) {
            int d = Integer.decode(data[4]);
            if (Integer.decode(data[0]) > Integer.decode(day)) {
                break;
            }
            if (data[0].equals(day)) {
                String[] counters = new String[2];
                if (data[2].equals(VOL_5)) {
                    counters[0] = LETTER_5 + String.format("%03d", fullRange5) +
                            " " + String.format("%09d", fullNumber5);
                    supply(d - 1, VOL_5);
                    counters[1] = LETTER_5 + String.format("%03d", fullRange5) +
                            " " + String.format("%09d", fullNumber5);
                    if (db.dataBase.indexOf(data) < db.dataBase.size()) {
                        supply(1, VOL_5);
                    }
                } else if (data[2].equals(VOL_7)) {
                    counters[0] = LETTER_7 + String.format("%03d", fullRange7) +
                            " " + String.format("%09d", fullNumber7);
                    supply(d - 1, VOL_7);
                    counters[1] = LETTER_7 + String.format("%03d", fullRange7) +
                            " " + String.format("%09d", fullNumber7);
                    if (db.dataBase.indexOf(data) < db.dataBase.size()) {
                        supply(1, VOL_7);
                    }
                }
                list.add(counters);
            } else {
                supply(d, data[2]);

            }
        }
        return list;
    }

    private void supply(int count, String vol) {
        if (vol.equals(VOL_5)) {
            int rng = fullRange5 % 30 == 0 ? 30 : fullRange5 % 30;
            rng += (number5 + count) / 500;
            if (rng > 30) {
                curPack5++;
                fullRange5 = toInt(db.stamps05.get(curPack5), 0, 3) + rng - 31;
                fullNumber5 = toInt(db.stamps05.get(curPack5), 3) + number5 - 1;
            } else {
                fullRange5 += (number5 + count) / 500;
            }
            supplyFullNum(count, vol);
            number5 = (number5 + count) % 500;
        } else if (vol.equals(VOL_7)) {
            int rng = fullRange7 % 30 == 0 ? 30 : fullRange7 % 30;
            rng += (number7 + count) / 500;
            if (rng > 30) {
                curPack7++;
                fullRange7 = toInt(db.stamps07.get(curPack7), 0, 3) + rng - 31;
                fullNumber7 = toInt(db.stamps07.get(curPack7), 3) + number7 - 1;
            } else {
                fullRange7 += (number7 + count) / 500;
            }
            supplyFullNum(count, vol);
            number7 = (number7 + count) % 500;
        }
    }

    private void supplyFullNum(int count, String vol) {
        if (vol.equals(VOL_5)) {
            if (number5 + count > 500) {
                int n2 = fullNumber5 - fullNumber5 / 1000 * 1000;
                int n1 = fullNumber5 - n2;
                if (fullNumber5 / 500 % 2 == 0) {                 // номера 001 ...500
                    fullNumber5 = n1 + (n2 + count) % 500;
                } else {                                       // номера 501 ... 000
                    fullNumber5 = n1 + (n2 + count) % 500 + 500;
                }
            } else {
                fullNumber5 += count;
            }
        } else if (vol.equals(VOL_7)) {
            if (number7 + count > 500) {
                int n2 = fullNumber7 - fullNumber7 / 1000 * 1000;
                int n1 = fullNumber7 - n2;
                if (fullNumber7 / 500 % 2 == 0) {                 // номера 001 ...500
                    fullNumber7 = n1 + (n2 + count) % 500;
                } else {                                       // номера 501 ... 000
                    fullNumber7 = n1 + (n2 + count) % 500 + 500;
                }
            } else {
                fullNumber7 += count;
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private int toInt(String str, int start, int end) {
        return Integer.decode(str.substring(start, end).replaceAll("^0*", ""));
    }

    private int toInt(String str, int start) {
        return Integer.decode(str.substring(start).replaceAll("^0*", ""));
    }

}

