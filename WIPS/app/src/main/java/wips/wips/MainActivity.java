//This application was developed by Michael Richard Mallar for his Graduate Research Thesis

/* The Wifi Indoor Positioning System (WIPS) android application's developmental purpose is to
 * utilize the built-in hardware to attain the unique MAC address, decibel levels, and frequencies
 * for all wireless access points that can be heard from the mobile phone. This information is
 * passed through a neural network, which will learn your location through training of the neural
 * net.
 */

/* This file, MainActivity.java is used to handle the applications UI , and for the acquisition of
 * acquisition of data that is to be passed on to the Perceptron.java class
 */

package wips.wips;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity{
    //Initialization of global variables
    private Button scanButton;
    private Button endButton;
    private Button trainButton;
    private Button locateButton;
    private TextView txtView;
    List<ScanResult> result;
    List<ScanResult> trainResult;

    double[][] roomNum = new double[1][1];
    String roomNumber;
    boolean lock = false;

    //This function is called upon startup of the application
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.activity_main);

        //Initialization of all components of UI
        txtView = (TextView) findViewById(R.id.textView2);
        scanButton = (Button) findViewById(R.id.startButton);
        endButton = (Button) findViewById(R.id.endButton);
        trainButton = (Button) findViewById(R.id.trainButton);
        locateButton = (Button) findViewById(R.id.locateButton);

        // initialization of the phones wifiscanner
        final Context context = this;
        final WifiManager globManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        final Lookup perceptronTable = new Lookup();

        // display greeting message, and formatting
        txtView.setText("Welcome to WiFi scanner, please click the 'Scan' button to get started "
                         + "scanning for WiFi access points.");
        txtView.setGravity(Gravity.CENTER);
        txtView.setTextSize(20);


        //Start Scan Button Implementation
        scanButton.setOnClickListener(new View.OnClickListener() {

            public String getMacId() {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return wifiInfo.getBSSID();

            }

            public String getSignalStrength() {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 100);
                String lvl = Integer.toString(level);
                return lvl;
            }

            public void startScanner() {
                List<WifiConfiguration> wapList = globManager.getConfiguredNetworks();
                globManager.startScan();
            }

            @Override
            public void onClick(View view) {
                //Create an alert window to allow for user input of room number for file
                //documentation
                AlertDialog.Builder alert = new AlertDialog.Builder(context);

                alert.setCancelable(false);
                alert.setTitle("Room Number");
                alert.setMessage("Please enter in the number of the nearest room:");

                // Set an EditText view to get user input
                final EditText input = new EditText(context);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        roomNumber = input.getText().toString();
                        return;
                    }
                });
                alert.show();
                startScanner();
                txtView.setText("Currently scanning for signals... \r\n" + "Click 'Stop' to get results");
            }
        });
        //End of Scan Button

        //Start of End Button Implementation
        endButton.setOnClickListener(new View.OnClickListener() {
            public <T> List<ScanResult> getScanData() {
                List<ScanResult> results = globManager.getScanResults();
                return results;
            }

            public void saveToSD(String filename, String data) {
                try {
                    String content = data;
                    File directory;
                    File file;
                    FileOutputStream outputStream;

                    directory = new File(Environment.getExternalStorageDirectory(), "WifiData/RM" + roomNumber + "/");
                    directory.mkdir();
                    file = new File(directory, filename);
                    outputStream = new FileOutputStream(file);
                    outputStream.write(content.getBytes());
                    outputStream.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return;
            }

            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String formattedDate = df.format(c.getTime());
                String filename = "WifiData_" + "Room" + roomNumber + "_" + formattedDate + ".txt";
                result = getScanData();
                String resultFormatted = result.toString();
                resultFormatted = resultFormatted.replaceAll(",", "\n\n");
                saveToSD(filename, resultFormatted);
                txtView.setText(resultFormatted);
                txtView.setMovementMethod(new ScrollingMovementMethod());
            }
        });
        // End, End Button

        //Start Train Button Implementation
        trainButton.setOnClickListener(new View.OnClickListener() {
            private Handler handler = new Handler();


            public void startScanner() {
                List<WifiConfiguration> wapList = globManager.getConfiguredNetworks();
                globManager.startScan();
                synchronized (globManager) {
                    globManager.notify();
                }
            }

            public void updateTextView(final String str){
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        txtView.setText(str);
                    }
                });
            }

            public void sleeper() throws InterruptedException {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                return;
            }

            public double[] getTrainingData() {
                double totalMacsObserved = 0;
                double sumBSSIDNumerical = 0;
                double sumLevels = 0;
                double sumFreq = 0;
                trainResult = globManager.getScanResults();
                //String tr = trainResult.toString();
                synchronized (globManager) {
                    for (ScanResult element : trainResult) {
                        //System.out.println(element.level);
                        if (element.BSSID != null && element.level > -90) {
                            totalMacsObserved++;
                            for (int i = 0; i < element.BSSID.length(); i++) {
                                sumBSSIDNumerical += Character.getNumericValue(element.BSSID.charAt(i));
                            }
                            sumLevels += element.level;
                            sumFreq += element.frequency;
                        }
                    }
                    //System.out.println(Integer.toString(totalMacsObserved) + "\n" +
                    //        Integer.toString(sumBSSIDNumerical) + "\n" + Integer.toString(sumLevels)
                    //        + "\n" + Integer.toString(sumFreq) + "\n");
                    try {
                        globManager.wait(1);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                double[] output = {totalMacsObserved, sumBSSIDNumerical, sumLevels, sumFreq};
                return output;
            }

            public double[][] getTrainDataAvgs(double totalDevices, double sumBSSID, double sumLevels, double sumFreq) {
                double averageSumBSSIDs = sumBSSID / totalDevices;
                double averageSumLevels = Math.abs(sumLevels / totalDevices);
                double averageSumFreq = sumFreq / totalDevices;

                //averageSumBSSIDs = Math.round(averageSumBSSIDs);
                //averageSumBSSIDs = averageSumBSSIDs/100;
                //averageSumLevels = Math.round(averageSumLevels);
                //averageSumLevels = averageSumLevels/100;
                //averageSumFreq = Math.round(averageSumFreq);
                //averageSumFreq = averageSumFreq/100;

                System.out.println(Double.toString(averageSumBSSIDs) + "\n" + Double.toString(averageSumLevels) + "\n" + Double.toString(averageSumFreq) + "\n");
                double[][] output = {{averageSumBSSIDs, averageSumLevels, averageSumFreq}};
                //System.out.println(Integer.toString(output[0]) + '\n' + Integer.toString(output[1]) + '\n' + Integer.toString(output[2]) + '\n');
                return output;
            }

            int sessionCounter = 0;

            public void startTrain() {
                sessionCounter++;
                String session = "Training Session #" + sessionCounter + '\n';
                Perceptron2 network = new Perceptron2(3, 4, 1, 0.7, 0.9);
                NumberFormat percentFormat = NumberFormat.getPercentInstance();
                percentFormat.setMinimumFractionDigits(4);
                double[][] neuralData;
                int i = 0;
                //for (int i = 0; i < 100; i++) {
                    startScanner();
                    double[] trainingData = getTrainingData();
                    neuralData = getTrainDataAvgs(trainingData[0],trainingData[1],trainingData[2],trainingData[3]);
                    network.addAverages(neuralData);
                    for (int j = 0; j < neuralData.length; j++) {
                        i++;
                        network.computeOutputs(neuralData[j]);
                        network.calcError(roomNum[j]);
                        network.learn();

                        String update = "Trial #" + i; //+ "\n Error: "
                               // + percentFormat.format(network.getError(neuralData.length));
                        updateTextView(session + update);

                        if(network.getError(neuralData.length) == 0.0000){
                            break;
                        }
                    }
                    //System.out.println("Trial #" + i + ",Error:"
                            //+ percentFormat.format(network.getError(neuralData.length)));

               // }
                perceptronTable.addPerceptron(network);

            }

            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setCancelable(false);
                alert.setTitle("Room Number");
                alert.setMessage("Please enter in the number of the nearest room:");

                final EditText input = new EditText(context);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        roomNumber = input.getText().toString();
                        double room = Integer.parseInt(roomNumber);
                        room = room/1000;
                        roomNum[0][0] = room;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for(int i = 0; i < 500; i++) {
                                    try {
                                        sleeper();
                                    }catch(InterruptedException ie){
                                        ie.printStackTrace();
                                    }
                                    startTrain();

                                }
                            }
                        }).start();
                        return;
                    }
                });
                alert.show();
            }
        });
        //End Train Button

        //Start Locate Button Implementation
        locateButton.setOnClickListener(new View.OnClickListener() {
            private Handler handler = new Handler();

            public void startScanner() {
                List<WifiConfiguration> wapList = globManager.getConfiguredNetworks();
                globManager.startScan();
                synchronized (globManager) {
                    globManager.notify();
                }
                //return;
            }

            public void updateTextView(final String str){
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        txtView.setText(str);
                    }
                });
            }

            public void sleeper() throws InterruptedException {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                return;
            }

            public double[] getTrainingData() {
                double totalMacsObserved = 0;
                double sumBSSIDNumerical = 0;
                double sumLevels = 0;
                double sumFreq = 0;
                trainResult = globManager.getScanResults();
                //String tr = trainResult.toString();
                synchronized (globManager) {
                    for (ScanResult element : trainResult) {
                        //System.out.println(element.level);
                        if (element.BSSID != null && element.level > -90) {
                            totalMacsObserved++;
                            for (int i = 0; i < element.BSSID.length(); i++) {
                                sumBSSIDNumerical += Character.getNumericValue(element.BSSID.charAt(i));
                            }
                            sumLevels += element.level;
                            sumFreq += element.frequency;
                        }
                    }
                    //System.out.println(Integer.toString(totalMacsObserved) + "\n" +
                    //        Integer.toString(sumBSSIDNumerical) + "\n" + Integer.toString(sumLevels)
                    //        + "\n" + Integer.toString(sumFreq) + "\n");
                    try {
                        globManager.wait(10);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                //txtView.setText("Gathered Training Data.");
                double[] output = {totalMacsObserved, sumBSSIDNumerical, sumLevels, sumFreq};
                return output;
            }

            public double[][] getTrainDataAvgs(double totalDevices, double sumBSSID, double sumLevels, double sumFreq) {
                double averageSumBSSIDs = sumBSSID / totalDevices;
                double averageSumLevels = Math.abs(sumLevels / totalDevices);
                double averageSumFreq = sumFreq / totalDevices;

                //averageSumBSSIDs = Math.round(averageSumBSSIDs);
                //averageSumBSSIDs = averageSumBSSIDs/100;
                //averageSumLevels = Math.round(averageSumLevels);
                //averageSumLevels = averageSumLevels/100;
                //averageSumFreq = Math.round(averageSumFreq);
                //averageSumFreq = averageSumFreq/100;

                //System.out.println(Integer.toString(averageSumBSSIDs) + "\n" + Integer.toString(averageSumLevels) + "\n" + Integer.toString(averageSumFreq) + "\n");
                double[][] output = {{averageSumBSSIDs, averageSumLevels, averageSumFreq}};
                //System.out.println(Integer.toString(output[0]) + '\n' + Integer.toString(output[1]) + '\n' + Integer.toString(output[2]) + '\n');
                return output;
            }

            public void startLocate() {
                startScanner();
                //Lookup table = new Lookup();
                double[] trainingData = getTrainingData();
                double[][] neuralNetworkData = getTrainDataAvgs(trainingData[0], trainingData[1], trainingData[2], trainingData[3]);
                //System.out.println(Arrays.deepToString(neuralNetworkData));
                Perceptron2 network = perceptronTable.compareAverages(neuralNetworkData);
                double[] guess = network.computeOutputs(neuralNetworkData[0]);
                guess[0] = guess[0]*1000;
                System.out.println('\n');
                //System.out.println(guess[0] + " " + guess[1]);
                for(int k = 0; k < guess.length; k++){
                    System.out.println(guess[k] + " ");
                    updateTextView(Double.toString(guess[k]));
                }
            }

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startLocate();
                    }
                }).start();
            }
        });
        // End Locate Button
    }
}