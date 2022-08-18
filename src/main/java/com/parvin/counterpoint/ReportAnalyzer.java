package com.parvin.counterpoint;

/**
 * TODO
 * @author dparvin
 *
 */
public class ReportAnalyzer {
	Report report;
	
	/**
	 * TODO
	 * @param report
	 */
	public ReportAnalyzer(Report report) {
		this.report = report;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public int countSimilarAndParallelMotionEvents() {
		return (int) report.getContrapuntalMotionEvents().stream()
				.filter(
						event -> event.getContrapuntalMotion() == Motion.PARALLEL
						|| event.getContrapuntalMotion() == Motion.SIMILAR)
				.count();
	}
	
	/**
	 * TODO
	 * @return
	 */
	public int countContraryMotionEvents() {
		return (int) report.getContrapuntalMotionEvents().stream()
				.filter(event -> event.getContrapuntalMotion() == Motion.CONTRARY)
				.count();
	}
}
