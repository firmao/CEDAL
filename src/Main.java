import java.io.File;
import java.util.Map;
import java.util.Set;

public class Main {

	public static void main(String[] args) {
		t4();
	}
	
	public static void t1()
	{
		try {
			File f = new File("input.nt");
			Map<String, Set<String>> result = MyTC.getTC(f);
			System.out.println("Output: " + result);
			MyTC.generateFile(result, "output.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void t2()
	{
		try {
			File f = new File("correct");
			MyTC.generateAllFiles(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void t3()
	{
		try {
			MyTCJena.testReadNtriple("input.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void t4()
	{
		try {
			Map<String, Set<String>> result = MyTCJena.getTC("input.nt");
			System.out.println("Output: " + result);
			MyTC.generateFile(result, "outputJena.nt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
