package com.faa.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 平均值计算封装
 * @Description:
 */
public class AverageUtil {

    /**
     * 最大元素个数
     */
    private int AVG_LEN;
    private List<Double> values;

    public AverageUtil(int size){
        AVG_LEN = size;
        this.values = new ArrayList<>();
    }

    public void add(double value){
        if(this.values.size() == AVG_LEN){
            this.values.remove(0);
        }

        this.values.add(value);
    }

    public double getAvg(){
        double all = 0;
        for (double d : this.values) {
            all += d;
        }
        return all / this.values.size();
    }
}
