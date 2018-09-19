package wips.wips;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Michael on 3/13/2017.
 */

public class Lookup {
    List<Perceptron2> perceptronTable = new ArrayList<>();

    public void addPerceptron(Perceptron2 p){
        perceptronTable.add(p);
    }

    public Perceptron2 compareAverages(double[][] inAverages) {
        System.out.println(Arrays.deepToString(inAverages));
        double in_mac = inAverages[0][0];
        double in_db = inAverages[0][1];
        double in_freq = inAverages[0][2];
        Perceptron2 defaultPerceptron = new Perceptron2(3, 4, 1, 0.7, 0.9);

        for (Perceptron2 perceptron : perceptronTable) {
            List<Double> mac_vals = new ArrayList<>();
            List<Double> db_vals = new ArrayList<>();
            List<Double> freq_vals = new ArrayList<>();
            for (double[][] val_element : perceptron.machineAverages) {
                for (int i = 0; i < val_element.length; i++) {
                    mac_vals.add(val_element[i][0]);
                    db_vals.add(val_element[i][1]);
                    freq_vals.add(val_element[i][2]);
                }
            }
            
        }
        return defaultPerceptron;
    }
}
