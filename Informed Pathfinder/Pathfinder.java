//Maddie Louis, Merissa (Li Ying) Tan
package pathfinder.informed;

import java.util.*;

/**
 * Maze Pathfinding algorithm that implements a basic, uninformed, breadth-first
 * tree search.
 */
public class Pathfinder {

	/**
	 * Given a MazeProblem, which specifies the actions and transitions available in
	 * the search, returns a solution to the problem as a sequence of actions that
	 * leads from the initial to a goal state.
	 * 
	 * @param problem A MazeProblem that specifies the maze, actions, transitions.
	 * @return An ArrayList of Strings representing actions that lead from the
	 *         initial to the goal state, of the format: ["R", "R", "L", ...]
	 */
	public static ArrayList<String> solve(MazeProblem problem) {
		return solve(problem, problem.INITIAL_STATE, false);
	}

	/**
	 * Returns the solved path to get from the initialState to either the key or
	 * goal state
	 * 
	 * @param problem      A MazeProblem that specifies the maze, actions,
	 *                     transitions.
	 * @param initialState The state that we start at, either the initial state or
	 *                     the key state
	 * @param foundKey     Whether or not the key has already been found
	 * @return An ArrayList of Strings representing actions that lead from the
	 *         initial to the goal state, of the format: ["R", "R", "L", ...]
	 */
	private static ArrayList<String> solve(MazeProblem problem, MazeState initialState, boolean foundKey) {
		if (problem.KEY_STATES.size() == 0) {
			return null;
		}
		Set<MazeState> visited = new HashSet<>();
		Queue<SearchTreeNode> frontier = new PriorityQueue<SearchTreeNode>((node1, node2) -> {
			return rankPriority(node1, node2);
		});
		frontier.add(new SearchTreeNode(initialState, null, null, 0));
		while (frontier.peek() != null) {
			SearchTreeNode nodeToExpand = frontier.poll();
			visited.add(nodeToExpand.state);
			if (!foundKey && problem.isKey(nodeToExpand.state)) {
				return getCombinedPath(problem, nodeToExpand);
			}
			if (foundKey && problem.isGoal(nodeToExpand.state)) {
				return getPath(nodeToExpand);
			}
			Map<String, MazeState> mapOfChildren = problem.getTransitions(nodeToExpand.state);
			for (Map.Entry<String, MazeState> child : mapOfChildren.entrySet()) {
				if (!visited.contains(child.getValue())) {
					frontier.add(new SearchTreeNode(child.getValue(), child.getKey(), nodeToExpand,
							getTotalCost(problem, nodeToExpand, child, foundKey)));
				}
			}
		}
		return null;
	}

	/**
	 * Returns the total cost of the child node
	 * 
	 * @param problem  A MazeProblem that specifies the maze, actions, transitions.
	 * @param parent   The parent of the child that we are analyzing.
	 * @param child    The child that we are analyzing the total cost of
	 * @param foundKey Whether or not the key has already been found
	 * @return The total cost of the child node (past + future cost)
	 */
	public static int getTotalCost(MazeProblem problem, SearchTreeNode parent, Map.Entry<String, MazeState> child,
			boolean foundKey) {
		int pastCost = (parent.totalCost + problem.getCost(child.getValue()));
		int futureCost = getManhattanDistance(problem, child.getValue(),
				foundKey ? problem.KEY_STATES : problem.GOAL_STATES);
		return pastCost + futureCost;
	}

	/**
	 * Returns the Manhattan distance from the given maze state to the nearest goal
	 * state
	 * 
	 * @param problem      A MazeProblem that specifies the maze, actions,
	 *                     transitions.
	 * @param currentState The state that we are getting the distance from
	 * @param finalStates  Either the set of Key or Goal States, depending on what
	 *                     we are trying to get to
	 * @return The lowest distance between the given state and the nearest key/goal
	 *         state
	 */
	public static int getManhattanDistance(MazeProblem problem, MazeState currentState, Set<MazeState> finalStates) {
		int lowestDistance = -1;
		for (MazeState state : finalStates) {
			int distance = calculateManhattanDistanceBetweenStates(currentState, state);
			if (lowestDistance == -1 || lowestDistance > distance) {
				lowestDistance = distance;
			}
		}
		return lowestDistance;
	}

	/**
	 * Returns the Manhattan distance between two states
	 * 
	 * @param a The first state
	 * @param b The second state
	 * @return The Manhattan distance between states a and b
	 */
	public static int calculateManhattanDistanceBetweenStates(MazeState a, MazeState b) {
		return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
	}

	/**
	 * Returns the path from the initial state ("I") to the key state ("K"),
	 * combined with the path from the key state to the goal state ("G")
	 * 
	 * @param problem      A MazeProblem that specifies the maze, actions,
	 *                     transitions.
	 * @param nodeToExpand The node at the key state ("K")
	 * @return The total path to get from the initial to the goal state, or null if
	 *         no such path exists
	 */
	private static ArrayList<String> getCombinedPath(MazeProblem problem, SearchTreeNode nodeToExpand) {
		ArrayList<String> pathFromInitialToKey = getPath(nodeToExpand);
		ArrayList<String> pathFromKeyToGoal = new ArrayList<String>();
		for (MazeState keyState : problem.KEY_STATES) {
			pathFromKeyToGoal = solve(problem, keyState, true);
		}
		if (pathFromKeyToGoal == null || pathFromInitialToKey == null) {
			return null;
		}
		pathFromInitialToKey.addAll(pathFromKeyToGoal);
		return pathFromInitialToKey;
	}

	/**
	 * 
	 * @param node1 First node to be compared
	 * @param node2 Second node to be compared
	 * @return 1 if the first node's total cost is higher, 2 if the second node's
	 *         total cost is higher, and 0 if they are the same.
	 */
	private static int rankPriority(SearchTreeNode node1, SearchTreeNode node2) {
		if (node2.totalCost < node1.totalCost) {
			return 1;
		} else if (node1.totalCost < node2.totalCost) {
			return -1;
		}
		return 0;
	}

	/**
	 * Returns an array of actions that it took to get to the goalNode
	 * 
	 * @param goalNode The node that we want to trace the path from
	 * @return An array of actions, i.e. ["U", "D", "R"]
	 */
	public static ArrayList<String> getPath(SearchTreeNode goalNode) {
		ArrayList<String> path = new ArrayList<String>();
		SearchTreeNode current = goalNode;
		while (current.parent != null) {
			path.add(0, current.action);
			current = current.parent;
		}
		return path;
	}

}

/**
 * SearchTreeNode that is used in the Search algorithm to construct the Search
 * tree.
 */
class SearchTreeNode {

	MazeState state;
	String action;
	SearchTreeNode parent;
	int totalCost;

	/**
	 * Constructs a new SearchTreeNode to be used in the Search Tree.
	 * 
	 * @param state     The MazeState (col, row) that this node represents.
	 * @param action    The action that *led to* this state / node.
	 * @param parent    Reference to parent SearchTreeNode in the Search Tree.
	 * @param totalCost The total cost to get to this node from the initial state
	 *                  thus far
	 */
	SearchTreeNode(MazeState state, String action, SearchTreeNode parent, int totalCost) {
		this.state = state;
		this.action = action;
		this.parent = parent;
		this.totalCost = totalCost;
	}

}