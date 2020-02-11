package org.thunlp.text.classifiers;

public class ClassifyResult implements Comparable<ClassifyResult>{
	/**
	 * 分类标签编号
	 */
	public int label;
	/**
	 * 分类概率
	 */
	public double prob;

	public ClassifyResult(int i, double val){
		prob = val;
		label = i;
	}
	
	public String toString () {
		return label + "\t" + prob;
	}

	@Override
	public int compareTo(ClassifyResult o) {
		if (prob != o.prob) {
			return prob > o.prob ? 1 : -1;
		}
		return 0;
	}
}
