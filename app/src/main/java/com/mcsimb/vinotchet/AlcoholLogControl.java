package com.mcsimb.vinotchet;

public class AlcoholLogControl extends LogControl {

    public boolean sumFlag;

    AlcoholLogControl(int index) {
        curIndex = index;
        wine = db.dataBase.get(curIndex)[1];
        vol = db.dataBase.get(curIndex)[2];
    }

    @Override
    void move(int dir) {
        //if (!sumFlag) {
            curIndex = Math.max(Math.min(curIndex + dir, db.dataBase.size() - 1), 0);
            wine = db.dataBase.get(curIndex)[1];
            vol = db.dataBase.get(curIndex)[2];
        //}
    }

    public String[] getEndCounters() {
        String[] dataList = new String[11];
        if (!db.dataBase.isEmpty()) {
            dataList = checkDataList(false);
            total = false;
        }
        return new String[] {dataList[2], dataList[7]};
    }
    
    @Override
    protected String[] getDataList() {
        String[] dataList = new String[11];
        if (!db.dataBase.isEmpty()) {
            //if (sumFlag) {
            //    dataList = getSummary();
            //    sumFlag = false;
            //    total = true;
            //} else {
                dataList = checkDataList(true);
                total = false;
            //}
        }
        return dataList;
    }

    double getIncomeSum(int ind) {
        double incomeSum = 0;
        for (int i = ind; i < db.dataBase.size(); i++) {
            if (db.dataBase.get(i)[1].equals(wine) && db.dataBase.get(i)[2].equals(vol)) {
                double[] in = income(i, toInt(db.dataBase.get(i)[3]));
                //9.01.18
                //incomeSum += round(in[0] + in[1], 10.0);
                incomeSum += in[0] + in[1];
            } else {
                break;
            }
        }
        return incomeSum;
    }

    private String[] checkDataList(boolean isTotal) {
        String[] dList = new String[11];
        double[] _in = income(curIndex, toInt(db.dataBase.get(curIndex)[3]));
        //9.01.18
        //double in = round(_in[0] + _in[1], 10.0);
        double in = _in[0] + _in[1];
        //double inSum = getIncomeSum(curIndex);
        String[] counters = getCounters(db.dataBase.get(curIndex)[2], wine);
        //if (curIndex > 0 && db.dataBase.get(curIndex - 1)[1].equals(wine) && db.dataBase.get(curIndex - 1)[2].equals(vol)) {
        //    dList[0] = "-";
        //} else {
            //dList[0] = noZero2(inSum);
			dList[0] = noZero2(in);
        //}
        //dList[1] = counters[0];
        //dList[2] = counters[1];
		String[] tot = getTotal();
		if (vol.equals("0.5")) {
			dList[1] = String.valueOf(Integer.decode(tot[0]) - Integer.decode(db.dataBase.get(curIndex)[3]));
			dList[2] = (isTotal ? tot[0] : counters[1]);
		} else {
			dList[1] = String.valueOf(Integer.decode(tot[1]) - Integer.decode(db.dataBase.get(curIndex)[3]));
			dList[2] = (isTotal ? tot[1] : counters[1]);
		}
        dList[3] = db.dataBase.get(curIndex)[3];
        double dal1 = toDal(curIndex, toInt(dList[3]));
        dList[4] = noZero2(dal1);
        dList[5] = noZero2(((in - _in[1]) - dal1) / (in - _in[1]) * 100);
        //dList[6] = counters[2];
        //dList[7] = counters[3];
		dList[6] = String.valueOf(Integer.decode(tot[2]) - Integer.decode(db.dataBase.get(curIndex)[4]));
		dList[7] = (isTotal ? tot[2] : counters[3]);
        dList[8] = db.dataBase.get(curIndex)[4];
        dList[9] = noZero2(toDal(curIndex, toInt(dList[8])));
        //dataList[10] = noZero1(inSum - in) + " / " + noZero2(_in[1]);
        //dList[10] = noZero2(inSum - in);
		dList[10] = "-";

        if (dList[0].equals("-") && dList[10].equals("-")) {
            sumFlag = true;
        }
        return dList;
    }

