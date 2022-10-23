package com.parvin.midi_analysis.counterpoint;

import java.util.Arrays;

import org.jfree.data.xy.XYIntervalSeries;

public class HistogramDataset {
	private int[] data;
	private double binSize;
	private double minValue;
	private double maxValue;

	public HistogramDataset(double minValue, double maxValue, double binSize) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.binSize = binSize;
		this.data = new int[calculateNumberOfBins()];
	}

	/**
	 * Add an observation to the dataset (by incrementing the item count for the appropriate bin).<br>
	 * @param value Value to increment the frequency of.
	 */
	public void addObservation(double value) {
		data[calculateRelevantBin(value)]++;
	}

	public int[] getBinnedData() {
		return Arrays.copyOf(data, data.length);
	}

	public double getBinSize() {
		return binSize;
	}

	public int getNumberOfBins() {
		return data.length;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getValue(int binNumber) {
		return data[binNumber];
	}

	public XYIntervalSeries toXYIntervalSeries(String name) {
		XYIntervalSeries xyIntervalSeries = new XYIntervalSeries(name);
		for (int binNumber = 0; binNumber < data.length; binNumber++) {
			double xMid = Math.min((binNumber * binSize + (binNumber + 1) * binSize) / 2.0, maxValue);
			xyIntervalSeries.add(xMid, 
					binNumber * binSize, 
					Math.min((binNumber + 1) * binSize, maxValue), 
					getValue(binNumber), 
					getValue(binNumber), 
					getValue(binNumber));
		}

		return xyIntervalSeries;
	}

	private int calculateNumberOfBins() {
		boolean extraBin = (maxValue - minValue) % binSize > 0;
		int bins = (int) ((maxValue - minValue) / binSize);
		if (extraBin) {
			bins++;
		}
		return bins;
	}

	private int calculateRelevantBin(double value) {
		if (value < minValue || value > maxValue) {
			throw new IllegalArgumentException("Value is outside the range of the histogram.");
		}
		if (value == maxValue) {
			return data.length - 1;
		}

		return (int) (value / binSize);
	}
}
