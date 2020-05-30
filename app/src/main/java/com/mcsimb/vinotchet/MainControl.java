package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainControl {

    private FileUtils db;
    private ArrayList<String> days;

    MainControl() {
        db = FileUtils.getInstance();
        days = new ArrayList<String>();
    }

    public ArrayList<String> getDays() {
        return days;
    }

    public void initControl(String m) {
        db.MONTH = m;
        days.clear();
        ArrayList<String[]> sList = db.readFile("sorts" + db.MONTH);
        for (String[] s : sList) {
            ArrayList<String> rest = new ArrayList<String>(Arrays.asList(s).subList(1, s.length));
            db.winesList.put(s[0], rest);
        }
        db.readIcons();
        db.dataBase = db.readFile("data" + db.MONTH);
        for (String[] data : db.dataBase) {
            if (!days.contains(data[0])) {
                days.add(data[0]);
            }
        }
        ArrayList<String[]> counters = db.readFile("counters" + db.MONTH);
        for (String[] s : counters) {
            String[] c = Arrays.asList(s).subList(1, 5).toArray(new String[4]);
            db.counters.put(s[0], c);
        }
        db.resetCounters = db.readFile("resetcounters").get(0);
		ArrayList<String[]> stamps = db.readFile("stamps" + db.MONTH);
		db.stamps05.clear();
		db.stamps07.clear();
        for (String[] s : stamps) {
			if (s[0].equals("0")) {
				db.stamps05.add(s[1]);
			} else {
				db.stamps07.add(s[1]);
			}
		}
    }

    private ArrayList<String[]> getRest() {
        ArrayList<String[]> rest = new ArrayList<String[]>();
        ReportLogControl rControl;
        for (String wine : db.winesList.keySet()) {
            rControl = new ReportLogControl(wine);
            if (!rControl.indexList.isEmpty()) {
                rControl.setSnapIndex(rControl.indexList.size() - 1);
                String endRest = rControl.getDataList()[9].replace(",", ".");
                if (rControl.divBlendFlag) {
                    endRest = endRest.split(" / ")[1].replace(",", ".");
                }
                rest.add(new String[]{wine, endRest});
            } else {
                rest.add(new String[]{wine, db.winesList.get(wine).get(0)});
            }
        }
        return rest;
    }

    public ArrayList<Product> dayProducts(String day) {
        ArrayList<Product> products = new ArrayList<Product>();
        StampsCounter counter = new StampsCounter();
        List<String[]> lst = counter.getDayStamps(day);
        int i = 0;
        for (String[] data : db.dataBase) {
            if (data[0].equals(day)) {
                products.add(new Product(data[1] + "  " + data[2], data[3], data[4],
										 lst.get(i)[0], lst.get(i)[1],
										 db.sortsIcons.get(data[1])));
                i++;
            }
        }
        return products;
    }

    public boolean addData(String day, String sort, String vol, String count1, String count2) {
        if (!days.contains(day)) {
            days.add(day);
        }
        db.dataBase.add(new String[]{day, sort, vol, count1, count2});
        db.writeFile("data" + db.MONTH, db.dataBase);
        ArrayList<String> blends = db.winesList.get(sort);
        if (Double.parseDouble(blends.get(blends.size() - 1)) == 0) {
            return true;
        }
        ReportLogControl rControl = new ReportLogControl(sort);
        rControl.setSnapIndex(rControl.indexList.size() - 1);
        rControl.isDivBlend();
        return rControl.divBlendFlag;
    }

	public boolean checkStamps(String vol) {
		int sum = 0;
		String startStamp = vol.equals("0.5") ? db.stamps05.get(0) : db.stamps07.get(0);
		int startRange = Integer.decode(startStamp.substring(0, 3).replaceAll("^0*", ""));
		int startNumber = Integer.decode(startStamp.substring(9).replaceAll("^0*", "")) % 500;
		int sumStamps = (startRange / 30 * 30 + 30 - startRange) * 500 + 500 - (startNumber % 500) + 1;
		for (int i = 1; i < (vol.equals("0.5") ? db.stamps05.size() : db.stamps07.size()); i++) {
			sumStamps += 15000;
		}
		for (String[] data : db.dataBase) {
			if (data[2].equals(vol)) {
				sum += Integer.decode(data[4]);
			}
		}
		if (sum > sumStamps) {
			return true;
		}
		return false;
	}

	public void addStampsRange(String range, String number, String vol) {
		ArrayList<String[]> stamps = new ArrayList<>();
		for (String s : db.stamps05) {
			stamps.add(new String[] {"0", s});
		}
		for (String s : db.stamps07) {
			stamps.add(new String[] {"1", s});
		}
		if (vol.equals("0.5")) {
			db.stamps05.add(range + number);
			stamps.add(new String[] {"0", range + number});
		} else {
			db.stamps07.add(range + number);
			stamps.add(new String[] {"1", range + number});
		}
		db.writeFile("stamps" + db.MONTH, stamps);
	}
	
    public void removeData(String day, int pos) {
        int index = -1;
        for (int i = 0; i < db.dataBase.size(); i++) {
            if (db.dataBase.get(i)[0].equals(day)) {
                index = i + pos;
                break;
            }
        }
        try {
            db.dataBase.remove(index);
        }
		catch (Exception e) {
            e.printStackTrace();
        }
        db.writeFile("data" + db.MONTH, db.dataBase);
    }

    public void addBlend(String sort, String newBlend) {
        ArrayList<String> blends = db.winesList.get(sort);
        if (Double.parseDouble(blends.get(blends.size() - 1)) == 0) {
            blends.set(blends.size() - 1, newBlend);
        } else {
            blends.add(newBlend);
        }
        db.winesList.put(sort, blends);
        ArrayList<String[]> sorts = new ArrayList<String[]>();
        for (String s : db.winesList.keySet()) {
            ArrayList<String> b = new ArrayList<String>();
            b.add(s);
            b.addAll(db.winesList.get(s));
            sorts.add(b.toArray(new String[b.size()]));
        }
        db.writeFile("sorts" + db.MONTH, sorts);
    }

    @SuppressLint("DefaultLocale")
    public String[] statistic() {
        int count05 = 0;
        int count07 = 0;
        ArrayList<String[]> _rest = getRest();
        for (String[] s : db.dataBase) {
            if (s[2].equals("0.5")) {
                count05 += Integer.decode(s[4]);
            } else {
                count07 += Integer.decode(s[4]);
            }
        }
        StringBuilder rest = new StringBuilder();
        for (String[] str : _rest) {
            for (String s : db.getSorts(false)) {
                if (str[0].equals(s)) {
                    rest.append(str[0]).append("   -  ").append(str[1]).append("\n");
                }
            }
        }
        //TODO : string concat
        return new String[]{"Бут. 0,5    -  " + String.valueOf(count05) + "\n" +
			"Бут. 0,7    -  " + String.valueOf(count07) + "\n" +
			"Всего, дал  -  " + String.format("%.2f", Math.round((count05 * 0.05 + count07 * 0.07) * 100.0) / 100.0),
			rest.toString()};
    }

    public String[] material(int start, int end) {
        String[] aList = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "", "0", "0", ""};
        String v05 = " 0,5";
        String v07 = " 0,7";
        int c05 = 0;
        int c07 = 0;
        Map<String, Integer> labels = new LinkedHashMap<String, Integer>();
        for (String s : db.winesList.keySet()) {
            labels.put(s + v05, 0);
            labels.put(s + v07, 0);
        }
        for (String[] s : db.dataBase) {
            if (Integer.decode(s[0]) >= start && Integer.decode(s[0]) <= end) {
                if (s[2].equals("0.5")) {
                    int c = Integer.decode(s[4]);
                    c05 += c;
                    labels.put(s[1] + v05, labels.get(s[1] + v05) + c);
                } else {
                    int c = Integer.decode(s[4]);
                    c07 += c;
                    labels.put(s[1] + v07, labels.get(s[1] + v07) + c);
                }
            }
        }

        aList[0] = toStr(c05 * 1.025); //alu
        aList[1] = toStr(c07 * 1.015); //пэт
        aList[2] = toStr((c05 + c07) * 1.012); // тук
        aList[3] = toStr((c05 + c07) * 1.008); //этикетки
        aList[4] = toStr(c05 * 0.05 * 0.0081 + c07 * 0.07 * 0.0057); //клей 5210
        aList[5] = toStr(c05 * 0.05 * 0.004 + c07 * 0.07 * 0.003); //клей 2400
        aList[6] = toStr((c05 + c07) * 0.003); //дегмос
        aList[7] = toStr((c05 + c07) * 0.0007); //тринатр
        aList[8] = toStr((c05 * 0.05 + c07 * 0.07) * 0.021); //ФК
        aList[10] = toStr(c05); //акц .5
        aList[11] = toStr(c07); //акц .7

        String[] bList = new String[labels.size()];
        int i = 0;
        for (String s : labels.keySet()) {
            bList[i] = toStr(labels.get(s) * 1.008);
            ++i;
        }
        int aLen = aList.length;
        int bLen = bList.length;
        String[] dataList = new String[aLen + bLen];
        System.arraycopy(aList, 0, dataList, 0, aLen);
        System.arraycopy(bList, 0, dataList, aLen, bLen);
        return dataList;
    }

    public String newMonth() {
        AlcoholLogControl alcohol = new AlcoholLogControl(0);
        ArrayList<String[]> rest;
        ArrayList<String[]> counters = new ArrayList<String[]>();
        ArrayList<String[]> stamps = new ArrayList<String[]>();
        String[] exMonths = db.existingMonths();
        String m = String.valueOf(Integer.decode(exMonths[exMonths.length - 1]) + 1);
        if (Integer.decode(m) > 12) {
            m = "01";
        }
        m = m.length() == 1 ? "0" + m : m;
        if (db.fileExists("sorts" + m)) {
            return "00";
        } else {
            rest = getRest();
        }

        for (String s : db.counters.keySet()) {
            String[] _c = new String[4];
            boolean v05 = false;
            boolean v07 = false;
            for (int i = db.dataBase.size() - 1; i > -1; i--) {
                if (db.dataBase.get(i)[1].equals(s)) {
                    if (db.dataBase.get(i)[2].equals("0.5") && !v05) {
                        alcohol.curIndex = i;
                        alcohol.wine = db.dataBase.get(alcohol.curIndex)[1];
                        alcohol.vol = db.dataBase.get(alcohol.curIndex)[2];
                        alcohol.sumFlag = false;
                        String[] data = alcohol.getEndCounters();
                        _c[0] = data[0];
                        _c[1] = data[1];
                        v05 = true;
                    } else if (db.dataBase.get(i)[2].equals("0.7") && !v07) {
                        alcohol.curIndex = i;
                        alcohol.wine = db.dataBase.get(alcohol.curIndex)[1];
                        alcohol.vol = db.dataBase.get(alcohol.curIndex)[2];
                        alcohol.sumFlag = false;
                        String[] data = alcohol.getEndCounters();
                        _c[2] = data[0];
                        _c[3] = data[1];
                        v07 = true;
                    }
                }
            }
            if (v05 && v07) {
                counters.add(new String[]{s, _c[0], _c[1], _c[2], _c[3]});
            } else if (v05 && !v07) {
                counters.add(new String[]{s, _c[0], _c[1], db.counters.get(s)[2], db.counters.get(s)[3]});
            } else if (!v05 && v07) {
                counters.add(new String[]{s, db.counters.get(s)[0], db.counters.get(s)[1], _c[2], _c[3]});
            } else {
                counters.add(new String[]{s, db.counters.get(s)[0], db.counters.get(s)[1], db.counters.get(s)[2], db.counters.get(s)[3]});
            }
        }

        /*int s5 = Integer.decode(db.stamps05.get(1));
        int s7 = Integer.decode(db.stamps07.get(1));
        for (int i = 0; i < db.dataBase.size(); i++) {
            if (db.dataBase.get(i)[2].equals("0.5")) {
                s5 -= Integer.decode(db.dataBase.get(i)[4]);
                if (s5 < 0) {
                    s5 += 15000;
                }
            } else {
                s7 -= Integer.decode(db.dataBase.get(i)[4]);
                if (s7 < 0) {
                    s7 += 15000;
                }
            }
        }
        stamps.add(new String[]{String.valueOf(s5)});
        stamps.add(new String[]{String.valueOf(s7)});*/

        db.sortsIcons.clear();
        db.dataBase.clear();
        db.winesList.clear();
        db.counters.clear();
        db.stamps05.clear();
		db.stamps07.clear();
        days.clear();

        db.writeFile("sorts" + m, rest);
        db.writeFile("data" + m, db.dataBase);
        db.writeFile("counters" + m, counters);
        db.writeFile("stamps" + m, stamps);
        return m;
    }

    private String toStr(double d) {
        return String.valueOf((int) Math.floor(d));
    }
}

