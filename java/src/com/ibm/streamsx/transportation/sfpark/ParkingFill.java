/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
package com.ibm.streamsx.transportation.sfpark;

import java.io.Serializable;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

public class ParkingFill implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6721694536946821937L;

    private String ospid;

    private int oper;
    private int occ;
    private int fill;
    private long ts;
    private double trend;

    public int getOper() {
        return oper;
    }

    public void setOper(int oper) {
        this.oper = oper;
    }

    public int getOcc() {
        return occ;
    }

    public void setOcc(int occ) {
        this.occ = occ;
    }

    public String getOspid() {
        return ospid;
    }

    public int getFill() {
        return fill;
    }

    public ParkingFill aggregate(Iterable<ParkingOccupancy> items) {
        
        Mean mean = new Mean();

        int count = 0;
        for (ParkingOccupancy occupancy : items) {
            ospid = occupancy.getOspid();
            // maintain the last values, as that's all
            // that matters for parking now!
            occ = occupancy.getOcc();
            oper = occupancy.getOper();
            setTs(occupancy.getTs());
            if (oper == 0)
                continue;
            count++;
            double fill = ((double) occ) / ((double) oper);
            mean.increment(fill);
        }

        if (ospid == null || oper == 0) {
            return null;
        }

        if (count > 5) {
            double[] values = new double[count * 2];
            int i = 0;
            for (ParkingOccupancy occupancy : items) {
                int occl = occupancy.getOcc();
                int operl = occupancy.getOper();
                long tsl = occupancy.getTs();
                if (operl == 0)
                    continue;

                // y, then x
                // spaces (y) vs time (x)
                values[i++] = occl;
                values[i++] = tsl;
            }

            OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
            ols.newSampleData(values, count, 1);

            double[] coe = ols.estimateRegressionParameters();
            if (coe.length >= 2)
                setTrend(coe[1] * 1000.0 * 60.0); // cars per minute
        }

        fill = (int) (mean.getResult() * 100.0);
        if (fill > 100)
            fill = 100;
        else if (fill < 0)
            fill = 0;

        return this;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public double getTrend() {
        return trend;
    }

    public void setTrend(double trend) {
        this.trend = trend;
    }
}
