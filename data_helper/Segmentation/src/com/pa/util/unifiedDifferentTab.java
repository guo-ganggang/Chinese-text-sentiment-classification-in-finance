package com.pa.util;

public class unifiedDifferentTab {
	private static String testString = "1005089297	大一新生中国手机音乐网";

	// 计算字符串中包含的空格个数
	public static int getBlankNum(String testString) {
		int count = 0;
		for (int i = 0; i < testString.length(); i++) {
			String tempString = String.valueOf(testString.charAt(i));
			if (tempString.equals(" ")) {
				count++;
			}
		}
		return count;
	}

	// 打印char[]数组
	public static void printArray(char[] testArray) {
		for (char i : testArray) {
			System.out.print(i);
		}
		System.out.println();
	}

	// 将字符串空格转换为20%
	public static String replaceAllBlank(String testString) {
        String afterReplace = "";
//		if (testString == null || testString.length() <= 0) {
//			return;
//		}
		// 字符数组初始长度
		int length = testString.length();
		// 字符数组增加长度后
		int newLength = newLength = getBlankNum(testString) * 2
				+ testString.length();
		char[] tempArray = new char[newLength];
		System.arraycopy(testString.toCharArray(), 0, tempArray, 0, testString
				.toCharArray().length);
		int indexofOriginal = length - 1;
		int indexofNew = newLength - 1;
		//System.out.println("未替换空格时的字符串：");
		//printArray(tempArray);
		while (indexofOriginal >= 0 && indexofOriginal != indexofNew) {
			if (tempArray[indexofOriginal] == ' ') {
				tempArray[indexofNew--] = '%';
				tempArray[indexofNew--] = '2';
				tempArray[indexofNew--] = '0';
			} else {
				tempArray[indexofNew--] = tempArray[indexofOriginal];
			}
			indexofOriginal--;
		}
		System.out.println("替换空格后的字符串：");
		printArray(tempArray);
		for(int i=0;i<tempArray.length;i++){
			afterReplace += tempArray[i];
		}
		
		return afterReplace;

	}

	public static void main(String[] args) {
		replaceAllBlank(testString);
	}
}

