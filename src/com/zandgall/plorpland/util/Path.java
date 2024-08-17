/* zandgall

 ## Path
 # A class that stores details on how to get from one place to another
 # Functions as a Queue of Points, with a big main "pathfind" function
 # This is just an implementation of the A* algorithm https://en.wikipedia.org/wiki/A*_search_algorithm

 : MADE IN NEOVIM */

package com.zandgall.plorpland.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.zandgall.plorpland.Main;

public class Path implements Serializable {
	private ArrayList<Point> path = new ArrayList<>();

	public Path() {
	}

	public void prepend(int x, int y) {
		path.addFirst(new Point(x, y));
	}

	public Point current() {
		return path.get(0);
	}

	public Point next() {
		if (path.size() > 1)
			return path.get(1);
		return null;
	}

	public Point progress() {
		return path.remove(0);
	}

	public boolean empty() {
		return path.isEmpty();
	}

	public int size() {
		return path.size();
	}

	/* public void debugRender(GraphicsContext g) {
		if (path.isEmpty())
			return;
		g.setLineWidth(0.2);
		g.setStroke(Color.BLACK);
		for (int i = 1; i < path.size(); i++) {
			g.strokeLine(path.get(i - 1).x + 0.5, path.get(i - 1).y + 0.5,
					path.get(i).x + 0.5, path.get(i).y + 0.5);
		}
	} */

	private static Path reconstruct(Node endPoint) {
		ArrayList<Point> check = new ArrayList<>();
		Path p = new Path();
		for (Node n = endPoint; n != null; n = n.getParent()) {
			if(check.contains(new Point(n.x, n.y))) {
				// Panic!
				System.out.println("Circular path found in pathfinding!");
				new RuntimeException().printStackTrace();
				return p;
			}
			check.add(new Point(n.x, n.y));
			p.prepend(n.x, n.y);
		}
		return p;
	}

	/**
	 * Find taxicab distance between a point and a target for pathfind algorithm
	 */
	private static double heuristic(int x, int y, int targetX, int targetY) {
		return Math.abs(targetX - x) + Math.abs(targetY - y);
	}

	/**
	 * An implementation of the A* search algorithm, for finding paths around solid
	 * tiles
	 *
	 * @param startX  Starting X position
	 * @param startY  Starting Y position
	 * @param targetX Target X position
	 * @param targetY Target Y position
	 * @param start   Predetermined steps to prepend to the path. Can be used to
	 *                define a starting direction, or to chain multiple paths.
	 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A*
	 *      Wikipedia</a>
	 */
	public static Path pathfind(int startX, int startY, int targetX, int targetY, Point... start) {
		ArrayList<Point> open = new ArrayList<>();

		HashMap<Point, Node> map = new HashMap<>();
		map.put(new Point(startX, startY),
				new Node(null, startX, startY, 0.0, heuristic(startX, startY, targetX, targetY)));

		// Mark out any previously trecked
		for (int i = 0; i < start.length; i++)
			map.put(start[i], new Node(i > 0 ? map.get(start[i - 1]) : null, start[i].x, start[i].y, 0.0, 0.0));
		if (start.length > 0 && !start[start.length - 1].equals(new Point(startX, startY)))
			map.get(new Point(startX, startY)).parent = map.get(start[start.length - 1]);

		// Add the starting point as an open node
		open.add(new Point(startX, startY));

		// Have a limit to how many pathfind iterations we take
		int iter = 0;
		while (!open.isEmpty() && iter < 1000) {
			iter++;

			// Sort by lowest 'fScore'
			open.sort((a, b) -> {
				double aV = (map.containsKey(a) ? map.get(a).fScore : Double.POSITIVE_INFINITY);
				double bV = (map.containsKey(b) ? map.get(b).fScore : Double.POSITIVE_INFINITY);
				return (int) Math.signum(aV - bV);
			});

			// Poll first point,
			Node current = map.get(open.removeFirst());

			// If we found the end, construct the path
			if (current.x == targetX && current.y == targetY)
				return reconstruct(current);

			// Define the neighbor coordinates
			int nX[] = { current.x, current.x + 1, current.x, current.x - 1 },
					nY[] = { current.y - 1, current.y, current.y + 1, current.y };

			// Loop through them, check if they have solid bounds and if they're not
			// marked yet or if the calculated gscore is less than what it had before
			for (int i = 0; i < 4; i++) {
				Point p = new Point(nX[i], nY[i]);

				// Tile is in provided prepended list of points
				if (Arrays.binarySearch(start, p) >= 0)	
					continue;
			
				// Tile doesnt exist or is solid, skip
				if (Main.getLevel().get(p.x, p.y) == null
						|| (Main.getLevel().get(p.x, p.y).solidBounds(p.x, p.y) != null)
						&& Main.getLevel().get(p.x, p.y).solidBounds(p.x, p.y).intersects(p.x + 0.4, p.y + 0.4, 0.2, 0.2))
					continue;

				// A node has a default gScore of infinity
				// if we found a shorter path to this point than what existed before,
				// or the first discovered path to this point, mark the path and see if we can
				// mark it as a new open node
				Node n = map.getOrDefault(p, new Node(p.x, p.y));
				if (n.gScore > current.gScore + 1) {
					n.parent = current;
					n.gScore = current.gScore + 1;
					n.fScore = current.gScore + 1 + heuristic(p.x, p.y, targetX, targetY);

					if (!map.containsKey(p))
						map.put(p, n);
					if (!open.contains(p))
						open.add(p);
				}
			}
		}

		return new Path();
	}

	// A storage class that just keeps a position, parent node,
	// and a gscore and fscore for pathfinding
	public static class Node {
		public Node parent = null;
		public Double gScore = Double.POSITIVE_INFINITY, fScore = Double.POSITIVE_INFINITY;

		public int x, y;

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Node(Node parent, int x, int y, Double gScore, Double fScore) {
			this.parent = parent;
			this.x = x;
			this.y = y;
			this.gScore = gScore;
			this.fScore = fScore;
		}

		public Node getParent() {
			return parent;
		}
	}
}
