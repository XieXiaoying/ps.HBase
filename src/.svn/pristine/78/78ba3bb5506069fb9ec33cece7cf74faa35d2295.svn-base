package matrix;
import java.util.*;

public class Matrix {
	public float matrix[][];
	public int n;
	public int m;
	
	 public int getM() {  
	        return m;  
	    }  
	 
	    public int getN() {  
	        return n;  
	    }  
	 
	    public Matrix() {  
	        this(0, 0);  
	    }  
	 
	    public Matrix(int m, int n) {  
	        this.m = m;  
	        this.n = n;  
	        this.matrix = new float[m][n];  
	    }  
	 
	    public Matrix(float[][] martix) {  
	        this.matrix = martix;  
	        calcDim();  
	    }  
	 
	    private void calcDim() {  
	        m = matrix.length;  
	        n = matrix[0].length;  
	    }  
	 
	    public void setMatrixElement(int i, int j, float value) {  
	 
	        try {  
	            if (i > m || j > n)  
	                throw new Exception("索引越界");  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        matrix[i][j] = value;  
	    }  
	 
	    public float getMartixElement(int i, int j) {  
	        return matrix[i][j];  
	    }  
	 
	    /**  
	     * 求矩阵M转置  
	     *   
	     * @return 转置矩阵  
	     */ 
	    public Matrix tranpose() {  
	        float[][] temp = new float[n][m];  
	        for (int i = 0; i < m; i++) {  
	            for (int j = 0; j < n; j++) {  
	                temp[j][i] = matrix[i][j];  
	            }  
	        }  
	        return new Matrix(temp);  
	    }  
	 
	    /**  
	     * 矩阵相加  
	     *   
	     * @param matrix  
	     * @return  
	     */ 
	    public Matrix add(Matrix matrix) {  
	        try {  
	            if (matrix.getM() != m || matrix.getN() != n)  
	                throw new Exception("矩阵维数不相等，不能相加");  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	 
	        float[][] temp = new float[m][n];  
	        for (int i = 0; i < m; i++) {  
	            for (int j = 0; j < n; j++) {  
	                temp[i][j] = matrix.getMartixElement(i, j) + this.matrix[i][j];  
	            }  
	        }  
	 
	        return new Matrix(temp);  
	    }  
	 
	    /**  
	     * 矩阵相减  
	     *   
	     * @param matrix  
	     * @return  
	     */ 
	    public Matrix reduce(Matrix matrix) {  
	        try {  
	            if (matrix.getM() != m || matrix.getN() != n)  
	                throw new Exception("矩阵维数不相等，不能相加");  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	 
	        float[][] temp = new float[m][n];  
	        for (int i = 0; i < m; i++) {  
	            for (int j = 0; j < n; j++) {  
	                temp[i][j] = this.matrix[i][j] - matrix.getMartixElement(i, j);  
	            }  
	        }  
	 
	        return new Matrix(temp);  
	    }  
	 
	    /**  
	     * 矩阵相乘  
	     *   
	     * @param matrix  
	     * @return  
	     */ 
	    public Matrix multiply(Matrix matrix) {  
	 
	        try {  
	            if (matrix.getM() != n || matrix.getN() != m)  
	                throw new Exception("矩阵维数不符合相乘要求");  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	 
	        float[][] temp = new float[m][m];  
	 
	        for (int i = 0; i < m; i++) {  
	            for (int k = 0; k < m; k++) {  
	                float t = 0;  
	                for (int j = 0; j < n; j++) {  
	                    t += this.matrix[i][j] * matrix.getMartixElement(j, k);  
	                }  
	 
	                temp[i][k] = t;  
	            }  
	        }  
	        return new Matrix(temp);  
	    }  
	 
	      
	    /**  
	     * 格式化输出  
	     *     
	     */ 
	    public String toFormatString() {  
	        StringBuffer buf = new StringBuffer();  
	        for (int i = 0; i < m; i++) {  
	            for (int j = 0; j < n; j++) {  
	                buf.append(matrix[i][j]).append("\t");  
	            }  
	            buf.append("\n");  
	        }  
	          
	        return buf.toString();  
	    }  
	 
	    @Override 
	    public int hashCode() {  
	        final int prime = 31;  
	        int result = 1;  
	        result = prime * result + m;  
	        result = prime * result + Arrays.hashCode(matrix);  
	        result = prime * result + n;  
	        return result;  
	    }  
	 
	    @Override 
	    public boolean equals(Object obj) {  
	        if (this == obj)  
	            return true;  
	        if (obj == null)  
	            return false;  
	        if (getClass() != obj.getClass())  
	            return false;  
	        Matrix other = (Matrix) obj;  
	        if (m != other.m)  
	            return false;  
	        if (!Arrays.equals(matrix, other.matrix))  
	            return false;  
	        if (n != other.n)  
	            return false;  
	        return true;  
	    }  
}
