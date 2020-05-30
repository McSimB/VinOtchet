package com.mcsimb.vinotchet;

public class RequirementsControl extends ReworkActsControl {

    RequirementsControl(String[] days) {
        super(days);
    }

    @Override
    protected String[] getDataList() {
        super.getDataList();
        int i = 0;
        double sum = 0;
        String[] dList = new String[8];
        for (double[] d : productRework.values()) {
            dList[i] = noZero2(d[0] + d[1]);
            sum += d[0] + d[1];
            ++i;
        }
        dList[i] = noZero2(sum);

        return dList;
    }
}



