/**
 * Copyright (c) 2013 Robert Nyholm. All rights reserved.
 */
package ax.ha.it.smsalarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;

/**
 * Class containing different convenience methods and 
 * other functionality.
 * 
 * @author Robert Nyholm <robert.nyholm@aland.net>
 * @version 2.2
 * @since 2.2
 */
public class Utils {
	/**
	 * To check if given <code>String</code> exists in given <code>List</code> 
	 * of <code>Strings</code>. Method is not case sensitive.
	 * 
	 * @param string String to check if exists in list.
	 * @param list List to check if string exists in.
	 * 
	 * @return <code>true</code> if given String exists in given List else <code>false</code>.<br>
	 * 		   <code>false</code> is also returned if either given argument is <code>null</code>.
	 */
	@SuppressLint("DefaultLocale")
	public static boolean existsIn(String string, List<String> list) {
		if (string != null && list != null) {
			List<String> caseUpperList = new ArrayList<String>();
			
			for (String str: list) {
				caseUpperList.add(str.toUpperCase());
			}
			
			return caseUpperList.contains(string.toUpperCase());
		} else {
			return false;
		}
	}
	
	/**
	 * To check if <code>String</code>(textToParse) passed in as argument
	 * contains another <code>String</code>(wordToFind) passed in as argument.
	 * This method only checks whole words and not a <code>CharSequence</code>.
	 * Method is not case sensitive.
	 * 
	 * @param wordToFind
	 *            Word to find.
	 * @param textToParse
	 *            Text to look for word in.
	 * 
	 * @return <code>true</code> if word is found else <code>false</code>.
	 * 
	 * @throws NullPointerException
	 *             if either or both params <code>wordToFind</code> and
	 *             <code>textToParse</code> is null.
	 * @throws IllegalArgumentException
	 *             if either or both params <code>wordToFind</code> and
	 *             <code>textToParse</code> is empty.
	 */
	public static boolean findWordEqualsIgnore(String wordToFind, String textToParse) {
		if (wordToFind != null && textToParse != null) {
			if (!wordToFind.isEmpty() && !textToParse.isEmpty()) {
				List<String> words = new ArrayList<String>();
				words = Arrays.asList(textToParse.split(" "));

				for (String word : words) {
					if (wordToFind.equalsIgnoreCase(word)) {
						return true;
					}
				}

				return false;
			} else {
				throw new IllegalArgumentException("One or both of given arguments are empty. Arguments --> wordToFind = " + wordToFind + ", textToParse = " + textToParse);
			}
		} else {
			throw new NullPointerException("One or both of given arguments are null. Arguments --> wordToFind = " + wordToFind + ", textToParse = " + textToParse);
		}
	}
}