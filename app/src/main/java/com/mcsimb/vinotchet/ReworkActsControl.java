package com.mcsimb.vinotchet;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReworkActsControl extends LogControl {

    Map<String, double[]> productRework = new LinkedHashMap<String, double[]>();

    ReworkActsControl(String[] days) {
        this.days = days;
    }

    @Override
    public void move(int direct) {
        curDay = Math.max(Math.min(curDay + direct, days.length - 1), 0);
    }

    @Override
    public boolean btnPrevVisibility() {
        return curDay != 0;
    }

    @Override
    public boolean btnNextVisibility() {
        return curDay != days.length - 1;
    }

    @Override
    protected String[] getDataList() {
        productRework.clear();
        for (String wine : db.winesList.keySet()) {
            productRework.put(wine, new double[]{0, 0});
        }
        String[] dList = new String[8];
        for (String[] product : db.dataBase) {
            if (product[0].equals(days[curDay])) {
                double[] d = getProductRework(db.dataBase.indexOf(product));
                double d0 = productRework.get(product[1])[0] + d[0];
                double d1 = productRework.get(product[1])[1] + d[1];
                productRework.put(product[1], new double[]{d0, d1});
            }
        }
        int i = 0;
        for (double[] d : productRework.values()) {
            dList[i] = noZero2(d[0]) + " / " + noZero2(d[1]);
            ++i;
        }
        return dList;
    }

    private double[] getProductRework(int index) {
        double[] d = new double[2];
        int count1 = toInt(db.dataBase.get(index)[3]);
        int count2 = toInt(db.dataBase.get(index)[4]);
        d[0] = rework(index, count1, count2)[0];
        d[1] = income(index, count1)[1];
        if (d[0] < d[1]) {
            d[1] = 0;
            return d;
        }
        d[0] = d[0] - d[1];
        return d;
    }
}
