package com.mcsimb.vinotchet;
import java.util.ArrayList;

public class JobLogControl extends AlcoholLogControl {

    private boolean forDayOnly;

    JobLogControl(int index, boolean forDayOnly) {
        super(index);
        this.forDayOnly = forDayOnly;
        if (forDayOnly) {
            initSnap("");
        }
    }

    @Override
    void initSnap(String sort) {
        indexList.clear();
        for (String[] d : db.dataBase) {
            if (d[0].equals(db.dataBase.get(curIndex)[0])) {
                indexList.add(db.dataBase.indexOf(d));
            }
        }
    }
    
    @Override
    void move(int dir) {
        if (forDayOnly) {
            setSnapIndex(Math.max(Math.min(snapIndex + dir, indexList.size() - 1), 0));
            wine = db.dataBase.get(curIndex)[1];
            vol = db.dataBase.get(curIndex)[2];
        } else {
            super.move(dir);
        }
    }

    @Override
    public boolean btnPrevVisibility() {
        if (forDayOnly) {
            return snapIndex != 0 && !indexList.isEmpty();
        } else {
            return super.btnPrevVisibility();
        }
    }

    @Override
    public boolean btnNextVisibility() {
        if (forDayOnly) {
            return snapIndex != indexList.size() - 1 && !indexList.isEmpty();
        } else {
            return super.btnNextVisibility();
        }
    }
    
    @Override
    protected String[] getDataList() {
        String[] dataList = new String[10];
        if (!db.dataBase.isEmpty()) {
            double[] _in = income(curIndex, toInt(db.dataBase.get(curIndex)[3]));
            //9.01.18
            //double in = round(_in[0] + _in[1], 10.0);
            double in = _in[0] + _in[1];
            double rew = rework(curIndex, toInt(db.dataBase.get(curIndex)[3]),
                                toInt(db.dataBase.get(curIndex)[4]))[0];
            double inSum = getIncomeSum(curIndex);
            if (curIndex > 0 && db.dataBase.get(curIndex - 1)[1].equals(wine) &&
                db.dataBase.get(curIndex - 1)[2].equals(vol)) {
                dataList[0] = noZero2(inSum);
                dataList[1] = "-";
            } else {
                dataList[0] = "-";
                dataList[1] = noZero2(inSum);
            }
            double dal1 = toDal(curIndex, toInt(db.dataBase.get(curIndex)[4]));
            dataList[2] = noZero2(toDouble(db.dataBase.get(curIndex)[2]));
            dataList[3] = db.dataBase.get(curIndex)[4];
            dataList[4] = noZero2(dal1);
            dataList[5] = noZero2(rew - _in[1]);
            //dataList[6] = noZero2(Math.floor(dal1 * 0.34) / 100.0);
            dataList[6] = noZero2(wineLoss034);
            dataList[7] = noZero2(Math.floor(wineLoss034 * 10000.0 / dal1) / 100.0);
            dataList[8] = "0,34";
            dataList[9] = noZero2(inSum - in);
        }
        return dataList;
    }
}
