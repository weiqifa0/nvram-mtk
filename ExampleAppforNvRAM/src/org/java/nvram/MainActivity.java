package com.java.nvram;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.util.HexDump;
import java.util.ArrayList;

// Add import statement for nvram
import vendor.mediatek.hardware.nvram.V1_1.INvram;

public class MainActivity extends Activity {
    private static final String TAG = "@INVRAM";

    private static final String WIFI_FILENAME =           "WIFI";
    private static final String BT_FILENAME =             "BT_Addr";
    private static final String PRODUCT_INFO_FILENAME =   "PRODUCT_INFO";
	private static final String AP_CFG_RDCL_HWMON_PS_FILENAME =   "PRODUCT_INFO";//AP_CFG_RDCL_HWMON_PS_LID

    private static final int MAC_ADDRESS_OFFSET = 4;
    private static final int MAC_ADDRESS_DIGITS = 6;

	private static final int BARCODE_OFFSET = 0;
    private static final int BARCODE_LEN = 64;

	private static final int HWMON_OFFSET = 0;//18
	private static final int HWMON_LEN = 12;  //4

    private TextView mMACAddr;
	private TextView mSerialNo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		String hwmon_ps;
		
        setContentView(R.layout.main);

		Log.d(TAG, "-->onCreate===========================");

