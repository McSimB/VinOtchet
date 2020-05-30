package com.mcsimb.vinotchet;

public class InfoLogControl extends LogControl {

    InfoLogControl(String wine) {
        initSnap(wine);
    }

    InfoLogControl(int index) {
        curIndex = index;
        initSnap(db.dataBase.get(curIndex)[1]);
    }

    @Override
    public void move(int direct) {
        setSnapIndex(Math.max(Math.min(snapIndex + direct, indexList.size() - 1), 0));
    }

    @Override
    public boolean btnPrevVisibility() {
        return snapIndex != 0 && !indexList.isEmpty();
    }

    @Override
    public boolean btnNextVisibility() {
        return snapIndex != indexList.size() - 1 && !indexList.isEmpty();
    }

    @Override
    protected String[] getDataList() {
        String[] dataList = new String[10];
        if (!indexList.isEmpty()) {
            double[] rest = isDivBlend();
            if (!divBlendFlag) {
                int count1 = toInt(db.dataBase.get(curIndex)[3]);
                int count2 = toInt(db.dataBase.get(curIndex)[4]);
                double[] in = income(curIndex, count1);
                dataList[1] = noZero2(in[0] + in[1]); //Поступило
                dataList[4] = toStr(count2); //Всего бут.
                dataList[3] = noZero2(toDal(curIndex, count2)); //Кол. дал
                dataList[5] = noZero2(rework(curIndex, count1, count2)[0]); //Брак
                //double loss034 = wineLoss034(toDal(curIndex, count2));
                dataList[8] = noZero2(wineLoss034); //Потери 0.34%
				/*if (toDouble2(dataList[5]) < 0) {
					dataList[5] = "-";
					dataList[8] = noZero2(toDouble2(dataList[1]) - toDouble2(dataList[3]));
				}*/
                double loss024 = round(toDouble2(dataList[8]) * 0.70588, 100.0);
                dataList[7] = noZero2(loss024); // 0.24%
                dataList[6] = noZero2(toDouble2(dataList[8]) - loss024); // 0.1%
                dataList[9] = dataList[4]; //Сдано прод.
            } else {
                int[] c = blendDivision(rest[0], curIndex);
                double[][] in = new double[2][2];
                double[] loss034 = new double[2];
                double[] rew = new double[2];
                for (int i = 0; i < 2; i++) {
                    newBlendFlag = i;
                    in[i] = income(curIndex, c[i * 2]);
                    rew[i] = rework(curIndex, c[i * 2], c[i * 2 + 1])[0];
                    //loss034[i] = wineLoss034(toDal(curIndex, c[i * 2 + 1]));
                    loss034[i] = wineLoss034;
                }
                newBlendFlag = 0;
                dataList[1] = noZero2(in[0][0] + in[0][1]) + " / " + noZero2(in[1][0]);
                dataList[4] = toStr(c[1]) + " / " + toStr(c[3]);
                dataList[3] = noZero2(toDal(curIndex, c[1])) + " / " + noZero2(toDal(curIndex, c[3]));
                dataList[8] = noZero2(loss034[0]) + " / " + noZero2(loss034[1]);
                double loss0241 = round(loss034[0] * 0.24 / 0.34, 100.0);
                double loss0242 = round(loss034[1] * 0.24 / 0.34, 100.0);
                dataList[7] = noZero2(loss0241) + " / " + noZero2(loss0242);
                dataList[6] = noZero2(loss034[0] - loss0241) + " / " + noZero2(loss034[1] - loss0242);
                dataList[5] = noZero2(rew[0]) + " / " + noZero2(rew[1]);
                dataList[9] = dataList[4];
            }
            dataList[0] = db.dataBase.get(curIndex)[0] + "." + db.MONTH; //Дата
            dataList[2] = noZero1(toDouble(db.dataBase.get(curIndex)[2])); //Вместим.
        }
        return dataList;
    }

}
