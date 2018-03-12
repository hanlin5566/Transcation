package com.hzcf.variable;

import java.text.ParseException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     * @throws ParseException 
     */
    public AppTest( String testName ) throws ParseException
    {
        super( testName );
        
//        Connection con = null;
//		PreparedStatement ps = null;
//		try {
//			con = jdbcTemplate.getDataSource().getConnection();
//			String sql = "update derived_var v set v.class_file = ? where v.var_id = 1;";
//			ps = con.prepareStatement(sql);
//			InputStream in = new FileInputStream("F:/workspace/HJ-Variable/target/classes/com/hzcf/variable/engine/algorithms/DirectVariableAlgorithms.class");// 生成被插入文件的节点流
//			// 设置Blob
//			ps.setBlob(1, in);
//
//			ps.executeUpdate();
//			con.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//
//		}
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
