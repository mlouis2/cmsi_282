//Maddie Louis, Merissa Tan

package lcs;

import java.util.HashSet;
import java.util.Set;

public class LCS {
    
    /**
     * memoCheck is used to verify the state of your tabulation after
     * performing bottom-up and top-down DP. Make sure to set it after
     * calling either one of topDownLCS or bottomUpLCS to pass the tests!
     */
    public static int[][] memoCheck;
    
    // -----------------------------------------------
    // Shared Helper Methods
    // -----------------------------------------------
  
    /**
     * Collects solution from a completed table
     * @param table The table that contains the length of the longest sub-LCS
     * @param row Current row
     * @param col Current column
     * @param rowString The String found along the table's rows
     * @param colString The String found along the table's cols
     * @return A set of the LCS strings
     */
    public static Set<String> collectSolution (int[][] table, int row, int col, String rowString, String colString) {
    	Set<String> solutions = new HashSet<String>();
    	if (row == 0 || col == 0) {
    		solutions.add("");
    		return solutions;
    	}
    	if (rowString.charAt(row - 1) != colString.charAt(col - 1)) {
    		if (table[row][col - 1] >= table[row - 1][col]) {
    			solutions.addAll(collectSolution(table, row, col - 1, rowString, colString));
    		}
    		if (table[row - 1][col] >= table[row][col - 1]) {
    			solutions.addAll(collectSolution(table, row - 1, col, rowString, colString));
    		}
    	} else {
    		Set<String> recursedSolutions = collectSolution(table, row - 1, col - 1, rowString, colString);
    		for (String sequence : recursedSolutions) {
    			solutions.add(sequence + rowString.charAt(row - 1));
    		}
    	}
    	return solutions;
    }
    
    /**
     * Executes LCS, either bottom-up or top-down
     * @param bottomUp Whether or not bottomUp DP should be used (other option is topDown)
     * @param rowString The String found along the table's rows
     * @param colString The String found along the table's cols
     * @return The set of LCS solutions
     */
    public static Set<String> executeLCS(boolean bottomUp, String rowString, String colString) {
    	memoCheck = new int[rowString.length() + 1][colString.length() + 1];
    	if (bottomUp) {
    	    bottomUpTableFill(rowString, colString, memoCheck);
    	} else {
    		boolean[][] completedCells = new boolean[rowString.length() + 1][colString.length() + 1];
    		topDownTableFill(memoCheck, rowString, colString, memoCheck.length - 1, memoCheck[0].length - 1, completedCells);
    	}
    	return collectSolution(memoCheck, memoCheck.length - 1, memoCheck[0].length - 1, rowString, colString);
    }
    

    // -----------------------------------------------
    // Bottom-Up LCS
    // -----------------------------------------------
    
    /**
     * Bottom-up dynamic programming approach to the LCS problem, which
     * solves larger and larger subproblems iterative using a tabular
     * memoization structure.
     * @param rowString The String found along the table's rows
     * @param colString The String found along the table's cols
     * @return The longest common subsequence between rStr and cStr +
     *         [Side Effect] sets memoCheck to refer to table
     */
    public static Set<String> bottomUpLCS (String rowString, String colString) {
    	return executeLCS(true, rowString, colString);
    }

    /**
     * Fills the table using bottomUp DP
     * @param rowString The String found along the table's rows
     * @param colString The String found along the table's cols
     * @param table The table to be filled
     */
	private static void bottomUpTableFill(String rowString, String colString, int[][] table) {
    	for (int row = 1; row < table.length; row++) {
    		for (int col = 1; col < table[0].length; col++) {
    			if (rowString.charAt(row - 1) == colString.charAt(col - 1)) {
    				table[row][col] = table[row - 1][col - 1] + 1;
    			} else {
    				table[row][col] = Math.max(table[row - 1][col], table[row][col - 1]);
    			}
    		}
    	}
	}
    
    
    // -----------------------------------------------
    // Top-Down LCS
    // -----------------------------------------------
    
    /**
     * Top-down dynamic programming approach to the LCS problem, which
     * solves smaller and smaller subproblems recursively using a tabular
     * memoization structure.
     * @param rowString The String found along the table's rows
     * @param colString The String found along the table's cols
     * @return The longest common subsequence between rStr and cStr +
     *         [Side Effect] sets memoCheck to refer to table  
     */
    public static Set<String> topDownLCS (String rowString, String colString) {
    	return executeLCS(false, rowString, colString);
    }
    
    /**
     * 
     * @param table The table to be filled
     * @param rowString The String found along the table's rows
     * @param colString The String found along the table's cols
     * @param row The current row
     * @param col The current column
     * @param completedCells Keeps track of which cells have already been filled
     * @return Returns the value of the table at row, col
     */
    public static int topDownTableFill(int[][] table, String rowString, String colString, int row, int col, boolean[][] completedCells) {
    	if (row == 0 || col == 0) {
    		return 0;
    	}
    	if (rowString.charAt(row - 1) != colString.charAt(col - 1)) {
    		int valueOfLeftCell;		
    		int valueOfAboveCell;
    		if (completedCells[row][col - 1]) {
    			valueOfLeftCell = table[row][col - 1];
    		} else {
    			valueOfLeftCell = topDownTableFill(table, rowString, colString, row, col - 1, completedCells);
    		}
    		if (completedCells[row - 1][col]) {
    			valueOfAboveCell = table[row - 1][col];
    		} else {
    			valueOfAboveCell = topDownTableFill(table, rowString, colString, row - 1, col, completedCells);
    		}
    		table[row][col] = Math.max(valueOfLeftCell, valueOfAboveCell);
    	} else {
    		if (completedCells[row - 1][col - 1]) {
    			table[row][col] = 1 + table[row - 1][col - 1];
    		} else {
    			table[row][col] = 1 + topDownTableFill(table, rowString, colString, row - 1, col - 1, completedCells);
    		}
    	}
    	completedCells[row][col] = true;
    	return table[row][col];
    }
    
}
