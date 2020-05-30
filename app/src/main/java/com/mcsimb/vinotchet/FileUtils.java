package com.mcsimb.vinotchet;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("FieldCanBeLocal")
public class FileUtils {

    private static FileUtils instance;

    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    @SuppressWarnings("WeakerAccess")
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final String DIR_SD = "VinOtchet";
    private final String EXT_TXT = ".txt";
    private final String EXT_PNG = ".png";

    public Map<String, ArrayList<String>> winesList = new TreeMap<String, ArrayList<String>>();
    public Map<String, String[]> counters = new TreeMap<String, String[]>();
    public Map<String, Bitmap> sortsIcons = new TreeMap<String, Bitmap>();
    public String[] resetCounters;
    public ArrayList<String> stamps05 = new ArrayList<String>();
	public ArrayList<String> stamps07 = new ArrayList<String>();
    public ArrayList<String[]> dataBase;
    public String MONTH = "00";

    private String[] usedSorts;
    private BufferedWriter bw;
    private BufferedReader br;
    private FileWriter fw;
    private FileReader fr;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private File sdPath;

    public static FileUtils getInstance() {
        if (instance == null) {
            instance = new FileUtils();
        }
        return instance;
    }

    public boolean verifyStoragePermissions(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Check if we have write permission
            int permission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                activity.requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                return false;
            }
        }
        return true;
    }

    public boolean mediaMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public boolean pathExists() {
        Boolean flag = false;
        sdPath = Environment.getExternalStorageDirectory();
        try {
            sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
            flag = sdPath.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public boolean fileExists(String fName) {
        Boolean flag = false;
        try {
            flag = new File(sdPath, fName + EXT_TXT).exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public void setUsedSorts(String[] sorts) {
        usedSorts = sorts;
    }

    public String[] getSorts(boolean allSorts) {
        String[] all = winesList.keySet().toArray(new String[winesList.size()]);
        if (allSorts) {
            return all;
        } else {
            ArrayList<String> used = new ArrayList<String>();
            for (int i = 0; i < usedSorts.length; i++) {
                if (usedSorts[i].equals("1")) {
                    used.add(all[i]);
                }
            }
            return used.toArray(new String[used.size()]);
        }
    }

    public String[] existingMonths() {
        ArrayList<String> months = new ArrayList<String>();
        String[] files = sdPath.list();
        Arrays.sort(files);
        for (String s : files) {
            if (s.startsWith("data")) {
                if (s.startsWith("data0")) {
                    months.add(s.substring(5, 6));
                } else
                    months.add(s.substring(4, 6));
            }
        }
        return months.toArray(new String[months.size()]);
    }

    public void writeFile(String fName, ArrayList<String[]> fData) {
        try {
            File sdFile = new File(sdPath, fName + EXT_TXT);
            fw = new FileWriter(sdFile);
            bw = new BufferedWriter(fw);
            StringBuilder data = new StringBuilder();
            for (String[] d : fData) {
                for (int i = 0; i < d.length; i++) {
                    if (i != d.length - 1) {
                        data.append(d[i]).append("; ");
                    } else
                        data.append(d[i]).append("\n");
                }
            }
            bw.write(data.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.flush();
                bw.close();
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String[]> readFile(String fName) {
        ArrayList<String[]> data = new ArrayList<String[]>();
        try {
            File sdFile = new File(sdPath, fName + EXT_TXT);
            fr = new FileReader(sdFile);
            br = new BufferedReader(fr);
            String str;
            while ((str = br.readLine()) != null) {
                data.add(str.split("; "));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public void readIcons() {
        sortsIcons.clear();
        for (String sort : winesList.keySet()) {
            Bitmap bitmap = readIcon(sort + EXT_PNG);
            sortsIcons.put(sort, bitmap);
        }
    }

    private Bitmap readIcon(String fName) {
        Bitmap icon = null;
        try {
            File sdFile = new File(sdPath, fName);
            fis = new FileInputStream(sdFile);
            bis = new BufferedInputStream(fis);
            icon = BitmapFactory.decodeStream(bis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                bis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return icon;
    }

    public String readUtils(String fileName) {
        StringBuilder data = new StringBuilder();
        try {
            File sdFile = new File(sdPath + "/utils", fileName);
            fr = new FileReader(sdFile);
            br = new BufferedReader(fr);
            String str;
            while ((str = br.readLine()) != null) {
                data.append(str).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data.toString();
    }

    public void writeExport(String data, String fileName) {
        try {
            File sdFile = new File(sdPath + "/export", fileName + ".html");
            fw = new FileWriter(sdFile);
            bw = new BufferedWriter(fw);
            bw.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.flush();
                bw.close();
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
