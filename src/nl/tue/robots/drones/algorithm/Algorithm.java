package nl.tue.robots.drones.algorithm;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.model.Model;

import java.util.*;

public class Algorithm {

    // Expanded nodes contain:
    // a node,
    // the distance from it to the root,
    // the expected distance from it to the destination
    // a parentExpandedNode
    private static ExpandedNode emptyNode = new ExpandedNode(null, 0, 0, null);

    /**
     * Function findPath
     *
     * @param startNode       is the node at which the drone currently is.
     * @param destinationNode is the node the drone has to go to
     * @return The function returns the set of transitions the node has to take
     * to get to his destination.
     * If there is not an available path, the function throws an exception.
     */
    public static ArrayList<Transition> findPath(Node startNode, Node destinationNode) {
        // Setup the expanded node list
        ExpandedNode rootNode = new ExpandedNode(startNode, 0,
                Model.getHeuristic(startNode, destinationNode), emptyNode);
        Set<ExpandedNode> seenNodes = new HashSet<>();
        seenNodes.add(rootNode);

        // Setup the frontier list
        ArrayList<ExpandedNode> frontier = new ArrayList<>();
        frontier.add(rootNode);

        ExpandedNode foundDestination = pathSearch(destinationNode, seenNodes, frontier);
        ArrayList<Transition> transitionList = new ArrayList<>();

        // Check whether foundDestination is the actual destination
        if (foundDestination.getNode() == destinationNode) {
            getPath(foundDestination, transitionList);
        }
        // else something went horribly wrong. Fix it! (Could not find path)
        return transitionList;
    }

    /**
     * Function pathSearch
     *
     * @param destinationNode is the node the drone has to go to
     * @param seenNodes       is the set of nodes that have already been visited
     * @param frontier        Ã¬s the set of nodes that still have to be expanded
     * @return The function returns an ExpandedNode object with the destinationNode in it.
     */
    public static ExpandedNode pathSearch(Node destinationNode, Set<ExpandedNode> seenNodes,
                                          ArrayList<ExpandedNode> frontier) {
        // First check the frontier isn't empty
        if (frontier.size() > 0) {

            // The frontier is not empty: expand the best node
            ExpandedNode nodeToExpand = frontier.get(0);
            Node currentNode = nodeToExpand.getNode();
            List<Node> newNodes = currentNode.getConnectedNodes();
            ArrayList<ExpandedNode> newExpandedNodes = new ArrayList<>();

            // remove all the nodes we have already visited
            for (ExpandedNode node : seenNodes) {
                newNodes.remove(node.getNode());
            }

            // Check whether one of the nodes is the destination node
            for (Node node : newNodes) {
                if (node == destinationNode) {
                    return createExpandedNode(node, destinationNode, nodeToExpand);
                }
                newExpandedNodes.add(createExpandedNode(node, destinationNode, nodeToExpand));
            }

            // Now that the new elements have been found, the node can be removed
            frontier.remove(0);

            // Now that the first node is removed, the new ones can be added.
            for (ExpandedNode node : newExpandedNodes) {
                // TODO: If the given node only has one available transition and is not
                // the destination, it does not have to be added to the frontier!
                if (seenNodes.add(node)) {
                    sortedAdd(node, frontier);
                }
            }

            // We have updated the frontier with new nodes, so it can start all over again.
            return pathSearch(destinationNode, seenNodes, frontier);
        } else {
            // Then the frontier is empty, which means there is no path
            // Still have to come up with what will be returned in this case.
            return new ExpandedNode(null, 0, 0, null);
        }
    }

    /**
     * Function getPath
     * Generates a transitionList
     *
     * @param lastNode    is the closest node to the start node of  which the path has not been created
     * @param transitions contains a transitionList from lastNode to the destinationNode
     * @return The function returns an ArrayList of Transitions which the drones has to take to reach
     * its goal.
     */
    public static void getPath(ExpandedNode lastNode, ArrayList<Transition> transitions) {
        if (lastNode.getParent().getNode() != null) {
            // Then we have not yet found the root.
            Node currentNode = lastNode.getNode();
            Node parentNode = lastNode.getParent().getNode();
            Transition currentTransition = parentNode.getTransition(currentNode);
            transitions.add(0, currentTransition);
            //recursive call
            getPath(lastNode.getParent(), transitions);
        }
        // If not then we have found the root, so we don't need to add to the transition list!
    }

    /**
     * Function createExpandedNode
     * Generates and returns a new ExpandedNode
     *
     * @param node               the node for which an ExpandedNode has to be created
     * @param destination        the destination node for the drone
     * @param parentNodeExpanded is the ExpandedNode of the parent of {@code node}
     * @return an ExpandedNode based around {@code node}
     */
    public static ExpandedNode createExpandedNode(Node node, Node destination,
                                                  ExpandedNode parentNodeExpanded) {
        Node parentNode = parentNodeExpanded.getNode();
        int transitionDistance = parentNodeExpanded.getDistanceTravelled() +
                node.getTransition(parentNode).getDistance();
        int heuristicDistance = Model.getHeuristic(node, destination);
        return new ExpandedNode(node, transitionDistance, heuristicDistance, parentNodeExpanded);
    }

    /**
     * Function sortedAdd
     * Adds the nodes in newNode to the sorted frontier
     *
     * @param newNode  a list with nodes that has to be integrated in {@code frontier}
     * @param frontier all nodes that still have to be expanded
     * @return frontier with all nodes in newNode integrated
     */
    public static void sortedAdd(ExpandedNode newNode, ArrayList<ExpandedNode> frontier) {
        frontier.add(newNode);
        Collections.sort(frontier);
    }

    /**
     * Finds the quickest way to get inside the building for the given node.
     * Uses breadth first search to find a node which is inside the building
     * @param startNode the node from which to get inside
     * @return the nodes on the inside which is the quickest to go to or {@code null} if no such node exists
     */
    public static Node findQuickestPathInside(Node startNode) {
        // Setup the expanded node list
        ExpandedNode rootNode = new ExpandedNode(startNode, 0,0, emptyNode);
        Set<Node> seenNodes = new HashSet<>();
        seenNodes.add(startNode);

        // Setup the frontier list
        LinkedList<ExpandedNode> frontier = new LinkedList<>();
        frontier.add(rootNode);

        ExpandedNode foundDestination = null;

        // create BFS tree until a node on the inside is found
        ExpandedNode inspect;
        while (foundDestination == null && frontier.size() > 0) {
            inspect = frontier.poll();
            for (Node n : inspect.getNode().getConnectedNodes()) {
                if (!seenNodes.contains(n)) {
                    // if not seen before, add to frontier
                    ExpandedNode expNode = new ExpandedNode(n, inspect.getDistanceTravelled() + 1, 0, inspect);
                    if (!n.isOutside()) {
                        // we found a node inside
                        foundDestination = expNode;
                    } else {
                        // just add it to the frontier
                        frontier.add(expNode);
                    }
                    seenNodes.add(n);
                }
            }
        } // once we terminate we either found a node which is on the inside or no such node exists

        return (foundDestination != null) ? foundDestination.getNode() : null;
    }
}