		Log.d(TAG, "-->get_hwmon_ps===========================");
		hwmon_ps = getHwmonPs();
		Log.d(TAG, "-->get_hwmon_ps:"+hwmon_ps);
		Log.d(TAG, "-->set_hwmon_ps");
		setHwmonPs(0x11224455);
		hwmon_ps = getHwmonPs();
		Log.d(TAG, "-->get_hwmon_ps:"+hwmon_ps);
    }

 

	private String setHwmonPs(int value) {
	    int i = 0;
        byte[] macAddr = new byte[HWMON_LEN];
        String buff = null;
		/*
        macAddr[0] = 0x1c;
        macAddr[1] = 0x11;
        macAddr[2] = 0x22;
        macAddr[3] = 0x33;
        macAddr[4] = 0x44;
        macAddr[5] = 0x56;
        */
		macAddr[0 + HWMON_OFFSET] = (byte)((value >> 24 )&0xFF);
		macAddr[1 + HWMON_OFFSET] = (byte)((value >> 16 )&0xFF);
		macAddr[2 + HWMON_OFFSET] = (byte)((value >> 8  )&0xFF);
		macAddr[3 + HWMON_OFFSET] = (byte)((value >> 0  )&0xFF);

        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return "";
            } else {
                Log.i(TAG, "NvRAMAgent running");
            }

            try {
                buff = agent.readFileByName(
                           AP_CFG_RDCL_HWMON_PS_FILENAME, HWMON_OFFSET + HWMON_LEN);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

			Log.i(TAG, "Raw data length:" + buff.length());
            Log.i(TAG, "Raw data:" + buff);
            if (buff.length() < 2 * (HWMON_OFFSET + HWMON_LEN)) {
                Log.e(TAG, "The foramt of NVRAM is not correct");
                return "";
            }
            // Remove \0 in the end
            byte[] buffArr = HexDump.hexStringToByteArray(
                                 buff.substring(0, buff.length() - 1));

			Log.i(TAG, "buffArr length:" + buffArr.length);

            for (i = 0; i < HWMON_LEN; i ++) {
                buffArr[i + HWMON_OFFSET] = macAddr[i];
            }

            ArrayList<Byte> dataArray = new ArrayList<Byte>(
                HWMON_OFFSET + HWMON_LEN);

            for (i = 0; i < HWMON_OFFSET + HWMON_LEN; i++) {
                dataArray.add(i, new Byte(buffArr[i]));
            }

            int flag = 0;
            try {
                flag = agent.writeFileByNamevec(AP_CFG_RDCL_HWMON_PS_FILENAME,
                                                HWMON_OFFSET + HWMON_LEN, dataArray);
				Log.e(TAG, "flag=" + flag);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
			
			boolean ok = false;
			try {
                ok = agent.BackupToBinRegion_All();
				Log.e(TAG, "backupToBinRegionAll ok?" + ok);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
		return "";
	/*
			int i = 0;
			String buff = null;
			StringBuffer nvramBuf = new StringBuffer();
	
			try {
				INvram agent = INvram.getService();
				if (agent == null) {
					Log.e(TAG, "NvRAMAgent is null");
					return "";
				}
	
				try {
					buff = agent.readFileByName(
							   AP_CFG_RDCL_HWMON_PS_FILENAME, HWMON_OFFSET + HWMON_LEN);
				} catch (Exception e) {
					e.printStackTrace();
					return "";
				}
				Log.i(TAG, "Raw data length:" + buff.length());
				Log.i(TAG, "Raw data:" + buff);
				if (buff.length() < 2 * (HWMON_OFFSET + HWMON_LEN)) {
					Log.e(TAG, "The foramt of NVRAM is not correct");
					return "";
				}
				// Remove the \0 special character.
				int serLen = buff.length() - 1;
				for (i = HWMON_OFFSET * 2; i < serLen; i += 2) {
					if ((i + 2) < serLen) {
						nvramBuf.append(buff.substring(i, i + 2));
						nvramBuf.append(" ");
					} else {
						nvramBuf.append(buff.substring(i));
					}
				}
				Log.d(TAG, "buff:" + nvramBuf.toString());
	
				// Remove \0 in the end
				byte[] buffArr = HexDump.hexStringToByteArray(
									 buff.substring(0, buff.length() - 1));
	
				Log.i(TAG, "buffArr length:" + buffArr.length);
	
				buffArr[0 + HWMON_OFFSET] = (byte)((value >> 24 )&0xFF);
				buffArr[1 + HWMON_OFFSET] = (byte)((value >> 16 )&0xFF);
				buffArr[2 + HWMON_OFFSET] = (byte)((value >> 8  )&0xFF);
				buffArr[3 + HWMON_OFFSET] = (byte)((value >> 0  )&0xFF);
	
				ArrayList<Byte> dataArray = new ArrayList<Byte>(
					HWMON_OFFSET + HWMON_LEN);
	
				for (i = 0; i < HWMON_OFFSET + HWMON_LEN; i++) {
					dataArray.add(i, new Byte(buffArr[i]));
				}
	
				int flag = 0;
				try {
					flag = agent.writeFileByNamevec(AP_CFG_RDCL_HWMON_PS_FILENAME,
													HWMON_OFFSET + HWMON_LEN, dataArray);
					Log.e(TAG, "flag=" + flag);
				} catch (Exception e) {
					e.printStackTrace();
					return "";
				}
	
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
	
			return nvramBuf.toString();
	*/
	}


	//AP_CFG_RDCL_HWMON_PS_FILENAME
	private String getHwmonPs(){
	    int i = 0;
        String buff = null;
        StringBuffer nvramBuf = new StringBuffer();

        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return "";
            }

            try {
                buff = agent.readFileByName(
                           AP_CFG_RDCL_HWMON_PS_FILENAME, HWMON_OFFSET + HWMON_LEN);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            Log.i(TAG, "Raw data length:" + buff.length());
            Log.i(TAG, "Raw data:" + buff);
            if (buff.length() < 2 * (HWMON_OFFSET + HWMON_LEN)) {
                Log.e(TAG, "The foramt of NVRAM is not correct");
                return "";
            }
            // Remove the \0 special character.
            int macLen = buff.length() - 1;
            for (i = HWMON_OFFSET * 2; i < macLen; i += 2) {
                if ((i + 2) < macLen) {
                    nvramBuf.append(buff.substring(i, i + 2));
                    nvramBuf.append(":");
                } else {
                    nvramBuf.append(buff.substring(i));
                }
            }
            Log.d(TAG, "buff:" + nvramBuf.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return nvramBuf.toString();
		/*
     	int i = 0;
        String buff = null;
        StringBuffer nvramBuf = new StringBuffer();

        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return "";
            }

            try {
                buff = agent.readFileByName(
                           AP_CFG_RDCL_HWMON_PS_FILENAME, HWMON_OFFSET + HWMON_LEN);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            Log.i(TAG, "Raw data length:" + buff.length());
            Log.i(TAG, "Raw data:" + buff);
            if (buff.length() < 2 * (HWMON_OFFSET + HWMON_LEN)) {
                Log.e(TAG, "The foramt of NVRAM is not correct");
                return "";
            }
            // Remove the \0 special character.
            int serLen = buff.length() - 1;
            for (i = BARCODE_OFFSET * 2; i < serLen; i += 2) {
                if ((i + 2) < serLen) {
                    nvramBuf.append(buff.substring(i, i + 2));
                    nvramBuf.append(" ");
                } else {
                    nvramBuf.append(buff.substring(i));
                }
            }
            Log.d(TAG, "buff:" + nvramBuf.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return nvramBuf.toString();
        */
	}

}
/*
	private String getSerialNoFromNvram() {
        int i = 0;
        String buff = null;
        StringBuffer nvramBuf = new StringBuffer();

        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return "";
            }

            try {
                buff = agent.readFileByName(
                           PRODUCT_INFO_FILENAME, BARCODE_OFFSET + BARCODE_LEN);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            Log.i(TAG, "Raw data length:" + buff.length());
            Log.i(TAG, "Raw data:" + buff);
            if (buff.length() < 2 * (BARCODE_OFFSET + BARCODE_LEN)) {
                Log.e(TAG, "The foramt of NVRAM is not correct");
                return "";
            }
            // Remove the \0 special character.
            int serLen = buff.length() - 1;
            for (i = BARCODE_OFFSET * 2; i < serLen; i += 2) {
                if ((i + 2) < serLen) {
                    nvramBuf.append(buff.substring(i, i + 2));
                    nvramBuf.append(" ");
                } else {
                    nvramBuf.append(buff.substring(i));
                }
            }
            Log.d(TAG, "buff:" + nvramBuf.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return nvramBuf.toString();
    }

	private String setSerialNoToNvram() {
        int i = 0;
        String buff = null;
        StringBuffer nvramBuf = new StringBuffer();

        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return "";
            }

            try {
                buff = agent.readFileByName(
                           PRODUCT_INFO_FILENAME, BARCODE_OFFSET + BARCODE_LEN);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            Log.i(TAG, "Raw data length:" + buff.length());
            Log.i(TAG, "Raw data:" + buff);
            if (buff.length() < 2 * (BARCODE_OFFSET + BARCODE_LEN)) {
                Log.e(TAG, "The foramt of NVRAM is not correct");
                return "";
            }
            // Remove the \0 special character.
            int serLen = buff.length() - 1;
            for (i = BARCODE_OFFSET * 2; i < serLen; i += 2) {
                if ((i + 2) < serLen) {
                    nvramBuf.append(buff.substring(i, i + 2));
                    nvramBuf.append(" ");
                } else {
                    nvramBuf.append(buff.substring(i));
                }
            }
            Log.d(TAG, "buff:" + nvramBuf.toString());

			// Remove \0 in the end
            byte[] buffArr = HexDump.hexStringToByteArray(
                                 buff.substring(0, buff.length() - 1));

			Log.i(TAG, "buffArr length:" + buffArr.length);

            buffArr[0 + BARCODE_OFFSET] = 0x68; // 'h'
            buffArr[1 + BARCODE_OFFSET] = 0x65; // 'e'
            buffArr[2+ BARCODE_OFFSET] = 0x6c; // 'l'
            buffArr[3+ BARCODE_OFFSET] = 0x6c; // 'l'
            buffArr[4+ BARCODE_OFFSET] = 0x6f; // 'o'
            buffArr[5+ BARCODE_OFFSET] = 0x41; // 'A'

            ArrayList<Byte> dataArray = new ArrayList<Byte>(
                BARCODE_OFFSET + BARCODE_LEN);

            for (i = 0; i < BARCODE_OFFSET + BARCODE_LEN; i++) {
                dataArray.add(i, new Byte(buffArr[i]));
            }

            int flag = 0;
            try {
                flag = agent.writeFileByNamevec(PRODUCT_INFO_FILENAME,
                                                BARCODE_OFFSET + BARCODE_LEN, dataArray);
				Log.e(TAG, "flag=" + flag);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return nvramBuf.toString();
    }

   private void updateMacAddr() {
        int i = 0;
        byte[] macAddr = new byte[MAC_ADDRESS_DIGITS];
        String buff = null;

        macAddr[0] = 0x1c;
        macAddr[1] = 0x11;
        macAddr[2] = 0x22;
        macAddr[3] = 0x33;
        macAddr[4] = 0x44;
        macAddr[5] = 0x56;

        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return;
            } else {
                Log.i(TAG, "NvRAMAgent running");
            }

            try {
                buff = agent.readFileByName(
                           WIFI_FILENAME, MAC_ADDRESS_OFFSET + MAC_ADDRESS_DIGITS);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

			Log.i(TAG, "Raw data length:" + buff.length());
            Log.i(TAG, "Raw data:" + buff);
            if (buff.length() < 2 * (MAC_ADDRESS_OFFSET + MAC_ADDRESS_DIGITS)) {
                Log.e(TAG, "The foramt of NVRAM is not correct");
                return;
            }
            // Remove \0 in the end
            byte[] buffArr = HexDump.hexStringToByteArray(
                                 buff.substring(0, buff.length() - 1));

			Log.i(TAG, "buffArr length:" + buffArr.length);

            for (i = 0; i < MAC_ADDRESS_DIGITS; i ++) {
                buffArr[i + MAC_ADDRESS_OFFSET] = macAddr[i];
            }

            ArrayList<Byte> dataArray = new ArrayList<Byte>(
                MAC_ADDRESS_OFFSET + MAC_ADDRESS_DIGITS);

            for (i = 0; i < MAC_ADDRESS_OFFSET + MAC_ADDRESS_DIGITS; i++) {
                dataArray.add(i, new Byte(buffArr[i]));
            }

            int flag = 0;
            try {
                flag = agent.writeFileByNamevec(WIFI_FILENAME,
                                                MAC_ADDRESS_OFFSET + MAC_ADDRESS_DIGITS, dataArray);
				Log.e(TAG, "flag=" + flag);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
			
			boolean ok = false;
			try {
                ok = agent.BackupToBinRegion_All();
				Log.e(TAG, "backupToBinRegionAll ok?" + ok);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private String getMacAddrFromNvram() {
        int i = 0;
        String buff = null;
        StringBuffer nvramBuf = new StringBuffer();

        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return "";
            }

            try {
                buff = agent.readFileByName(
                           WIFI_FILENAME, MAC_ADDRESS_OFFSET + MAC_ADDRESS_DIGITS);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            Log.i(TAG, "Raw data length:" + buff.length());
            Log.i(TAG, "Raw data:" + buff);
            if (buff.length() < 2 * (MAC_ADDRESS_OFFSET + MAC_ADDRESS_DIGITS)) {
                Log.e(TAG, "The foramt of NVRAM is not correct");
                return "";
            }
            // Remove the \0 special character.
            int macLen = buff.length() - 1;
            for (i = MAC_ADDRESS_OFFSET * 2; i < macLen; i += 2) {
                if ((i + 2) < macLen) {
                    nvramBuf.append(buff.substring(i, i + 2));
                    nvramBuf.append(":");
                } else {
                    nvramBuf.append(buff.substring(i));
                }
            }
            Log.d(TAG, "buff:" + nvramBuf.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return nvramBuf.toString();
    }    
}*/