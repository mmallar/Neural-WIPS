package wips.wips;

/*
 * Created by Michael on 1/17/2017.
 */

public class Perceptron {
    static float alpha = (float) 0.05;
    static float noisemax = (float) 0.40;
    private float outputWeight[];
    private float hiddenWeight[][];
    private int size;

    float sigmoid(float x){
        return (float)(1/(1+Math.pow(2.71828,-x)));
    }

    Perceptron(int nodes){
        size = nodes;
        outputWeight = new float[nodes + 1];

        for (int i = 0; i < nodes + 1; i++){
            outputWeight[i] = (float)((((Math.random()%1000)/1000.0)*noisemax) - (noisemax/2));
        }

        hiddenWeight = new float[nodes][nodes];

        for(int j = 0; j < nodes; j++){
            hiddenWeight[j] = new float[nodes+1];
            for(int k = 0; k < nodes+1 ; k++){
                hiddenWeight[j][k] = (float)((((Math.random()%1000)/1000.0)*noisemax) - (noisemax/2));
            }
        }
    }

    int getPrediction(int inputs[]){
        float hidden[] = new float[size];

        for(int i = 0; i < size; i++){
            float sum = 0;
            for(int j = 0; j < size; j++){
                sum += (inputs[j] == 0?0:1) * hiddenWeight[i][j];
            }
            sum += hiddenWeight[i][size];
            hidden[i] = sigmoid(sum);
        }

        float sum = 0;

        for(int k = 0; k < size; k++){
            sum+= hidden[k]*outputWeight[k];
        }

        sum+=outputWeight[size];

        return sigmoid(sum)>=0.5?1:0;
    }

    float getRawPrediction(int inputs[]){
        float hidden[] = new float[size];

        for(int i = 0; i < size; i++){
            float sum = 0;

            for(int j = 0 ; j < size ; j++){
                sum += (inputs[j]==0?0:1)*hiddenWeight[i][j];
            }
            sum += hiddenWeight[i][size];
            hidden[i] = sigmoid(sum);
        }

        float sum = 0;

        for(int k = 0; k < size; k++){
            sum += hidden[k]*outputWeight[k];
        }

        sum += outputWeight[size];

        return sum;
    }

    boolean train(int inputs[], double desiredOutputs) {
        float hidden[] = new float[size];

        for (int i = 0; i < size; i++) {
            float sum = 0;

            for (int j = 0; j < size; j++) {
                sum += (inputs[j] == 0 ? 0 : 1) * hiddenWeight[i][j];
            }

            sum += hiddenWeight[i][size];
            hidden[i] = sigmoid(sum);
        }

        float sum = 0;

        for(int k = 0; k < size; k++){
            sum += hidden[k]*outputWeight[k];
        }

        sum += outputWeight[size];
        float prediction = sigmoid(sum);

        double error = (desiredOutputs - prediction)*prediction*(1-prediction);
        double hiddenerror[] = new double[size+1];

        for(int m = 0; m < size; m++){
            hiddenerror[m]=hidden[m]*(1-hidden[m])*outputWeight[m]*error;
            outputWeight[m]+=error*hidden[m]*alpha;
        }

        outputWeight[size]+=alpha*error;

        for(int n = 0; n < size; n++){
            for(int p = 0; p < size; p++){
                hiddenWeight[n][p]+=alpha*hiddenerror[n]*(inputs[p]==0?0:1);
            }
            hiddenWeight[n][size]+=alpha*hiddenerror[n];
        }

        return ((prediction>=0.5?1:0) == desiredOutputs);
    }
}