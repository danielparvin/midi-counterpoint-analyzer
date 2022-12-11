package com.parvin.midi_analysis.counterpoint;

import java.util.Arrays;

import org.jfree.data.xy.XYIntervalSeries;

public class HistogramDataset {
	private int[] frequencyData;
	private double binSize;
	private double maxValue;
	private double minValue;
	
	public HistogramDataset(double minValue, double maxValue, double binSize) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.binSize = binSize;
		this.frequencyData = new int[calculateNumberOfBins()];
	}

	/**
	 * Add an observation to the dataset (by incrementing the item count for the appropriate bin).<br>
	 * @param value Value to increment the frequency of.
	 */
	public void addObservation(double value) {
		frequencyData[calculateRelevantBin(value)]++;
	}

	public int[] getBinnedData() {
		return Arrays.copyOf(frequencyData, frequencyData.length);
	}

	public double getBinSize() {
		return binSize;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public int getNumberOfBins() {
		return frequencyData.length;
	}

	public int getFrequencyAtBin(int binNumber) {
		return frequencyData[binNumber];
	}
	
	public int getFrequencyOfValue(double value) {
		return frequencyData[calculateRelevantBin(value)];
	}

	public XYIntervalSeries toXYIntervalSeries(String name) {
		XYIntervalSeries xyIntervalSeries = new XYIntervalSeries(name);
		for (int binNumber = 0; binNumber < frequencyData.length; binNumber++) {
			double xLow = binNumber * binSize + minValue;
			double xHigh = Math.min((binNumber + 1) * binSize + minValue, maxValue);
			double xMid = (xLow + xHigh) / 2.0;
			double y = getFrequencyAtBin(binNumber);
			xyIntervalSeries.add(xMid, xLow, xHigh, y, y, y);
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
			return frequencyData.length - 1;
		}

		return (int) (value / binSize);
	}
}
