package worker.seedmorn.com.bletest;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by ZAB on 2017/10/27.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleService extends Service {

    private final static String TAG = "MainActivity";

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGattCharacteristic characteristicWrite;
    private UUID serviceUUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    private UUID charatUUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    private BluetoothLeScanner scanner;
    private MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        binder = new MyBinder();
        Log.d(TAG, "onCreate: 服务创建成功");
        initialize();
    }

    /**
     * 蓝牙统筹对象
     */

    private BluetoothGatt bluetoothGatt;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,
                "------------>");
        initialize();
        bluetoothAdapter.enable();//打开蓝牙
        return binder;
    }


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult: 搜到设备" + result.getDevice().getName() + "--" + result.getDevice().getAddress());
            try {
                if (result.getDevice().getAddress().equals("06:22:00:00:00:02")&&result.getDevice().getName().equals("ELB")) {
                    Log.d(TAG, "onLeScan: 搜索到设备" + result.getDevice().getAddress());
                    bluetoothDevice = result.getDevice();
                    scanner.stopScan(scanCallback);
                    Log.e(TAG, "停止扫描,开始连接设备...");
                    if (bluetoothGatt == null) {
                        bluetoothGatt = bluetoothDevice.connectGatt(BleService.this, false, gattCallback);
                    } else {
                        bluetoothGatt.connect();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "bluetoothGatt: 蓝牙正在连接。。。" + e);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public class MyBinder extends Binder {
        public void startScan() {

            scanner.startScan(scanCallback);
//            bluetoothAdapter.startLeScan(leScanCallback);
        }

        public void stop() {
            scanner.stopScan(scanCallback);
        }

        public void send() {
            startOrder();
        }

        public boolean setNotify(boolean enable) {
            boolean s = bluetoothGatt.setCharacteristicNotification(characteristicWrite, true);
            Log.d(TAG, "setNotify: 通知开启"+s);
            if (s) {
                List<BluetoothGattDescriptor> descriptorList = characteristicWrite.getDescriptors();
                if (descriptorList != null && descriptorList.size() > 0) {
                    for (BluetoothGattDescriptor descriptor : descriptorList) {
                  descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                      bluetoothGatt.writeDescriptor(descriptor);
                    }
                }
            }
            return s;
        }



        public void readValues() {

            boolean s = bluetoothGatt.readCharacteristic(characteristicWrite);
            if (s) {
                Log.d(TAG, "booleanWrite: 读取成功" + bytesToHex(characteristicWrite.getValue()));
            } else {
                Log.d(TAG, "booleanWrite: 读失败");

            }
        }
    }

//    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            try {
//                if (device.getAddress().equals("CA63F169D0A3")) {
//                    Log.d(TAG, "onLeScan: 搜索到设备" + device.getAddress());
//                    bluetoothDevice = device;
//                    bluetoothAdapter.stopLeScan(leScanCallback);//停止扫描
//                    Log.e(TAG, "停止扫描,开始连接设备...");
//                    if (bluetoothGatt == null) {
//                        bluetoothGatt = bluetoothDevice.connectGatt(BleService.this, false, gattCallback);
//                    } else {
//                        bluetoothGatt.connect();
//                    }
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "bluetoothGatt: 蓝牙正在连接。。。" + e);
//            }
//        }
//    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange: 蓝牙连接成功" + gatt.getDevice().getAddress() + "----------");
                Log.d(TAG, "onConnectionStateChange: 准备开启发现");
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onLeScan: 连接已断开");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered: 找到服务,打印所有服务");
            List<BluetoothGattService> serviceLis = gatt.getServices();
            for (int i = 0; i < serviceLis.size(); i++) {
                Log.d(TAG, "---Service_UUID   --->" + serviceLis.get(i).getUuid());
                List<BluetoothGattCharacteristic> characterList = serviceLis.get(i).getCharacteristics();
                for (int j = 0; j < characterList.size(); j++) {
                    Log.d(TAG, "--->----特征：" + characterList.get(j).toString());
                }
            }

            BluetoothGattService service = bluetoothGatt.getService(serviceUUID);
            if (service == null) {
                Log.d(TAG, "onServicesDiscovered: service=null error");
                Log.d(TAG, "onServicesDiscovered: 没有服务");
                return;
            }
            characteristicWrite = service.getCharacteristic(charatUUID);
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead:发现特征~~~~~~~~" + bytesToHex(characteristic.getValue()));
        }

        /**
         * 蓝牙返回指令回调
         * @param gatt
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite:  蓝牙指令回调" + bytesToHex(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharacteristicChanged:通知回调"+bytesToHex(characteristic.getValue()));
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead:读回调");

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorWrite:写回调");
        }

    };

    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.i(TAG,
                        "538 初始化蓝牙适配器失败 原因： BluetoothManager为空  Unable to initialize BluetoothManager.");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.enable();//打开蓝牙

        if (bluetoothAdapter == null) {
            Log.i(TAG,
                    "545 初始化蓝牙适配器失败 原因： BluetoothAdapter为空 Unable to obtain a BluetoothAdapter.");
            return false;
        }
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        return true;
    }

    // byte数组转换成16进制字符串
    private String bytesToHex(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < src.length; i++) {
            // 之所以用byte和0xff相与，是因为int是32位，与0xff相与后就舍弃前面的24位，只保留后8位
            String str = Integer.toHexString(src[i] & 0xff);
            if (str.length() < 2) { // 不足两位要补0
                stringBuilder.append(0);
            }
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    private void startOrder() {
        byte[] startOrder = {(byte) 0xAA, (byte) 0x55, (byte) 0x05, (byte) 0x55, (byte) 0x05 + (byte) 0x55};
        writeData(startOrder);
    }

    private void writeData(byte[] bytes) {
        if (bytes != null && characteristicWrite != null) {
            characteristicWrite.setValue(bytes);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean result = bluetoothGatt
                    .writeCharacteristic(characteristicWrite);
            if(result){
                Log.d(TAG, "写成功: ");
            }else {
                Log.d(TAG, "写失败" + result);
            }
        }
    }
}

