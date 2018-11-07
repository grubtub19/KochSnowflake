/**
 * A grouping of multiple data
 */
public class TreeData {
    public boolean leafNode;
    public int numSegments;
    public float perimeter;
    public float area;

    public TreeData() {
        numSegments = 0;
        perimeter = 0;
        area = 0;
        leafNode = false;
    }

    /**
     * Add data from another TreeData to the current TreeData
     * @param data to add to current TreeData
     */
    public void addData(TreeData data) {
        numSegments += data.numSegments;
        perimeter += data.perimeter;
        area += data.area;
    }
}