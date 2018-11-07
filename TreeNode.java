import java.util.LinkedList;

/**
 * A single node of a tree with a parent node and multiple children nodes. Each node contains information about the
 * current state of recursion. Information is calculated at leaf nodes and propagated up the tree.
 */
public class TreeNode {
    public TreeData data;
    public TreeNode parent;
    public LinkedList<TreeNode> children;

    /**
     * Initializes a blank TreeData and a blank LinkedList of children
     */
    public TreeNode() {
        this.data = new TreeData();
        this.children = new LinkedList<TreeNode>();
    }

    /**
     * Adds a child to the current TreeNode. Sets the current TreeNode as the parent to this child.
     * @param child
     */
    public void addChild(TreeNode child) {
        child.parent = this;
        this.children.add(child);
    }

    /**
     * Gets information about the state of the Koch Snowflake at the nth recursive level. Call this on the root node.
     * If n is larger than maxN, it will return the same value as if n = maxN.
     * @param n level of recursion
     * @return TreeData of Koch Snowflake at n level of recursion
     */
    public TreeData getTreeDataAtN(int n) {
        if (n < 0) {
            return data;
        }
        if(!children.isEmpty()) {
            TreeData sumData = new TreeData();
            for (TreeNode child : children) {
                sumData.addData(child.getTreeDataAtN(n-1));
            }
            sumData.area += data.area;
            return sumData;
        } else {
            return data;
        }
    }
}