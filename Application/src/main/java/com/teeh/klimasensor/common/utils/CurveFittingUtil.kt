package com.teeh.klimasensor.common.utils

import com.jjoe64.graphview.series.DataPoint

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.fitting.GaussianCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints

import java.util.ArrayList

object CurveFittingUtil {

    //    public static List<DataPoint> getGaussianFit(List<DataPoint> input) {
    //        WeightedObservedPoints obs = new WeightedObservedPoints();
    //        for (DataPoint point : input) {
    //            obs.add(point.getX(),  point.getY());
    //        }
    //
    //        GaussianCurveFitter fitter = GaussianCurveFitter.create();
    //
    //        fitter.fit(obs.toList());
    //
    //    }

    fun getMovingAverage(list: List<DataPoint>, n: Int?): List<DataPoint> {
        val avgList = ArrayList<DataPoint>()
        for (i in n!! - 1 until list.size) {
            var sum = 0.0
            val date = list[i].x
            for (j in 0 until n) {
                sum += list[i - j].y
            }
            val avg = sum / n
            avgList.add(DataPoint(date, avg))
        }
        return avgList
    }

    /**
     * Weighted Moving Average taking a list of weights and a parameter shift determining the offset of the weights w.r.t the calculated value.
     * weights.size()/2 >= shift > 0: forward biased moving average
     * -weights.size()/2 =< shift < 0: backward biased moving average
     * shift == 0: symetric moving average
     *
     * @param list
     * @param weights
     * @param shift
     * @return
     */
    fun getWeightedMovingAverage(list: List<DataPoint>, weights: List<Double>, shift: Int): List<DataPoint> {
        var weights = weights
        val mvgAvg = ArrayList<DataPoint>()
        val span = weights.size

        if (shift > span / 2 || -shift < -span / 2) {
            throw RuntimeException("abs(shift) > span/2")
        }

        val weightSum = CalcUtil.sum(weights)
        if (weightSum > 1.0) {
            weights = CalcUtil.normalize(weights, weightSum)
        }

        val start = span / 2 - shift
        val end = list.size - span / 2 + shift

        for (i in start until end) {
            val date = list[i].x
            var sum = 0.0
            val tempStart = i - start
            for (j in 0 until span) {
                sum += weights[j] * list[tempStart + j].y
            }
            mvgAvg.add(DataPoint(date, sum))
        }

        return mvgAvg
    }

    fun getUniformWeightVector(size: Int): List<Double> {
        val weights = ArrayList<Double>()
        for (i in 0 until size) {
            weights.add(1.0 / size)
        }
        return weights
    }

    fun getNormalWeightVector(size: Int?): List<Double> {
        val distr = NormalDistribution()
        var x = -3.0 // we start at -3*sigma
        val step = 6.0 / (size!! - 1)
        val weights = ArrayList<Double>()
        for (i in 0 until size) {
            weights.add(distr.density(x))
            x = x + step
        }
        val sum = CalcUtil.sum(weights)
        if (sum > 1.0) {
            CalcUtil.normalize(weights, sum)
        }
        return weights
    }
}
