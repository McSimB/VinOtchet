package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;

import java.util.ArrayList;

abstract class LogControl {

    final ArrayList<Integer> indexList = new ArrayList<Integer>();
    final FileUtils db = FileUtils.getInstance();

    int count05;
    int count07;
    int snapIndex;
    int curBlend;
    int blend = 0;
    int curIndex = 0;
    int newBlendFlag = 0;
    boolean divBlendFlag = false;
    String wine;
    String vol;
    String[] days;
    int curDay = 0;
    boolean total;
    double wineLoss034;

    protected abstract String[] getDataList();

    protected void export() {
    }

    void initSnap(String sort) {
        boolean ini = false;
        indexList.clear();
        for (String[] s : db.dataBase) {
            if (s[1].equals(sort)) {
                indexList.add(db.dataBase.indexOf(s));
                if (curIndex == db.dataBase.indexOf(s)) {
                    snapIndex = indexList.indexOf(curIndex);
                    ini = true;
                }
            }
        }
        if (!indexList.isEmpty() && !ini) {
            setSnapIndex(0);
        }
    }

    void setSnapIndex(int index) {
        snapIndex = index;
        curIndex = indexList.get(snapIndex);
    }

    void move(int direct) {
        curIndex = Math.max(Math.min(curIndex + direct, db.dataBase.size() - 1), 0);
        wine = db.dataBase.get(curIndex)[1];
        vol = db.dataBase.get(curIndex)[2];
    }

    boolean btnPrevVisibility() {
        return curIndex != 0;
    }

    boolean btnNextVisibility() {
        return curIndex != db.dataBase.size() - 1 && !db.dataBase.isEmpty();
    }

    double toDal(int ind, int count) {
        double dal;
        if (db.dataBase.get(ind)[2].equals("0.5")) {
            dal = count * 0.05;
        } else
            dal = count * 0.07;
        return dal;
    }

    //Поступило, дал
    double[] income(int ind, int count1) {
        //double sink; //слив
        //if (db.get(ind)[2].equals("0.5")) {
        //	sink = 2.0;
        //} else sink = 1.68;

        double dal = toDal(ind, count1);
        double in = round(toDal(ind, count1) * 1.001, 10);

        //for (int i = ind - 1; i > -1; i--) {
        //	if (db.get(i)[2].equals(db.get(ind)[2])) {
        //		if (db.get(i)[1].equals(db.get(ind)[1])) {
        //			sink = 0;
        //		} else break;
        //	}
        //}
        //if (newBlendFlag == 1) {
        //	sink = 0;
        //}

        //9.01.18
        if (db.dataBase.get(ind)[2].equals("0.5")) {
            if (((in - dal) / in * 100 > 0.17) && !divBlendFlag) {
                return new double[]{in - 0.1 > dal ? in - 0.1 : dal, 0};
            }
        } else {
            if (((in - dal) / in * 100 > 0.14) && !divBlendFlag) {
                return new double[]{in - 0.1 > dal ? in - 0.1 : dal, 0};
            }
        }

        return new double[]{in /*dal*/, 0 /*sink*/};
    }

    //Потери
    private double wineLoss076(double dal) {
        return round(dal * 0.0072, 100.0);
    }

	/*double wineLoss034(double dal) {
		return (int) (dal * 0.34) / 100.0;
	}*/

    //Расход:на розлив + потери 0.76 + лабор-ия
    //и потери 0.42%
    double[] debit(int ind, int count1, int count2) {
        double deb;
        double r100;
        double loss042;
        double lab = 0.0;
        double dal = toDal(ind, count2);
        if (rework(ind, count1, count2)[1] == 0.25) {
            lab = 0.05;
        }
        deb = dal + wineLoss076(dal) + lab;
        r100 = deb - (int) (deb * 10) / 10.0; //Сотая часть дроби deb
        double roundFactor = 0.05;
        if (r100 > roundFactor) {
            deb = deb - r100 + 0.1;  //Отброшена сотая часть дроби deb
            loss042 = wineLoss076(dal) - wineLoss034 - r100 + 0.1;
        } else {
            deb = deb - r100;
            loss042 = wineLoss076(dal) - wineLoss034 - r100;
        }
        return new double[]{deb, loss042};
    }