    private String[] getCounters(String v, String w) {
        int c1, c2;
        if (v.equals("0.5")) {
            c1 = toInt(db.counters.get(w)[0]);
            c2 = toInt(db.counters.get(w)[1]);
            if ((db.MONTH.equals(db.resetCounters[0].split(" ")[0])) &&
                    (curIndex >= toInt(db.resetCounters[0].split(" ")[1]))) {
                if (db.resetCounters[1].equals("1")) {
                    c1 = 0;
                }
                if (db.resetCounters[3].equals("1")) {
                    c2 = 0;
                }
            }
        } else {
            c1 = toInt(db.counters.get(w)[2]);
            c2 = toInt(db.counters.get(w)[3]);
            if ((db.MONTH.equals(db.resetCounters[0].split(" ")[0])) &&
                    (curIndex >= toInt(db.resetCounters[0].split(" ")[1]))) {
                if (db.resetCounters[2].equals("1")) {
                    c1 = 0;
                }
                if (db.resetCounters[4].equals("1")) {
                    c2 = 0;
                }
            }
        }
        if (c1 != 0) {
            for (int i = 0; i < curIndex; i++) {
                if (db.dataBase.get(i)[1].equals(w) && db.dataBase.get(i)[2].equals(v)) {
                    c1 += toInt(db.dataBase.get(i)[3]);
                }
            }
        }
        if (c2 != 0) {
            for (int i = 0; i < curIndex; i++) {
                if (db.dataBase.get(i)[1].equals(w) && db.dataBase.get(i)[2].equals(v)) {
                    c2 += toInt(db.dataBase.get(i)[4]);
                }
            }
        }
        if (c1 == 0) {
            for (int i = toInt(db.resetCounters[0].split(" ")[1]); i < curIndex; i++) {
                if (db.dataBase.get(i)[1].equals(w) && db.dataBase.get(i)[2].equals(v)) {
                    c1 += toInt(db.dataBase.get(i)[3]);
                }
            }
        }
        if (c2 == 0) {
            for (int i = toInt(db.resetCounters[0].split(" ")[1]); i < curIndex; i++) {
                if (db.dataBase.get(i)[1].equals(w) && db.dataBase.get(i)[2].equals(v)) {
                    c2 += toInt(db.dataBase.get(i)[4]);
                }
            }
        }
        return new String[]{toStr(c1), toStr(c1 + toInt(db.dataBase.get(curIndex)[3])),
                toStr(c2), toStr(c2 + toInt(db.dataBase.get(curIndex)[4]))};
    }

    private String[] getSummary() {
        String[] dList = new String[11];
        for (int i = curIndex - 1; i > -1; i--) {
            if (!db.dataBase.get(i)[1].equals(wine) || !db.dataBase.get(i)[2].equals(vol)) {
                double sum = getIncomeSum(i + 1);
                dList[0] = noZero1(sum);
                int c1 = 0;
                for (int k = i + 1; k < curIndex + 1; k++) {
                    c1 += toInt(db.dataBase.get(k)[3]);
                }
                dList[3] = toStr(c1);
                double dal = vol.equals("0.5") ? c1 * 0.05 : c1 * 0.07;
                dList[4] = noZero2(dal);
                dList[5] = noZero2(((sum - dal) / dal) * 100);
                for (int k = 1; k < dList.length; k++) {
                    if (k != 3 && k != 4 && k != 5) {
                        dList[k] = "-";
                    }
                }
                break;
            }
        }
        return dList;
    }

    private String[] getTotal() {
        int t5 = 0, t7 = 0, t = 0;
        for (String w : db.winesList.keySet()) {
            for (String v : new String[]{"0.5", "0.7"}) {
                String[] c = getCounters(v, w);
                t += Integer.decode(c[2]);
                if (v.equals("0.5")) {
                    t5 += Integer.decode(c[0]);
                } else {
                    t7 += Integer.decode(c[0]);
                }
            }
        }
        t5 += toInt(db.dataBase.get(curIndex)[3]);
        t7 += toInt(db.dataBase.get(curIndex)[3]);
        t += toInt(db.dataBase.get(curIndex)[4]);
        return new String[]{toStr(t5), toStr(t7), toStr(t)};
    }
}

