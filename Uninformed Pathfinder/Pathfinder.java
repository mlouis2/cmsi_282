//Maddie Louis, Merissa (Li Ying) Tan, Adriana Donkers
package pathfinder.uninformed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Maze Pathfinding algorithm that implements a basic, uninformed, breadth-first tree search.
 */
public class Pathfinder {
    
    /**
     * Given a MazeProblem, which specifies the actions and transitions available in the
     * search, returns a solution to the problem as a sequence of actions that leads from
     * the initial to a goal state.
     * 
     * @param problem A MazeProblem that specifies the maze, actions, transitions.
     * @return An ArrayList of Strings representing actions that lead from the initial to
     * the goal state, of the format: ["R", "R", "L", ...]
     */
    public static ArrayList<String> solve (MazeProblem problem) {
    	Queue<SearchTreeNode> frontier = new LinkedList<>();
    	
    	frontier.add(new SearchTreeNode(problem.INITIAL_STATE, null, null));
    	
    	while (frontier.peek() != null) {
    		SearchTreeNode nodeToExpand = frontier.poll();
    		if (problem.isGoal(nodeToExpand.state)) {
    			return getPath(nodeToExpand);
    		}
    		Map<String, MazeState> mapOfChildren = problem.getTransitions(nodeToExpand.state);
    		 for (Map.Entry<String, MazeState> child : mapOfChildren.entrySet()) {
    			 frontier.add(new SearchTreeNode(child.getValue(), child.getKey(), nodeToExpand));
    		 }	
    	}
        return null;
    }
    
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
    
    /**
     * Constructs a new SearchTreeNode to be used in the Search Tree.
     * 
     * @param state The MazeState (col, row) that this node represents.
     * @param action The action that *led to* this state / node.
     * @param parent Reference to parent SearchTreeNode in the Search Tree.
     */
    SearchTreeNode (MazeState state, String action, SearchTreeNode parent) {
        this.state = state;
        this.action = action;
        this.parent = parent;
    }
    
}