    //Испр. брак и лабор-ия
    double[] rework(int ind, int count1, int count2) {
        double rew;
        double lab = 0.25; // Возвратила лаб-ия
        double dal = toDal(ind, count2);
        double[] in = income(ind, count1);
        //wineLoss034 = (int) (dal * 0.34) / 100.0;
        wineLoss034 = (int) (dal * 0.034) / 10.0;
        //9.01.18
        //rew = round(in[0] + in[1], 10.0) - dal - wineLoss034;
        rew = in[0] + in[1] - dal - wineLoss034;
        if (rew < 0) {
            rew = 0;
            //9.01.18
            //wineLoss034 = round(in[0] + in[1], 10.0) - dal;
            wineLoss034 = in[0] + in[1] - dal;
        }
        if (rew < 0.3) {
            lab = 0.0;
        }
        return new double[]{round(rew, 100.0), lab};
    }

    //Имеется ли разделение купажей. Возвращает остаток
    //закрываемого купажа, остаток кон., расход
    double[] isDivBlend() {
        blend = 0;
        double deb = 0.0;
        double divRest = 0.0;
        double rest = toDouble(db.winesList.get(db.dataBase.get(curIndex)[1]).get(blend));
        for (int i = 0; i < snapIndex + 1; i++) {
            String[] d = db.dataBase.get(indexList.get(i));
            divRest = 0.0;
            deb = debit(indexList.get(i), toInt(d[3]), toInt(d[4]))[0];
            rest = rest - deb;
            divBlendFlag = false;
            if (rest < 1) {
                divBlendFlag = true;
                divRest = rest + deb;
                try {
                    String b = db.winesList.get(db.dataBase.get(curIndex)[1]).get(++blend);
                    if (rest > -1) {
                        rest = 0;
                    }
                    rest += toDouble(b);

                    // 31.01.17
                    int[] c = blendDivision(divRest, indexList.get(i));
                    double[][] in = new double[2][2];
                    double[][] rew = new double[2][2];
                    double rest0 = toDouble(db.winesList.get(db.dataBase.get(curIndex)[1]).get(blend));
                    for (int j = 0; j < 2; j++) {
                        newBlendFlag = j;
                        in[j] = income(indexList.get(i), c[j * 2]);
                        rew[j] = rework(indexList.get(i), c[j * 2], c[j * 2 + 1]);
                    }
                    newBlendFlag = 0;
                    double rew2 = rew[1][0] - rew[1][1] * 1.2;
                    double loss0422;
                    if (c[2] > 0) {
                        //9.01.18
                        //loss0422 = rest0 + rew2 + rew[1][1] - round(in[1][0], 10.0) - rest;
                        loss0422 = rest0 + rew2 + rew[1][1] - in[1][0] - rest;
                        if (loss0422 < 0) {
                            rest -= 0.1;
                            deb += 0.1;
                        }
                    }
                    //

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new double[]{divRest, rest, deb};
    }

    int[] blendDivision(double rest, int ind) {
        int count1_1, count2_1;
        int count1_2;
        int count2_2;
        double dal1 = (rest - 0.05) / 1.0074;
        String[] d = db.dataBase.get(ind);
        if (d[2].equals("0.5")) {
            count2_1 = (int) (dal1 / 0.05);
        } else {
            count2_1 = (int) (dal1 / 0.07);
        }
        if (toInt(d[4]) - count2_1 < 15) {
            count2_1 = toInt(d[4]);
        }
        count1_1 = (int) (count2_1 * toInt(d[3]) / toDouble(d[4]));
        count1_2 = toInt(d[3]) - count1_1;
        count2_2 = toInt(d[4]) - count2_1;
        return new int[]{count1_1, count2_1, count1_2, count2_2};

    }

    double round(double value, double precis) {
        return Math.round(value * precis) / precis;
    }

    double toDouble(String s) {
        return Double.parseDouble(s);
    }

    double toDouble2(String s) {
        double d;
        if (s.equals("-")) {
            d = 0;
        } else {
            d = toDouble(s.replace(",", "."));
        }
        return d;
    }

    int toInt(String i) {
        return Integer.decode(i);
    }

    String toStr(Object s) {
        return String.valueOf(s);
    }

    @SuppressLint("DefaultLocale")
    String noZero1(double d) {
        String s;
        double _d = round(d, 10.0);
        if (d == 0) {
            s = "-";
        } else {
            if (_d - Math.round(d) == 0) {
                s = String.format("%.0f", d);
            } else {
                s = String.format("%.1f", _d);
            }
        }
        return s;
    }

    @SuppressLint("DefaultLocale")
    String noZero2(double d) {
        String s;
        double _d = round(d, 100.0);
        if (d == 0) {
            s = "-";
        } else {
            if (_d - Math.round(d) == 0) {
                s = String.format("%.0f", d);
            } else if (_d - round(d, 10.0) == 0) {
                s = String.format("%.1f", d);
            } else {
                s = String.format("%.2f", _d);
            }
        }
        return s;
    }
}

