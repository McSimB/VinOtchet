package com.mcsimb.vinotchet;

import java.util.ArrayList;

public class MonthSummaryControl extends InfoLogControl {

    private int blendCount;
    private int prevOrNextBlend;
    private int[] blendIndexes;
    private ReportLogControl report;

    MonthSummaryControl(String wine) {
        super(wine);
    }

    @Override
    public void initSnap(String wine) {
        super.initSnap(wine);
        blendCount = db.winesList.get(wine).size();
        //for (String s: db.winesList.get(wine)) {
        //if (blendCount != 1 && toDouble(s) == 0) {
        //blendCount -= 1;
        //}
        //}
        blendIndexes = new int[blendCount];
        curBlend = 0;
        report = new ReportLogControl(wine);
    }

    @Override
    public void move(int direct) {
        curBlend = Math.max(Math.min(curBlend + direct, blendCount - 1), 0);
    }

    @Override
    public boolean btnPrevVisibility() {
        return curBlend != 0;
    }

    @Override
    public boolean btnNextVisibility() {
        return curBlend != blendCount - 1;
    }

    @Override
    public String[] getDataList() {
        String[] dataList = {"0", "0", "0", "0", "", "0", "0", "0"};
        String[] iList;
        String[] rList;
        count05 = 0;
        count07 = 0;
        if (curBlend == 0) {
            prevOrNextBlend = 0;
        }
        for (snapIndex = blendIndexes[curBlend]; snapIndex < indexList.size(); snapIndex++) {
            curIndex = indexList.get(snapIndex);
            report.setSnapIndex(snapIndex);
            iList = super.getDataList();
            rList = report.getDataList();
            if (!divBlendFlag) {
                dataList[0] = noZero2(toDouble2(dataList[0]) + toDouble2(iList[1]));
                dataList[1] = noZero2(toDouble2(dataList[1]) + toDouble2(iList[3]));
                dataList[2] = noZero2(toDouble2(dataList[2]) + toDouble2(iList[5]));
                dataList[3] = noZero2(toDouble2(dataList[3]) + toDouble2(iList[8]));

                dataList[5] = noZero2(toDouble2(dataList[5]) + toDouble2(rList[2]));
                dataList[6] = noZero2(toDouble2(dataList[6]) + toDouble2(rList[4]));
                dataList[7] = noZero2(toDouble2(dataList[7]) + toDouble2(rList[7]));

                if (db.dataBase.get(curIndex)[2].equals("0.5")) {
                    count05 += toInt(iList[4]);
                } else
                    count07 += toInt(iList[4]);
            } else {
                dataList[0] = noZero2(toDouble2(dataList[0]) +
                        toDouble2(iList[1].split(" / ")[prevOrNextBlend]));
                dataList[1] = noZero2(toDouble2(dataList[1]) +
                        toDouble2(iList[3].split(" / ")[prevOrNextBlend]));
                dataList[2] = noZero2(toDouble2(dataList[2]) +
                        toDouble2(iList[5].split(" / ")[prevOrNextBlend]));
                dataList[3] = noZero2(toDouble2(dataList[3]) +
                        toDouble2(iList[8].split(" / ")[prevOrNextBlend]));

                dataList[5] = noZero2(toDouble2(dataList[5]) +
                        toDouble2(rList[2].split(" / ")[prevOrNextBlend]));
                dataList[6] = noZero2(toDouble2(dataList[6]) +
                        toDouble2(rList[4].split(" / ")[prevOrNextBlend]));
                dataList[7] = noZero2(toDouble2(dataList[7]) +
                        toDouble2(rList[7].split(" / ")[prevOrNextBlend]));

                if (db.dataBase.get(curIndex)[2].equals("0.5")) {
                    count05 += toInt(iList[4].split(" / ")[prevOrNextBlend]);
                } else
                    count07 += toInt(iList[4].split(" / ")[prevOrNextBlend]);

                if (prevOrNextBlend == 0) {
                    blendIndexes[curBlend + 1] = snapIndex;
                    prevOrNextBlend = 1;
                    break;
                } else
                    prevOrNextBlend = 0;
            }
        }
        return dataList;
    }

