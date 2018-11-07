import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.*;

public class KochSnowflake extends JFrame implements GLEventListener {
    private int rendering_program;
    private int vao[] = new int[1];
    private int maxN;
    private float sideLength;

    /**
     * Saves parameters, changes window, initialises OpenGL stuff
     * @param sideLength
     * @param maxN
     */
    public KochSnowflake(float sideLength, int maxN) {
        this.sideLength = sideLength;
        this.maxN = maxN;
        setTitle("KochSnowflake");
        setIconImageFromUrl("http://www.origami-make.org/origami-paper-triangle/Images/origami-paper-triangle.jpg");
        setSize(1000,1000);
        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas myCanvas = new GLCanvas(capabilities);
        myCanvas.addGLEventListener(this);
        getContentPane().add(myCanvas);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            float sideLen = Float.parseFloat(args[0]);
            int n = Integer.parseInt(args[1]);
            new KochSnowflake(sideLen, n);
        } catch(NumberFormatException e) {
            System.out.println("Arguments are (side length, n)");
        }
    }

    /**
     * Given a url to an image file, sets the window icon to that image. (I was learning JFrame)
     * @param urlString url to image file
     */
    private void setIconImageFromUrl(String urlString) {
        try {
            this.setIconImage(getBufferedImageFromUrl(urlString));
        } catch (MalformedURLException error) {
            System.out.println("Malformed URL, check formatting/n" + error);
        } catch (IOException error) {
            System.out.println("Connection error. Failed to download window icon from internet. Check your connection or image url.");
        }
    }

    /**
     * Given a url to an image file, returns a BufferedImage of that file
     * @param urlString url to image file
     * @return BufferedImage of image file
     * @throws MalformedURLException url format is incorrect
     * @throws IOException Connection issue. Either no connection or incorrect url.
     */
    private BufferedImage getBufferedImageFromUrl(String urlString) throws MalformedURLException, IOException {
        URL imageUrl = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
        return ImageIO.read(connection.getInputStream());
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();
        rendering_program = createShaderProgram();
        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
    }

    /**
     * Attaches vertex and fragment shader to a new shader program
     * @return
     */
    private int createShaderProgram() {
        	GL4 gl = (GL4) GLContext.getCurrentGL();

            String vshaderSource[] = GLSLUtils.readShaderSource("shaders/vert.shader.2d");
            String fshaderSource[] = GLSLUtils.readShaderSource("shaders/frag.shader.2d");

            int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
            int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

            gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
            gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);

            gl.glCompileShader(vShader);
            gl.glCompileShader(fShader);

            int vfprogram = gl.glCreateProgram();
            gl.glAttachShader(vfprogram, vShader);
            gl.glAttachShader(vfprogram, fShader);
            gl.glLinkProgram(vfprogram);
            return vfprogram;
        }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) { }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) { }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();
        gl.glUseProgram(rendering_program);

        float sideLen = 1;
        setUniform(gl, "scale", 0.6f*sideLen);

        TreeNode root = drawFlake(gl, sideLen, maxN);
        printTable(root, maxN);
    }

    /**
     * Populates a table of information by recursively traversing the Tree for each value of n.
     * @param root the root TreeNode of the entire Snowflake
     * @param n the maximum iterations to use (any number larger than maxN will result in the same values as maxN's.
     */
    private void printTable(TreeNode root, int n) {
        System.out.println(" n | Segment Count |   Perimeter   |      Area     |");
        for (int i = 0; i <= n; i++) {
            TreeData results = root.getTreeDataAtN(i);
            System.out.printf("%2d | %13d | %13f | %13f |\n", i, results.numSegments, results.perimeter, results.area);
        }
    }

    /**
     * Set uniform variable from shader to a float value
     * @param gl
     * @param name String: uniform variable name
     * @param number float value to assign
     */
    private void setUniform(GL4 gl, String name, float number) {
        int uniform_loc = gl.glGetUniformLocation(rendering_program, name);
        gl.glProgramUniform1f(rendering_program, uniform_loc, number);
    }

    /**
     * The original triangle from which the first recursive calls are made. If n == 0, no recursion is needed, the
     * base triangle is drawn and calculations are made.
     * @param gl
     * @param sideLen the length of one side of the base equilateral triangle
     * @param maxN the number of the iteration at which recursion stops.
     * @return populated root TreeNode
     */
    private TreeNode drawFlake(GL4 gl, float sideLen, int maxN) {
        this.maxN = maxN;
        Point[] points = calcStartingPoints(sideLen);

        TreeNode node = new TreeNode();
        node.data.area = areaOfEqTriangleGivenSide(sideLen);
        node.data.perimeter = sideLen * 3;
        node.data.numSegments = 3;

        if(maxN > 0) {
            node.addChild(drawSide(gl, points[0], points[1], 1));
            node.addChild(drawSide(gl, points[1], points[2], 1));
            node.addChild(drawSide(gl, points[2], points[0], 1));
        } else {
            drawLine(gl, points[0], points[1]);
            drawLine(gl, points[1], points[2]);
            drawLine(gl, points[2], points[0]);
        }
        return node;
    }

    /**
     * Recursive call to create a new triangle given a line. One line segment turns into four line segments.
     * Area is calculated each time, but segment count and perimeter length is only calculated at the final level of
     * recursion. If p1 is on the left and p2 is on the right to form a horizontal line, the triangle is drawn vertically.
     * @param gl
     * @param p1 left Point of original line
     * @param p2 right Point of original line
     * @param n current level of recursion
     * @return TreeNode of child
     */
    private TreeNode drawSide(GL4 gl, Point p1, Point p2, int n) {
        Point next = nextPoint(p1, p2);
        Point oneThird = third(p1, p2);
        Point twoThirds = third(p2, p1);

        TreeNode node = new TreeNode();
        node.data.area = areaOfEqTriangleGivenSide(lineLen(p1,oneThird));
        node.data.numSegments = 4;
        node.data.perimeter = 4 * lineLen(p1, oneThird);

        if (n < maxN) {
            node.addChild(drawSide(gl, p1, oneThird, n+1));
            node.addChild(drawSide(gl, oneThird, next, n+1));
            node.addChild(drawSide(gl, next, twoThirds, n+1));
            node.addChild(drawSide(gl, twoThirds, p2, n+1));
        } else {
            node.data.leafNode = true;
            drawLine(gl, p1, oneThird);
            drawLine(gl, oneThird, next);
            drawLine(gl, next, twoThirds);
            drawLine(gl, twoThirds, p2);
        }
        return node;
    }

    /**
     * Calculates the tip of the new triangle given the original line segment. New Point is above the horizontal line
     * with p1 on the left and p2 on the right.
     * @param p1 left Point of original line
     * @param p2 right Point of original line
     * @return Point of tip of new triangle
     */
    private Point nextPoint(Point p1, Point p2) {
        Point middle = pointPlusVector(p1, vectorTimesScalar(pointMinusPoint(p2, p1), 0.5f));
        return new Point((-(p2.y - p1.y) * (float)Math.sqrt(3) / 6.0f) + middle.x, ((p2.x - p1.x) * (float)Math.sqrt(3) / 6.0f) + middle.y);
    }

    /**
     * Calculates the points of the base equilateral triangle.
     * @param sideLen length of one side of the equilateral triangle
     * @return an array of Points of length 3. Order is bottom-left [0], top [1], and then bottom-right [2].
     */
    private Point[] calcStartingPoints(float sideLen) {
        float h = (float) Math.sqrt(3) / 2.0f * sideLen;
        return new Point[]{ new Point(-sideLen/2.0f,-h/3.0f), new Point(0,h*2/3.0f), new Point(sideLen/2.0f,-h/3.0f) };
    }

    private Point vectorTimesScalar(Point v, float n) {
        return new Point(v.x * n, v.y * n);
    }

    private Point pointPlusVector(Point p, Point v) {
        return new Point(p.x + v.x, p.y + v.y);
    }

    private Point pointMinusPoint(Point p1, Point p2) {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }

    /**
     * Finds the Point that is a third of the distance from p1 to p2.
     * @param p1 origin Point
     * @param p2 destination Point
     * @return Point a third away from p1 toward p2
     */
    private Point third(Point p1, Point p2) {
        return new Point(p1.x + ((p2.x - p1.x) / 3.0f), p1.y + ((p2.y - p1.y) / 3.0f));
    }

    /**
     * Calculates the area of an equilateral triangle given the length of one side
     * @param sideLen length of one side of equilateral triangle
     * @return area of equilateral triangle
     */
    private float areaOfEqTriangleGivenSide(float sideLen) {
        return (float)( Math.sqrt(3) / 4) * sideLen * sideLen;
    }

    /**
     * Returns the length of a line between p1 and p2
     * @param p1 1st Point of line segment
     * @param p2 2nd Point of line segment
     * @return length of line segment
     */
    private float lineLen(Point p1, Point p2) {
        float x = p1.x-p2.x;
        float y = p1.y-p2.y;
        return (float)Math.sqrt(Math.abs(x*x+y*y));
    }

    /**
     * Makes a call to shader drawing GL_LINES of line created by Points p1 and p2
     * @param gl
     * @param p1 1st Point of line segment
     * @param p2 2nd Point of line segment
     */
    private void drawLine(GL4 gl, Point p1, Point p2) {
        setUniform(gl,"x1", p1.x);
        setUniform(gl,"y1", p1.y);
        setUniform(gl,"x2", p2.x);
        setUniform(gl,"y2", p2.y);

        gl.glPointSize(2.0f);
        gl.glDrawArrays(GL_LINES, 0, 2);
    }
}