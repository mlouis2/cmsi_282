//Maddie Louis, Merissa Tan, Adriana Donkers
package nim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Artificial Intelligence responsible for playing the game of Nim! Implements
 * the alpha-beta-pruning mini-max search algorithm
 */
public class NimPlayer {

	private final int MAX_REMOVAL;

	NimPlayer(int MAX_REMOVAL) {
		this.MAX_REMOVAL = MAX_REMOVAL;
	}

	/**
	 * 
	 * @param remaining Integer representing the amount of stones left in the pile
	 * @return An int action representing the number of stones to remove in the
	 *         range of [1, MAX_REMOVAL]
	 */
	public int choose(int remaining) {
		GameTreeNode root = new GameTreeNode(remaining, 0, true);

		int minimaxScore = alphaBetaMinimax(root, Integer.MIN_VALUE, Integer.MAX_VALUE, true,
				new HashMap<GameTreeNode, Integer>());
		for (GameTreeNode child : root.children) {
			if (child.score == minimaxScore) {
				return child.action;
			}
		}
		return 1;
	}

	/**
	 * Constructs the minimax game tree by the tenets of alpha-beta pruning with
	 * memoization for repeated states.
	 * 
	 * @param node    The root of the current game sub-tree
	 * @param alpha   Smallest minimax score possible
	 * @param beta    Largest minimax score possible
	 * @param isMax   Boolean representing whether the given node is a max (true) or
	 *                min (false) node
	 * @param visited Map of GameTreeNodes to their minimax scores to avoid
	 *                repeating large subtrees
	 * @return Minimax score of the given node + [Side effect] constructs the game
	 *         tree originating from the given node
	 */
	private int alphaBetaMinimax(GameTreeNode node, int alpha, int beta, boolean isMax,
			Map<GameTreeNode, Integer> visited) {
		if (node.remaining == 0) {
			return (isMax) ? -1 : 1;
		}
		return determineScore(node, alpha, beta, isMax, visited);
	}

	/**
	 * Determines the score of the node provided
	 * 
	 * @param node    The root of the current game sub-tree
	 * @param alpha   Smallest minimax score possible
	 * @param beta    Largest minimax score possible
	 * @param isMax   Boolean representing whether the given node is a max (true) or
	 *                min (false) node
	 * @param visited Map of GameTreeNodes to their minimax scores to avoid
	 *                repeating large subtrees
	 * @return Minimax score of the given node + [Side effect] constructs the game
	 *         tree originating from the given node
	 */
	public int determineScore(GameTreeNode node, int alpha, int beta, boolean isMax,
			Map<GameTreeNode, Integer> visited) {
		int currentMinimaxScore = (isMax) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		for (int i = 1; i <= Math.min(this.MAX_REMOVAL, node.remaining); i++) {
			GameTreeNode child = new GameTreeNode(node.remaining - i, i, !node.isMax);
			node.children.add(child);
			if (visited.get(child) != null) {
				currentMinimaxScore = (isMax)
						? Math.max(currentMinimaxScore, visited.get(child))
						: Math.min(currentMinimaxScore, visited.get(child));
			} else {
				currentMinimaxScore = (isMax)
						? Math.max(currentMinimaxScore, alphaBetaMinimax(child, alpha, beta, child.isMax, visited))
						: Math.min(currentMinimaxScore, alphaBetaMinimax(child, alpha, beta, child.isMax, visited));
				visited.put(child, currentMinimaxScore);
			}
			child.score = currentMinimaxScore;
			if (isMax) {
				alpha = Math.max(alpha, currentMinimaxScore);
			} else {
				beta = Math.min(beta, currentMinimaxScore);
			}
			if (beta <= alpha) {
				break;
			}
		}
		return currentMinimaxScore;
	}
}

/**
 * GameTreeNode to manage the Nim game tree.
 */
class GameTreeNode {

	int remaining, action, score;
	boolean isMax;
	ArrayList<GameTreeNode> children;

	/**
	 * Constructs a new GameTreeNode with the given number of stones remaining in
	 * the pile, and the action that led to it. We also initialize an empty
	 * ArrayList of children that can be added-to during search, and a placeholder
	 * score of -1 to be updated during search.
	 * 
	 * @param remaining The Nim game state represented by this node: the # of stones
	 *                  remaining in the pile
	 * @param action    The action (# of stones removed) that led to this node
	 * @param isMax     Boolean as to whether or not this is a maxnode
	 */
	GameTreeNode(int remaining, int action, boolean isMax) {
		this.remaining = remaining;
		this.action = action;
		this.isMax = isMax;
		children = new ArrayList<>();
		score = 0;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof GameTreeNode
				? remaining == ((GameTreeNode) other).remaining && isMax == ((GameTreeNode) other).isMax
						&& action == ((GameTreeNode) other).action
				: false;
	}

	@Override
	public int hashCode() {
		return remaining + ((isMax) ? 1 : 0);
	}

}