    @Override
    public void export() {
        StringBuilder infoFile = new StringBuilder(db.readUtils("header"));
        StringBuilder reportFile = new StringBuilder(db.readUtils("header2"));
        ArrayList<String[]> infoData = new ArrayList<String[]>();
        ArrayList<String[]> reportData = new ArrayList<String[]>();
        String[] iList;
        String[] rList;
        int c05;
        int c07;

        for (String _wine : db.winesList.keySet()) {
            initSnap(_wine);
            for (int _blend = 0; _blend < blendCount; _blend++) {
                String[] sum = {"0", "0", "0", "0", "", "0", "0", "0"};
                curBlend = _blend;
                c05 = 0;
                c07 = 0;
                if (curBlend == 0) {
                    prevOrNextBlend = 0;
                }
                infoData.clear();
                reportData.clear();
                for (snapIndex = blendIndexes[curBlend]; snapIndex < indexList.size(); snapIndex++) {
                    curIndex = indexList.get(snapIndex);
                    report.setSnapIndex(snapIndex);
                    iList = super.getDataList();
                    rList = report.getDataList();
                    if (!divBlendFlag) {
                        rList[3] = " ";

                        sum[0] = noZero1(toDouble2(sum[0]) + toDouble2(iList[1]));
                        sum[1] = noZero2(toDouble2(sum[1]) + toDouble2(iList[3]));
                        sum[2] = noZero2(toDouble2(sum[2]) + toDouble2(iList[5]));
                        sum[3] = noZero2(toDouble2(sum[3]) + toDouble2(iList[8]));

                        sum[5] = noZero2(toDouble2(sum[5]) + toDouble2(rList[2]));
                        sum[6] = noZero2(toDouble2(sum[6]) + toDouble2(rList[4]));
                        sum[7] = noZero2(toDouble2(sum[7]) + toDouble2(rList[7]));

                        if (db.dataBase.get(curIndex)[2].equals("0.5")) {
                            c05 += toInt(iList[4]);
                        } else
                            c07 += toInt(iList[4]);

                    } else {

                        if (db.dataBase.get(curIndex)[2].equals("0.5")) {
                            c05 += toInt(iList[4].split(" / ")[prevOrNextBlend]);
                        } else
                            c07 += toInt(iList[4].split(" / ")[prevOrNextBlend]);

                        sum[0] = noZero1(toDouble2(sum[0]) +
                                toDouble2(iList[1].split(" / ")[prevOrNextBlend]));
                        sum[1] = noZero2(toDouble2(sum[1]) +
                                toDouble2(iList[3].split(" / ")[prevOrNextBlend]));
                        sum[2] = noZero2(toDouble2(sum[2]) +
                                toDouble2(iList[5].split(" / ")[prevOrNextBlend]));
                        sum[3] = noZero2(toDouble2(sum[3]) +
                                toDouble2(iList[8].split(" / ")[prevOrNextBlend]));

                        sum[5] = noZero2(toDouble2(sum[5]) +
                                toDouble2(rList[2].split(" / ")[prevOrNextBlend]));
                        sum[6] = noZero2(toDouble2(sum[6]) +
                                toDouble2(rList[4].split(" / ")[prevOrNextBlend]));
                        sum[7] = noZero2(toDouble2(sum[7]) +
                                toDouble2(rList[7].split(" / ")[prevOrNextBlend]));

                        for (int i = 0; i < 10; i++) {
                            if (i == 2) {
                                rList[i] = rList[i].split(" / ")[prevOrNextBlend];
                            } else if (i == 3) {
                                iList[i] = iList[i].split(" / ")[prevOrNextBlend];
                                rList[i] = " ";
                            } else if (i != 0) {
                                iList[i] = iList[i].split(" / ")[prevOrNextBlend];
                                rList[i] = rList[i].split(" / ")[prevOrNextBlend];
                            }
                        }

                        if (prevOrNextBlend == 0) {
                            blendIndexes[curBlend + 1] = snapIndex;
                            prevOrNextBlend = 1;
                            infoData.add(iList);
                            reportData.add(rList);
                            break;
                        } else {
                            prevOrNextBlend = 0;
                        }
                    }
                    infoData.add(iList);
                    reportData.add(rList);
                }
                // Сведения /////////////
                String month = MainActivity.MONTHS[Integer.decode(db.MONTH) - 1].toUpperCase();
				//String month = "СЕНТЯБРЬ";
                infoFile.append("<h2>ООО \"ВЕДРЕНЬ\"</h2>");
                infoFile.append("<h2>ЦЕХ РОЗЛИВА</h2>");
                infoFile.append("<h1 align=\"center\">СВЕДЕНИЯ О РАБОТЕ ЦЕХА РОЗЛИВА ЗА ").append(month).append(" 20").append(MainActivity.YEAR).append("г.").append("</h1>");
                infoFile.append("<h1 align=\"center\"> Наименование вина: " + "<a href=\"\">").append(_wine).append("</a>").append("</h1>");
                infoFile.append("<table align=\"center\"><tr><td rowspan=\"2\">&nbsp;&nbsp;&nbsp;Дата&nbsp;&nbsp;&nbsp;</td><td rowspan=\"2\">Поступило в цех</td><td colspan=\"3\">Розлито в бутылки</td><td rowspan=\"2\">Испра-<br>вимый<br>брак</td><td colspan=\"3\">Потери фактические</td><td rowspan=\"2\">Сдано готов.<br>продукции</td></tr><tr><td>Вместим. бут.</td><td>Количество дал</td><td>Всего бутылок</td><td >До 1 счетчика</td><td>От 1 до 2 счетчика</td><td>Всего потери</td></tr>");
                String s;
                int l;
                if (infoData.size() < 28) {
                    l = 28;
                } else {
                    l = infoData.size();
                }
                for (int i = 0; i < l; i++) {
                    infoFile.append("<tr>\n");
                    for (int k = 0; k < 10; k++) {
                        if (i < infoData.size()) {
                            s = infoData.get(i)[k];
                        } else {
                            s = " ";
                        }
                        infoFile.append("<td>").append(s).append("</td>\n");
                    }
                    infoFile.append("</tr>\n");
                }
                infoFile.append("<tr>\n");
                for (int i = 0; i < 10; i++) {
                    int k;
                    if (i == 1) k = 0;
                    else if (i == 3) k = 1;
                    else if (i == 5) k = 2;
                    else if (i == 8) k = 3;
                    else k = 4;
                    infoFile.append("<td><b>").append(sum[k]).append("</b></td>\n");
                }
                infoFile.append("</tr>\n");
                infoFile.append("</table>\n");
                infoFile.append("<h2><pre>\nНачальник цеха ________________\t\t\t\t0.5 - ").append(c05).append("\t\t0.7 - ").append(c07).append("\t\t\n\n</pre></h2>");
                infoFile.append("<h2><pre>Бухгалтер      ________________\t\t\t\t0.5 - ").append(noZero2(c05 * 0.05)).append("\t\t0.7 - ").append(noZero2(c07 * 0.07)).append("\t\t</pre></h2>\n");
                infoFile.append("<p class=\"page\"></p>\n");

                // Отчеты /////////////
                reportFile.append("<h4>ООО \"ВЕДРЕНЬ\"</h4>");
                reportFile.append("<h3 align=\"center\">ОТЧЕТ О ДВИЖЕНИИ ВИНОПРОДУКЦИИ ЗА ").append(month).append(" 20").append(MainActivity.YEAR).append("г.").append("</h3>");
                reportFile.append("<h3 align=\"center\"> Наименование вина: " + "<a href=\"\">").append(_wine).append("</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Купаж № ____").append("</h3>");
                reportFile.append("<table align=\"center\"><tr><td rowspan=\"3\">&nbsp;&nbsp;&nbsp;Дата&nbsp;&nbsp;&nbsp;</td><td rowspan=\"3\">Остаток на<br>начало дня,<br>дал</td><td colspan=\"3\">ПРИХОД</td><td colspan=\"4\">РАСХОД</td><td rowspan=\"3\">Остаток на<br>конец дня,<br>дал</td></tr><tr><td rowspan=\"2\"></td><td rowspan=\"2\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td rowspan=\"2\"></td><td colspan=\"3\">Потери при фильтрации</td><td rowspan=\"2\">Подано<br>на розлив,<br>дал</td></tr><tr><td>0.09%</td><td>0.33%</td><td>Итого</td></tr>");
                if (reportData.size() < 24) {
                    l = 24;
                } else {
                    l = reportData.size();
                }
                for (int i = 0; i < l; i++) {
                    reportFile.append("<tr>\n");
                    for (int k = 0; k < 10; k++) {
                        if (i < reportData.size()) {
                            s = reportData.get(i)[k];
                        } else {
                            s = " ";
                        }
                        reportFile.append("<td>").append(s).append("</td>\n");
                    }
                    reportFile.append("</tr>\n");
                }
                reportFile.append("<tr>\n");
                for (int i = 0; i < 10; i++) {
                    int k;
                    if (i == 2) k = 5;
                    else if (i == 4) k = 6;
                    else if (i == 7) k = 7;
                    else if (i == 8) k = 0;
                    else k = 4;
                    reportFile.append("<td><b>").append(sum[k]).append("</b></td>\n");
                }
                reportFile.append("</tr>\n");
                reportFile.append("</table>\n");
                reportFile.append("<h3><pre>\nНачальник цеха __________________\n\n</pre></h3>");
                reportFile.append("<h3><pre>Бухгалтер      __________________</pre></h3>\n");
                reportFile.append("<p class=\"page\"></p>\n");
            }
        }
        infoFile.append("</body></html>");
        reportFile.append("</body></html>");
        db.writeExport(infoFile.toString(), "info");
        db.writeExport(reportFile.toString(), "report");
    }
}
